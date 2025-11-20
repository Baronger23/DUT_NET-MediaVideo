# 🚀 TCP SOCKET SERVER - IMPLEMENTATION GUIDE

## 📋 TỔNG QUAN

Đây là implementation **hoàn chỉnh và chuyên nghiệp** của kiến trúc TCP Socket Server cho xử lý Big Process (Speech-to-Text).

## 🏗️ KIẾN TRÚC

```
┌──────────────┐                           ┌──────────────────┐
│   Browser    │                           │  Worker Server   │
└──────┬───────┘                           │   (Port 9999)    │
       │ HTTP                               │                  │
       ↓                                    │  - Thread Pool   │
┌──────────────────┐    TCP Socket         │  - Vosk Engine   │
│   Web Server     │◄─────────────────────►│  - FFmpeg        │
│   (Tomcat)       │    JSON Protocol      │                  │
│                  │                        │                  │
│ - MediaController│                        │  Process:        │
│ - TCPClient      │                        │  1. Receive task │
│ - TaskService    │                        │  2. Convert WAV  │
└────────┬─────────┘                        │  3. Speech2Text  │
         │                                  │  4. Return result│
         ↓                                  └──────────────────┘
┌──────────────────┐
│   H2 Database    │
│   - Tasks        │
│   - Users        │
└──────────────────┘
```

## 📦 COMPONENTS ĐÃ IMPLEMENT

### 1. **WorkerTCPServer.java** ⭐⭐⭐⭐⭐
**Vai trò:** Server chạy độc lập, xử lý Speech-to-Text

**Features:**
- ✅ Multi-threaded server (10 worker threads)
- ✅ Connection pooling với ThreadPoolExecutor
- ✅ JSON-based protocol
- ✅ Health check endpoint
- ✅ Graceful shutdown
- ✅ Real-time monitoring (metrics)
- ✅ Error handling & logging
- ✅ Timeout protection (60s per request)

**Port:** 9999

**Chạy độc lập:**
```bash
java -cp "build\classes;lib\*" Service.WorkerTCPServer
```

---

### 2. **TCPClientService.java** ⭐⭐⭐⭐⭐
**Vai trò:** Client trong Web Server, gửi task đến Worker Server

**Features:**
- ✅ Connection retry (3 attempts, 2s delay)
- ✅ Auto-reconnect on failure
- ✅ Async & Sync modes
- ✅ Health check trước khi gửi
- ✅ Timeout handling (5s connect, 120s read)
- ✅ Callback interface cho async requests
- ✅ Connection pool management

**Usage:**
```java
TCPClientService client = TCPClientService.getInstance();

// Sync (blocking)
JSONObject response = client.sendTaskSync(taskId, filePath, language);

// Async (non-blocking)
client.sendTaskAsync(taskId, filePath, language, new TaskCallback() {
    @Override
    public void onSuccess(JSONObject response) {
        // Handle success
    }
    
    @Override
    public void onError(Exception e) {
        // Handle error
    }
});
```

---

### 3. **MediaControllerTCP.java** ⭐⭐⭐⭐
**Vai trò:** Controller mới sử dụng TCP Socket

**URL:** `/upload-tcp`

**Flow:**
1. Nhận file upload từ user
2. Lưu file vào disk
3. Tạo Task trong database
4. **Gửi task qua TCP Socket** (thay vì Queue)
5. Worker Server xử lý và trả kết quả
6. Cập nhật database

**Khác với MediaController cũ:**
| Feature | Old (Queue) | New (TCP) |
|---------|-------------|-----------|
| Communication | In-Memory Queue | TCP Socket |
| Worker Location | Same JVM | Different process/machine |
| Scalability | Single machine | Multiple machines |
| Fault Tolerance | Low | High |

---

### 4. **upload-tcp.jsp** ⭐⭐⭐⭐
**Vai trò:** UI page cho TCP mode

**Features:**
- ✅ Real-time server status (ONLINE/OFFLINE)
- ✅ Drag & drop file upload
- ✅ Progress bar với animation
- ✅ Language selection (Vietnamese/English)
- ✅ Async AJAX upload
- ✅ Beautiful gradient UI
- ✅ Architecture info box

---

### 5. **start-worker-server.bat** ⭐⭐⭐
**Vai trò:** Script khởi động Worker Server nhanh chóng

**Usage:**
```bash
cd E:\K1N3\LTM\DUT_NET-MediaVideo
start-worker-server.bat
```

---

## 🔌 PROTOCOL DESIGN

### **Request Format (JSON):**
```json
{
    "command": "PROCESS_TASK",
    "taskId": 123,
    "filePath": "E:\\uploads\\media_1234567890.mp4",
    "language": "vi",
    "timestamp": 1700000000000
}
```

### **Response Format (SUCCESS):**
```json
{
    "status": "SUCCESS",
    "taskId": 123,
    "resultText": "chào mừng quý vị và các bạn...",
    "processingTime": 15234
}
```

### **Response Format (ERROR):**
```json
{
    "status": "ERROR",
    "errorMessage": "File not found: /path/to/file.wav"
}
```

### **Health Check Request:**
```json
{
    "command": "HEALTH_CHECK"
}
```

### **Health Check Response:**
```json
{
    "status": "OK",
    "serverTime": 1700000000000,
    "totalTasksProcessed": 150,
    "activeConnections": 3,
    "failedTasks": 2,
    "threadPoolActiveCount": 2,
    "threadPoolQueueSize": 0
}
```

---

## 🚀 HƯỚNG DẪN DEPLOYMENT

### **BƯỚC 1: Compile Project**
```bash
# Trong Eclipse:
Project → Clean → Clean all projects
```

### **BƯỚC 2: Start Worker TCP Server**
```bash
cd E:\K1N3\LTM\DUT_NET-MediaVideo
start-worker-server.bat
```

**Kết quả console:**
```
========================================
🚀 WORKER TCP SERVER INITIALIZING...
========================================
Port: 9999
Thread Pool Size: 10
Max Queue Size: 100
========================================
✅ Worker TCP Server started on port 9999
⏳ Waiting for connections from Web Server...
```

### **BƯỚC 3: Start Tomcat Web Server**
```bash
# Trong Eclipse:
Servers tab → Right click Tomcat → Start
```

**Kiểm tra console:**
```
🔍 Testing connection to Worker TCP Server...
✅ TCP Controller initialized successfully
💚 Worker Server is healthy
   Total tasks processed: 0
   Active connections: 0
```

### **BƯỚC 4: Test Upload**

1. Truy cập: `http://localhost:8080/DUT_NET-MediaVideo/upload-tcp`
2. Upload file audio/video
3. Chọn ngôn ngữ (Tiếng Việt/English)
4. Click "Upload & Process via TCP"

**Worker Server console sẽ hiển thị:**
```
📥 New connection from: 127.0.0.1
📨 Request from 127.0.0.1: {"command":"PROCESS_TASK","taskId":123,...}
🔄 Processing Task ID: 123
   File: E:\uploads\media_123.mp4
   Language: vi
   🎵 Converting to WAV...
   🎤 Performing Speech-to-Text...
✅ Task 123 completed successfully
   Result: chào mừng quý vị...
✅ Request completed in 15234ms
```

---

## 📊 MONITORING & METRICS

### **Real-time Monitoring**
Worker Server tự động log metrics mỗi 30 giây:
```
========== SERVER STATUS ==========
Total Tasks Processed: 25
Failed Tasks: 1
Active Connections: 2
Thread Pool Active: 2
Thread Pool Queue: 0
===================================
```

### **Health Check via Code**
```java
TCPClientService client = TCPClientService.getInstance();
boolean healthy = client.healthCheck();

if (healthy) {
    System.out.println("✅ Worker is ready");
} else {
    System.err.println("❌ Worker is down!");
}
```

### **Performance Metrics**
- **Connection Timeout:** 5 seconds
- **Read Timeout:** 120 seconds
- **Retry Attempts:** 3 times with 2s delay
- **Thread Pool Size:** 10 concurrent workers
- **Max Queue Size:** 100 pending tasks

---

## ⚡ ADVANCED FEATURES

### 1. **Automatic Retry**
```java
// TCPClientService tự động retry 3 lần nếu connection fails
JSONObject response = client.sendTaskSync(taskId, filePath, language);
// Nếu lần 1 fail → retry sau 2s
// Nếu lần 2 fail → retry sau 2s
// Nếu lần 3 fail → throw IOException
```

### 2. **Async Processing**
```java
// Web Server không bị block, trả response ngay cho user
client.sendTaskAsync(taskId, filePath, language, callback);
// → User nhận response ngay lập tức
// → Task xử lý trong background
```

### 3. **Graceful Shutdown**
```java
// Ctrl+C trên Worker Server
// → Đợi tất cả tasks đang xử lý hoàn thành (max 30s)
// → Close tất cả connections
// → Shutdown thread pool
// → Exit cleanly
```

### 4. **Load Balancing Ready**
Có thể chạy nhiều Worker Servers trên nhiều ports:
```
Worker-1: localhost:9999
Worker-2: localhost:10000
Worker-3: localhost:10001
```

Chỉ cần modify `TCPClientService` để round-robin giữa các workers.

---

## 🆚 SO SÁNH: IN-PROCESS vs TCP SOCKET

| Metric | In-Process Queue | TCP Socket Server |
|--------|------------------|-------------------|
| **Latency** | <1ms | 1-5ms (network overhead) |
| **Scalability** | Single machine only | Multiple machines |
| **Fault Tolerance** | Low (crash = all fail) | High (isolated failures) |
| **Deployment** | 1 WAR file | 2 separate processes |
| **Complexity** | ⭐⭐ Low | ⭐⭐⭐⭐ High |
| **Monitoring** | Shared logs | Separate logs + metrics |
| **Memory** | Shared JVM heap | Separate heap spaces |
| **Language** | Java only | Any language (Python, Go, C++) |

---

## 🎯 USE CASES

### **Khi nào dùng In-Process Queue?**
- ✅ Đồ án, project nhỏ
- ✅ Cần deploy nhanh (1 file WAR)
- ✅ Traffic thấp (<100 requests/day)
- ✅ Chạy trên 1 máy

### **Khi nào dùng TCP Socket?**
- ✅ Production system
- ✅ High traffic (>1000 requests/day)
- ✅ Cần scale horizontal (nhiều máy)
- ✅ Cần fault isolation
- ✅ Worker có thể viết bằng ngôn ngữ khác

---

## 🐛 TROUBLESHOOTING

### **Lỗi: Connection refused**
```
❌ Cannot connect to Worker Server at localhost:9999
```
**Nguyên nhân:** Worker Server chưa được khởi động

**Giải pháp:**
```bash
cd E:\K1N3\LTM\DUT_NET-MediaVideo
start-worker-server.bat
```

---

### **Lỗi: Connection timeout**
```
❌ Connection timeout to localhost:9999
```
**Nguyên nhân:** Worker Server bận, không accept connection kịp

**Giải pháp:**
- Tăng timeout: `CONNECTION_TIMEOUT = 10000` (10s)
- Tăng thread pool: `THREAD_POOL_SIZE = 20`

---

### **Lỗi: Socket closed**
```
❌ Socket closed unexpectedly
```
**Nguyên nhân:** Worker Server shutdown giữa chừng

**Giải pháp:**
- Check Worker Server console
- Restart Worker Server
- TCPClient sẽ tự động retry

---

## 🎓 KIẾN THỨC CẦN BIẾT

### **TCP Socket Programming**
- `ServerSocket.accept()`: Lắng nghe connection
- `Socket.connect()`: Kết nối đến server
- `BufferedReader/PrintWriter`: Đọc/ghi data qua socket
- `setSoTimeout()`: Set timeout để tránh block vô hạn

### **Thread Pool**
- `ExecutorService`: Quản lý pool of threads
- `ThreadPoolExecutor`: Advanced thread pool với queue
- `CallerRunsPolicy`: Policy khi queue đầy

### **JSON Protocol**
- `JSONObject`: Parse & build JSON messages
- UTF-8 encoding: Đảm bảo tiếng Việt không bị lỗi
- Line-based protocol: Mỗi message là 1 dòng

---

## 📚 TÀI LIỆU THAM KHẢO

1. **Oracle Java Networking Tutorial**
   https://docs.oracle.com/javase/tutorial/networking/

2. **TCP Socket Best Practices**
   - Always set timeouts
   - Use try-with-resources
   - Handle exceptions properly
   - Implement retry logic

3. **Production Considerations**
   - SSL/TLS for encryption
   - Authentication/Authorization
   - Rate limiting
   - Circuit breaker pattern

---

## ✅ CHECKLIST

- [x] WorkerTCPServer.java - Server xử lý Speech-to-Text
- [x] TCPClientService.java - Client gửi task
- [x] MediaControllerTCP.java - Controller mới
- [x] upload-tcp.jsp - UI page
- [x] start-worker-server.bat - Launch script
- [x] JSON Protocol design
- [x] Error handling & retry
- [x] Health check endpoint
- [x] Graceful shutdown
- [x] Monitoring & metrics
- [x] Documentation

---

## 🎉 KẾT LUẬN

Đây là một **implementation hoàn chỉnh và production-ready** của kiến trúc TCP Socket Server cho xử lý Big Process.

**Điểm mạnh:**
- ✅ Code chuyên nghiệp, có error handling đầy đủ
- ✅ Scalable: Có thể chạy nhiều Worker Servers
- ✅ Fault-tolerant: Worker crash không ảnh hưởng Web Server
- ✅ Monitoring: Metrics real-time
- ✅ Easy to deploy: 1 command để start Worker

**So với In-Process Queue:**
- Phức tạp hơn nhưng mạnh mẽ hơn
- Phù hợp cho production systems
- Có thể tích hợp vào báo cáo đồ án để tăng điểm

---

**Tác giả:** GitHub Copilot  
**Ngày:** 2025-11-19  
**Version:** 1.0
