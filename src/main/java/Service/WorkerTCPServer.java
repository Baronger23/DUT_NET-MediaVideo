package Service;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONObject;

import Model.BO.TaskBO;

/**
 * 🚀 TCP Socket Server - Xử lý Speech-to-Text từ xa
 * 
 * Kiến trúc:
 * - Lắng nghe trên port 9999
 * - Nhận task request từ Web Server qua TCP
 * - Xử lý Speech-to-Text bằng Vosk
 * - Trả kết quả về Web Server
 * 
 * Chạy độc lập: java -cp ... Service.WorkerTCPServer
 */
public class WorkerTCPServer {
    
    // ========== CONFIGURATION ==========
    private static final int PORT = 9999;
    private static final int THREAD_POOL_SIZE = 10; // 10 workers đồng thời
    private static final int MAX_QUEUE_SIZE = 100;
    private static final int CONNECTION_TIMEOUT = 60000; // 60 seconds
    
    // ========== COMPONENTS ==========
    private final ExecutorService threadPool;
    private final TaskBO taskBO;
    private final VoskModelManager modelManager;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = true;
    
    // ========== MONITORING ==========
    private final AtomicInteger totalTasksProcessed = new AtomicInteger(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger failedTasks = new AtomicInteger(0);
    
    public WorkerTCPServer() {
        this.threadPool = new ThreadPoolExecutor(
            THREAD_POOL_SIZE, 
            THREAD_POOL_SIZE,
            60L, 
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(MAX_QUEUE_SIZE),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        this.taskBO = new TaskBO();
        this.modelManager = VoskModelManager.getInstance();
        
        System.out.println("========================================");
        System.out.println("🚀 WORKER TCP SERVER INITIALIZING...");
        System.out.println("========================================");
        System.out.println("Port: " + PORT);
        System.out.println("Thread Pool Size: " + THREAD_POOL_SIZE);
        System.out.println("Max Queue Size: " + MAX_QUEUE_SIZE);
        System.out.println("========================================");
        
        // ✅ QUAN TRỌNG: Load Vosk models khi khởi động
        System.out.println("📦 Loading Vosk models...");
        this.modelManager.initializeModels();
        System.out.println("✅ " + this.modelManager.getModelsStatus());
        System.out.println("========================================");
    }
    
    /**
     * Khởi động TCP Server
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        serverSocket.setSoTimeout(5000); // Check shutdown flag every 5s
        
        System.out.println("✅ Worker TCP Server started on port " + PORT);
        System.out.println("⏳ Waiting for connections from Web Server...");
        System.out.println();
        
        // Start monitoring thread
        startMonitoringThread();
        
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(CONNECTION_TIMEOUT);
                
                activeConnections.incrementAndGet();
                
                String clientAddr = clientSocket.getInetAddress().getHostAddress();
                System.out.println("📥 New connection from: " + clientAddr);
                
                // Submit to thread pool
                threadPool.submit(() -> handleClient(clientSocket));
                
            } catch (SocketTimeoutException e) {
                // Normal timeout, check isRunning flag
                continue;
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("❌ Error accepting connection: " + e.getMessage());
                }
            }
        }
        
        shutdown();
    }
    
    /**
     * Xử lý request từ Web Server
     */
    private void handleClient(Socket socket) {
        String clientAddr = socket.getInetAddress().getHostAddress();
        long startTime = System.currentTimeMillis();
        
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8")
            );
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), 
                true
            )
        ) {
            // 1. Đọc request JSON
            String jsonRequest = in.readLine();
            
            if (jsonRequest == null || jsonRequest.isEmpty()) {
                sendErrorResponse(out, "Empty request");
                return;
            }
            
            System.out.println("📨 Request from " + clientAddr + ": " + 
                jsonRequest.substring(0, Math.min(100, jsonRequest.length())));
            
            JSONObject request = new JSONObject(jsonRequest);
            
            // 2. Parse request
            String command = request.optString("command", "PROCESS_TASK");
            
            if (command.equals("HEALTH_CHECK")) {
                handleHealthCheck(out);
                return;
            }
            
            if (command.equals("PROCESS_TASK")) {
                handleProcessTask(request, out);
            } else {
                sendErrorResponse(out, "Unknown command: " + command);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("✅ Request completed in " + duration + "ms");
            
        } catch (Exception e) {
            System.err.println("❌ Error handling client " + clientAddr + ": " + e.getMessage());
            e.printStackTrace();
            failedTasks.incrementAndGet();
        } finally {
            activeConnections.decrementAndGet();
            
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore close errors
            }
        }
    }
    
    /**
     * Xử lý task Speech-to-Text
     */
    private void handleProcessTask(JSONObject request, PrintWriter out) {
        int taskId = -1;
        
        try {
            // Parse request
            taskId = request.getInt("taskId");
            String filePath = request.getString("filePath");
            String language = request.optString("language", "vi");
            
            System.out.println("🔄 Processing Task ID: " + taskId);
            System.out.println("   File: " + filePath);
            System.out.println("   Language: " + language);
            
            // ✅ Bắt đầu đo thời gian xử lý
            long startTime = System.currentTimeMillis();
            
            // Update task status to PROCESSING
            taskBO.datTaskDangXuLy(taskId);
            
            // Verify file exists
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IOException("File not found: " + filePath);
            }
            
            // Convert to WAV if needed
            String wavPath = filePath;
            if (!filePath.toLowerCase().endsWith(".wav")) {
                System.out.println("   🎵 Converting to WAV...");
                wavPath = convertToWav(filePath);
            }
            
            // Speech-to-Text with Vosk
            System.out.println("   🎤 Performing Speech-to-Text...");
            String resultText = performSpeechToText(wavPath, language);
            
            // ✅ Tính thời gian xử lý (milliseconds)
            long endTime = System.currentTimeMillis();
            int processingTimeMs = (int) (endTime - startTime);
            
            // Update database với thời gian xử lý chính xác
            taskBO.hoanThanhTask(taskId, resultText, processingTimeMs);
            
            // Send success response
            JSONObject response = new JSONObject();
            response.put("status", "SUCCESS");
            response.put("taskId", taskId);
            response.put("resultText", resultText);
            response.put("processingTimeMs", processingTimeMs);
            
            out.println(response.toString());
            
            totalTasksProcessed.incrementAndGet();
            System.out.println("✅ Task " + taskId + " completed successfully");
            System.out.println("   Result: " + resultText.substring(0, Math.min(100, resultText.length())) + "...");
            System.out.println("   Processing time: " + processingTimeMs + "ms");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to process Task " + taskId + ": " + e.getMessage());
            e.printStackTrace();
            
            // Update task as failed
            if (taskId > 0) {
                taskBO.datTaskThatBai(taskId, "Error: " + e.getMessage());
            }
            
            // Send error response
            sendErrorResponse(out, e.getMessage());
            failedTasks.incrementAndGet();
        }
    }
    
    /**
     * Thực hiện Speech-to-Text với Vosk
     */
    private String performSpeechToText(String wavPath, String language) throws Exception {
        org.vosk.Model model = modelManager.getModel(language);
        
        try (FileInputStream fis = new FileInputStream(wavPath)) {
            org.vosk.Recognizer recognizer = new org.vosk.Recognizer(model, 16000);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            StringBuilder fullText = new StringBuilder();
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    String result = recognizer.getResult();
                    
                    // ✅ FIX: Sửa lỗi encoding từ Vosk
                    result = fixVoskEncoding(result);
                    
                    JSONObject json = new JSONObject(result);
                    String text = json.optString("text", "");
                    
                    if (!text.isEmpty()) {
                        fullText.append(text).append(" ");
                    }
                }
            }
            
            // Get final result
            String finalResult = recognizer.getFinalResult();
            
            // ✅ FIX: Sửa lỗi encoding từ Vosk
            finalResult = fixVoskEncoding(finalResult);
            
            JSONObject json = new JSONObject(finalResult);
            String text = json.optString("text", "");
            
            if (!text.isEmpty()) {
                fullText.append(text);
            }
            
            recognizer.close();
            
            return fullText.toString().trim();
        }
    }
    
    /**
     * ⚠️ FIX LỖI ENCODING TỪ VOSK NATIVE LIBRARY
     * 
     * VẤN ĐỀ: Vosk native library (C++) trả về UTF-8 bytes,
     * nhưng JNI bridge tự động convert sang String với encoding ISO-8859-1 (default),
     * gây ra lỗi hiển thị tiếng Việt: "chÃ o má»«ng" thay vì "chào mừng"
     * 
     * GIẢI PHÁP: Convert lại từ ISO-8859-1 bytes sang UTF-8 String
     */
    private String fixVoskEncoding(String voskResult) {
        if (voskResult == null || voskResult.isEmpty()) {
            return voskResult;
        }
        
        try {
            // Vosk trả về UTF-8 bytes nhưng Java nhận nhầm là ISO-8859-1
            // Ta cần lấy lại bytes với encoding ISO-8859-1, rồi decode lại với UTF-8
            byte[] utf8Bytes = voskResult.getBytes("ISO-8859-1");
            String fixedResult = new String(utf8Bytes, "UTF-8");
            
            return fixedResult;
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi fix encoding, trả về string gốc: " + e.getMessage());
            return voskResult;
        }
    }
    
    /**
     * Convert media file to WAV format using FFmpeg
     */
    private String convertToWav(String inputPath) throws IOException, InterruptedException {
        File inputFile = new File(inputPath);
        String outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + ".wav";
        
        // Build FFmpeg command
        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-i", inputPath,
            "-ar", "16000",
            "-ac", "1",
            "-y",
            outputPath
        );
        
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        // Wait for conversion
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new IOException("FFmpeg conversion failed with exit code: " + exitCode);
        }
        
        return outputPath;
    }
    
    /**
     * Health check endpoint
     */
    private void handleHealthCheck(PrintWriter out) {
        JSONObject response = new JSONObject();
        response.put("status", "OK");
        response.put("serverTime", System.currentTimeMillis());
        response.put("totalTasksProcessed", totalTasksProcessed.get());
        response.put("activeConnections", activeConnections.get());
        response.put("failedTasks", failedTasks.get());
        response.put("threadPoolActiveCount", ((ThreadPoolExecutor) threadPool).getActiveCount());
        response.put("threadPoolQueueSize", ((ThreadPoolExecutor) threadPool).getQueue().size());
        
        out.println(response.toString());
        System.out.println("💓 Health check responded");
    }
    
    /**
     * Gửi error response
     */
    private void sendErrorResponse(PrintWriter out, String errorMessage) {
        JSONObject response = new JSONObject();
        response.put("status", "ERROR");
        response.put("errorMessage", errorMessage);
        out.println(response.toString());
    }
    
    /**
     * Thread giám sát server
     */
    private void startMonitoringThread() {
        Thread monitorThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(30000); // Every 30 seconds
                    
                    System.out.println();
                    System.out.println("========== SERVER STATUS ==========");
                    System.out.println("Total Tasks Processed: " + totalTasksProcessed.get());
                    System.out.println("Failed Tasks: " + failedTasks.get());
                    System.out.println("Active Connections: " + activeConnections.get());
                    System.out.println("Thread Pool Active: " + ((ThreadPoolExecutor) threadPool).getActiveCount());
                    System.out.println("Thread Pool Queue: " + ((ThreadPoolExecutor) threadPool).getQueue().size());
                    System.out.println("===================================");
                    System.out.println();
                    
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        
        monitorThread.setDaemon(true);
        monitorThread.setName("MonitorThread");
        monitorThread.start();
    }
    
    /**
     * Graceful shutdown
     */
    public void shutdown() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("🛑 SHUTTING DOWN WORKER TCP SERVER...");
        System.out.println("========================================");
        
        isRunning = false;
        
        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("✅ Server socket closed");
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
        
        // Shutdown thread pool
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
                System.out.println("⚠️  Force shutdown thread pool");
            } else {
                System.out.println("✅ Thread pool shutdown gracefully");
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
        
        System.out.println("========================================");
        System.out.println("✅ SERVER STOPPED");
        System.out.println("========================================");
    }
    
    /**
     * Main method - Chạy server standalone
     */
    public static void main(String[] args) {
        WorkerTCPServer server = new WorkerTCPServer();
        
        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n⚠️  Shutdown signal received...");
            server.shutdown();
        }));
        
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("❌ Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
