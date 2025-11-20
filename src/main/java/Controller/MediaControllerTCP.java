package Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.json.JSONObject;

import Model.Bean.User;
import Model.BO.TaskBO;
import Service.TCPClientService;

/**
 * 🚀 MediaControllerTCP - Upload file và xử lý qua TCP Socket Server
 * 
 * Flow:
 * 1. User upload file → Web Server
 * 2. Web Server lưu file, tạo Task trong DB
 * 3. Web Server gửi task qua TCP → Worker Server
 * 4. Worker Server xử lý Speech-to-Text
 * 5. Worker Server trả kết quả → Web Server cập nhật DB
 * 
 * So với MediaController cũ:
 * - Cũ: Queue-based (in-process)
 * - Mới: TCP Socket (distributed)
 */
@WebServlet("/upload-tcp")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 100,      // 100MB
    maxRequestSize = 1024 * 1024 * 150    // 150MB
)
public class MediaControllerTCP extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIRECTORY = "uploads";
    
    private TaskBO taskBO;
    private TCPClientService tcpClient;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.taskBO = new TaskBO();
        this.tcpClient = TCPClientService.getInstance();
        
        // Tạo thư mục upload nếu chưa có
        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            System.out.println("✅ Đã tạo thư mục upload: " + uploadPath);
        }
        
        // Test connection đến Worker Server
        System.out.println("🔍 Testing connection to Worker TCP Server...");
        boolean connected = tcpClient.testConnection();
        
        if (connected) {
            System.out.println("✅ TCP Controller initialized successfully");
            tcpClient.healthCheck();
        } else {
            System.err.println("⚠️  WARNING: Cannot connect to Worker Server!");
            System.err.println("   Make sure Worker TCP Server is running on port 9999");
            System.err.println("   Start it with: java -cp ... Service.WorkerTCPServer");
        }
    }
    
    public MediaControllerTCP() {
        super();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Kiểm tra đăng nhập
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Health check Worker Server
        boolean workerHealthy = tcpClient.healthCheck();
        request.setAttribute("workerHealthy", workerHealthy);
        
        // Hiển thị trang upload
        request.getRequestDispatcher("/View/upload-tcp.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        // Kiểm tra đăng nhập
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        try {
            // ===== BƯỚC 1: NHẬN FILE UPLOAD =====
            Part filePart = request.getPart("mediaFile");
            
            if (filePart == null || filePart.getSize() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"No file uploaded\"}");
                return;
            }
            
            // Get language
            String language = request.getParameter("language");
            if (language == null || language.isEmpty()) {
                language = "vi"; // Default Vietnamese
            }
            
            // Validate language
            if (!language.equals("vi") && !language.equals("en")) {
                language = "vi";
            }
            
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            
            // ===== BƯỚC 2: LƯU FILE VÀO SERVER =====
            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
            
            // Tạo tên file unique
            String timeStamp = String.valueOf(System.currentTimeMillis());
            String fileExtension = "";
            int lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex > 0) {
                fileExtension = fileName.substring(lastDotIndex);
            }
            
            String uniqueFileName = "media_" + timeStamp + fileExtension;
            String filePath = uploadPath + File.separator + uniqueFileName;
            
            // Save file
            filePart.write(filePath);
            
            System.out.println("📁 File saved: " + filePath);
            System.out.println("📊 File size: " + filePart.getSize() + " bytes");
            
            // ===== BƯỚC 3: TẠO TASK TRONG DATABASE =====
            int taskId = taskBO.taoTaskMoi(
                user.getId(),
                fileName,
                filePath,
                language
            );
            
            if (taskId <= 0) {
                throw new ServletException("Failed to create task in database");
            }
            
            System.out.println("✅ Task created with ID: " + taskId);
            
            // ===== BƯỚC 4: GỬI TASK QUA TCP SOCKET =====
            System.out.println("📤 Sending task to Worker Server via TCP...");
            
            // Send async (không block request)
            tcpClient.sendTaskAsync(taskId, filePath, language, new TCPClientService.TaskCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    System.out.println("✅ Task " + taskId + " completed successfully via TCP!");
                    System.out.println("   Result: " + response.toString());
                }
                
                @Override
                public void onError(Exception e) {
                    System.err.println("❌ Task " + taskId + " failed: " + e.getMessage());
                    
                    // Update task status to FAILED in database
                    try {
                        taskBO.datTaskThatBai(taskId, "TCP Error: " + e.getMessage());
                    } catch (Exception ex) {
                        System.err.println("Failed to update task status: " + ex.getMessage());
                    }
                }
            });
            
            // ===== BƯỚC 5: TRẢ RESPONSE CHO CLIENT =====
            JSONObject responseJson = new JSONObject();
            responseJson.put("success", true);
            responseJson.put("taskId", taskId);
            responseJson.put("message", "File uploaded successfully. Processing via TCP Socket Server...");
            responseJson.put("fileName", fileName);
            responseJson.put("language", language);
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(responseJson.toString());
            
            System.out.println("✅ Response sent to client: Task ID " + taskId);
            
        } catch (Exception e) {
            System.err.println("❌ Error in MediaControllerTCP: " + e.getMessage());
            e.printStackTrace();
            
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(errorResponse.toString());
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        System.out.println("🛑 MediaControllerTCP destroyed");
    }
}
