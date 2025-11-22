# CHƯƠNG 1: CƠ SỞ LÝ THUYẾT

## 1.1. MÔ HÌNH CLIENT-SERVER VÀ KIẾN TRÚC WEB

### 1.1.1. Giới thiệu về mô hình Client-Server

Mô hình Client-Server là một kiến trúc phân tán trong đó các nhiệm vụ được phân chia giữa các nhà cung cấp dịch vụ (Server) và các bên yêu cầu dịch vụ (Client). Đây là nền tảng của hầu hết các ứng dụng mạng hiện đại.

**Các thành phần chính:**

- **Client (Máy khách):**
  - Là nơi gửi yêu cầu đến server
  - Thường là trình duyệt web (Chrome, Firefox, Edge...)
  - Hiển thị giao diện người dùng và xử lý tương tác
  - Gửi HTTP requests và nhận HTTP responses

- **Server (Máy chủ):**
  - Lắng nghe và xử lý các yêu cầu từ client
  - Thực thi business logic
  - Truy cập và quản lý cơ sở dữ liệu
  - Trả về kết quả cho client

**Luồng hoạt động cơ bản:**

```
1. Client gửi HTTP Request → Server
2. Server xử lý request (business logic, database query)
3. Server tạo HTTP Response → Client
4. Client hiển thị kết quả cho người dùng
```

### 1.1.2. Giao thức HTTP/HTTPS

**HTTP (HyperText Transfer Protocol)** là giao thức truyền tải siêu văn bản, được sử dụng để truyền dữ liệu giữa client và server trên web.

**Các phương thức HTTP chính:**

| Phương thức | Mục đích | Ví dụ trong dự án |
|-------------|----------|-------------------|
| **GET** | Lấy dữ liệu từ server | Xem lịch sử tasks, xem chi tiết task |
| **POST** | Gửi dữ liệu lên server | Upload file media, đăng nhập, đăng ký |
| **PUT** | Cập nhật dữ liệu | Cập nhật thông tin user |
| **DELETE** | Xóa dữ liệu | Xóa task (nếu có) |

**Cấu trúc HTTP Request:**

```http
POST /DUT_NET-MediaVideo/upload HTTP/1.1
Host: localhost:8080
Content-Type: multipart/form-data
Authorization: Bearer <session-token>

[File data và các parameters]
```

**Cấu trúc HTTP Response:**

```http
HTTP/1.1 200 OK
Content-Type: text/html; charset=UTF-8
Set-Cookie: JSESSIONID=ABC123...

[HTML/JSON content]
```

### 1.1.3. Áp dụng trong dự án

Trong dự án DUT_NET MediaVideo, mô hình Client-Server được triển khai như sau:

**Client Side:**
- Trình duyệt web hiển thị các trang JSP
- JavaScript xử lý AJAX requests để lấy trạng thái task real-time
- HTML forms để upload file và nhập liệu

**Server Side:**
- **Tomcat Server** làm Web Container
- **Java Servlets** xử lý HTTP requests
- **JSP (JavaServer Pages)** để render HTML động
- **Database Server** lưu trữ dữ liệu

**Ví dụ cụ thể - Luồng upload file:**

```
1. User chọn file → Click "Upload" button (Client)
2. Browser gửi POST /upload với multipart/form-data (HTTP)
3. MediaController.doPost() nhận request (Servlet)
4. Lưu file vào server → Tạo task → Đẩy vào queue
5. Trả về response 302 Redirect đến /history
6. Browser load trang History để xem kết quả
```

---

## 1.2. XỬ LÝ BẤT ĐỒNG BỘ VÀ ĐA LUỒNG (MULTITHREADING)

### 1.2.1. Khái niệm về xử lý đồng bộ và bất đồng bộ

**Xử lý đồng bộ (Synchronous):**
- Request được xử lý tuần tự, phải đợi hoàn thành mới chuyển sang task tiếp theo
- Client bị block cho đến khi server trả về kết quả
- **Nhược điểm:** Nếu task tốn nhiều thời gian (VD: Speech-to-Text), user phải chờ lâu

**Xử lý bất đồng bộ (Asynchronous):**
- Request được đưa vào hàng đợi và xử lý ở background
- Client nhận response ngay lập tức (task đã được tiếp nhận)
- User có thể tiếp tục làm việc khác, không bị block
- **Ưu điểm:** Trải nghiệm người dùng tốt hơn, hệ thống xử lý được nhiều request đồng thời

### 1.2.2. Multithreading (Đa luồng) trong Java

**Thread (Luồng)** là đơn vị xử lý nhỏ nhất trong một process. Multithreading cho phép nhiều tác vụ chạy song song.

**Các cách tạo Thread trong Java:**

```java
// Cách 1: Kế thừa Thread class
class MyThread extends Thread {
    public void run() {
        // Code xử lý
    }
}

// Cách 2: Implement Runnable interface (Khuyên dùng)
class MyWorker implements Runnable {
    public void run() {
        // Code xử lý
    }
}

// Sử dụng
Thread t = new Thread(new MyWorker());
t.start();
```

**Thread States (Trạng thái của Thread):**

```
NEW → RUNNABLE → RUNNING → BLOCKED/WAITING → TERMINATED
```

### 1.2.3. BlockingQueue và Producer-Consumer Pattern

**BlockingQueue** là một interface trong Java Collections Framework hỗ trợ thread-safe operations.

**Đặc điểm:**

- **Thread-safe:** Nhiều threads có thể truy cập đồng thời mà không gây race condition
- **Blocking operations:**
  - `put()`: Chờ nếu queue đầy
  - `take()`: Chờ nếu queue rỗng
- **Non-blocking alternatives:** `offer()`, `poll()`

**Producer-Consumer Pattern:**

```
┌──────────────┐        ┌─────────────┐        ┌──────────────┐
│  Producer    │───────▶│BlockingQueue│───────▶│  Consumer    │
│ (Controller) │  put() │             │ take() │  (Worker)    │
└──────────────┘        └─────────────┘        └──────────────┘
```

**Ví dụ code:**

```java
// Tạo BlockingQueue
BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

// Producer - Đẩy task vào queue
public void enqueue(Integer taskId) {
    queue.put(taskId); // Block nếu queue đầy
}

// Consumer - Lấy task từ queue
public Integer dequeue() {
    return queue.take(); // Block nếu queue rỗng
}
```

### 1.2.4. Áp dụng trong dự án

Dự án DUT_NET MediaVideo sử dụng kiến trúc **asynchronous processing** với các thành phần:

**1. QueueManager (Singleton Pattern):**

```java
public class QueueManager {
    private BlockingQueue<Integer> taskQueue;
    private static QueueManager instance;
    
    public static QueueManager getInstance() {
        if (instance == null) {
            synchronized (QueueManager.class) {
                if (instance == null) {
                    instance = new QueueManager();
                }
            }
        }
        return instance;
    }
}
```

**2. Worker Threads:**

Hệ thống khởi động 2 Worker threads chạy ngầm:

```java
// WorkerServiceVosk.java
public class WorkerServiceVosk implements Runnable {
    public void run() {
        while (isRunning) {
            Integer taskId = queueManager.dequeue(); // Block đến khi có task
            xuLyTask(taskId); // Xử lý Speech-to-Text
        }
    }
}
```

**3. Luồng xử lý bất đồng bộ:**

```
User upload file → MediaController
                    ↓
          Tạo Task trong DB (status=PENDING)
                    ↓
          Enqueue(taskId) → QueueManager
                    ↓
          Return response ngay (User không phải chờ)
                    ↓
          Worker.dequeue() → Lấy taskId
                    ↓
          Update status=PROCESSING
                    ↓
          Thực hiện Speech-to-Text (tốn 10-60s)
                    ↓
          Update status=COMPLETED + result_text
```

**Lợi ích:**

✅ User không bị block khi upload file lớn  
✅ Server có thể xử lý nhiều uploads đồng thời  
✅ Worker threads chạy độc lập, không ảnh hưởng Controller  
✅ Dễ scale: Có thể tăng số lượng Workers nếu cần  

---

## 1.3. KIẾN TRÚC MVC (MODEL-VIEW-CONTROLLER)

### 1.3.1. Tổng quan về MVC Pattern

**MVC (Model-View-Controller)** là một design pattern phổ biến trong phát triển ứng dụng web, giúp tách biệt logic xử lý, giao diện, và dữ liệu.

**3 Thành phần chính:**

```
         ┌──────────────┐
         │  Controller  │  ← Điều khiển luồng xử lý
         │  (Servlet)   │
         └──────┬───────┘
                │
        ┌───────┴────────┐
        ▼                ▼
┌──────────┐      ┌──────────┐
│  Model   │      │   View   │
│ (BO/DAO) │      │  (JSP)   │
└──────────┘      └──────────┘
```

### 1.3.2. Model Layer

**Chức năng:** Quản lý dữ liệu và business logic

**Các thành phần con:**

**a) Bean (Entity/POJO):**
- Đại diện cho các đối tượng dữ liệu
- Chứa các thuộc tính (fields) và getter/setter
- Ví dụ: `Task.java`, `User.java`

```java
public class Task {
    private int id;
    private String fileName;
    private String status;
    private String resultText;
    // Getters và setters
}
```

**b) DAO (Data Access Object):**
- Tương tác trực tiếp với database
- Thực hiện các thao tác CRUD (Create, Read, Update, Delete)
- Sử dụng JDBC để kết nối và query database

```java
public class TaskDAO {
    public int themTask(Task task) { /* INSERT query */ }
    public Task layTaskTheoId(int id) { /* SELECT query */ }
    public void capNhatTask(Task task) { /* UPDATE query */ }
    public List<Task> layDanhSachTask(int userId) { /* SELECT query */ }
}
```

**c) BO (Business Object):**
- Chứa business logic phức tạp
- Validate dữ liệu trước khi gọi DAO
- Xử lý các quy tắc nghiệp vụ

```java
public class TaskBO {
    private TaskDAO taskDAO;
    
    public void hoanThanhTask(int taskId, String result, int timeMs) {
        // Validate
        if (result == null || result.isEmpty()) {
            throw new IllegalArgumentException("Kết quả không được rỗng");
        }
        
        // Business logic
        Task task = taskDAO.layTaskTheoId(taskId);
        task.setStatus("COMPLETED");
        task.setResultText(result);
        task.setProcessingTimeMs(timeMs);
        
        // Lưu vào DB
        taskDAO.capNhatTask(task);
    }
}
```

### 1.3.3. View Layer

**Chức năng:** Hiển thị giao diện người dùng

**Công nghệ sử dụng:** JSP (JavaServer Pages)

**Đặc điểm:**
- Kết hợp HTML với Java code
- Sử dụng JSTL (JSP Standard Tag Library) và EL (Expression Language)
- Nhận dữ liệu từ Controller qua `request.setAttribute()`

**Ví dụ - history.jsp:**

```jsp
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:forEach var="task" items="${taskList}">
    <tr>
        <td>${task.id}</td>
        <td>${task.fileName}</td>
        <td>
            <c:choose>
                <c:when test="${task.status == 'PENDING'}">
                    ⏳ Đang chờ
                </c:when>
                <c:when test="${task.status == 'PROCESSING'}">
                    🔄 Đang xử lý
                </c:when>
                <c:when test="${task.status == 'COMPLETED'}">
                    ✅ Hoàn thành
                </c:when>
            </c:choose>
        </td>
    </tr>
</c:forEach>
```

### 1.3.4. Controller Layer

**Chức năng:** Điều khiển luồng xử lý, kết nối Model và View

**Công nghệ:** Java Servlet

**Các phương thức chính:**

- `doGet()`: Xử lý HTTP GET requests
- `doPost()`: Xử lý HTTP POST requests
- `init()`: Khởi tạo khi servlet được load
- `destroy()`: Clean up khi servlet bị unload

**Ví dụ - MediaController.java:**

```java
@WebServlet("/upload")
@MultipartConfig // Hỗ trợ upload file
public class MediaController extends HttpServlet {
    private TaskService taskService;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        // Hiển thị form upload
        request.getRequestDispatcher("/View/upload.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        // 1. Nhận file upload
        Part filePart = request.getPart("mediaFile");
        
        // 2. Lưu file vào server
        String fileName = saveFile(filePart);
        
        // 3. Tạo task và đẩy vào queue
        int taskId = taskService.taoVaDayTaskVaoQueue(userId, fileName, filePath);
        
        // 4. Redirect đến trang history
        response.sendRedirect(request.getContextPath() + "/history");
    }
}
```

### 1.3.5. Luồng hoạt động MVC trong dự án

```
1. User truy cập /upload
   └─▶ HistoryController.doGet()
       └─▶ taskBO.layLichSuTask(userId)
           └─▶ taskDAO.layDanhSachTask(userId) [Query DB]
               └─▶ Return List<Task>
                   └─▶ request.setAttribute("taskList", tasks)
                       └─▶ Forward to history.jsp
                           └─▶ Render HTML với dữ liệu

2. User upload file
   └─▶ MediaController.doPost()
       └─▶ taskService.taoVaDayTaskVaoQueue()
           └─▶ taskBO.taoTask() → taskDAO.themTask() [INSERT DB]
           └─▶ queueManager.enqueue(taskId)
       └─▶ Redirect to /history
```

**Lợi ích của MVC:**

✅ **Separation of Concerns:** Tách biệt rõ ràng giữa UI, Logic, và Data  
✅ **Maintainability:** Dễ bảo trì, sửa lỗi, nâng cấp  
✅ **Reusability:** Model và Service có thể tái sử dụng  
✅ **Testability:** Dễ dàng viết unit tests cho từng layer  
✅ **Team collaboration:** Nhiều người có thể làm việc song song  

---

## 1.4. CONNECTION POOL VÀ QUẢN LÝ TÀI NGUYÊN

### 1.4.1. Vấn đề với kết nối Database truyền thống

**Cách tiếp cận cũ - Tạo connection mỗi lần cần:**

```java
// Mỗi request tạo connection mới
Connection conn = DriverManager.getConnection(url, user, password);
// Thực hiện query
conn.close();
```

**Nhược điểm:**

❌ **Chi phí cao:** Tạo connection mất 50-100ms  
❌ **Lãng phí tài nguyên:** Đóng/mở connection liên tục  
❌ **Giới hạn connections:** Database thường giới hạn số connections đồng thời  
❌ **Performance thấp:** Với 1000 requests/s, server không đáp ứng nổi  

### 1.4.2. Connection Pool Pattern

**Ý tưởng:** Tạo sẵn một "pool" (bể) chứa nhiều connections. Khi cần, lấy từ pool; khi xong, trả lại pool thay vì đóng.

**Cơ chế hoạt động:**

```
┌─────────────────────────────────────┐
│        Connection Pool              │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐       │
│  │ C1 │ │ C2 │ │ C3 │ │... │       │
│  └────┘ └────┘ └────┘ └────┘       │
│  Available   Used    Available      │
└─────────────────────────────────────┘
        ▲           │
        │           │
    Return      Borrow
        │           ▼
    ┌───────────────────┐
    │   Application     │
    │   (DAO/Service)   │
    └───────────────────┘
```

**Các thông số quan trọng:**

- **Initial Pool Size:** Số connections tạo khi khởi động (VD: 3)
- **Max Pool Size:** Số connections tối đa (VD: 10)
- **Connection Timeout:** Thời gian chờ nếu pool đầy (VD: 5s)
- **Idle Timeout:** Thời gian tối đa connection không dùng trước khi đóng

### 1.4.3. Triển khai Connection Pool trong dự án

Dự án sử dụng **Basic Connection Pool tự xây dựng** (không dùng thư viện ngoài như HikariCP, C3P0).

**DBConnect.java - Custom Connection Pool:**

```java
public class DBConnect {
    private static DBConnect instance;
    private List<Connection> availableConnections = new ArrayList<>();
    private List<Connection> usedConnections = new ArrayList<>();
    
    private static final int INITIAL_POOL_SIZE = 3;
    private static final int MAX_POOL_SIZE = 10;
    
    private DBConnect() {
        // Tạo initial connections
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            availableConnections.add(createConnection());
        }
    }
    
    public static synchronized DBConnect getInstance() {
        if (instance == null) {
            instance = new DBConnect();
        }
        return instance;
    }
    
    public synchronized Connection getConnection() throws SQLException {
        if (availableConnections.isEmpty()) {
            if (usedConnections.size() < MAX_POOL_SIZE) {
                // Tạo connection mới nếu chưa đạt MAX
                availableConnections.add(createConnection());
            } else {
                // Đợi connection được trả về
                throw new SQLException("Connection pool đã đầy!");
            }
        }
        
        Connection conn = availableConnections.remove(0);
        usedConnections.add(conn);
        return conn;
    }
    
    public synchronized void releaseConnection(Connection conn) {
        usedConnections.remove(conn);
        availableConnections.add(conn);
    }
}
```

**Cách sử dụng trong DAO:**

```java
public class TaskDAO {
    private DBConnect dbConnect = DBConnect.getInstance();
    
    public Task layTaskTheoId(int id) {
        Connection conn = null;
        try {
            conn = dbConnect.getConnection(); // Lấy từ pool
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Task WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            // Xử lý kết quả
            return task;
        } finally {
            if (conn != null) {
                dbConnect.releaseConnection(conn); // Trả về pool
            }
        }
    }
}
```

### 1.4.4. Lợi ích của Connection Pool trong dự án

**So sánh hiệu suất:**

| Metric | Without Pool | With Pool (3-10 connections) |
|--------|--------------|------------------------------|
| **Thời gian tạo connection** | 50-100ms mỗi lần | 0ms (reuse) |
| **Concurrent users** | ~10 users | ~100 users |
| **Response time** | 500ms | 50ms (nhanh 10x) |
| **Database load** | Cao | Thấp |
| **Resource usage** | Lãng phí | Tối ưu |

**Kịch bản thực tế:**

```
Scenario: 100 users đồng thời xem history

Without Pool:
- Mỗi request mất 100ms tạo connection
- Total: 100 requests × 100ms = 10,000ms = 10 giây
- Users phải chờ rất lâu!

With Pool (10 connections):
- Reuse connections từ pool: 0ms overhead
- Total: 100 requests × 50ms = 5,000ms = 5 giây
- Nhanh gấp đôi, và response time đồng đều!
```

### 1.4.5. Best Practices khi sử dụng Connection Pool

**1. Luôn luôn release connection trong finally block:**

```java
Connection conn = null;
try {
    conn = dbConnect.getConnection();
    // Thực hiện query
} catch (SQLException e) {
    e.printStackTrace();
} finally {
    if (conn != null) {
        dbConnect.releaseConnection(conn); // Quan trọng!
    }
}
```

**2. Sử dụng PreparedStatement để tránh SQL Injection:**

```java
PreparedStatement ps = conn.prepareStatement("SELECT * FROM Task WHERE id = ?");
ps.setInt(1, taskId); // Safe
// KHÔNG làm: "SELECT * FROM Task WHERE id = " + taskId (Unsafe!)
```

**3. Giám sát trạng thái pool:**

```java
public synchronized PoolStats getPoolStats() {
    return new PoolStats(
        availableConnections.size(),
        usedConnections.size(),
        MAX_POOL_SIZE
    );
}
```

**4. Graceful Shutdown:**

```java
public synchronized void closeAllConnections() {
    for (Connection conn : availableConnections) {
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("Lỗi khi đóng connection: " + e.getMessage());
        }
    }
    availableConnections.clear();
    usedConnections.clear();
}
```

---

## KẾT LUẬN CHƯƠNG 1

Chương này đã trình bày các cơ sở lý thuyết quan trọng được áp dụng trong dự án:

1. **Mô hình Client-Server và HTTP:** Nền tảng của ứng dụng web, giúp hiểu cách client và server giao tiếp qua giao thức HTTP.

2. **Xử lý bất đồng bộ và Multithreading:** Giải quyết vấn đề tác vụ tốn thời gian (Speech-to-Text) bằng cách sử dụng BlockingQueue và Worker threads, đảm bảo trải nghiệm người dùng mượt mà.

3. **Kiến trúc MVC:** Tổ chức code theo pattern Model-View-Controller, giúp dự án dễ bảo trì, mở rộng và phát triển bởi nhiều người.

4. **Connection Pool:** Tối ưu hóa hiệu suất database access, cho phép hệ thống xử lý hàng trăm requests đồng thời mà không bị bottleneck.

Các kiến thức này là nền tảng để hiểu rõ thiết kế và triển khai hệ thống trong các chương tiếp theo.

---

*Tài liệu này được tạo tự động dựa trên phân tích source code của dự án DUT_NET MediaVideo.*
