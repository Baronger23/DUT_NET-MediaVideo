package Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ✅ TỐI ƯU HÓA: Quản lý hàng đợi Task (Queue) - Singleton Pattern
 * - Thread-safe với double-checked locking
 * - Monitoring với AtomicInteger
 * - Sử dụng BlockingQueue để đảm bảo thread-safe
 */
public class QueueManager {
    private static volatile QueueManager instance;
    private final BlockingQueue<Integer> taskQueue;
    
    // ✅ Monitoring metrics
    private final AtomicInteger totalEnqueued = new AtomicInteger(0);
    private final AtomicInteger totalDequeued = new AtomicInteger(0);
    
    private QueueManager() {
        // Khởi tạo hàng đợi với capacity lớn
        this.taskQueue = new LinkedBlockingQueue<>(1000);
        System.out.println("✅ QueueManager đã được khởi tạo (Capacity: 1000)");
    }
    
    /**
     * ✅ Thread-safe Singleton với double-checked locking
     */
    public static QueueManager getInstance() {
        if (instance == null) {
            synchronized (QueueManager.class) {
                if (instance == null) {
                    instance = new QueueManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * ✅ Đẩy Task ID vào hàng đợi với monitoring
     * @param taskId ID của task cần xử lý
     * @return true nếu thêm thành công
     */
    public boolean enqueue(int taskId) {
        try {
            taskQueue.put(taskId); // Blocking nếu queue đầy
            totalEnqueued.incrementAndGet();
            System.out.println("[QUEUE] ✓ Đã thêm Task ID: " + taskId + " | Đang chờ: " + taskQueue.size() + " | Tổng đã nhận: " + totalEnqueued.get());
            return true;
        } catch (InterruptedException e) {
            System.err.println("❌ Lỗi khi thêm task vào queue: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * ✅ Lấy Task ID từ hàng đợi với monitoring (Worker sử dụng)
     * @return Task ID, hoặc null nếu bị interrupt
     */
    public Integer dequeue() throws InterruptedException {
        Integer taskId = taskQueue.take(); // Blocking cho đến khi có task
        totalDequeued.incrementAndGet();
        System.out.println("[QUEUE] ◄ Worker lấy Task ID: " + taskId + " | Còn lại: " + taskQueue.size() + " | Tổng đã xử lý: " + totalDequeued.get());
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
    
    /**
     * ✅ Lấy thống kê queue (monitoring)
     */
    public String getQueueStats() {
        return String.format("Queue Stats - Waiting: %d, Total Enqueued: %d, Total Processed: %d, Remaining: %d",
            taskQueue.size(),
            totalEnqueued.get(),
            totalDequeued.get(),
            totalEnqueued.get() - totalDequeued.get()
        );
    }
    
    /**
     * ✅ Reset statistics (dùng cho testing hoặc maintenance)
     */
    public void resetStats() {
        totalEnqueued.set(0);
        totalDequeued.set(0);
    }
}
