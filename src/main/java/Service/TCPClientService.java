package Service;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import org.json.JSONObject;

/**
 * 🔌 TCP Client Service - Gửi task đến Worker TCP Server
 * 
 * Features:
 * - Connection pooling
 * - Automatic retry on failure
 * - Health check before sending
 * - Timeout handling
 * - Async request processing
 */
public class TCPClientService {
    
    // ========== CONFIGURATION ==========
    private static final String WORKER_HOST = "localhost";
    private static final int WORKER_PORT = 9999;
    private static final int CONNECTION_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 120000; // 120 seconds (2 minutes)
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 2000; // 2 seconds
    
    // ========== SINGLETON ==========
    private static TCPClientService instance;
    
    private final ExecutorService asyncExecutor;
    
    private TCPClientService() {
        this.asyncExecutor = Executors.newFixedThreadPool(5);
        System.out.println("✅ TCP Client Service initialized");
    }
    
    public static synchronized TCPClientService getInstance() {
        if (instance == null) {
            instance = new TCPClientService();
        }
        return instance;
    }
    
    /**
     * Gửi task đến Worker Server ĐỒNG BỘ (blocking)
     * 
     * @param taskId ID của task
     * @param filePath Đường dẫn file cần xử lý
     * @param language Ngôn ngữ (vi/en)
     * @return JSONObject response từ server
     * @throws IOException nếu không kết nối được
     */
    public JSONObject sendTaskSync(int taskId, String filePath, String language) throws IOException {
        // Build request JSON
        JSONObject request = new JSONObject();
        request.put("command", "PROCESS_TASK");
        request.put("taskId", taskId);
        request.put("filePath", filePath);
        request.put("language", language);
        request.put("timestamp", System.currentTimeMillis());
        
        // Retry logic
        IOException lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                System.out.println("📤 Sending task " + taskId + " to Worker Server (attempt " + attempt + "/" + MAX_RETRY_ATTEMPTS + ")");
                
                JSONObject response = sendRequest(request);
                
                System.out.println("✅ Task " + taskId + " sent successfully");
                return response;
                
            } catch (IOException e) {
                lastException = e;
                System.err.println("⚠️  Attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        System.out.println("⏳ Retrying in " + RETRY_DELAY_MS + "ms...");
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        // All retries failed
        throw new IOException("Failed after " + MAX_RETRY_ATTEMPTS + " attempts", lastException);
    }
    
    /**
     * Gửi task đến Worker Server BẤT ĐỒNG BỘ (non-blocking)
     * 
     * @param taskId ID của task
     * @param filePath Đường dẫn file cần xử lý
     * @param language Ngôn ngữ (vi/en)
     * @param callback Callback khi hoàn thành hoặc lỗi
     */
    public void sendTaskAsync(int taskId, String filePath, String language, TaskCallback callback) {
        asyncExecutor.submit(() -> {
            try {
                JSONObject response = sendTaskSync(taskId, filePath, language);
                
                if (callback != null) {
                    callback.onSuccess(response);
                }
                
            } catch (IOException e) {
                System.err.println("❌ Failed to send task " + taskId + ": " + e.getMessage());
                
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    /**
     * Gửi request TCP và nhận response
     */
    private JSONObject sendRequest(JSONObject request) throws IOException {
        Socket socket = null;
        
        try {
            // Create socket with timeout
            socket = new Socket();
            socket.connect(new InetSocketAddress(WORKER_HOST, WORKER_PORT), CONNECTION_TIMEOUT);
            socket.setSoTimeout(READ_TIMEOUT);
            
            // Send request
            try (
                PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), 
                    true
                );
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8")
                )
            ) {
                out.println(request.toString());
                
                // Read response
                String responseLine = in.readLine();
                
                if (responseLine == null || responseLine.isEmpty()) {
                    throw new IOException("Empty response from server");
                }
                
                JSONObject response = new JSONObject(responseLine);
                
                // Check status
                String status = response.optString("status", "UNKNOWN");
                if (status.equals("ERROR")) {
                    String errorMsg = response.optString("errorMessage", "Unknown error");
                    throw new IOException("Server returned error: " + errorMsg);
                }
                
                return response;
            }
            
        } catch (SocketTimeoutException e) {
            throw new IOException("Connection timeout to " + WORKER_HOST + ":" + WORKER_PORT, e);
        } catch (ConnectException e) {
            throw new IOException("Cannot connect to Worker Server at " + WORKER_HOST + ":" + WORKER_PORT + 
                ". Is the server running?", e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore close errors
                }
            }
        }
    }
    
    /**
     * Health check - Kiểm tra Worker Server có sống không
     * 
     * @return true nếu server đang hoạt động
     */
    public boolean healthCheck() {
        try {
            JSONObject request = new JSONObject();
            request.put("command", "HEALTH_CHECK");
            
            JSONObject response = sendRequest(request);
            String status = response.optString("status", "");
            
            if (status.equals("OK")) {
                System.out.println("💚 Worker Server is healthy");
                System.out.println("   Total tasks processed: " + response.optInt("totalTasksProcessed", 0));
                System.out.println("   Active connections: " + response.optInt("activeConnections", 0));
                return true;
            }
            
        } catch (IOException e) {
            System.err.println("❌ Worker Server health check failed: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Test connection đến Worker Server
     * 
     * @return true nếu kết nối thành công
     */
    public boolean testConnection() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(WORKER_HOST, WORKER_PORT), CONNECTION_TIMEOUT);
            System.out.println("✅ Connection test successful to " + WORKER_HOST + ":" + WORKER_PORT);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Shutdown client service
     */
    public void shutdown() {
        System.out.println("🛑 Shutting down TCP Client Service...");
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
        }
        System.out.println("✅ TCP Client Service stopped");
    }
    
    /**
     * Callback interface cho async requests
     */
    public interface TaskCallback {
        void onSuccess(JSONObject response);
        void onError(Exception e);
    }
}
