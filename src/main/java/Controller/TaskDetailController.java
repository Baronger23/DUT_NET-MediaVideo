package Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import Model.Bean.Task;
import Model.Bean.User;
import Service.TaskService;

/**
 * API để lấy chi tiết một Task
 * Dùng cho AJAX hoặc xem chi tiết kết quả
 */
@WebServlet("/api/task/*")
public class TaskDetailController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TaskService taskService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.taskService = new TaskService();
    }
    
    public TaskDetailController() {
        super();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Kiểm tra đăng nhập
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Chưa đăng nhập\"}");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        // Lấy Task ID từ URL: /api/task/123
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Thiếu Task ID\"}");
            return;
        }
        
        try {
            int taskId = Integer.parseInt(pathInfo.substring(1));
            
            // Kiểm tra quyền truy cập
            if (!taskService.kiemTraQuyenTruyCap(taskId, user.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Không có quyền truy cập task này\"}");
                return;
            }
            
            // Lấy thông tin task
            Task task = taskService.layThongTinTask(taskId);
            
            if (task == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Không tìm thấy task\"}");
                return;
            }
            
            // Trả về JSON hoặc forward đến JSP
            String format = request.getParameter("format");
            if ("json".equals(format)) {
                // ✅ Set UTF-8 encoding trước khi lấy Writer
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=UTF-8");
                
                String jsonResponse = taskToJson(task);
                
                // ✅ Sử dụng Writer thay vì OutputStream để tránh double encoding
                // Writer tự động xử lý encoding đúng cách
                response.getWriter().write(jsonResponse);
                response.getWriter().flush();
            } else {
                // Forward đến JSP để hiển thị chi tiết
                request.setAttribute("task", task);
                request.getRequestDispatcher("/View/task-detail.jsp").forward(request, response);
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Task ID không hợp lệ\"}");
        } catch (Exception e) {
            System.err.println("[TaskDetailController] Lỗi: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Lỗi server\"}");
        }
    }
    
    /**
     * Chuyển Task object sang JSON string đơn giản
     */
    private String taskToJson(Task task) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(task.getId()).append(",");
        json.append("\"userId\":").append(task.getUserId()).append(",");
        json.append("\"fileName\":\"").append(escapeJson(task.getFileName())).append("\",");
        json.append("\"serverFilePath\":\"").append(escapeJson(task.getServerFilePath())).append("\",");
        json.append("\"status\":\"").append(escapeJson(task.getStatus())).append("\",");
        json.append("\"submissionTime\":\"").append(task.getSubmissionTime() != null ? task.getSubmissionTime().toString() : "").append("\",");
        json.append("\"completionTime\":\"").append(task.getCompletionTime() != null ? task.getCompletionTime().toString() : "").append("\",");
        json.append("\"resultText\":\"").append(escapeJson(task.getResultText())).append("\",");
        json.append("\"processingTimeMs\":").append(task.getProcessingTimeMs() != null ? task.getProcessingTimeMs() : "null");
        json.append("}");
        return json.toString();
    }
    
    /**
     * Escape ký tự đặc biệt trong JSON
     * Xử lý đúng UTF-8 cho tiếng Việt
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    // Giữ nguyên các ký tự UTF-8 (bao gồm tiếng Việt)
                    // Không cần escape Unicode characters
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}
