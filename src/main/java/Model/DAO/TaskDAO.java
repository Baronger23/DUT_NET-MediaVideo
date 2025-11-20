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
    private TaskCache taskCache;
    
    public TaskDAO() {
        this.dbConnect = DBConnect.getInstance();
        this.taskCache = TaskCache.getInstance();
    }
    
    /**
     * Thêm task mới vào database với status 'PENDING' và ngôn ngữ
     * @return Task ID (LAST_INSERT_ID) để đẩy vào hàng đợi, hoặc -1 nếu thất bại
     */
    public int themTaskMoi(int userId, String fileName, String serverFilePath, String language) {
        String sql = "INSERT INTO Task (user_id, file_name, server_file_path, status, language, submission_time) VALUES (?, ?, ?, 'PENDING', ?, NOW())";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
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
        } finally {
            // ✅ QUAN TRỌNG: Trả connection về pool
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return -1;
    }
    
    /**
     * Worker cập nhật status thành 'PROCESSING' khi bắt đầu xử lý
     */
    public boolean capNhatStatusProcessing(int taskId) {
        String sql = "UPDATE Task SET status = 'PROCESSING' WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, taskId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // ✅ Invalidate cache sau khi update
                taskCache.invalidate(taskId);
                System.out.println("Đã cập nhật task " + taskId + " sang PROCESSING");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật status PROCESSING: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ✅ QUAN TRỌNG: Trả connection về pool
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return false;
    }
    
    /**
     * Worker cập nhật khi hoàn thành xử lý (30% điểm)
     */
    public boolean capNhatTaskHoanThanh(int taskId, String resultText, int processingTimeMs) {
        String sql = "UPDATE Task SET status = 'COMPLETED', result_text = ?, completion_time = NOW(), processing_time_ms = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            
            // H2 Database v2.x tự động sử dụng UTF-8 encoding
            // KHÔNG cần convert, chỉ cần set string trực tiếp
            stmt.setString(1, resultText);
            stmt.setInt(2, processingTimeMs);
            stmt.setInt(3, taskId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // ✅ Invalidate cache sau khi update
                taskCache.invalidate(taskId);
                System.out.println("Đã cập nhật task " + taskId + " hoàn thành");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật task hoàn thành: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ✅ QUAN TRỌNG: Trả connection về pool
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return false;
    }
    
    /**
     * Cập nhật status thành 'FAILED' khi xử lý thất bại
     */
    public boolean capNhatTaskThatBai(int taskId, String errorMessage) {
        String sql = "UPDATE Task SET status = 'FAILED', result_text = ?, completion_time = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, errorMessage);
            stmt.setInt(2, taskId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // ✅ Invalidate cache sau khi update
                taskCache.invalidate(taskId);
                System.out.println("Đã cập nhật task " + taskId + " thất bại");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật task thất bại: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ✅ QUAN TRỌNG: Trả connection về pool
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return false;
    }
    
    /**
     * ✅ TỐI ƯU: Lấy lịch sử task của user với LIMIT (tránh load quá nhiều)
     * @param userId ID người dùng
     * @return Danh sách task (tối đa 100 task gần nhất)
     */
    public List<Task> layLichSuTaskTheoUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        // Giới hạn 100 task gần nhất để tránh quá tải
        String sql = "SELECT * FROM Task WHERE user_id = ? ORDER BY submission_time DESC LIMIT 100";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, userId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = mapResultSetToTask(rs);
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử task: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ✅ QUAN TRỌNG: Trả connection về pool
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return tasks;
    }
    
    /**
     * ✅ TỐI ƯU: Lấy lịch sử task với pagination
     */
    public List<Task> layLichSuTaskTheoUserPaginated(int userId, int page, int pageSize) {
        List<Task> tasks = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        String sql = "SELECT * FROM Task WHERE user_id = ? ORDER BY submission_time DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, userId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = mapResultSetToTask(rs);
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử task (paginated): " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ✅ QUAN TRỌNG: Trả connection về pool
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return tasks;
    }
    
    /**
     * ✅ TỐI ƯU: Đếm tổng số task của user (cho pagination)
     */
    public int demTongSoTaskCuaUser(int userId) {
        String sql = "SELECT COUNT(*) FROM Task WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm task: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ✅ QUAN TRỌNG: Trả connection về pool
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return 0;
    }
    
    /**
     * ✅ TỐI ƯU: Lấy thông tin chi tiết một task theo ID với cache
     */
    public Task layTaskTheoId(int taskId) {
        // Kiểm tra cache trước
        Task cachedTask = taskCache.get(taskId);
        if (cachedTask != null) {
            return cachedTask;
        }
        
        // Nếu không có trong cache, query từ DB
        String sql = "SELECT * FROM Task WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, taskId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                Task task = mapResultSetToTask(rs);
                // Lưu vào cache
                taskCache.put(taskId, task);
                return task;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy task theo ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ✅ QUAN TRỌNG: Trả connection về pool
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return null;
    }
    
    /**
     * Lấy tất cả task đang pending (dùng cho worker)
     */
    public List<Task> layTatCaTaskPending() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM Task WHERE status = 'PENDING' ORDER BY submission_time ASC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = mapResultSetToTask(rs);
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy task pending: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ✅ QUAN TRỌNG: Trả connection về pool
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return tasks;
    }
    
    /**
     * Xóa task theo ID (chỉ cho phép xóa nếu là task của user đó)
     */
    public boolean xoaTask(int taskId, int userId) {
        String sql = "DELETE FROM Task WHERE id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // ✅ Invalidate cache sau khi xóa
                taskCache.invalidate(taskId);
                System.out.println("Đã xóa task " + taskId + " của user " + userId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa task: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ✅ QUAN TRỌNG: Trả connection về pool
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return false;
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
        
        // Đọc result_text - H2 Database đã tự động xử lý UTF-8
        // KHÔNG cần convert encoding vì H2 đã lưu đúng UTF-8
        String resultText = rs.getString("result_text");
        task.setResultText(resultText);
        
        // processingTimeMs có thể null
        int processingTime = rs.getInt("processing_time_ms");
        if (!rs.wasNull()) {
            task.setProcessingTimeMs(processingTime);
        }
        
        return task;
    }
}
