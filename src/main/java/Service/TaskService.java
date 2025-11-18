package Service;

import java.util.List;

import Model.Bean.Task;
import Model.BO.TaskBO;

/**
 * Service Layer - Logic nghiệp vụ cho Task
 * Kết nối giữa Controller và BO, tích hợp với Queue
 */
public class TaskService {
    private TaskBO taskBO;
    private QueueManager queueManager;
    
    public TaskService() {
        this.taskBO = new TaskBO();
        this.queueManager = QueueManager.getInstance();
    }
    
    /**
     * Tạo task mới và đẩy vào Queue
     * @param userId ID người dùng
     * @param fileName Tên file
     * @param serverFilePath Đường dẫn file trên server
     * @param language Ngôn ngữ (vi=Tiếng Việt, en=Tiếng Anh)
     * @return Task ID nếu thành công, -1 nếu thất bại
     */
    public int taoVaDayTaskVaoQueue(int userId, String fileName, String serverFilePath, String language) {
        // 1. Tạo Task trong Database với status PENDING và language
        int taskId = taskBO.taoTaskMoi(userId, fileName, serverFilePath, language);
        
        if (taskId <= 0) {
            System.err.println("[TaskService] Không thể tạo task trong database");
            return -1;
        }
        
        // 2. Đẩy Task ID vào Queue
        boolean success = queueManager.enqueue(taskId);
        
        if (!success) {
            System.err.println("[TaskService] Không thể đẩy task " + taskId + " vào queue");
            return -1;
        }
        
        System.out.println("[TaskService] Task " + taskId + " (ngôn ngữ: " + language + ") đã được tạo và đẩy vào queue thành công");
        return taskId;
    }
    
    /**
     * Lấy lịch sử task của user
     */
    public List<Task> layLichSuTask(int userId) {
        return taskBO.layLichSuTask(userId);
    }
    
    /**
     * Lấy chi tiết một task
     */
    public Task layThongTinTask(int taskId) {
        return taskBO.layThongTinTask(taskId);
    }
    
    /**
     * Kiểm tra task có thuộc về user không (bảo mật)
     */
    public boolean kiemTraQuyenTruyCap(int taskId, int userId) {
        return taskBO.kiemTraTaskThuocUser(taskId, userId);
    }
    
    /**
     * Đếm số task theo trạng thái
     */
    public int demTaskTheoTrangThai(int userId, String status) {
        return taskBO.demTaskTheoTrangThai(userId, status);
    }
    
    /**
     * Lấy số lượng task đang chờ trong queue
     */
    public int getSoLuongTaskTrongQueue() {
        return queueManager.getQueueSize();
    }
    
    /**
     * Xóa task (chỉ cho phép xóa nếu là task của user đó)
     */
    public boolean xoaTask(int taskId, int userId) {
        return taskBO.xoaTask(taskId, userId);
    }
}
