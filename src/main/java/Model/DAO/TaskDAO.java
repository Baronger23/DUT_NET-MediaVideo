package Model.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import Model.Bean.Task;

public class TaskDAO {
    private DBConnect dbConnect;
    
    public TaskDAO() {
        this.dbConnect = DBConnect.getInstance();
    }
    
    /**
     * Thêm task mới vào database với status 'PENDING' và ngôn ngữ
     * @return Task ID (LAST_INSERT_ID) để đẩy vào hàng đợi, hoặc -1 nếu thất bại
     */
    public int themTaskMoi(int userId, String fileName, String serverFilePath, String language) {
        String sql = "INSERT INTO Task (user_id, file_name, server_file_path, status, language, submission_time) VALUES (?, ?, ?, 'PENDING', ?, NOW())";
        
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, fileName);
            stmt.setString(3, serverFilePath);
            stmt.setString(4, language);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Lấy LAST_INSERT_ID()
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int taskId = rs.getInt(1);
                    System.out.println("Đã thêm task mới với ID: " + taskId + ", Ngôn ngữ: " + language);
                    return taskId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm task mới: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Worker cập nhật status thành 'PROCESSING' khi bắt đầu xử lý
     */
    public boolean capNhatStatusProcessing(int taskId) {
        String sql = "UPDATE Task SET status = 'PROCESSING' WHERE id = ?";
        
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Đã cập nhật task " + taskId + " sang PROCESSING");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật status PROCESSING: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Worker cập nhật khi hoàn thành xử lý (30% điểm)
     */
    public boolean capNhatTaskHoanThanh(int taskId, String resultText, int processingTimeMs) {
        String sql = "UPDATE Task SET status = 'COMPLETED', result_text = ?, completion_time = NOW(), processing_time_ms = ? WHERE id = ?";
        
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Đảm bảo UTF-8 encoding khi lưu vào database
            if (resultText != null) {
                // Convert sang UTF-8 bytes rồi tạo lại String để đảm bảo encoding đúng
                byte[] utf8Bytes = resultText.getBytes("UTF-8");
                String utf8Text = new String(utf8Bytes, "UTF-8");
                stmt.setString(1, utf8Text);
            } else {
                stmt.setString(1, null);
            }
            
            stmt.setInt(2, processingTimeMs);
            stmt.setInt(3, taskId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Đã cập nhật task " + taskId + " hoàn thành");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật task hoàn thành: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi UTF-8 encoding: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Cập nhật status thành 'FAILED' khi xử lý thất bại
     */
    public boolean capNhatTaskThatBai(int taskId, String errorMessage) {
        String sql = "UPDATE Task SET status = 'FAILED', result_text = ?, completion_time = NOW() WHERE id = ?";
        
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, errorMessage);
            stmt.setInt(2, taskId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Đã cập nhật task " + taskId + " thất bại");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật task thất bại: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Lấy lịch sử task của user (sắp xếp theo thời gian mới nhất)
     */
    public List<Task> layLichSuTaskTheoUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM Task WHERE user_id = ? ORDER BY submission_time DESC";
        
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = mapResultSetToTask(rs);
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử task: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tasks;
    }
    
    /**
     * Lấy thông tin chi tiết một task theo ID
     */
    public Task layTaskTheoId(int taskId) {
        String sql = "SELECT * FROM Task WHERE id = ?";
        
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTask(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy task theo ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Lấy tất cả task đang pending (dùng cho worker)
     */
    public List<Task> layTatCaTaskPending() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM Task WHERE status = 'PENDING' ORDER BY submission_time ASC";
        
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = mapResultSetToTask(rs);
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy task pending: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tasks;
    }
    
    /**
     * Map ResultSet sang đối tượng Task
     */
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setUserId(rs.getInt("user_id"));
        task.setFileName(rs.getString("file_name"));
        task.setServerFilePath(rs.getString("server_file_path"));
        task.setStatus(rs.getString("status"));
        task.setSubmissionTime(rs.getTimestamp("submission_time"));
        task.setCompletionTime(rs.getTimestamp("completion_time"));
        
        // Đọc language (mặc định là tiếng Việt nếu NULL)
        String language = rs.getString("language");
        task.setLanguage(language != null ? language : "vi");
        
        // Đọc result_text với UTF-8 encoding đúng cách
        String resultText = rs.getString("result_text");
        if (resultText != null) {
            try {
                // Đảm bảo UTF-8 encoding khi đọc từ database
                byte[] utf8Bytes = resultText.getBytes("ISO-8859-1");
                String utf8Text = new String(utf8Bytes, "UTF-8");
                task.setResultText(utf8Text);
            } catch (Exception e) {
                // Nếu lỗi, dùng text gốc
                task.setResultText(resultText);
            }
        } else {
            task.setResultText(null);
        }
        
        // processingTimeMs có thể null
        int processingTime = rs.getInt("processing_time_ms");
        if (!rs.wasNull()) {
            task.setProcessingTimeMs(processingTime);
        }
        
        return task;
    }
}
