package Listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import Service.WorkerServiceVosk;

/**
 * Listener khởi động Worker khi server start
 * Worker sẽ chạy ngầm và xử lý các task trong Queue với Vosk STT
 */
@WebListener
public class WorkerInitializer implements ServletContextListener {
    private Thread workerThread1;
    private Thread workerThread2;
    private WorkerServiceVosk worker1;
    private WorkerServiceVosk worker2;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("========================================");
        System.out.println("🚀 Server đang khởi động...");
        System.out.println("========================================");
        
        // Khởi động 2 Worker threads với Vosk STT (có thể tăng giảm tùy ý)
        worker1 = new WorkerServiceVosk("Worker-1");
        workerThread1 = new Thread(worker1);
        workerThread1.setDaemon(false); // Không phải daemon để đảm bảo xử lý xong task
        workerThread1.start();
        
        worker2 = new WorkerServiceVosk("Worker-2");
        workerThread2 = new Thread(worker2);
        workerThread2.setDaemon(false);
        workerThread2.start();
        
        System.out.println("✓ Worker-1 và Worker-2 đã được khởi động");
        System.out.println("✓ Hệ thống sẵn sàng xử lý tasks");
        System.out.println("========================================");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("========================================");
        System.out.println("🛑 Server đang shutdown...");
        System.out.println("========================================");
        
        // Dừng các Worker
        if (worker1 != null) {
            worker1.shutdown();
        }
        if (worker2 != null) {
            worker2.shutdown();
        }
        
        // Đợi Worker hoàn thành
        try {
            if (workerThread1 != null) {
                workerThread1.interrupt();
                workerThread1.join(5000); // Đợi tối đa 5 giây
            }
            if (workerThread2 != null) {
                workerThread2.interrupt();
                workerThread2.join(5000);
            }
        } catch (InterruptedException e) {
            System.err.println("Lỗi khi đợi Worker dừng: " + e.getMessage());
        }
        
        System.out.println("✓ Các Worker đã dừng");
        System.out.println("========================================");
    }
}
