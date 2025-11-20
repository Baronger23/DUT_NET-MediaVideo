package Listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.Statement;

import Model.DAO.DBConnect;

/**
 * Listener để khởi tạo database khi server start
 * Tự động tạo bảng và dữ liệu mẫu nếu database trống
 */
@WebListener
public class DatabaseInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("========================================");
        System.out.println("🚀 BẮT ĐẦU KHỞI TẠO DATABASE...");
        System.out.println("========================================");
        
        Connection conn = null;
        Statement stmt = null;
        
        try {
            DBConnect dbConnect = DBConnect.getInstance();
            conn = dbConnect.getConnection();
            stmt = conn.createStatement();
            
            // ===== TẠO BẢNG USER =====
            String createUserTable = 
                "CREATE TABLE IF NOT EXISTS \"user\" (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    username VARCHAR(50) NOT NULL UNIQUE," +
                "    password VARCHAR(255) NOT NULL," +
                "    email VARCHAR(100)," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.execute(createUserTable);
            System.out.println("✅ Bảng 'user' đã được tạo");
            
            // ===== TẠO BẢNG TASK =====
            String createTaskTable = 
                "CREATE TABLE IF NOT EXISTS Task (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    user_id INT NOT NULL," +
                "    file_name VARCHAR(255) NOT NULL," +
                "    server_file_path VARCHAR(500) NOT NULL," +
                "    status VARCHAR(20) DEFAULT 'PENDING'," +
                "    language VARCHAR(10) DEFAULT 'vi'," +
                "    result_text TEXT," +
                "    submission_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    completion_time TIMESTAMP NULL," +
                "    processing_time_ms INT DEFAULT 0," +
                "    FOREIGN KEY (user_id) REFERENCES \"user\"(id) ON DELETE CASCADE" +
                ")";
            stmt.execute(createTaskTable);
            System.out.println("✅ Bảng 'Task' đã được tạo");
            
            // ===== TẠO INDEX =====
            try {
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_task_user_id ON Task(user_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_task_status ON Task(status)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_task_language ON Task(language)");
                System.out.println("✅ Các index đã được tạo");
            } catch (Exception e) {
                // Index có thể đã tồn tại, bỏ qua
            }
            
            // ===== THÊM DỮ LIỆU MẪU (chỉ nếu chưa có) =====
            // Kiểm tra xem đã có user chưa
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM \"user\"");
            rs.next();
            int userCount = rs.getInt(1);
            
            if (userCount == 0) {
                System.out.println("📝 Thêm dữ liệu mẫu...");
                
                // Thêm user mẫu
                stmt.execute(
                    "INSERT INTO \"user\" (username, password, email) VALUES " +
                    "('admin', 'admin123', 'admin@example.com'), " +
                    "('demo', 'demo123', 'demo@example.com')"
                );
                System.out.println("✅ Đã thêm 2 user mẫu:");
                System.out.println("   - username: admin, password: admin123");
                System.out.println("   - username: demo, password: demo123");
                
                // Thêm task mẫu
                stmt.execute(
                    "INSERT INTO Task (user_id, file_name, server_file_path, status, language, result_text, submission_time, completion_time, processing_time_ms) VALUES " +
                    "(1, 'sample_vi.mp3', '/uploads/sample_vi.mp3', 'COMPLETED', 'vi', 'Đây là bản ghi âm tiếng Việt mẫu.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5000), " +
                    "(1, 'sample_en.mp3', '/uploads/sample_en.mp3', 'COMPLETED', 'en', 'This is a sample English audio recording.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4500)"
                );
                System.out.println("✅ Đã thêm 2 task mẫu");
            } else {
                System.out.println("ℹ️  Database đã có dữ liệu, bỏ qua việc thêm dữ liệu mẫu");
            }
            
            System.out.println("========================================");
            System.out.println("✅ KHỞI TẠO DATABASE THÀNH CÔNG!");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ LỖI KHI KHỞI TẠO DATABASE:");
            System.err.println("========================================");
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (Exception e) {}
            }
            if (conn != null) {
                DBConnect.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("========================================");
        System.out.println("🛑 ĐÓNG KẾT NỐI DATABASE...");
        System.out.println("========================================");
        
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // ✅ QUAN TRỌNG: Shutdown H2 Database trước khi đóng connections
            conn = DBConnect.getInstance().getConnection();
            stmt = conn.createStatement();
            stmt.execute("SHUTDOWN");
            System.out.println("✅ H2 Database đã được shutdown");
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi shutdown H2: " + e.getMessage());
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (Exception e) {}
            }
            if (conn != null) {
                try { conn.close(); } catch (Exception e) {}
            }
        }
        
        try {
            DBConnect.getInstance().closeAllConnections();
            System.out.println("✅ Đã đóng tất cả connections");
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi đóng connections: " + e.getMessage());
        }
        
        System.out.println("========================================");
        System.out.println("✅ DATABASE SHUTDOWN HOÀN TẤT");
        System.out.println("========================================");
    }
}
