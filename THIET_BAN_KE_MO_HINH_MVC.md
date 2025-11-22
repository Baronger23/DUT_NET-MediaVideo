# THIẾT BẢN KẾ MÔ HÌNH MVC

## 1. TỔNG QUAN KIẾN TRÚC MVC

### 1.1. Sơ đồ kiến trúc tổng thể

```
┌────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                                │
│                    (Trình duyệt Web Browser)                        │
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │   HTML/CSS   │  │  JavaScript  │  │   AJAX       │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
└─────────────────────────────┬──────────────────────────────────────┘
                              │ HTTP Request/Response
                              │ (POST/GET)
                              ▼
┌────────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                             │
│                         (VIEW - JSP)                                │
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │  login.jsp   │  │  upload.jsp  │  │ history.jsp  │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
│  ┌──────────────┐  ┌──────────────┐                               │
│  │ register.jsp │  │   home.jsp   │                               │
│  └──────────────┘  └──────────────┘                               │
└─────────────────────────────┬──────────────────────────────────────┘
                              │ forward/include
                              │ request.setAttribute()
                              ▼
┌────────────────────────────────────────────────────────────────────┐
│                      CONTROLLER LAYER                               │
│                       (Java Servlet)                                │
│                                                                      │
│  ┌───────────────────┐  ┌───────────────────┐  ┌────────────────┐│
│  │ LoginServlet      │  │ RegisterServlet   │  │ HomeServlet    ││
│  │ (/login)          │  │ (/register)       │  │ (/home)        ││
│  └───────────────────┘  └───────────────────┘  └────────────────┘│
│  ┌───────────────────┐  ┌───────────────────┐  ┌────────────────┐│
│  │ MediaController   │  │ HistoryController │  │TaskDetail      ││
│  │ (/upload)         │  │ (/history)        │  │Controller      ││
│  └───────────────────┘  └───────────────────┘  │(/api/task/{id})││
│                                                  └────────────────┘│
└─────────────────────────────┬──────────────────────────────────────┘
                              │ Gọi Service/BO
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ SERVICE LAYER    │  │ SERVICE LAYER    │  │ SERVICE LAYER    │
│                  │  │                  │  │                  │
│  TaskService     │  │  QueueManager    │  │VoskModelManager  │
│                  │  │  (Singleton)     │  │  (Singleton)     │
└────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘
         │                     │                     │
         ▼                     ▼                     ▼
┌────────────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                             │
│                      (MODEL - BO)                                   │
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐│
│  │   TaskBO     │  │   UserBO     │  │  WorkerServiceVosk       ││
│  │              │  │              │  │  (Background Thread)     ││
│  └──────┬───────┘  └──────┬───────┘  └──────────────────────────┘│
└─────────┼──────────────────┼─────────────────────────────────────┘
          │                  │
          ▼                  ▼
┌────────────────────────────────────────────────────────────────────┐
│                    DATA ACCESS LAYER                                │
│                      (MODEL - DAO)                                  │
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐│
│  │   TaskDAO    │  │   UserDAO    │  │   DBConnect              ││
│  │              │  │              │  │   (Connection Pool)      ││
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────────┘│
└─────────┼──────────────────┼────────────────────┼─────────────────┘
          │                  │                    │
          └──────────────────┴────────────────────┘
                             │
                             ▼
┌────────────────────────────────────────────────────────────────────┐
│                       DATABASE LAYER                                │
│                   MySQL / H2 Database                               │
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │  Task Table  │  │  User Table  │  │ Config Table │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
└────────────────────────────────────────────────────────────────────┘
```

---

## 2. CHI TIẾT CÁC LAYER

### 2.1. View Layer (JSP Pages)

**Chức năng:** Hiển thị giao diện người dùng

**Các file chính:**

```
/src/main/webapp/View/
├── login.jsp          → Trang đăng nhập
├── register.jsp       → Trang đăng ký
├── home.jsp           → Trang chủ (dashboard)
├── upload.jsp         → Form upload file media
├── history.jsp        → Lịch sử tasks
└── logout.jsp         → Xử lý đăng xuất
```

**Mối quan hệ:**
```
login.jsp ──────────▶ LoginServlet ──────────▶ home.jsp
                          │
                          ▼
                       UserBO
                          │
                          ▼
                       UserDAO
                          │
                          ▼
                       Database
```

**Công nghệ sử dụng:**
- JSP (JavaServer Pages)
- JSTL (JSP Standard Tag Library)
- EL (Expression Language)
- HTML5, CSS3, JavaScript

---

### 2.2. Controller Layer (Servlets)

**Chức năng:** Điều khiển luồng xử lý, xử lý request/response

**Sơ đồ chi tiết MediaController (Servlet quan trọng nhất):**

```
┌────────────────────────────────────────────────────────────────┐
│                     MediaController                             │
│                     (@WebServlet("/upload"))                    │
├────────────────────────────────────────────────────────────────┤
│                                                                  │
│  doGet(request, response)                                       │
│  │                                                               │
│  ├─▶ 1. Kiểm tra session (user đã login?)                      │
│  │                                                               │
│  ├─▶ 2. Lấy thống kê queue                                     │
│  │      queueSize = taskService.getSoLuongTaskTrongQueue()     │
│  │                                                               │
│  ├─▶ 3. Set attribute                                           │
│  │      request.setAttribute("queueSize", queueSize)           │
│  │                                                               │
│  └─▶ 4. Forward đến upload.jsp                                 │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  doPost(request, response)                                      │
│  │                                                               │
│  ├─▶ 1. Kiểm tra session                                       │
│  │                                                               │
│  ├─▶ 2. Nhận file upload (multipart/form-data)                │
│  │      Part filePart = request.getPart("mediaFile")          │
│  │      String fileName = filePart.getSubmittedFileName()     │
│  │                                                               │
│  ├─▶ 3. Nhận ngôn ngữ                                          │
│  │      String language = request.getParameter("language")    │
│  │                                                               │
│  ├─▶ 4. Lưu file vào server                                    │
│  │      String uniqueName = timestamp + "_" + fileName        │
│  │      filePart.write(uploadPath + "/" + uniqueName)         │
│  │                                                               │
│  ├─▶ 5. Tạo task và đẩy vào queue                             │
│  │      int taskId = taskService.taoVaDayTaskVaoQueue(        │
│  │          userId, fileName, filePath, language)             │
│  │                                                               │
│  └─▶ 6. Redirect đến /history                                  │
│         response.sendRedirect("/history?success=true")         │
│                                                                  │
└────────────────────────────────────────────────────────────────┘
          │                           │
          ▼                           ▼
    TaskService                  QueueManager
```

**Các Servlet khác:**

| Servlet | URL Pattern | Chức năng |
|---------|-------------|-----------|
| LoginServlet | /login | Xác thực đăng nhập |
| RegisterServlet | /register | Đăng ký tài khoản mới |
| HomeServlet | /home | Hiển thị dashboard |
| HistoryController | /history | Hiển thị lịch sử tasks |
| TaskDetailController | /api/task/{id} | API chi tiết task |
| DownloadController | /download/{taskId} | Download kết quả |
| UserController | /user/profile | Quản lý thông tin user |

---

### 2.3. Service Layer

**Chức năng:** Tổ chức business logic phức tạp, orchestration

**Sơ đồ chi tiết TaskService:**

```
┌────────────────────────────────────────────────────────────────┐
│                        TaskService                              │
├────────────────────────────────────────────────────────────────┤
│                                                                  │
│  - taskBO: TaskBO                                               │
│  - queueManager: QueueManager                                   │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  taoVaDayTaskVaoQueue(userId, fileName, filePath, language)    │
│  │                                                               │
│  ├─▶ 1. Gọi TaskBO để tạo task trong DB                       │
│  │      taskId = taskBO.taoTaskMoi(...)                        │
│  │                                                               │
│  ├─▶ 2. Kiểm tra taskId hợp lệ                                 │
│  │      if (taskId > 0)                                         │
│  │                                                               │
│  ├─▶ 3. Đẩy taskId vào queue                                   │
│  │      success = queueManager.enqueue(taskId)                 │
│  │                                                               │
│  └─▶ 4. Return taskId                                           │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  layLichSuTask(userId): List<Task>                             │
│  │                                                               │
│  └─▶ return taskBO.layLichSuTaskTheoUser(userId)              │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  layChiTietTask(taskId): Task                                  │
│  │                                                               │
│  └─▶ return taskBO.layThongTinTask(taskId)                    │
│                                                                  │
└────────────────────────────────────────────────────────────────┘
```

**Sơ đồ QueueManager (Singleton Pattern):**

```
┌────────────────────────────────────────────────────────────────┐
│                      QueueManager                               │
│                      (Singleton)                                │
├────────────────────────────────────────────────────────────────┤
│                                                                  │
│  - instance: QueueManager (static)                             │
│  - taskQueue: BlockingQueue<Integer>                           │
│  - totalEnqueued: AtomicInteger                                │
│  - totalDequeued: AtomicInteger                                │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  getInstance(): QueueManager (synchronized)                     │
│  │                                                               │
│  └─▶ Double-Checked Locking                                    │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  enqueue(taskId): boolean                                       │
│  │                                                               │
│  ├─▶ taskQueue.put(taskId)  // Blocking                       │
│  │                                                               │
│  └─▶ totalEnqueued.incrementAndGet()                           │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  dequeue(): Integer                                             │
│  │                                                               │
│  ├─▶ taskId = taskQueue.take()  // Blocking                   │
│  │                                                               │
│  └─▶ totalDequeued.incrementAndGet()                           │
│                                                                  │
└────────────────────────────────────────────────────────────────┘
```

---

### 2.4. Model Layer - Business Object (BO)

**Chức năng:** Business logic, validation, gọi DAO

**Sơ đồ TaskBO:**

```
┌────────────────────────────────────────────────────────────────┐
│                          TaskBO                                 │
├────────────────────────────────────────────────────────────────┤
│                                                                  │
│  - taskDAO: TaskDAO                                             │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  taoTaskMoi(userId, fileName, filePath, language): int         │
│  │                                                               │
│  ├─▶ 1. Validate userId > 0                                    │
│  │                                                               │
│  ├─▶ 2. Validate fileName not empty                            │
│  │                                                               │
│  ├─▶ 3. Validate filePath not empty                            │
│  │                                                               │
│  ├─▶ 4. Validate language ("vi" hoặc "en")                     │
│  │      if (invalid) → language = "vi" (default)               │
│  │                                                               │
│  └─▶ 5. Gọi DAO để insert                                      │
│         return taskDAO.themTaskMoi(...)                         │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  datTaskDangXuLy(taskId): boolean                              │
│  │                                                               │
│  ├─▶ 1. Validate taskId > 0                                    │
│  │                                                               │
│  └─▶ 2. Gọi DAO để update status                               │
│         return taskDAO.capNhatStatusProcessing(taskId)          │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  hoanThanhTask(taskId, resultText, processingTimeMs): boolean  │
│  │                                                               │
│  ├─▶ 1. Validate inputs                                        │
│  │                                                               │
│  ├─▶ 2. Business rule: processingTimeMs >= 0                   │
│  │                                                               │
│  └─▶ 3. Gọi DAO để update                                      │
│         return taskDAO.capNhatTaskHoanThanh(...)                │
│                                                                  │
└────────────────────────────────────────────────────────────────┘
```

---

### 2.5. Model Layer - Data Access Object (DAO)

**Chức năng:** Tương tác trực tiếp với database

**Sơ đồ TaskDAO:**

```
┌────────────────────────────────────────────────────────────────┐
│                          TaskDAO                                │
├────────────────────────────────────────────────────────────────┤
│                                                                  │
│  - dbConnect: DBConnect                                         │
│  - taskCache: TaskCache                                         │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  themTaskMoi(userId, fileName, filePath, language): int        │
│  │                                                               │
│  ├─▶ 1. Lấy connection từ pool                                 │
│  │      conn = dbConnect.getConnection()                        │
│  │                                                               │
│  ├─▶ 2. Tạo PreparedStatement                                  │
│  │      sql = "INSERT INTO Task (...) VALUES (?, ?, ?, ...)"   │
│  │      stmt = conn.prepareStatement(sql, RETURN_GENERATED_KEYS)│
│  │                                                               │
│  ├─▶ 3. Set parameters                                          │
│  │      stmt.setInt(1, userId)                                  │
│  │      stmt.setString(2, fileName)                             │
│  │      ...                                                      │
│  │                                                               │
│  ├─▶ 4. Execute update                                          │
│  │      rowsAffected = stmt.executeUpdate()                     │
│  │                                                               │
│  ├─▶ 5. Lấy LAST_INSERT_ID                                     │
│  │      rs = stmt.getGeneratedKeys()                            │
│  │      taskId = rs.getInt(1)                                   │
│  │                                                               │
│  └─▶ 6. Release connection về pool                             │
│         dbConnect.releaseConnection(conn)                        │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  layTaskTheoId(taskId): Task                                   │
│  │                                                               │
│  ├─▶ 1. Check cache                                             │
│  │      cachedTask = taskCache.get(taskId)                     │
│  │      if (cachedTask != null) return cachedTask              │
│  │                                                               │
│  ├─▶ 2. Query database                                          │
│  │      sql = "SELECT * FROM Task WHERE id = ?"                │
│  │      rs = stmt.executeQuery()                                │
│  │                                                               │
│  ├─▶ 3. Map ResultSet → Task object                            │
│  │                                                               │
│  ├─▶ 4. Lưu vào cache                                           │
│  │      taskCache.put(taskId, task)                            │
│  │                                                               │
│  └─▶ 5. Return task                                             │
│                                                                  │
└────────────────────────────────────────────────────────────────┘
```

**Sơ đồ DBConnect (Connection Pool):**

```
┌────────────────────────────────────────────────────────────────┐
│                   DBConnect (Connection Pool)                   │
│                         (Singleton)                             │
├────────────────────────────────────────────────────────────────┤
│                                                                  │
│  - instance: DBConnect (static)                                │
│  - availableConnections: List<Connection>                      │
│  - usedConnections: List<Connection>                           │
│  - INITIAL_POOL_SIZE = 3                                        │
│  - MAX_POOL_SIZE = 10                                           │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  Constructor (private)                                          │
│  │                                                               │
│  └─▶ Tạo INITIAL_POOL_SIZE connections                         │
│         for (i = 0; i < 3; i++)                                 │
│             availableConnections.add(createConnection())        │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  getConnection(): Connection (synchronized)                     │
│  │                                                               │
│  ├─▶ 1. Nếu availableConnections rỗng                          │
│  │      if (usedConnections.size() < MAX_POOL_SIZE)            │
│  │          Tạo connection mới                                  │
│  │      else                                                     │
│  │          Throw SQLException("Pool đầy!")                    │
│  │                                                               │
│  ├─▶ 2. Lấy connection từ available                            │
│  │      conn = availableConnections.remove(0)                  │
│  │                                                               │
│  ├─▶ 3. Chuyển sang used                                        │
│  │      usedConnections.add(conn)                              │
│  │                                                               │
│  └─▶ 4. Return connection                                       │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  releaseConnection(conn): void (synchronized)                   │
│  │                                                               │
│  ├─▶ 1. Remove từ used                                          │
│  │      usedConnections.remove(conn)                           │
│  │                                                               │
│  └─▶ 2. Add vào available                                       │
│         availableConnections.add(conn)                          │
│                                                                  │
└────────────────────────────────────────────────────────────────┘
```

---

### 2.6. Model Layer - Entity (Bean)

**Chức năng:** Đại diện cho entity trong database

**Sơ đồ Task Bean:**

```
┌────────────────────────────────────────────────────────────────┐
│                          Task.java                              │
│                          (Bean/POJO)                            │
├────────────────────────────────────────────────────────────────┤
│                                                                  │
│  - id: int                                                      │
│  - userId: int                                                  │
│  - fileName: String                                             │
│  - serverFilePath: String                                       │
│  - status: String  (PENDING, PROCESSING, COMPLETED, FAILED)    │
│  - language: String  (vi, en)                                   │
│  - submissionTime: Timestamp                                    │
│  - completionTime: Timestamp                                    │
│  - resultText: String                                           │
│  - processingTimeMs: Integer                                    │
│                                                                  │
│  ────────────────────────────────────────────────────────────  │
│                                                                  │
│  + Constructors:                                                │
│    - Task()                                                     │
│    - Task(id, userId, fileName, ...)                           │
│                                                                  │
│  + Getters: getId(), getUserId(), getFileName(), ...           │
│  + Setters: setId(), setUserId(), setFileName(), ...           │
│                                                                  │
└────────────────────────────────────────────────────────────────┘
         │
         │ Mapping 1-1
         ▼
┌────────────────────────────────────────────────────────────────┐
│                      Database Table: Task                       │
├────────────────────────────────────────────────────────────────┤
│  id                 INT PRIMARY KEY AUTO_INCREMENT              │
│  user_id            INT NOT NULL                                │
│  file_name          VARCHAR(255)                                │
│  server_file_path   TEXT                                        │
│  status             VARCHAR(50)                                 │
│  language           VARCHAR(10)                                 │
│  submission_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP         │
│  completion_time    TIMESTAMP NULL                              │
│  result_text        TEXT                                        │
│  processing_time_ms INT                                         │
└────────────────────────────────────────────────────────────────┘
```

---

## 3. LUỒNG HOẠT ĐỘNG HOÀN CHỈNH

### 3.1. Sequence Diagram - Upload và xử lý file

```
User     Browser    MediaController    TaskService    QueueManager    TaskBO    TaskDAO    Database    Worker    VoskModel
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │ 1. Select file         │                │              │            │         │          │          │          │
 │─────────▶│              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │ 2. Click "Upload"      │                │              │            │         │          │          │          │
 │─────────▶│              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │ 3. POST /upload             │              │            │         │          │          │          │
 │          │─────────────▶│                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │ 4. Lưu file vào server       │            │         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │ 5. taoVaDayTaskVaoQueue()    │            │         │          │          │          │
 │          │              │───────────────▶│              │            │         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │ 6. taoTaskMoi()          │         │          │          │          │
 │          │              │                │─────────────────────────▶│         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │            │ 7. themTaskMoi()  │          │          │
 │          │              │                │              │            │────────▶│          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │ 8. INSERT │          │          │
 │          │              │                │              │            │         │─────────▶│          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │ 9. taskId│          │          │
 │          │              │                │              │            │         │◀─────────│          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │            │ 10. taskId        │          │          │
 │          │              │                │              │            │◀────────│          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │ 11. taskId               │         │          │          │          │
 │          │              │                │◀─────────────────────────│         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │ 12. enqueue(taskId)      │         │          │          │          │
 │          │              │                │─────────────▶│            │         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │ 13. taskId     │              │            │         │          │          │          │
 │          │              │◀───────────────│              │            │         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │ 14. Redirect /history         │              │            │         │          │          │          │
 │          │◀─────────────│                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │ 15. Success            │                │              │            │         │          │          │          │
 │◀─────────│              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │ ──────────────────────────────────────────────────── │
 │          │              │                │              │            ASYNC PROCESSING (Background)            │
 │          │              │                │              │ ──────────────────────────────────────────────────── │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │ 16. dequeue()     │
 │          │              │                │              │            │         │          │◀─────────│          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │ 17. taskId │         │          │          │          │
 │          │              │                │              │─────────────────────────────────────────▶│          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │ 18. UPDATE status  │
 │          │              │                │              │            │         │          │──────────│          │
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │ 19. Speech-to-Text │
 │          │              │                │              │            │         │          │─────────────────▶│
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │ 20. resultText    │
 │          │              │                │              │            │         │          │◀─────────────────│
 │          │              │                │              │            │         │          │          │          │
 │          │              │                │              │            │         │          │ 21. UPDATE result │
 │          │              │                │              │            │         │          │──────────│          │
 │          │              │                │              │            │         │          │          │          │
```

---

## 4. BIỂU ĐỒ TRIỂN KHAI (DEPLOYMENT DIAGRAM)

```
┌────────────────────────────────────────────────────────────────────┐
│                        Client Machine                               │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │         Web Browser (Chrome/Firefox/Edge)                     │ │
│  │                                                                │ │
│  │  - HTML5                                                       │ │
│  │  - CSS3                                                        │ │
│  │  - JavaScript                                                  │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                              │                                      │
│                              │ HTTP/HTTPS                           │
└──────────────────────────────┼──────────────────────────────────────┘
                               │
                               │ Port 8080
                               │
┌──────────────────────────────┼──────────────────────────────────────┐
│                        Server Machine                               │
│                              │                                      │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │           Apache Tomcat 10.1 Server                         │   │
│  │                                                              │   │
│  │  ┌──────────────────────────────────────────────────────┐  │   │
│  │  │      DUT_NET-MediaVideo.war                           │  │   │
│  │  │                                                        │  │   │
│  │  │  - Servlets (MediaController, HistoryController...)   │  │   │
│  │  │  - JSP Pages (upload.jsp, history.jsp...)            │  │   │
│  │  │  - Service Layer (TaskService, QueueManager...)      │  │   │
│  │  │  - Model Layer (TaskBO, TaskDAO, DBConnect...)       │  │   │
│  │  │  - Worker Threads (WorkerServiceVosk x2)             │  │   │
│  │  └──────────────────────────────────────────────────────┘  │   │
│  └────────────────────────────────────────────────────────────┘   │
│                              │                                      │
│                              │ JDBC                                 │
│                              │                                      │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │           MySQL Server 8.0 / H2 Database                    │   │
│  │                                                              │   │
│  │  - Database: media_processor_db                             │   │
│  │  - Tables: Task, User                                       │   │
│  │  - Connection Pool: 3-10 connections                        │   │
│  └────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │           File System                                        │   │
│  │                                                              │   │
│  │  - /uploads/          (Uploaded media files)                │   │
│  │  - /models/           (Vosk STT models)                     │   │
│  │    ├─ vosk-model-vn-0.4/                                    │   │
│  │    └─ vosk-model-small-en-us-0.15/                          │   │
│  └────────────────────────────────────────────────────────────┘   │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 5. DESIGN PATTERNS ĐÃ SỬ DỤNG

### 5.1. Singleton Pattern

**Áp dụng cho:**
- QueueManager
- VoskModelManager
- DBConnect

**Mục đích:** Đảm bảo chỉ có 1 instance duy nhất trong toàn hệ thống

**Biểu đồ UML:**

```
┌─────────────────────────────────┐
│       QueueManager              │
├─────────────────────────────────┤
│ - instance: QueueManager (static)│
│ - taskQueue: BlockingQueue      │
├─────────────────────────────────┤
│ - QueueManager()  (private)     │
│ + getInstance(): QueueManager   │
│   (static, synchronized)        │
│ + enqueue(taskId): boolean      │
│ + dequeue(): Integer            │
└─────────────────────────────────┘
```

### 5.2. MVC Pattern

**Áp dụng cho:** Toàn bộ ứng dụng

**Mục đích:** Tách biệt UI, Logic, Data

### 5.3. DAO Pattern

**Áp dụng cho:** TaskDAO, UserDAO

**Mục đích:** Abstract database access

### 5.4. Producer-Consumer Pattern

**Áp dụng cho:** MediaController (Producer) + WorkerServiceVosk (Consumer)

**Mục đích:** Xử lý bất đồng bộ

**Biểu đồ:**

```
┌──────────────┐        ┌─────────────┐        ┌──────────────┐
│  Producer    │───────▶│BlockingQueue│───────▶│  Consumer    │
│ (Controller) │  put() │             │ take() │  (Worker)    │
└──────────────┘        └─────────────┘        └──────────────┘
```

### 5.5. Connection Pool Pattern

**Áp dụng cho:** DBConnect

**Mục đích:** Tái sử dụng database connections

---

*Tài liệu thiết bản kế được tạo cho dự án DUT_NET MediaVideo*
