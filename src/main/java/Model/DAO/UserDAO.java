package Model.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Model.DAO.DBConnect;
import Model.Bean.User;

public class UserDAO {
	private DBConnect dbConnect;
	
    public UserDAO() {
        this.dbConnect = DBConnect.getInstance();
    }
    
    public User xacThucDangNhap(String username, String password) {
        String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
        
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            // TODO: Trong thực tế cần mã hóa password trước khi so sánh
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xác thực đăng nhập: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean dangKyUser(String username, String password, String email) {
        String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            // TODO: Trong thực tế cần mã hóa password trước khi lưu
            stmt.setString(2, password);
            stmt.setString(3, email);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi đăng ký user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean kiemTraUsernameTonTai(String username) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra username: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean kiemTraEmailTonTai(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra email: " + e.getMessage());
        }
        
        return false;
    }

	private User mapResultSetToUser(ResultSet rs) {
		try {
			int id = rs.getInt("id");
			String username = rs.getString("username");
			String password = rs.getString("password");
			String email = rs.getString("email");
			java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
			
			return new User(id, username, password, email, createdAt);
		} catch (SQLException e) {
			System.err.println("Lỗi khi ánh xạ ResultSet sang User: " + e.getMessage());
		}
		return null;
	} 
}
