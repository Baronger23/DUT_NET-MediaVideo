# KIẾN TRÚC XỬ LÝ BIG PROCESS VÀ MULTITHREADING

## 📋 TỔNG QUAN DỰ ÁN

Dự án **Speech-to-Text** của bạn sử dụng kiến trúc **Producer-Consumer Pattern** với **Multithreading** để xử lý Big Process (Speech Recognition - 30% điểm).

---

## 🏗️ KIẾN TRÚC TỔNG QUAN

```
┌─────────────────────────────────────────────────────────────────┐
│                         WEB CLIENT                               │
│                    (Browser - HTTP Request)                      │
└────────────────────────┬────────────────────────────────────────┘
                         │ HTTP/HTTPS (Port 8080)
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│                      TOMCAT SERVER                               │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              SERVLET CONTAINER (Main Thread)              │   │
│  │  - MediaController: Nhận file upload                     │   │
│  │  - TaskService: Tạo Task & Enqueue                       │   │
│  └──────────────────────┬───────────────────────────────────┘   │
│                         │                                        │
│                         ↓                                        │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              QUEUE MANAGER (Thread-Safe)                  │   │
│  │         BlockingQueue<Integer> (Capacity: 1000)          │   │
│  │  - Enqueue: Đẩy Task ID vào queue                        │   │
│  │  - Dequeue: Worker lấy Task ID ra (FIFO)                 │   │
│  │  - Thread-safe: LinkedBlockingQueue                      │   │
│  └──────────────┬───────────────────┬───────────────────────┘   │
│                 │                   │                            │
│                 ↓                   ↓                            │
│  ┌──────────────────────┐  ┌──────────────────────┐            │
│  │    WORKER-1 THREAD   │  │    WORKER-2 THREAD   │            │
│  │  (WorkerServiceVosk) │  │  (WorkerServiceVosk) │            │
│  │                      │  │                      │            │
│  │  1. Dequeue Task     │  │  1. Dequeue Task     │            │
│  │  2. Load Vosk Model  │  │  2. Load Vosk Model  │            │
│  │  3. FFmpeg Convert   │  │  3. FFmpeg Convert   │            │
│  │  4. Speech-to-Text   │  │  4. Speech-to-Text   │            │
│  │  5. Update Database  │  │  5. Update Database  │            │
│  └──────────────────────┘  └──────────────────────┘            │
│                 │                   │                            │
│                 └───────────┬───────┘                            │
│                             ↓                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              H2 DATABASE (Embedded)                       │   │
│  │  - Connection Pool: 10 connections                        │   │
│  │  - Tables: User, Task                                     │   │
│  │  - Thread-safe: Synchronized connections                 │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🧵 1. XỬ LÝ MULTITHREADING (ĐÃ SỬ DỤNG)

### 1.1. **Producer Thread (Main Thread)**
**File:** `MediaController.java`, `TaskService.java`

```java
// PRODUCER: Tạo task và đẩy vào Queue
public int taoVaDayTaskVaoQueue(int userId, String fileName, String serverFilePath, String language) {
    // 1. Tạo Task trong Database (PENDING)
    int taskId = taskBO.taoTaskMoi(userId, fileName, serverFilePath, language);
    
    // 2. Đẩy vào Queue (Producer pattern)
    queueManager.enqueue(taskId);  // ✅ Thread-safe
    
    return taskId;
}
```

**Đặc điểm:**
- ✅ **Non-blocking**: User upload xong → Server trả response ngay lập tức
- ✅ **Asynchronous**: Task được xử lý ở background
- ✅ **Scalable**: Có thể nhận hàng ngàn request cùng lúc

---

### 1.2. **Consumer Threads (Worker Threads)**
**File:** `WorkerServiceVosk.java`, `WorkerInitializer.java`

```java
// Khởi động 2 Worker Threads khi server start
@WebListener
public class WorkerInitializer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Worker 1
        worker1 = new WorkerServiceVosk("Worker-1");
        workerThread1 = new Thread(worker1);
        workerThread1.start();
        
        // Worker 2
        worker2 = new WorkerServiceVosk("Worker-2");
        workerThread2 = new Thread(worker2);
        workerThread2.start();
    }
}
```

**Worker loop (Consumer pattern):**
```java
@Override
public void run() {
    while (isRunning) {
        // 1. Lấy Task từ Queue (BLOCKING nếu queue rỗng)
        Integer taskId = queueManager.dequeue();  // ✅ Thread-safe
        
        // 2. Xử lý Big Process (Speech-to-Text)
        xuLyTask(taskId);
    }
}
```

**Đặc điểm:**
- ✅ **Blocking Queue**: Worker đợi khi queue rỗng (không waste CPU)
- ✅ **Parallel Processing**: 2 workers xử lý đồng thời 2 tasks
- ✅ **Thread-safe**: `LinkedBlockingQueue` đảm bảo không race condition

---

### 1.3. **QueueManager - Thread-Safe Queue**
**File:** `QueueManager.java`

```java
public class QueueManager {
    // ✅ Thread-safe BlockingQueue
    private final BlockingQueue<Integer> taskQueue = new LinkedBlockingQueue<>(1000);
    
    // ✅ Atomic counters (thread-safe)
    private final AtomicInteger totalEnqueued = new AtomicInteger(0);
    private final AtomicInteger totalDequeued = new AtomicInteger(0);
    
    // Producer: Thêm task vào queue
    public boolean enqueue(int taskId) {
        taskQueue.put(taskId);  // Blocking nếu queue đầy
        totalEnqueued.incrementAndGet();
        return true;
    }
    
    // Consumer: Lấy task từ queue
    public Integer dequeue() throws InterruptedException {
        Integer taskId = taskQueue.take();  // Blocking nếu queue rỗng
        totalDequeued.incrementAndGet();
        return taskId;
    }
}
```

**Thread-Safety:**
- ✅ `BlockingQueue`: Java cung cấp, thread-safe by default
- ✅ `AtomicInteger`: Đếm số task không cần synchronized
- ✅ `Singleton Pattern`: Double-checked locking

---

## 💾 2. CONNECTION POOL (Thread-Safe Database Access)

**File:** `DBConnect.java`

```java
public class DBConnect {
    // ✅ Connection Pool để nhiều threads dùng chung
    private List<Connection> availableConnections = new ArrayList<>();
    private List<Connection> usedConnections = new ArrayList<>();
    private static final int MAX_POOL_SIZE = 10;
    
    // ✅ Synchronized: Đảm bảo chỉ 1 thread lấy connection 1 lúc
    public synchronized Connection getConnection() throws SQLException {
        if (availableConnections.isEmpty()) {
            // Tạo connection mới nếu chưa đạt max
            Connection newConn = createNewConnection();
            usedConnections.add(newConn);
            return newConn;
        }
        
        // Lấy connection từ pool
        Connection conn = availableConnections.remove(availableConnections.size() - 1);
        usedConnections.add(conn);
        return conn;
    }
    
    // ✅ Trả connection về pool sau khi dùng xong
    public synchronized void releaseConnection(Connection conn) {
        usedConnections.remove(conn);
        availableConnections.add(conn);
    }
}
```

**Tại sao cần Connection Pool?**
- ✅ **Multiple Workers**: 2 workers cần truy cập database đồng thời
- ✅ **Performance**: Tạo connection rất tốn thời gian (~100ms)
- ✅ **Thread-safe**: Synchronized đảm bảo không race condition

---

## 🔥 3. BIG PROCESS (30% ĐIỂM)

### 3.1. **Speech-to-Text Processing**
**File:** `WorkerServiceVosk.java`

```java
private String thucHienSpeechToTextVosk(String filePath, String language) {
    // BƯỚC 1: FFmpeg - Convert video/audio sang WAV
    // - CPU-intensive: Decode codec, resample audio
    // - Thời gian: 5-30 giây (tùy file size)
    String wavPath = chuyenDoiSangWav(filePath);
    
    // BƯỚC 2: Vosk Model - Load model vào RAM
    // - Memory-intensive: Model tiếng Việt ~150MB
    Model model = modelManager.getModel(language);
    
    // BƯỚC 3: Vosk Recognition - Nhận dạng giọng nói
    // - CPU-intensive: Deep learning inference
    // - Thời gian: 10-60 giây (tùy độ dài audio)
    Recognizer recognizer = new Recognizer(model, sampleRate);
    
    while ((bytesRead = ais.read(buffer)) != -1) {
        if (recognizer.acceptWaveForm(buffer, bytesRead)) {
            String partialResult = recognizer.getResult();
            result.append(extractTextFromJson(partialResult));
        }
    }
    
    return result.toString();
}
```

**Đặc điểm Big Process:**
- ✅ **CPU-intensive**: FFmpeg encoding + Vosk inference
- ✅ **Memory-intensive**: Load Vosk model ~150MB per language
- ✅ **Time-consuming**: 15-90 giây per task
- ✅ **Resource-heavy**: Không thể xử lý đồng bộ trên Main Thread

**Tại sao cần Multithreading?**
- ❌ **Nếu xử lý trên Main Thread**: User phải đợi 60 giây → Server bị block
- ✅ **Với Worker Threads**: User nhận response ngay, task xử lý background

---

## 🌐 4. TCP/UDP TRONG DỰ ÁN (ĐÃ DÙNG TCP)

### 4.1. **HTTP/HTTPS (TCP-based)**

Dự án của bạn **ĐÃ SỬ DỤNG TCP** thông qua **HTTP Protocol**:

```
Client (Browser)  →  [TCP Socket]  →  Tomcat Server (Port 8080)
                     3-way handshake
                     Data transfer
                     Connection close
```

**Ví dụ Flow:**
```java
// 1. Client upload file (HTTP POST - TCP)
POST /upload HTTP/1.1
Host: localhost:8080
Content-Type: multipart/form-data
Content-Length: 5242880

[Binary file data...]

// 2. Server response (TCP)
HTTP/1.1 200 OK
Content-Type: application/json

{"taskId": 123, "status": "PENDING"}

// 3. Client polling kết quả (HTTP GET - TCP)
GET /api/task/123?format=json HTTP/1.1

// 4. Server response
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{"id":123, "status":"COMPLETED", "resultText":"chào mừng quý vị..."}
```

**TCP đảm bảo:**
- ✅ **Reliable**: Dữ liệu đến đầy đủ, đúng thứ tự
- ✅ **Error checking**: Checksum, retransmission
- ✅ **Flow control**: Không bị mất packet

---

### 4.2. **H2 Database TCP Mode (Optional)**

H2 Database có thể chạy ở **TCP Server Mode**:

```java
// Embedded mode (hiện tại - không dùng TCP)
jdbc:h2:~/media_processor_db

// TCP Server mode (nếu muốn remote access)
jdbc:h2:tcp://localhost:9092/~/media_processor_db
```

---

### 4.3. **UDP - KHÔNG SỬ DỤNG**

Dự án của bạn **KHÔNG dùng UDP** vì:
- ❌ HTTP sử dụng TCP (không phải UDP)
- ❌ File upload cần reliable (TCP), không dùng UDP
- ❌ Speech-to-Text cần kết quả chính xác (TCP)

**Khi nào dùng UDP?**
- ✅ Live streaming (video/audio realtime)
- ✅ Online gaming (low latency > reliability)
- ✅ DNS queries (fast, one-shot requests)

---

## 🎯 5. TÓM TẮT KIẾN TRÚC

| **Thành phần**          | **Công nghệ**              | **Thread Model**       | **Network Protocol** |
|------------------------|----------------------------|------------------------|---------------------|
| Web Client             | Browser (HTML/JS)          | Single-threaded (JS)   | HTTP/TCP            |
| Tomcat Servlet         | Jakarta EE 10              | Thread-per-request     | HTTP/TCP            |
| QueueManager           | BlockingQueue              | Multi-threaded         | N/A (In-process)    |
| Worker Pool            | Java Thread                | 2 Worker threads       | N/A (In-process)    |
| Connection Pool        | JDBC (H2)                  | Synchronized (10 conn) | Embedded (no TCP)   |
| Vosk Models            | Native C++ library         | Shared memory          | N/A (In-process)    |
| FFmpeg                 | External process           | Subprocess (blocking)  | N/A (CLI)           |

---

## 📊 6. PERFORMANCE METRICS

**Với 2 Worker Threads:**
- ✅ **Throughput**: 2 tasks đồng thời
- ✅ **Queue capacity**: 1000 tasks
- ✅ **Average processing time**: 30-60 giây/task
- ✅ **Max concurrent users**: ~100 users (Tomcat default: 200 threads)

**Nếu muốn scale:**
- 🚀 Tăng số Workers: 4-8 threads (tùy CPU cores)
- 🚀 Dùng **Thread Pool**: `ExecutorService` thay vì tạo Thread thủ công
- 🚀 Distributed Queue: Redis, RabbitMQ
- 🚀 Load Balancer: Multiple Tomcat instances

---

## 🔍 7. SO SÁNH VỚI CÁC MÔ HÌNH KHÁC

### 7.1. **Synchronous (Blocking) - KHÔNG DÙNG**
```java
// BAD: User phải đợi 60 giây
@WebServlet("/upload")
public void doPost(HttpServletRequest req, HttpServletResponse res) {
    File file = uploadFile(req);
    String result = processVosk(file);  // Block 60 giây!
    res.getWriter().write(result);
}
```

### 7.2. **Thread-per-Request - KHÔNG TỐT**
```java
// BAD: Tạo quá nhiều threads
@WebServlet("/upload")
public void doPost(HttpServletRequest req, HttpServletResponse res) {
    File file = uploadFile(req);
    new Thread(() -> processVosk(file)).start();  // Leak threads!
    res.getWriter().write("Processing...");
}
```

### 7.3. **Producer-Consumer với Queue - ĐÃ DÙNG ✅**
```java
// GOOD: Controlled thread pool + Queue
@WebServlet("/upload")
public void doPost(HttpServletRequest req, HttpServletResponse res) {
    File file = uploadFile(req);
    int taskId = taskService.enqueue(file);  // Non-blocking
    res.getWriter().write("{\"taskId\":" + taskId + "}");
}
```

---

## 💡 KẾT LUẬN

**Dự án của bạn ĐÃ SỬ DỤNG:**
1. ✅ **Multithreading**: 2 Worker threads + Main thread pool
2. ✅ **Producer-Consumer Pattern**: Queue-based async processing
3. ✅ **Thread-Safe Queue**: `BlockingQueue` + `AtomicInteger`
4. ✅ **Connection Pool**: Synchronized database access
5. ✅ **TCP Protocol**: HTTP/HTTPS (built-in via Tomcat)
6. ✅ **Big Process**: Speech-to-Text (CPU + Memory intensive)

**KHÔNG SỬ DỤNG:**
- ❌ UDP Protocol (không cần thiết cho use case này)
- ❌ Socket programming trực tiếp (đã dùng HTTP)
- ❌ Distributed messaging (RabbitMQ, Kafka) - chưa cần

**Đây là kiến trúc CHUẨN cho Big Process trong Java Web Application!** 🎉

---

**Ngày tạo:** 19/11/2025  
**Tác giả:** GitHub Copilot
