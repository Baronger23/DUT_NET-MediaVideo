//package Listener;
//
//import jakarta.servlet.ServletContextEvent;
//import jakarta.servlet.ServletContextListener;
//import jakarta.servlet.annotation.WebListener;
//
//import Model.DAO.DBConnect;
//import Service.VoskModelManager;
//import Service.WorkerServiceVosk;
//
///**
// * ✅ TỐI ƯU HÓA: Listener khởi động Worker khi server start
// * - Load Vosk models 1 lần duy nhất
// * - Khởi động Worker pool
// * - Graceful shutdown
// */
//@WebListener
//public class WorkerInitializer implements ServletContextListener {
//    private Thread workerThread1;
//    private Thread workerThread2;
//    private WorkerServiceVosk worker1;
//    private WorkerServiceVosk worker2;
//    
//    @Override
//    public void contextInitialized(ServletContextEvent sce) {
//        System.out.println("========================================");
//        System.out.println("🚀 Server đang khởi động...");
//        System.out.println("========================================");
//        
//        // ✅ BƯỚC 1: Khởi tạo Connection Pool
//        DBConnect dbConnect = DBConnect.getInstance();
//        System.out.println("✓ Connection Pool: " + dbConnect.getPoolStats());
//        
//        // ✅ BƯỚC 2: Load Vosk models 1 lần duy nhất (tiết kiệm RAM)
//        VoskModelManager modelManager = VoskModelManager.getInstance();
//        modelManager.initializeModels();
//        System.out.println("✓ " + modelManager.getModelsStatus());
//        
//        // ✅ BƯỚC 3: Khởi động Worker threads
//        worker1 = new WorkerServiceVosk("Worker-1");
//        workerThread1 = new Thread(worker1);
//        workerThread1.setDaemon(false);
//        workerThread1.start();
//        
//        worker2 = new WorkerServiceVosk("Worker-2");
//        workerThread2 = new Thread(worker2);
//        workerThread2.setDaemon(false);
//        workerThread2.start();
//        
//        System.out.println("✓ Worker-1 và Worker-2 đã được khởi động");
//        System.out.println("✓ Hệ thống sẵn sàng xử lý tasks");
//        System.out.println("========================================");
//    }
//    
//    @Override
//    public void contextDestroyed(ServletContextEvent sce) {
//        System.out.println("========================================");
//        System.out.println("🛑 Server đang shutdown...");
//        System.out.println("========================================");
//        
//        // ✅ BƯỚC 1: Dừng các Worker
//        if (worker1 != null) {
//            worker1.shutdown();
//        }
//        if (worker2 != null) {
//            worker2.shutdown();
//        }
//        
//        // Đợi Worker hoàn thành
//        try {
//            if (workerThread1 != null) {
//                workerThread1.interrupt();
//                workerThread1.join(5000);
//            }
//            if (workerThread2 != null) {
//                workerThread2.interrupt();
//                workerThread2.join(5000);
//            }
//        } catch (InterruptedException e) {
//            System.err.println("Lỗi khi đợi Worker dừng: " + e.getMessage());
//        }
//        
//        System.out.println("✓ Các Worker đã dừng");
//        
//        // ✅ BƯỚC 2: Đóng Vosk models
//        VoskModelManager modelManager = VoskModelManager.getInstance();
//        modelManager.closeModels();
//        
//        // ✅ BƯỚC 3: Đóng Connection Pool
//        DBConnect dbConnect = DBConnect.getInstance();
//        dbConnect.closeAllConnections();
//        
//        System.out.println("✓ Cleanup hoàn tất");
//        System.out.println("========================================");
//    }
//}
