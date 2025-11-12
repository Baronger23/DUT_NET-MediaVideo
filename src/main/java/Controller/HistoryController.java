package Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

import Model.Bean.Task;
import Model.Bean.User;
import Service.TaskService;

/**
 * HistoryController - Xem lịch sử Task
 * Bước 6: Người dùng xem danh sách Task và kết quả xử lý
 */
@WebServlet("/history")
public class HistoryController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TaskService taskService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.taskService = new TaskService();
    }
    
    public HistoryController() {
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
        
        User user = (User) session.getAttribute("user");
        
        try {
            // ===== BƯỚC 6: LẤY LỊCH SỬ TASK CỦA USER =====
            List<Task> taskHistory = taskService.layLichSuTask(user.getId());
            
            // Thống kê theo trạng thái
            int pendingCount = taskService.demTaskTheoTrangThai(user.getId(), "PENDING");
            int processingCount = taskService.demTaskTheoTrangThai(user.getId(), "PROCESSING");
            int completedCount = taskService.demTaskTheoTrangThai(user.getId(), "COMPLETED");
            int failedCount = taskService.demTaskTheoTrangThai(user.getId(), "FAILED");
            
            // Đặt attributes để hiển thị trên JSP
            request.setAttribute("taskHistory", taskHistory);
            request.setAttribute("pendingCount", pendingCount);
            request.setAttribute("processingCount", processingCount);
            request.setAttribute("completedCount", completedCount);
            request.setAttribute("failedCount", failedCount);
            request.setAttribute("totalCount", taskHistory != null ? taskHistory.size() : 0);
            
            System.out.println("[HistoryController] Đã lấy " + (taskHistory != null ? taskHistory.size() : 0) + " task của User ID: " + user.getId());
            
        } catch (Exception e) {
            System.err.println("[HistoryController] Lỗi khi lấy lịch sử: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Không thể tải lịch sử. Vui lòng thử lại.");
        }
        
        request.getRequestDispatcher("/View/history.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
