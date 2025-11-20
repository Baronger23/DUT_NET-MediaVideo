package Listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import Service.WorkerTCPServer;

/**
 * 🚀 WorkerTCPServerListener - Tự động khởi động TCP Worker Server khi Tomcat start
 * 
 * Listener này sẽ:
 * 1. Khởi động TCP Worker Server trong một thread riêng khi Tomcat start
 * 2. Tắt TCP Worker Server một cách graceful khi Tomcat stop
 * 
 * ✅ Lợi ích:
 * - Không cần chạy TCP Server thủ công bằng file .bat
 * - TCP Server tự động start/stop cùng với Tomcat
 * - Dễ dàng deploy và quản lý
 */
@WebListener
public class WorkerTCPServerListener implements ServletContextListener {
    
    private WorkerTCPServer tcpServer;
    private Thread serverThread;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("========================================");
        System.out.println("🚀 KHỞI ĐỘNG TCP WORKER SERVER...");
        System.out.println("========================================");
        
        try {
            // Tạo TCP Server instance
            tcpServer = new WorkerTCPServer();
            
            // Khởi động TCP Server trong thread riêng (không block Tomcat)
            serverThread = new Thread(() -> {
                try {
                    tcpServer.start();
                } catch (Exception e) {
                    System.err.println("❌ Lỗi khi khởi động TCP Server: " + e.getMessage());
                    e.printStackTrace();
                }
            }, "TCP-Worker-Server-Thread");
            
            serverThread.setDaemon(false); // Không tự động tắt khi Tomcat tắt
            serverThread.start();
            
            // Đợi một chút để TCP Server khởi động
            Thread.sleep(2000);
            
            System.out.println("========================================");
            System.out.println("✅ TCP WORKER SERVER ĐÃ ĐƯỢC KHỞI ĐỘNG");
            System.out.println("   Port: 9999");
            System.out.println("   Thread: " + serverThread.getName());
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ LỖI KHI KHỞI ĐỘNG TCP WORKER SERVER:");
            System.err.println("========================================");
            e.printStackTrace();
            System.err.println("⚠️  TCP Worker Server sẽ không khả dụng!");
            System.err.println("   Ứng dụng web vẫn chạy nhưng không thể xử lý Speech-to-Text");
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("========================================");
        System.out.println("🛑 ĐANG TẮT TCP WORKER SERVER...");
        System.out.println("========================================");
        
        try {
            if (tcpServer != null) {
                // Shutdown TCP Server gracefully
                tcpServer.shutdown();
                System.out.println("✅ TCP Server đã được shutdown");
            }
            
            if (serverThread != null && serverThread.isAlive()) {
                // Đợi thread kết thúc (timeout 5 giây)
                serverThread.join(5000);
                
                if (serverThread.isAlive()) {
                    System.err.println("⚠️  TCP Server thread vẫn còn chạy sau 5 giây");
                } else {
                    System.out.println("✅ TCP Server thread đã kết thúc");
                }
            }
            
            System.out.println("========================================");
            System.out.println("✅ TCP WORKER SERVER ĐÃ ĐƯỢC TẮT");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tắt TCP Worker Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
