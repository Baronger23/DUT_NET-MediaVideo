package Model.BO;

import java.util.List;

import Model.Bean.Task;
import Model.DAO.TaskDAO;

/**
 * Business Object cho Task - Tầng logic nghiệp vụ
 * Xử lý các quy tắc nghiệp vụ trước khi gọi xuống DAO
 */
public class TaskBO {
    private TaskDAO taskDAO;
    
    public TaskBO() {
        this.taskDAO = new TaskDAO();
    }
    
    /**
     * Tạo task mới khi người dùng upload file
     * @param userId ID của người dùng
     * @param fileName Tên file gốc
     * @param serverFilePath Đường dẫn file trên server
     * @param language Ngôn ngữ (vi=Tiếng Việt, en=Tiếng Anh)
     * @return Task ID để đẩy vào Queue, hoặc -1 nếu thất bại
     */
    public int taoTaskMoi(int userId, String fileName, String serverFilePath, String language) {
        // Kiểm tra tham số đầu vào
        if (userId <= 0) {
            System.err.println("User ID không hợp lệ");
            return -1;
        }
        
        if (fileName == null || fileName.trim().isEmpty()) {
            System.err.println("Tên file không được để trống");
            return -1;
        }
        
        if (serverFilePath == null || serverFilePath.trim().isEmpty()) {
            System.err.println("Đường dẫn file không được để trống");
            return -1;
        }
        
        // Validate language (mặc định là tiếng Việt)
        if (language == null || language.trim().isEmpty()) {
            language = "vi";
        }
        
        if (!language.equals("vi") && !language.equals("en")) {
            System.err.println("Ngôn ngữ không hợp lệ, mặc định dùng tiếng Việt");
            language = "vi";
        }
        
        // Gọi DAO để thêm task với ngôn ngữ
        return taskDAO.themTaskMoi(userId, fileName, serverFilePath, language);
    }
    
    /**
     * Worker đánh dấu task đang được xử lý
     */
    public boolean datTaskDangXuLy(int taskId) {
        if (taskId <= 0) {
            System.err.println("Task ID không hợp lệ");
            return false;
        }
        
        return taskDAO.capNhatStatusProcessing(taskId);
    }
    
    /**
     * Worker hoàn thành xử lý task (30% điểm)
     * @param taskId ID của task
     * @param resultText Kết quả Speech-to-Text
     * @param processingTimeMs Thời gian xử lý (milliseconds)
     */
    public boolean hoanThanhTask(int taskId, String resultText, int processingTimeMs) {
        if (taskId <= 0) {
            System.err.println("Task ID không hợp lệ");
            return false;
        }
        
        if (resultText == null) {
            resultText = ""; // Cho phép kết quả rỗng
        }
        
        if (processingTimeMs < 0) {
            System.err.println("Thời gian xử lý không thể âm");
            return false;
        }
        
        return taskDAO.capNhatTaskHoanThanh(taskId, resultText, processingTimeMs);
    }
    
    /**
     * Đánh dấu task thất bại
     */
    public boolean datTaskThatBai(int taskId, String errorMessage) {
        if (taskId <= 0) {
            System.err.println("Task ID không hợp lệ");
            return false;
        }
        
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorMessage = "Lỗi không xác định";
        }
        
        return taskDAO.capNhatTaskThatBai(taskId, errorMessage);
    }
    
    /**
     * Lấy lịch sử task của user để hiển thị trên giao diện
     * @param userId ID của người dùng
     * @return Danh sách task (sắp xếp theo thời gian mới nhất)
     */
    public List<Task> layLichSuTask(int userId) {
        if (userId <= 0) {
            System.err.println("User ID không hợp lệ");
            return null;
        }
        
        return taskDAO.layLichSuTaskTheoUser(userId);
    }
    
    /**
     * Lấy thông tin chi tiết một task
     */
    public Task layThongTinTask(int taskId) {
        if (taskId <= 0) {
            System.err.println("Task ID không hợp lệ");
            return null;
        }
        
        return taskDAO.layTaskTheoId(taskId);
    }
    
    /**
     * Lấy danh sách task đang pending (cho Worker)
     */
    public List<Task> layDanhSachTaskPending() {
        return taskDAO.layTatCaTaskPending();
    }
    
    /**
     * Kiểm tra task có thuộc về user hay không (để bảo mật)
     */
    public boolean kiemTraTaskThuocUser(int taskId, int userId) {
        Task task = taskDAO.layTaskTheoId(taskId);
        
        if (task == null) {
            return false;
        }
        
        return task.getUserId() == userId;
    }
    
    /**
     * Đếm số lượng task theo trạng thái của user
     */
    public int demTaskTheoTrangThai(int userId, String status) {
        List<Task> allTasks = taskDAO.layLichSuTaskTheoUser(userId);
        
        if (allTasks == null) {
            return 0;
        }
        
        int count = 0;
        for (Task task : allTasks) {
            if (task.getStatus().equals(status)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Xóa task (chỉ cho phép xóa nếu là task của user đó)
     */
    public boolean xoaTask(int taskId, int userId) {
        if (taskId <= 0 || userId <= 0) {
            System.err.println("Task ID hoặc User ID không hợp lệ");
            return false;
        }
        
        // Kiểm tra task có thuộc về user không
        if (!kiemTraTaskThuocUser(taskId, userId)) {
            System.err.println("User " + userId + " không có quyền xóa task " + taskId);
            return false;
        }
        
        return taskDAO.xoaTask(taskId, userId);
    }
}
