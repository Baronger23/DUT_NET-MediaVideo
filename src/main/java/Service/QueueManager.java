package Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Quản lý hàng đợi Task (Queue) - Singleton Pattern
 * Sử dụng BlockingQueue để đảm bảo thread-safe
 */
public class QueueManager {
    private static QueueManager instance;
    private BlockingQueue<Integer> taskQueue;
    
    private QueueManager() {
        // Khởi tạo hàng đợi với capacity lớn
        this.taskQueue = new LinkedBlockingQueue<>(1000);
        System.out.println("QueueManager đã được khởi tạo");
    }
    
    /**
     * Singleton instance
     */
    public static synchronized QueueManager getInstance() {
        if (instance == null) {
            instance = new QueueManager();
        }
        return instance;
    }
    
    /**
     * Đẩy Task ID vào hàng đợi
     * @param taskId ID của task cần xử lý
     * @return true nếu thêm thành công
     */
    public boolean enqueue(int taskId) {
        try {
            taskQueue.put(taskId); // Blocking nếu queue đầy
            System.out.println("[QUEUE] Đã thêm Task ID: " + taskId + " vào hàng đợi. Số lượng: " + taskQueue.size());
            return true;
        } catch (InterruptedException e) {
            System.err.println("Lỗi khi thêm task vào queue: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * Lấy Task ID từ hàng đợi (Worker sử dụng)
     * @return Task ID, hoặc null nếu queue rỗng
     */
    public Integer dequeue() throws InterruptedException {
        Integer taskId = taskQueue.take(); // Blocking cho đến khi có task
        System.out.println("[QUEUE] Worker lấy Task ID: " + taskId + ". Còn lại: " + taskQueue.size());
        return taskId;
    }
    
    /**
     * Lấy số lượng task đang chờ trong queue
     */
    public int getQueueSize() {
        return taskQueue.size();
    }
    
    /**
     * Kiểm tra queue có rỗng không
     */
    public boolean isEmpty() {
        return taskQueue.isEmpty();
    }
}
