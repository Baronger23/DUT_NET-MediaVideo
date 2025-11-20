//package Controller;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.MultipartConfig;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import jakarta.servlet.http.Part;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Paths;
//
//import Model.Bean.User;
//import Service.TaskService;
//
///**
// * MediaController - Xử lý upload file media (video/audio)
// * Bước 1 & 2: Nhận file, tạo Task, đẩy vào Queue
// */
//@WebServlet("/upload")
//@MultipartConfig(
//    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
//    maxFileSize = 1024 * 1024 * 100,      // 100MB
//    maxRequestSize = 1024 * 1024 * 150    // 150MB
//)
//public class MediaController extends HttpServlet {
//    private static final long serialVersionUID = 1L;
//    private static final String UPLOAD_DIRECTORY = "uploads";
//    private TaskService taskService;
//    
//    @Override
//    public void init() throws ServletException {
//        super.init();
//        this.taskService = new TaskService();
//        
//        // Tạo thư mục upload nếu chưa có
//        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
//        File uploadDir = new File(uploadPath);
//        if (!uploadDir.exists()) {
//            uploadDir.mkdirs();
//            System.out.println("Đã tạo thư mục upload: " + uploadPath);
//        }
//    }
//    
//    public MediaController() {
//        super();
//    }
//    
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        // Hiển thị trang upload
//        HttpSession session = request.getSession(false);
//        
//        if (session == null || session.getAttribute("user") == null) {
//            response.sendRedirect(request.getContextPath() + "/login");
//            return;
//        }
//        
//        // Lấy thống kê queue
//        int queueSize = taskService.getSoLuongTaskTrongQueue();
//        request.setAttribute("queueSize", queueSize);
//        
//        request.getRequestDispatcher("/View/upload.jsp").forward(request, response);
//    }
//    
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        
//        // Kiểm tra đăng nhập
//        HttpSession session = request.getSession(false);
//        if (session == null || session.getAttribute("user") == null) {
//            response.sendRedirect(request.getContextPath() + "/login");
//            return;
//        }
//        
//        User user = (User) session.getAttribute("user");
//        
//        try {
//            // ===== BƯỚC 1: NHẬN FILE UPLOAD VÀ NGÔN NGỮ =====
//            Part filePart = request.getPart("mediaFile");
//            
//            if (filePart == null || filePart.getSize() == 0) {
//                request.setAttribute("error", "Vui lòng chọn file để upload");
//                request.getRequestDispatcher("/View/upload.jsp").forward(request, response);
//                return;
//            }
//            
//            // Lấy ngôn ngữ từ form (mặc định là tiếng Việt)
//            String language = request.getParameter("language");
//            if (language == null || language.isEmpty()) {
//                language = "vi"; // Mặc định tiếng Việt
//            }
//            
//            // Validate language
//            if (!language.equals("vi") && !language.equals("en")) {
//                language = "vi"; // Fallback to Vietnamese
//            }
//            
//            System.out.println("[MediaController] Ngôn ngữ được chọn: " + language);
//            
//            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
//            
//            // Kiểm tra định dạng file
//            if (!isValidMediaFile(fileName)) {
//                request.setAttribute("error", "Chỉ chấp nhận file video (.mp4, .avi, .mov) hoặc audio (.mp3, .wav, .m4a)");
//                request.getRequestDispatcher("/View/upload.jsp").forward(request, response);
//                return;
//            }
//            
//            // Lưu file vào server
//            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
//            String uniqueFileName = generateUniqueFileName(fileName);
//            String serverFilePath = uploadPath + File.separator + uniqueFileName;
//            
//            filePart.write(serverFilePath);
//            System.out.println("[MediaController] File đã được lưu: " + serverFilePath);
//            
//            // ===== BƯỚC 2: TẠO TASK VÀ ĐẨY VÀO QUEUE (với ngôn ngữ) =====
//            int taskId = taskService.taoVaDayTaskVaoQueue(
//                user.getId(), 
//                fileName, 
//                serverFilePath,
//                language  // Thêm tham số ngôn ngữ
//            );
//            
//            if (taskId > 0) {
//                // Thành công
//                request.setAttribute("success", "File đã được tải lên thành công! Task ID: " + taskId);
//                request.setAttribute("taskId", taskId);
//                System.out.println("[MediaController] ✓ Task " + taskId + " đã được tạo và đẩy vào queue");
//            } else {
//                // Thất bại
//                request.setAttribute("error", "Không thể tạo task. Vui lòng thử lại.");
//                // Xóa file đã upload
//                new File(serverFilePath).delete();
//            }
//            
//        } catch (Exception e) {
//            System.err.println("[MediaController] Lỗi khi xử lý upload: " + e.getMessage());
//            e.printStackTrace();
//            request.setAttribute("error", "Lỗi: " + e.getMessage());
//        }
//        
//        request.getRequestDispatcher("/View/upload.jsp").forward(request, response);
//    }
//    
//    /**
//     * Kiểm tra định dạng file hợp lệ
//     */
//    private boolean isValidMediaFile(String fileName) {
//        String lowerFileName = fileName.toLowerCase();
//        return lowerFileName.endsWith(".mp4") || 
//               lowerFileName.endsWith(".avi") || 
//               lowerFileName.endsWith(".mov") || 
//               lowerFileName.endsWith(".mp3") || 
//               lowerFileName.endsWith(".wav") || 
//               lowerFileName.endsWith(".m4a") ||
//               lowerFileName.endsWith(".flv") ||
//               lowerFileName.endsWith(".wmv");
//    }
//    
//    /**
//     * Tạo tên file duy nhất (tránh trùng lặp)
//     */
//    private String generateUniqueFileName(String originalFileName) {
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        String extension = "";
//        
//        int lastDotIndex = originalFileName.lastIndexOf('.');
//        if (lastDotIndex > 0) {
//            extension = originalFileName.substring(lastDotIndex);
//            originalFileName = originalFileName.substring(0, lastDotIndex);
//        }
//        
//        // Loại bỏ ký tự đặc biệt
//        originalFileName = originalFileName.replaceAll("[^a-zA-Z0-9]", "_");
//        
//        return originalFileName + "_" + timestamp + extension;
//    }
//}
