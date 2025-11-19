package Model.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ TỐI ƯU HÓA: Basic Connection Pool (không cần thư viện ngoài)
 * - Tạo sẵn nhiều connections để Workers dùng
 * - Thread-safe với synchronized
 * - Auto-reconnect khi connection bị lỗi
 */
public class DBConnect {
    // H2 Database - Embedded mode (file-based)
    // Database sẽ được tạo tự động tại thư mục user.home hoặc project folder
    // ✅ H2 v2.x tự động sử dụng UTF-8, không cần set CHARSET
    private static final String DB_URL = "jdbc:h2:~/media_processor_db;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String DB_USERNAME = "sa";
    private static final String DB_PASSWORD = ""; 
    
    private static DBConnect instance;
    
    // ✅ Connection Pool: Danh sách connections sẵn sàng
    private List<Connection> availableConnections = new ArrayList<>();
    private List<Connection> usedConnections = new ArrayList<>();
    private static final int MAX_POOL_SIZE = 10;
    private static final int INITIAL_POOL_SIZE = 3;
    
    /**
     * ✅ Khởi tạo Connection Pool
     */
    private DBConnect() {
        try {
            Class.forName("org.h2.Driver");
            
            // Tạo sẵn một số connections
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                availableConnections.add(createNewConnection());
            }
            
            System.out.println("✅ H2 Database Connection Pool đã được khởi tạo");
            System.out.println("   - Database: ~/media_processor_db");
            System.out.println("   - Initial Pool Size: " + INITIAL_POOL_SIZE + " connections");
            System.out.println("   - Max Pool Size: " + MAX_POOL_SIZE + " connections");
            System.out.println("   - H2 Console: http://localhost:8082 (nếu đã bật)");
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Không tìm thấy H2 JDBC Driver: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }
    
    /**
     * Tạo connection mới
     */
    private Connection createNewConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        // H2 đã hỗ trợ UTF-8 mặc định, không cần set thêm
        return conn;
    }
    
    /**
     * ✅ Thread-safe Singleton
     */
    public static DBConnect getInstance() {
        if (instance == null) {
            synchronized (DBConnect.class) {
                if (instance == null) {
                    instance = new DBConnect();
                }
            }
        }
        return instance;
    }
    
    /**
     * ✅ Lấy connection từ Pool (thread-safe)
     * Mỗi thread sẽ nhận được connection riêng
     */
    public synchronized Connection getConnection() throws SQLException {
        // Nếu không còn connection available
        if (availableConnections.isEmpty()) {
            // Nếu chưa đạt max pool size, tạo connection mới
            if (usedConnections.size() < MAX_POOL_SIZE) {
                Connection newConn = createNewConnection();
                usedConnections.add(newConn);
                return newConn;
            } else {
                // Đã đạt max, phải đợi
                throw new SQLException("Connection pool đã đầy. Vui lòng thử lại sau.");
            }
        }
        
        // Lấy connection từ pool
        Connection conn = availableConnections.remove(availableConnections.size() - 1);
        
        // Kiểm tra connection còn sống không
        if (conn == null || conn.isClosed() || !conn.isValid(2)) {
            conn = createNewConnection(); // Tạo mới nếu hỏng
        }
        
        usedConnections.add(conn);
        return conn;
    }
    
    /**
     * ✅ Trả connection về pool (QUAN TRỌNG: Gọi sau khi dùng xong)
     */
    public synchronized void releaseConnection(Connection conn) {
        if (conn != null) {
            usedConnections.remove(conn);
            availableConnections.add(conn);
        }
    }
    
    /**
     * ✅ Đóng toàn bộ connections khi shutdown server
     */
    public synchronized void closeAllConnections() {
        // Đóng available connections
        for (Connection conn : availableConnections) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng connection: " + e.getMessage());
            }
        }
        
        // Đóng used connections
        for (Connection conn : usedConnections) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng connection: " + e.getMessage());
            }
        }
        
        availableConnections.clear();
        usedConnections.clear();
        
        System.out.println("✅ Tất cả connections đã được đóng");
    }
    
    /**
     * Lấy thống kê Pool (để monitor)
     */
    public synchronized String getPoolStats() {
        return String.format("Pool Stats - Available: %d, Used: %d, Total: %d",
            availableConnections.size(),
            usedConnections.size(),
            availableConnections.size() + usedConnections.size()
        );
    }
}
