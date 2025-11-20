package Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;

import Model.Bean.Task;
import Model.Bean.User;
import Model.BO.TaskBO;
import Service.ExportService;

/**
 * Controller xử lý tải xuống kết quả Speech-to-Text
 * Hỗ trợ định dạng: TXT, DOCX, PDF
 * ✅ Đã chuyển sang TaskBO (phù hợp với kiến trúc TCP)
 */
@WebServlet("/download/*")
public class DownloadController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TaskBO taskBO;
    private ExportService exportService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.taskBO = new TaskBO();
        this.exportService = new ExportService();
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
        
        User user = (User) session.getAttribute("user");
        
        // Lấy Task ID và format từ URL: /download/{taskId}/{format}
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu thông tin tải xuống");
            return;
        }
        
        String[] pathParts = pathInfo.substring(1).split("/");
        if (pathParts.length < 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "URL không hợp lệ");
            return;
        }
        
        try {
            int taskId = Integer.parseInt(pathParts[0]);
            String format = pathParts[1].toLowerCase();
            
            // Kiểm tra quyền truy cập
            if (!taskBO.kiemTraTaskThuocUser(taskId, user.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền truy cập task này");
                return;
            }
            
            // Lấy thông tin task
            Task task = taskBO.layThongTinTask(taskId);
            
            if (task == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy task");
                return;
            }
            
            // Kiểm tra task đã hoàn thành chưa
            if (!"COMPLETED".equals(task.getStatus())) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task chưa hoàn thành");
                return;
            }
            
            // Kiểm tra có kết quả không
            if (task.getResultText() == null || task.getResultText().trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task không có kết quả");
                return;
            }
            
            // Xuất file theo định dạng
            String fileName = getCleanFileName(task.getFileName());
            byte[] fileContent;
            String fileExtension;
            
            // ✅ Đảm bảo response encoding là UTF-8
            response.setCharacterEncoding("UTF-8");
            
            switch (format) {
                case "txt":
                    response.setContentType("text/plain; charset=UTF-8");
                    fileExtension = ".txt";
                    fileContent = exportService.exportToTxt(task.getResultText());
                    break;
                    
                case "docx":
                case "word":
                    response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document; charset=UTF-8");
                    fileExtension = ".docx";
                    fileContent = exportService.exportToDocx(task.getResultText(), task.getFileName());
                    break;
                    
                case "pdf":
                    response.setContentType("application/pdf; charset=UTF-8");
                    fileExtension = ".pdf";
                    fileContent = exportService.exportToPdf(task.getResultText(), task.getFileName());
                    break;
                    
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Định dạng không được hỗ trợ");
                    return;
            }
            
            // Set Content-Disposition với encoding đúng cho tiếng Việt
            response.setHeader("Content-Disposition", encodeFileName(fileName + fileExtension));
            
            // Ghi file ra response
            response.setContentLength(fileContent.length);
            OutputStream out = response.getOutputStream();
            out.write(fileContent);
            out.flush();
            out.close();
            
            System.out.println("[DownloadController] User " + user.getUsername() + 
                             " đã tải xuống task " + taskId + " định dạng " + format);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task ID không hợp lệ");
        } catch (Exception e) {
            System.err.println("[DownloadController] Lỗi khi tải xuống: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tạo file tải xuống");
        }
    }
    
    /**
     * Làm sạch tên file, loại bỏ phần mở rộng và ký tự đặc biệt
     */
    private String getCleanFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "result";
        }
        
        // Loại bỏ phần mở rộng
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileName = fileName.substring(0, lastDotIndex);
        }
        
        // Loại bỏ ký tự đặc biệt nguy hiểm
        fileName = fileName.replaceAll("[/\\\\:*?\"<>|]", "_");
        
        return fileName;
    }
    
    /**
     * Encode tên file để sử dụng trong HTTP header Content-Disposition
     * Hỗ trợ tiếng Việt có dấu theo chuẩn RFC 5987
     */
    private String encodeFileName(String fileName) {
        try {
            // ASCII fallback cho trình duyệt cũ
            String asciiFileName = fileName.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
            
            // UTF-8 encoded theo RFC 5987
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            // Format: filename="ascii_fallback"; filename*=UTF-8''encoded_name
            return "attachment; filename=\"" + asciiFileName + "\"; filename*=UTF-8''" + encodedFileName;
        } catch (Exception e) {
            // Fallback nếu có lỗi
            return "attachment; filename=\"download\"";
        }
    }
}
