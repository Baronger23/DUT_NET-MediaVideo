package Model.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnect {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/media_processor_db?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = ""; 
    
    private static DBConnect instance;
    private Connection connection;
    

    private DBConnect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            // Đảm bảo charset UTF-8 cho connection
            if (this.connection != null && !this.connection.isClosed()) {
                try (var stmt = this.connection.createStatement()) {
                    stmt.execute("SET NAMES 'utf8mb4'");
                    stmt.execute("SET CHARACTER SET utf8mb4");
                    stmt.execute("SET character_set_connection=utf8mb4");
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy MySQL JDBC Driver: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }
    

    public static synchronized DBConnect getInstance() {
        if (instance == null) {
            instance = new DBConnect();
        }
        return instance;
    }
    

    public Connection getConnection() throws SQLException {

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            // Đảm bảo charset UTF-8 cho connection mới
            if (connection != null && !connection.isClosed()) {
                try (var stmt = connection.createStatement()) {
                    stmt.execute("SET NAMES 'utf8mb4'");
                    stmt.execute("SET CHARACTER SET utf8mb4");
                    stmt.execute("SET character_set_connection=utf8mb4");
                }
            }
        }
        return connection;
    }
    

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đóng kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }
    

    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
