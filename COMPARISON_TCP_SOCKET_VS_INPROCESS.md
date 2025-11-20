# SO SÁNH 2 KIẾN TRÚC XỬ LÝ BIG PROCESS

## 🎯 TỔNG QUAN

Có **2 cách chính** để xử lý Big Process trong Java Web Application:

1. **Kiến trúc hiện tại của bạn**: Embedded Workers với BlockingQueue (In-Process)
2. **Kiến trúc thầy giáo nói**: TCP Socket Server riêng biệt (Out-of-Process)

---

## 📊 SO SÁNH 2 KIẾN TRÚC

| **Tiêu chí**              | **Kiến trúc hiện tại (In-Process)** | **TCP Socket Server (Out-of-Process)** |
|---------------------------|-------------------------------------|----------------------------------------|
| **Độ phức tạp**           | ⭐⭐ Đơn giản                       | ⭐⭐⭐⭐ Phức tạp                      |
| **Performance**           | ⭐⭐⭐⭐ Rất nhanh (no network)     | ⭐⭐⭐ Chậm hơn (network latency)      |
| **Scalability**           | ⭐⭐ Limited (single JVM)          | ⭐⭐⭐⭐⭐ Unlimited (distributed)      |
| **Resource Isolation**    | ⭐⭐ Shared JVM memory             | ⭐⭐⭐⭐⭐ Isolated processes          |
| **Fault Tolerance**       | ⭐⭐ Worker crash → App crash      | ⭐⭐⭐⭐ Server crash → App OK         |
| **Deployment**            | ⭐⭐⭐⭐ 1 process duy nhất          | ⭐⭐ Cần deploy 2 services riêng      |
| **Monitoring**            | ⭐⭐⭐ Logs trong 1 file            | ⭐⭐⭐⭐ Distributed tracing           |
| **Load Balancing**        | ⭐ Không có                        | ⭐⭐⭐⭐⭐ Dễ dàng scale horizontal    |

---

## 🏗️ KIẾN TRÚC 1: HIỆN TẠI (IN-PROCESS WORKERS)

### **Sơ đồ:**
```
┌─────────────────────────────────────────────────────────────┐
│               TOMCAT SERVER (Single JVM Process)             │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  HTTP Thread Pool (Main Threads)                       │  │
│  │  - MediaController: Nhận request                       │  │
│  │  - TaskService: Enqueue task                           │  │
│  └──────────────────┬─────────────────────────────────────┘  │
│                     │ In-Memory Queue                         │
│                     ↓                                         │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  BlockingQueue<Integer> (Thread-Safe)                  │  │
│  └──────────────┬─────────────────┬─────────────────────┘  │
│                 │                 │                           │
│                 ↓                 ↓                           │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │  Worker-1 Thread │  │  Worker-2 Thread │                 │
│  │  (Same JVM)      │  │  (Same JVM)      │                 │
│  │  - Vosk Process  │  │  - Vosk Process  │                 │
│  │  - FFmpeg        │  │  - FFmpeg        │                 │
│  └──────────────────┘  └──────────────────┘                 │
│                 │                 │                           │
│                 └────────┬────────┘                           │
│                          ↓                                    │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  H2 Database (Embedded)                                │  │
│  └────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### **Ưu điểm:**
✅ **Đơn giản**: Không cần cấu hình TCP socket, protocol  
✅ **Nhanh**: Không có network latency (in-memory queue)  
✅ **Dễ debug**: Tất cả logs trong 1 process  
✅ **Dễ deploy**: Chỉ 1 WAR file duy nhất  
✅ **Transaction**: Dễ dàng quản lý ACID với database  

### **Nhược điểm:**
❌ **Limited scalability**: Chỉ chạy trên 1 server (không thể scale ra nhiều máy)  
❌ **Memory limit**: Workers và Web app cùng dùng chung heap memory  
❌ **Single point of failure**: Tomcat crash → tất cả workers cũng crash  
❌ **Resource contention**: Workers và HTTP threads tranh CPU/RAM  

### **Code hiện tại:**
```java
// QueueManager.java - In-Memory Queue
public class QueueManager {
    private final BlockingQueue<Integer> taskQueue = new LinkedBlockingQueue<>(1000);
    
    public boolean enqueue(int taskId) {
        taskQueue.put(taskId);  // ✅ In-memory, very fast
        return true;
    }
    
    public Integer dequeue() {
        return taskQueue.take();  // ✅ No network overhead
    }
}
```

---

## 🏗️ KIẾN TRÚC 2: TCP SOCKET SERVER (OUT-OF-PROCESS)

### **Sơ đồ:**
```
┌──────────────────────────────────┐
│   TOMCAT WEB SERVER (JVM 1)      │
│  ┌────────────────────────────┐  │
│  │  HTTP Thread Pool          │  │
│  │  - MediaController         │  │
│  │  - TaskService             │  │
│  └──────────────┬─────────────┘  │
│                 │ TCP Socket        
│                 │ (Port 9999)       
└─────────────────┼─────────────────┘
                  │ Network
                  ↓
┌──────────────────────────────────────────────────────────┐
│       WORKER SERVER (JVM 2 - Separate Process)           │
│  ┌────────────────────────────────────────────────────┐  │
│  │  TCP Server Socket (Listening Port 9999)           │  │
│  │  - Accept connections from Web Server              │  │
│  │  - Receive Task ID via TCP                         │  │
│  └──────────────────┬─────────────────────────────────┘  │
│                     │                                     │
│                     ↓                                     │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Task Processor (Thread Pool)                      │  │
│  │  - ExecutorService (10 threads)                    │  │
│  └──────┬────────────────┬────────────────────────────┘  │
│         │                │                                │
│         ↓                ↓                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │
│  │  Worker-1   │  │  Worker-2   │  │  Worker-N   │      │
│  │  Thread     │  │  Thread     │  │  Thread     │      │
│  │  - Vosk     │  │  - Vosk     │  │  - Vosk     │      │
│  │  - FFmpeg   │  │  - FFmpeg   │  │  - FFmpeg   │      │
│  └─────────────┘  └─────────────┘  └─────────────┘      │
│         │                │                │               │
│         └────────────────┴────────────────┘               │
│                          ↓                                │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Send Result back via TCP Socket                   │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
                          │ TCP Response
                          ↓
┌──────────────────────────────────┐
│   TOMCAT WEB SERVER              │
│  - Receive result                │
│  - Update Database               │
└──────────────────────────────────┘
```

### **Ưu điểm:**
✅ **Scalability**: Có thể chạy nhiều Worker Servers trên nhiều máy khác nhau  
✅ **Resource isolation**: Web Server và Worker Server không tranh tài nguyên  
✅ **Fault tolerance**: Worker Server crash không ảnh hưởng Web Server  
✅ **Load balancing**: Dễ dàng phân tải (Round-robin, Least-connection)  
✅ **Independent scaling**: Scale Web và Worker riêng biệt  
✅ **Multiple languages**: Worker có thể viết bằng Python, C++, Go...  

### **Nhược điểm:**
❌ **Phức tạp**: Cần implement TCP protocol, serialization, error handling  
❌ **Network latency**: Mỗi task phải qua network (thêm 1-5ms)  
❌ **Network failures**: TCP connection có thể bị ngắt, timeout  
❌ **Deployment phức tạp**: Phải deploy và quản lý 2 services riêng  
❌ **Debugging khó**: Logs phân tán ở nhiều nơi  

---

## 💻 CODE DEMO: TCP SOCKET SERVER

### **1. Worker Server (Separate Java Process)**

```java
package WorkerServer;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * ✅ WORKER SERVER - Lắng nghe TCP connections từ Web Server
 * Xử lý Big Process và trả kết quả qua TCP Socket
 */
public class WorkerTCPServer {
    private static final int PORT = 9999;
    private static final int THREAD_POOL_SIZE = 10;
    
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean isRunning = true;
    
    public static void main(String[] args) {
        WorkerTCPServer server = new WorkerTCPServer();
        server.start();
    }
    
    public void start() {
        try {
            // 1. Tạo Server Socket lắng nghe port 9999
            serverSocket = new ServerSocket(PORT);
            System.out.println("✅ Worker Server đang lắng nghe trên port " + PORT);
            
            // 2. Tạo Thread Pool để xử lý nhiều tasks đồng thời
            threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            
            // 3. Accept connections từ Web Server
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("✓ Nhận connection từ: " + clientSocket.getInetAddress());
                
                // 4. Submit task vào thread pool
                threadPool.submit(new WorkerTask(clientSocket));
            }
            
        } catch (IOException e) {
            System.err.println("❌ Lỗi Worker Server: " + e.getMessage());
        }
    }
    
    /**
     * Worker Task - Xử lý 1 task từ Web Server
     */
    class WorkerTask implements Runnable {
        private Socket socket;
        
        public WorkerTask(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) {
                // 1. Đọc request từ Web Server
                String request = in.readLine();
                System.out.println("[Worker] Nhận request: " + request);
                
                // Parse request (format: "TASK_ID:123|FILE_PATH:/uploads/audio.mp3|LANGUAGE:vi")
                String[] parts = request.split("\\|");
                int taskId = Integer.parseInt(parts[0].split(":")[1]);
                String filePath = parts[1].split(":")[1];
                String language = parts[2].split(":")[1];
                
                // 2. Xử lý Big Process (Speech-to-Text)
                String result = processSpeechToText(taskId, filePath, language);
                
                // 3. Trả kết quả về Web Server
                String response = "SUCCESS|" + taskId + "|" + result;
                out.println(response);
                
                System.out.println("[Worker] ✓ Đã xử lý Task " + taskId + " thành công");
                
            } catch (Exception e) {
                System.err.println("[Worker] ❌ Lỗi xử lý task: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try { socket.close(); } catch (IOException e) {}
            }
        }
        
        /**
         * Xử lý Speech-to-Text (Big Process)
         */
        private String processSpeechToText(int taskId, String filePath, String language) {
            try {
                System.out.println("[Worker] Bắt đầu xử lý Task " + taskId);
                
                // TODO: Thực hiện FFmpeg + Vosk như code hiện tại
                // String wavPath = convertToWav(filePath);
                // String result = voskRecognize(wavPath, language);
                
                // DEMO: Simulate processing
                Thread.sleep(5000); // Giả lập xử lý 5 giây
                
                return "chào mừng quý vị và các bạn đến với buổi thảo luận...";
                
            } catch (Exception e) {
                return "ERROR: " + e.getMessage();
            }
        }
    }
}
```

---

### **2. Web Server Client (Gửi task đến Worker Server)**

```java
package Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.*;

/**
 * ✅ WEB SERVER - Gửi task đến Worker Server qua TCP Socket
 */
@WebServlet("/upload-tcp")
public class MediaControllerTCP extends HttpServlet {
    private static final String WORKER_HOST = "localhost";
    private static final int WORKER_PORT = 9999;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Upload file và lưu vào server
        String filePath = handleFileUpload(request);
        int taskId = createTaskInDatabase(filePath);
        
        // 2. Gửi task đến Worker Server qua TCP
        String result = sendToWorkerServer(taskId, filePath, "vi");
        
        // 3. Cập nhật kết quả vào database
        updateTaskResult(taskId, result);
        
        // 4. Trả response cho user
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("{\"taskId\":" + taskId + ",\"status\":\"COMPLETED\"}");
    }
    
    /**
     * Gửi task đến Worker Server qua TCP Socket
     */
    private String sendToWorkerServer(int taskId, String filePath, String language) {
        try (
            Socket socket = new Socket(WORKER_HOST, WORKER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            System.out.println("[Web] Kết nối đến Worker Server: " + WORKER_HOST + ":" + WORKER_PORT);
            
            // 1. Gửi request đến Worker
            String request = "TASK_ID:" + taskId + "|FILE_PATH:" + filePath + "|LANGUAGE:" + language;
            out.println(request);
            System.out.println("[Web] Đã gửi request: " + request);
            
            // 2. Đợi nhận kết quả từ Worker (BLOCKING!)
            String response = in.readLine();
            System.out.println("[Web] Nhận response: " + response);
            
            // 3. Parse response (format: "SUCCESS|123|result text...")
            String[] parts = response.split("\\|", 3);
            if ("SUCCESS".equals(parts[0])) {
                return parts[2]; // Return result text
            } else {
                return "ERROR: Processing failed";
            }
            
        } catch (IOException e) {
            System.err.println("[Web] ❌ Lỗi kết nối Worker Server: " + e.getMessage());
            return "ERROR: Cannot connect to Worker Server";
        }
    }
    
    // Helper methods
    private String handleFileUpload(HttpServletRequest request) {
        // TODO: Implement file upload logic
        return "/uploads/audio.mp3";
    }
    
    private int createTaskInDatabase(String filePath) {
        // TODO: Insert into database
        return 123;
    }
    
    private void updateTaskResult(int taskId, String result) {
        // TODO: Update database
    }
}
```

---

## ⚡ CÁCH CẢI TIẾN: ASYNCHRONOUS TCP (NON-BLOCKING)

Vấn đề của code trên là **BLOCKING**: Web Server phải đợi Worker xử lý xong (60 giây!). Giải pháp:

### **Asynchronous Communication Pattern:**

```java
/**
 * ✅ Non-blocking approach: Web Server không đợi Worker
 */
@WebServlet("/upload-async-tcp")
public class MediaControllerAsyncTCP extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Upload file và tạo Task (status = PENDING)
        String filePath = handleFileUpload(request);
        int taskId = createTaskInDatabase(filePath, "PENDING");
        
        // 2. Gửi task đến Worker Server (Async - không đợi kết quả)
        sendToWorkerServerAsync(taskId, filePath, "vi");
        
        // 3. Trả response ngay lập tức (không đợi xử lý xong)
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("{\"taskId\":" + taskId + ",\"status\":\"PENDING\"}");
    }
    
    /**
     * Gửi task đến Worker Server (Non-blocking)
     */
    private void sendToWorkerServerAsync(int taskId, String filePath, String language) {
        // Chạy trong thread riêng để không block Main Thread
        new Thread(() -> {
            try (
                Socket socket = new Socket(WORKER_HOST, WORKER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) {
                String request = "TASK_ID:" + taskId + "|FILE_PATH:" + filePath + "|LANGUAGE:" + language;
                out.println(request);
                System.out.println("[Web] Đã gửi task " + taskId + " đến Worker Server");
                
                // Không đợi response, Worker sẽ tự update database sau
                
            } catch (IOException e) {
                System.err.println("[Web] Lỗi gửi task: " + e.getMessage());
            }
        }).start();
    }
}
```

---

## 📊 SO SÁNH CHI TIẾT 2 CÁCH

### **1. Performance:**

| **Metric**               | **In-Process (Hiện tại)** | **TCP Socket Server** |
|--------------------------|---------------------------|----------------------|
| Latency (enqueue)        | < 1ms                     | 1-5ms (network)      |
| Throughput               | 2 tasks/concurrent        | 10+ tasks/concurrent |
| Memory overhead          | Low (shared JVM)          | Medium (2 JVMs)      |
| CPU overhead             | Low                       | Medium (serialization)|

### **2. Scalability:**

| **Aspect**               | **In-Process**            | **TCP Socket**       |
|--------------------------|---------------------------|----------------------|
| Max workers              | 2-8 (limited by JVM heap) | Unlimited (distributed) |
| Horizontal scaling       | ❌ Không thể              | ✅ Dễ dàng           |
| Load balancing           | ❌ Không có               | ✅ Round-robin, etc. |
| Cloud deployment         | ⚠️ Khó (monolithic)      | ✅ Dễ (microservices)|

### **3. Reliability:**

| **Aspect**               | **In-Process**            | **TCP Socket**       |
|--------------------------|---------------------------|----------------------|
| Fault isolation          | ❌ Worker crash → App crash | ✅ Isolated         |
| Recovery                 | ⚠️ Restart toàn bộ app   | ✅ Restart worker only|
| Health check             | ⚠️ Khó                   | ✅ TCP heartbeat     |

---

## 🎯 KẾT LUẬN & KHUYẾN NGHỊ

### **Khi nào dùng IN-PROCESS (Kiến trúc hiện tại)?**
✅ **Phù hợp với:**
- Dự án nhỏ/vừa (< 1000 users)
- Đội nhỏ (1-3 developers)
- Budget hạn chế (1 server)
- Yêu cầu đơn giản, ổn định
- **ĐỒ ÁN TỐT NGHIỆP** ✅

### **Khi nào dùng TCP SOCKET SERVER?**
✅ **Phù hợp với:**
- Dự án lớn (> 10,000 users)
- Cần scale horizontal
- Budget đủ (nhiều servers)
- Đội lớn (5+ developers)
- Yêu cầu high availability
- **Production systems**

---

## 💡 KHUYẾN NGHỊ CHO ĐỒ ÁN CỦA BẠN

**GIỮ NGUYÊN KIẾN TRÚC HIỆN TẠI** vì:

1. ✅ **Đơn giản, dễ hiểu**: Phù hợp với đồ án tốt nghiệp
2. ✅ **Đủ tính năng**: Đáp ứng đủ yêu cầu Big Process (30%)
3. ✅ **Dễ demo**: Chỉ cần start 1 Tomcat server
4. ✅ **Dễ chấm điểm**: Giảng viên dễ hiểu và đánh giá
5. ✅ **Performance tốt**: Latency thấp, no network overhead

**NẾU muốn nâng cao (tăng điểm):**
- Có thể implement **cả 2 cách** và so sánh performance
- Viết thêm chapter trong báo cáo về "Alternative Architecture"
- Demo cả 2 kiến trúc và đo latency, throughput

---

## 📚 TÀI LIỆU THAM KHẢO

**Design Patterns:**
- Producer-Consumer Pattern (hiện tại)
- Client-Server Pattern (TCP Socket)
- Master-Worker Pattern
- Message Queue Pattern (RabbitMQ, Kafka)

**Java Technologies:**
- `java.util.concurrent.BlockingQueue`
- `java.net.Socket` và `ServerSocket`
- `java.util.concurrent.ExecutorService`

**Real-world Examples:**
- **In-Process**: Spring Boot @Async, Quartz Scheduler
- **TCP Socket**: Redis, Memcached, Elasticsearch
- **Message Queue**: RabbitMQ, Apache Kafka, AWS SQS

---

**Ngày tạo:** 19/11/2025  
**So sánh giữa:** In-Process Workers vs TCP Socket Server  
**Khuyến nghị:** Giữ kiến trúc hiện tại cho đồ án (đơn giản, hiệu quả)
