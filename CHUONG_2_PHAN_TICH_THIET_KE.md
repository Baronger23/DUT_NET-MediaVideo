# CHƯƠNG 2: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG

## 2.1. PHÂN TÍCH BÀI TOÁN

### 2.1.1. Đặt vấn đề

Trong thời đại số hóa, nội dung đa phương tiện (audio và video) ngày càng phổ biến. Tuy nhiên, việc trích xuất văn bản từ các file này để phục vụ mục đích tìm kiếm, phụ đề, hoặc lưu trữ vẫn còn gặp nhiều khó khăn:

- **Tốn thời gian:** Phải nghe/xem và ghi chép thủ công
- **Dễ sai sót:** Khó ghi chép chính xác, đặc biệt với âm thanh chất lượng thấp
- **Khó mở rộng:** Không thể xử lý hàng loạt nhiều file cùng lúc
- **Chi phí cao:** Các dịch vụ cloud tính phí theo thời lượng

Dự án này giải quyết vấn đề bằng cách xây dựng một hệ thống web tự động hóa quá trình chuyển đổi audio/video sang văn bản.

### 2.1.2. Phân tích Input (Đầu vào)

Hệ thống nhận các input sau từ người dùng:

| Input | Loại dữ liệu | Mô tả | Ràng buộc |
|-------|--------------|-------|-----------|
| **File media** | File upload | Audio (.mp3, .wav) hoặc Video (.mp4, .avi) | Kích thước max: 100MB |
| **Ngôn ngữ** | String | Ngôn ngữ của file audio/video | "vi" (tiếng Việt) hoặc "en" (tiếng Anh) |
| **User credentials** | String | Username và password để đăng nhập | Username: 3-50 ký tự, Password: min 6 ký tự |

**Ví dụ request upload file:**

```http
POST /DUT_NET-MediaVideo/upload HTTP/1.1
Content-Type: multipart/form-data

------WebKitFormBoundary
Content-Disposition: form-data; name="mediaFile"; filename="bai_giang.mp3"
Content-Type: audio/mpeg

[Binary file data]
------WebKitFormBoundary
Content-Disposition: form-data; name="language"

vi
------WebKitFormBoundary--
```

### 2.1.3. Phân tích Output (Đầu ra)

Hệ thống trả về các output sau cho người dùng:

| Output | Loại dữ liệu | Mô tả | Ví dụ |
|--------|--------------|-------|-------|
| **Văn bản trích xuất** | Text | Nội dung âm thanh được chuyển thành văn bản | "Chào mừng các bạn đến với bài giảng..." |
| **Trạng thái task** | String | Trạng thái xử lý hiện tại | PENDING, PROCESSING, COMPLETED, FAILED |
| **Thời gian xử lý** | Integer | Thời gian xử lý tính bằng milliseconds | 45000 (45 giây) |
| **Lịch sử tasks** | List | Danh sách các tasks đã upload | Bảng hiển thị trên trang history |
| **File export** | Text file | File .txt chứa kết quả có thể tải về | task_123_result.txt |

**Ví dụ response hiển thị kết quả:**

```json
{
    "taskId": 123,
    "fileName": "bai_giang.mp3",
    "status": "COMPLETED",
    "language": "vi",
    "resultText": "Chào mừng các bạn đến với bài giảng về lập trình mạng...",
    "submissionTime": "2024-11-22 14:30:00",
    "completionTime": "2024-11-22 14:31:45",
    "processingTimeMs": 45000
}
```

### 2.1.4. Luồng xử lý tổng quan

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│             │         │              │         │              │
│    User     │────────▶│   System     │────────▶│    User      │
│             │ INPUT   │              │ OUTPUT  │              │
└─────────────┘         └──────────────┘         └──────────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │  Processing Steps  │
                    │                    │
                    │  1. Upload file    │
                    │  2. Create task    │
                    │  3. Queue task     │
                    │  4. STT process    │
                    │  5. Save result    │
                    └────────────────────┘
```

---

## 2.2. PHÂN TÍCH CHỨC NĂNG

Hệ thống được chia thành các chức năng chính phục vụ hai nhóm người dùng: **Người dùng thông thường** và **Hệ thống** (xử lý tự động).

### 2.2.1. Chức năng dành cho người dùng

**1. Quản lý tài khoản**

- **Đăng ký:** User tạo tài khoản mới với username, password, email
- **Đăng nhập:** Xác thực thông tin và tạo session
- **Đăng xuất:** Hủy session và chuyển về trang login

**2. Upload và quản lý file**

- **Upload file media:**
  - Chọn file audio/video từ máy tính
  - Chọn ngôn ngữ (tiếng Việt/tiếng Anh)
  - Upload lên server
  - Nhận xác nhận task đã được tạo

- **Xem lịch sử tasks:**
  - Hiển thị danh sách các tasks đã upload
  - Hiển thị trạng thái real-time (Đang chờ, Đang xử lý, Hoàn thành)
  - Sắp xếp theo thời gian mới nhất

- **Xem chi tiết task:**
  - Xem kết quả văn bản đã trích xuất
  - Xem thông tin chi tiết (thời gian xử lý, ngôn ngữ, file name)
  - Tải xuống kết quả dạng file .txt

### 2.2.2. Chức năng xử lý tự động (Background)

**1. Queue Management:**
- Nhận task mới từ Controller
- Lưu trữ task ID trong hàng đợi (FIFO - First In First Out)
- Cung cấp task cho Workers khi có yêu cầu

**2. Worker Processing:**
- Worker threads chạy ngầm liên tục
- Lấy task từ queue (blocking operation)
- Thực hiện Speech-to-Text processing
- Cập nhật kết quả vào database

**3. Speech-to-Text Conversion:**
- Extract audio từ video (nếu là file video)
- Chuyển đổi audio sang định dạng WAV (yêu cầu của Vosk)
- Áp dụng model phù hợp (tiếng Việt hoặc tiếng Anh)
- Trích xuất văn bản từ audio

### 2.2.3. Use Case Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                      DUT_NET MediaVideo                       │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│   ┌─────────┐                                                │
│   │  User   │                                                │
│   └────┬────┘                                                │
│        │                                                      │
│        ├──▶ (Đăng ký)                                        │
│        ├──▶ (Đăng nhập)                                      │
│        ├──▶ (Upload file media)                              │
│        │     └──▶ (Chọn ngôn ngữ)                            │
│        ├──▶ (Xem lịch sử tasks)                              │
│        ├──▶ (Xem chi tiết task)                              │
│        └──▶ (Tải xuống kết quả)                              │
│                                                               │
│   ┌─────────┐                                                │
│   │ System  │                                                │
│   └────┬────┘                                                │
│        │                                                      │
│        ├──▶ (Quản lý Queue)                                  │
│        ├──▶ (Worker xử lý task)                              │
│        │     ├──▶ (Extract audio)                            │
│        │     ├──▶ (Convert format)                           │
│        │     └──▶ (Speech-to-Text)                           │
│        └──▶ (Cập nhật trạng thái)                            │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

### 2.2.4. Ưu tiên chức năng

| Mức độ | Chức năng | Lý do |
|--------|-----------|-------|
| **Cao** | Upload file và tạo task | Core functionality, không thể thiếu |
| **Cao** | Speech-to-Text processing | Yêu cầu chính của đề bài (30% điểm) |
| **Cao** | Xử lý bất đồng bộ với Queue + Workers | Đáp ứng yêu cầu "tính toán lớn" |
| **Trung bình** | Quản lý user (login/register) | Cần thiết cho multi-user |
| **Trung bình** | Xem lịch sử và chi tiết | User experience |
| **Thấp** | Export file .txt | Nice to have, không bắt buộc |

---

## 2.3. XÂY DỰNG CHƯƠNG TRÌNH - PHÂN TÍCH KIẾN TRÚC MVC

Đây là phần **QUAN TRỌNG NHẤT** của chương 2, phân tích chi tiết cấu trúc MVC và vai trò của từng file trong dự án.

### 2.3.1. Tổng quan kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (Browser)                          │
│                     HTML/CSS/JavaScript                          │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP Request/Response
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Presentation Layer (View)                      │
│                          JSP Pages                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │login.jsp │  │upload.jsp│  │history.jsp│ │home.jsp  │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Controller Layer (Servlet)                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐│
│  │MediaController  │  │HistoryController│  │LoginServlet     ││
│  └─────────────────┘  └─────────────────┘  └─────────────────┘│
└───────────────────────────┬─────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│Service Layer │   │Service Layer │   │Service Layer │
│ TaskService  │   │QueueManager  │   │VoskModel     │
│              │   │              │   │Manager       │
└──────┬───────┘   └──────┬───────┘   └──────┬───────┘
       │                   │                   │
       ▼                   ▼                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Business Logic Layer                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────────────────┐ │
│  │ TaskBO   │  │ UserBO   │  │   WorkerServiceVosk          │ │
│  │          │  │          │  │   (Runs in background)       │ │
│  └────┬─────┘  └────┬─────┘  └──────────────────────────────┘ │
└───────┼─────────────┼────────────────────────────────────────────┘
        │             │
        ▼             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Data Access Layer (DAO)                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────────────────┐ │
│  │ TaskDAO  │  │ UserDAO  │  │   DBConnect (Connection Pool)│ │
│  └────┬─────┘  └────┬─────┘  └──────────┬───────────────────┘ │
└───────┼─────────────┼────────────────────┼──────────────────────┘
        │             │                    │
        └─────────────┴────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Database Layer                             │
│                   MySQL / H2 Database                            │
│  ┌──────────┐  ┌──────────┐                                    │
│  │Task Table│  │User Table│                                    │
│  └──────────┘  └──────────┘                                    │
└─────────────────────────────────────────────────────────────────┘
```

### 2.3.2. View Layer (Presentation) - Các file JSP

**Vị trí:** `/src/main/webapp/View/`

#### **1. login.jsp** - Trang đăng nhập

**Chức năng:**
- Hiển thị form đăng nhập với username và password
- Xử lý submit form gửi đến LoginServlet
- Hiển thị thông báo lỗi nếu đăng nhập thất bại

**Các thành phần chính:**
```jsp
<form action="${pageContext.request.contextPath}/login" method="post">
    <input type="text" name="username" required>
    <input type="password" name="password" required>
    <button type="submit">Đăng nhập</button>
</form>
```

#### **2. register.jsp** - Trang đăng ký

**Chức năng:**
- Form đăng ký tài khoản mới
- Validate input (username, password, email)
- Submit đến RegisterServlet

#### **3. upload.jsp** - Trang upload file

**Chức năng:**
- Form upload file với enctype="multipart/form-data"
- Dropdown chọn ngôn ngữ (Tiếng Việt/Tiếng Anh)
- Hiển thị số task đang chờ trong queue

**Đặc điểm quan trọng:**
```jsp
<form action="${pageContext.request.contextPath}/upload" 
      method="post" 
      enctype="multipart/form-data">
    
    <input type="file" name="mediaFile" accept="audio/*,video/*" required>
    
    <select name="language">
        <option value="vi" selected>🇻🇳 Tiếng Việt</option>
        <option value="en">🇺🇸 Tiếng Anh</option>
    </select>
    
    <button type="submit">🚀 Upload và Xử lý</button>
</form>

<p>Số task đang chờ: <strong>${queueSize}</strong></p>
```

#### **4. history.jsp** - Trang lịch sử tasks

**Chức năng:**
- Hiển thị bảng danh sách tasks của user
- Hiển thị trạng thái real-time với icon
- Nút "Xem chi tiết" cho mỗi task

**Ví dụ code:**
```jsp
<table>
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
                    <c:otherwise>
                        ❌ Thất bại
                    </c:otherwise>
                </c:choose>
            </td>
            <td>${task.submissionTime}</td>
            <td>
                <a href="${pageContext.request.contextPath}/api/task/${task.id}">
                    👁️ Xem
                </a>
            </td>
        </tr>
    </c:forEach>
</table>
```

**Đặc điểm:**
- Sử dụng JSTL (JSP Standard Tag Library) để loop qua danh sách
- Expression Language (EL) để truy cập dữ liệu: `${task.fileName}`
- Conditional rendering với `<c:choose>` cho status

#### **5. home.jsp** - Trang chủ

**Chức năng:**
- Dashboard hiển thị thống kê tổng quan
- Số lượng tasks theo trạng thái (Pending, Processing, Completed)
- Liên kết nhanh đến các chức năng chính

---

### 2.3.3. Controller Layer - Các Servlet

**Vị trí:** `/src/main/java/Controller/`

#### **1. MediaController.java** - Controller quan trọng nhất

**Chức năng:** Xử lý upload file media và tạo task

**Annotation:**
```java
@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 100,      // 100MB
    maxRequestSize = 1024 * 1024 * 150    // 150MB
)
```

**Các phương thức:**

**a) doGet() - Hiển thị form upload:**
```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    // Kiểm tra đăng nhập
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect("/login");
        return;
    }
    
    // Lấy số task đang trong queue
    int queueSize = taskService.getSoLuongTaskTrongQueue();
    request.setAttribute("queueSize", queueSize);
    
    // Forward đến upload.jsp
    request.getRequestDispatcher("/View/upload.jsp").forward(request, response);
}
```

**b) doPost() - Xử lý upload file:**
```java
protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    // 1. Lấy thông tin user từ session
    User user = (User) session.getAttribute("user");
    
    // 2. Nhận file upload
    Part filePart = request.getPart("mediaFile");
    String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
    
    // 3. Nhận ngôn ngữ
    String language = request.getParameter("language");
    if (language == null || language.trim().isEmpty()) {
        language = "vi"; // Mặc định tiếng Việt
    }
    
    // 4. Lưu file vào server
    String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
    String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
    String serverFilePath = uploadPath + File.separator + uniqueFileName;
    filePart.write(serverFilePath);
    
    // 5. Tạo task và đẩy vào queue
    int taskId = taskService.taoVaDayTaskVaoQueue(
        user.getId(), 
        fileName, 
        serverFilePath,
        language
    );
    
    // 6. Redirect đến history
    if (taskId > 0) {
        response.sendRedirect(request.getContextPath() + "/history?success=true");
    } else {
        response.sendRedirect(request.getContextPath() + "/upload?error=true");
    }
}
```

**Vai trò quan trọng:**
- ✅ Nhận file upload từ client (multipart/form-data)
- ✅ Lưu file vào thư mục `uploads/` trên server
- ✅ Tạo task mới trong database với status PENDING
- ✅ Đẩy task ID vào Queue để Worker xử lý
- ✅ Không block client, trả về response ngay lập tức

#### **2. HistoryController.java** - Hiển thị lịch sử

**Chức năng:** Lấy danh sách tasks của user và hiển thị

```java
@WebServlet("/history")
public class HistoryController extends HttpServlet {
    private TaskService taskService;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        // Lấy user từ session
        User user = (User) session.getAttribute("user");
        
        // Lấy danh sách tasks
        List<Task> taskList = taskService.layLichSuTask(user.getId());
        
        // Set attribute để JSP hiển thị
        request.setAttribute("taskList", taskList);
        
        // Forward đến history.jsp
        request.getRequestDispatcher("/View/history.jsp").forward(request, response);
    }
}
```

#### **3. TaskDetailController.java** - API chi tiết task

**Chức năng:** Trả về thông tin chi tiết của một task

```java
@WebServlet("/api/task/*")
public class TaskDetailController extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        // Lấy taskId từ URL path
        String pathInfo = request.getPathInfo(); // /123
        int taskId = Integer.parseInt(pathInfo.substring(1));
        
        // Lấy task từ database
        Task task = taskService.layChiTietTask(taskId);
        
        // Kiểm tra quyền truy cập (task phải thuộc về user)
        User user = (User) session.getAttribute("user");
        if (task.getUserId() != user.getId()) {
            response.sendError(403, "Forbidden");
            return;
        }
        
        // Trả về JSON hoặc JSP
        String format = request.getParameter("format");
        if ("json".equals(format)) {
            // Trả về JSON
            response.setContentType("application/json");
            response.getWriter().write(toJson(task));
        } else {
            // Forward đến JSP
            request.setAttribute("task", task);
            request.getRequestDispatcher("/View/task-detail.jsp").forward(request, response);
        }
    }
}
```

#### **4. LoginServlet.java & RegisterServlet.java**

**Chức năng:** Xác thực và đăng ký user

```java
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserBO userBO;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // Kiểm tra credentials
        User user = userBO.kiemTraDangNhap(username, password);
        
        if (user != null) {
            // Tạo session
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            response.sendRedirect("/home");
        } else {
            request.setAttribute("error", "Sai username hoặc password");
            request.getRequestDispatcher("/View/login.jsp").forward(request, response);
        }
    }
}
```

---

### 2.3.4. Service Layer - Business Logic

**Vị trí:** `/src/main/java/Service/`

#### **1. TaskService.java** - Service chính cho Task

**Chức năng:** Tổ chức các thao tác liên quan đến Task

```java
public class TaskService {
    private TaskBO taskBO;
    private QueueManager queueManager;
    
    /**
     * Tạo task mới và đẩy vào queue (Được gọi từ MediaController)
     */
    public int taoVaDayTaskVaoQueue(int userId, String fileName, 
                                     String serverFilePath, String language) {
        // 1. Tạo task trong DB
        int taskId = taskBO.taoTaskMoi(userId, fileName, serverFilePath, language);
        
        if (taskId > 0) {
            // 2. Đẩy vào queue
            boolean success = queueManager.enqueue(taskId);
            if (success) {
                System.out.println("Task " + taskId + " đã được đẩy vào queue");
                return taskId;
            }
        }
        
        return -1;
    }
    
    /**
     * Lấy lịch sử tasks của user
     */
    public List<Task> layLichSuTask(int userId) {
        return taskBO.layLichSuTaskTheoUser(userId);
    }
    
    /**
     * Lấy số lượng task trong queue
     */
    public int getSoLuongTaskTrongQueue() {
        return queueManager.getQueueSize();
    }
}
```

**Vai trò:**
- Kết nối giữa Controller và BO
- Orchestrate các thao tác phức tạp (tạo task + enqueue)
- Đơn giản hóa code trong Controller

#### **2. QueueManager.java** - Quản lý hàng đợi

**Chức năng:** Singleton quản lý BlockingQueue

```java
public class QueueManager {
    private static QueueManager instance;
    private BlockingQueue<Integer> taskQueue;
    private AtomicInteger totalEnqueued;
    private AtomicInteger totalDequeued;
    
    private QueueManager() {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.totalEnqueued = new AtomicInteger(0);
        this.totalDequeued = new AtomicInteger(0);
    }
    
    /**
     * Singleton với Double-Checked Locking
     */
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
    
    /**
     * Producer - Đẩy task vào queue (Thread-safe)
     */
    public boolean enqueue(Integer taskId) {
        try {
            taskQueue.put(taskId); // Blocking nếu queue đầy
            totalEnqueued.incrementAndGet();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * Consumer - Lấy task từ queue (Thread-safe, Blocking)
     */
    public Integer dequeue() throws InterruptedException {
        Integer taskId = taskQueue.take(); // Block nếu queue rỗng
        totalDequeued.incrementAndGet();
        return taskId;
    }
    
    /**
     * Lấy số task đang trong queue
     */
    public int getQueueSize() {
        return taskQueue.size();
    }
}
```

**Đặc điểm quan trọng:**
- ✅ **Thread-safe:** Sử dụng BlockingQueue
- ✅ **Singleton Pattern:** Chỉ có 1 instance duy nhất
- ✅ **Blocking operations:** `put()` và `take()` tự động chờ
- ✅ **Monitoring:** Sử dụng AtomicInteger để đếm

#### **3. VoskModelManager.java** - Quản lý Vosk Models

**Chức năng:** Singleton load và share Vosk models cho tất cả Workers

```java
public class VoskModelManager {
    private static VoskModelManager instance;
    private Model modelVietnamese;
    private Model modelEnglish;
    private boolean isInitialized = false;
    
    private VoskModelManager() {
        // Private constructor
    }
    
    public static VoskModelManager getInstance() {
        if (instance == null) {
            synchronized (VoskModelManager.class) {
                if (instance == null) {
                    instance = new VoskModelManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Load models 1 lần duy nhất khi server start
     */
    public synchronized void initializeModels() {
        if (isInitialized) {
            return; // Đã load rồi
        }
        
        try {
            String basePath = System.getProperty("user.dir");
            String viModelPath = basePath + "/models/vosk-model-vn-0.4";
            String enModelPath = basePath + "/models/vosk-model-small-en-us-0.15";
            
            System.out.println("🚀 Đang load Vosk models...");
            
            this.modelVietnamese = new Model(viModelPath);
            System.out.println("✅ Model tiếng Việt đã được load");
            
            this.modelEnglish = new Model(enModelPath);
            System.out.println("✅ Model tiếng Anh đã được load");
            
            this.isInitialized = true;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi load models: " + e.getMessage());
        }
    }
    
    /**
     * Workers gọi method này để lấy model (Không tạo mới)
     */
    public Model getModel(String language) {
        if (!isInitialized) {
            throw new IllegalStateException("Models chưa được khởi tạo!");
        }
        
        if ("en".equals(language)) {
            return modelEnglish;
        } else {
            return modelVietnamese; // Mặc định
        }
    }
    
    /**
     * Cleanup khi server shutdown
     */
    public synchronized void close() {
        if (modelVietnamese != null) {
            modelVietnamese.close();
        }
        if (modelEnglish != null) {
            modelEnglish.close();
        }
        isInitialized = false;
    }
}
```

**Lợi ích:**
- ✅ **Tiết kiệm memory:** Load model 1 lần, share cho nhiều Workers
- ✅ **Khởi động nhanh:** Không phải load lại mỗi khi Worker start
- ✅ **Thread-safe:** Synchronized methods

#### **4. WorkerServiceVosk.java** - Worker xử lý Speech-to-Text

**Chức năng:** Thread chạy ngầm, lấy task từ queue và xử lý

```java
public class WorkerServiceVosk implements Runnable {
    private String workerName;
    private TaskBO taskBO;
    private QueueManager queueManager;
    private VoskModelManager modelManager;
    private boolean isRunning = true;
    
    public WorkerServiceVosk(String workerName) {
        this.workerName = workerName;
        this.taskBO = new TaskBO();
        this.queueManager = QueueManager.getInstance();
        this.modelManager = VoskModelManager.getInstance();
    }
    
    @Override
    public void run() {
        System.out.println("[" + workerName + "] Worker đã khởi động");
        
        while (isRunning) {
            try {
                // Lấy task từ queue (Blocking - chờ nếu rỗng)
                Integer taskId = queueManager.dequeue();
                
                // Xử lý task
                xuLyTask(taskId);
                
            } catch (InterruptedException e) {
                System.out.println("[" + workerName + "] Worker bị interrupt");
                break;
            }
        }
    }
    
    /**
     * Xử lý một task cụ thể
     */
    private void xuLyTask(int taskId) {
        long startTime = System.currentTimeMillis();
        
        try {
            System.out.println("[" + workerName + "] Bắt đầu xử lý Task ID: " + taskId);
            
            // 1. Cập nhật status = PROCESSING
            taskBO.datTaskDangXuLy(taskId);
            
            // 2. Lấy thông tin task
            Task task = taskBO.layThongTinTask(taskId);
            String filePath = task.getServerFilePath();
            String language = task.getLanguage();
            
            // 3. Thực hiện Speech-to-Text (30% ĐIỂM - TÁC VỤ LỚN)
            System.out.println("[" + workerName + "] ===== BẮT ĐẦU SPEECH-TO-TEXT =====");
            String resultText = thucHienSpeechToTextVosk(filePath, language);
            
            // 4. Tính thời gian xử lý
            long endTime = System.currentTimeMillis();
            int processingTimeMs = (int) (endTime - startTime);
            
            // 5. Lưu kết quả vào DB
            taskBO.hoanThanhTask(taskId, resultText, processingTimeMs);
            
            System.out.println("[" + workerName + "] ✓ Hoàn thành Task ID: " + taskId 
                             + " trong " + processingTimeMs + "ms");
            
        } catch (Exception e) {
            System.err.println("[" + workerName + "] Lỗi xử lý task: " + e.getMessage());
            taskBO.datTaskThatBai(taskId, e.getMessage());
        }
    }
    
    /**
     * ⭐ TÁC VỤ QUAN TRỌNG NHẤT - SPEECH-TO-TEXT (30% ĐIỂM)
     */
    private String thucHienSpeechToTextVosk(String filePath, String language) 
            throws IOException {
        
        // Lấy model từ VoskModelManager (shared)
        Model model = modelManager.getModel(language);
        
        // Tạo Recognizer
        try (Recognizer recognizer = new Recognizer(model, 16000)) {
            
            // Đọc file audio
            FileInputStream fis = new FileInputStream(new File(filePath));
            byte[] buffer = new byte[4096];
            int bytesRead;
            StringBuilder resultBuilder = new StringBuilder();
            
            // Xử lý từng chunk
            while ((bytesRead = fis.read(buffer)) != -1) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    String result = recognizer.getResult();
                    resultBuilder.append(parseResult(result));
                }
            }
            
            // Lấy phần cuối
            String finalResult = recognizer.getFinalResult();
            resultBuilder.append(parseResult(finalResult));
            
            return resultBuilder.toString();
        }
    }
    
    /**
     * Parse JSON result từ Vosk
     */
    private String parseResult(String jsonResult) {
        // Extract "text" field from JSON
        // {"text": "hello world"}
        // ...
    }
}
```

**Vai trò QUAN TRỌNG:**
- ✅ **Xử lý bất đồng bộ:** Chạy ở background thread
- ✅ **Tác vụ tính toán lớn:** Speech-to-Text processing (đáp ứng yêu cầu 30% điểm)
- ✅ **Tự động hóa:** Liên tục lấy task từ queue và xử lý
- ✅ **Cập nhật trạng thái:** PENDING → PROCESSING → COMPLETED/FAILED

---

### 2.3.5. Model Layer - Business Object và Data Access

**Vị trí:** `/src/main/java/Model/`

#### **Cấu trúc:**
```
Model/
├── Bean/
│   ├── Task.java       (Entity)
│   └── User.java       (Entity)
├── BO/
│   ├── TaskBO.java     (Business Object)
│   └── UserBO.java     (Business Object)
└── DAO/
    ├── TaskDAO.java    (Data Access Object)
    ├── UserDAO.java    (Data Access Object)
    ├── DBConnect.java  (Connection Pool)
    └── TaskCache.java  (Caching)
```

#### **1. Bean (Entity) - Task.java**

**Chức năng:** Đại diện cho một record trong bảng Task

```java
public class Task {
    private int id;
    private int userId;
    private String fileName;
    private String serverFilePath;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private Timestamp submissionTime;
    private Timestamp completionTime;
    private String resultText;
    private Integer processingTimeMs;
    private String language; // vi, en
    
    // Constructors
    public Task() {}
    
    public Task(int id, int userId, String fileName, ...) {
        this.id = id;
        // ...
    }
    
    // Getters và Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    // ...
}
```

**Đặc điểm:**
- POJO (Plain Old Java Object) - Không có logic xử lý
- Chỉ chứa fields và getter/setter
- Mapping 1-1 với table trong database

#### **2. BO (Business Object) - TaskBO.java**

**Chức năng:** Chứa business logic, validate, và gọi DAO

```java
public class TaskBO {
    private TaskDAO taskDAO;
    
    public TaskBO() {
        this.taskDAO = new TaskDAO();
    }
    
    /**
     * Tạo task mới với validation
     */
    public int taoTaskMoi(int userId, String fileName, 
                          String serverFilePath, String language) {
        // Validate input
        if (userId <= 0) {
            System.err.println("User ID không hợp lệ");
            return -1;
        }
        
        if (fileName == null || fileName.trim().isEmpty()) {
            System.err.println("File name không được rỗng");
            return -1;
        }
        
        // Validate language
        if (language == null || language.trim().isEmpty()) {
            language = "vi"; // Default
        }
        
        if (!language.equals("vi") && !language.equals("en")) {
            language = "vi";
        }
        
        // Gọi DAO để insert
        return taskDAO.themTaskMoi(userId, fileName, serverFilePath, language);
    }
    
    /**
     * Worker hoàn thành task
     */
    public boolean hoanThanhTask(int taskId, String resultText, int processingTimeMs) {
        // Validate
        if (taskId <= 0) {
            return false;
        }
        
        if (resultText == null || resultText.trim().isEmpty()) {
            resultText = "[Không có kết quả]";
        }
        
        // Business rule: Processing time phải > 0
        if (processingTimeMs < 0) {
            processingTimeMs = 0;
        }
        
        // Gọi DAO để update
        return taskDAO.capNhatTaskHoanThanh(taskId, resultText, processingTimeMs);
    }
    
    /**
     * Lấy lịch sử tasks của user
     */
    public List<Task> layLichSuTaskTheoUser(int userId) {
        if (userId <= 0) {
            return new ArrayList<>();
        }
        return taskDAO.layDanhSachTaskTheoUser(userId);
    }
}
```

**Vai trò:**
- Validate dữ liệu trước khi gọi DAO
- Áp dụng business rules
- Error handling và logging
- Kết nối giữa Service và DAO

#### **3. DAO (Data Access Object) - TaskDAO.java**

**Chức năng:** Tương tác trực tiếp với database

```java
public class TaskDAO {
    private DBConnect dbConnect;
    private TaskCache taskCache;
    
    public TaskDAO() {
        this.dbConnect = DBConnect.getInstance();
        this.taskCache = TaskCache.getInstance();
    }
    
    /**
     * INSERT task mới vào database
     */
    public int themTaskMoi(int userId, String fileName, 
                           String serverFilePath, String language) {
        String sql = "INSERT INTO Task (user_id, file_name, server_file_path, " +
                     "status, language, submission_time) " +
                     "VALUES (?, ?, ?, 'PENDING', ?, NOW())";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnect.getConnection(); // Lấy từ pool
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setInt(1, userId);
            stmt.setString(2, fileName);
            stmt.setString(3, serverFilePath);
            stmt.setString(4, language);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // LAST_INSERT_ID
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi INSERT task: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Đóng resources
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn); // Trả về pool
        }
        
        return -1;
    }
    
    /**
     * UPDATE task hoàn thành
     */
    public boolean capNhatTaskHoanThanh(int taskId, String resultText, 
                                        int processingTimeMs) {
        String sql = "UPDATE Task SET status = 'COMPLETED', " +
                     "result_text = ?, " +
                     "completion_time = NOW(), " +
                     "processing_time_ms = ? " +
                     "WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, resultText);
            stmt.setInt(2, processingTimeMs);
            stmt.setInt(3, taskId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Invalidate cache
                taskCache.invalidate(taskId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi UPDATE task: " + e.getMessage());
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return false;
    }
    
    /**
     * SELECT task theo ID (với caching)
     */
    public Task layTaskTheoId(int taskId) {
        // Check cache trước
        Task cachedTask = taskCache.get(taskId);
        if (cachedTask != null) {
            return cachedTask;
        }
        
        String sql = "SELECT * FROM Task WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, taskId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setUserId(rs.getInt("user_id"));
                task.setFileName(rs.getString("file_name"));
                task.setServerFilePath(rs.getString("server_file_path"));
                task.setStatus(rs.getString("status"));
                task.setLanguage(rs.getString("language"));
                task.setSubmissionTime(rs.getTimestamp("submission_time"));
                task.setCompletionTime(rs.getTimestamp("completion_time"));
                task.setResultText(rs.getString("result_text"));
                task.setProcessingTimeMs(rs.getInt("processing_time_ms"));
                
                // Lưu vào cache
                taskCache.put(taskId, task);
                
                return task;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SELECT task: " + e.getMessage());
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return null;
    }
    
    /**
     * SELECT danh sách tasks của user (có giới hạn 100)
     */
    public List<Task> layDanhSachTaskTheoUser(int userId) {
        String sql = "SELECT * FROM Task WHERE user_id = ? " +
                     "ORDER BY submission_time DESC LIMIT 100";
        
        List<Task> tasks = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnect.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Task task = new Task();
                // Map fields...
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SELECT tasks: " + e.getMessage());
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) dbConnect.releaseConnection(conn);
        }
        
        return tasks;
    }
}
```

**Đặc điểm quan trọng:**
- ✅ **PreparedStatement:** Tránh SQL Injection
- ✅ **Connection Pool:** Lấy và trả connection đúng cách
- ✅ **Resource management:** Đóng statement và connection trong finally
- ✅ **Caching:** Giảm database queries
- ✅ **Error handling:** Try-catch và logging

#### **4. DBConnect.java - Connection Pool**

**Chức năng:** Quản lý pool các database connections

```java
public class DBConnect {
    private static DBConnect instance;
    private List<Connection> availableConnections;
    private List<Connection> usedConnections;
    
    private static final int INITIAL_POOL_SIZE = 3;
    private static final int MAX_POOL_SIZE = 10;
    private static final String URL = "jdbc:mysql://localhost:3306/media_processor_db";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    
    private DBConnect() {
        availableConnections = new ArrayList<>();
        usedConnections = new ArrayList<>();
        
        // Tạo initial connections
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            availableConnections.add(createConnection());
        }
        
        System.out.println("✅ Connection Pool đã được khởi tạo");
        System.out.println("   - Initial: " + INITIAL_POOL_SIZE + " connections");
        System.out.println("   - Max: " + MAX_POOL_SIZE + " connections");
    }
    
    public static synchronized DBConnect getInstance() {
        if (instance == null) {
            instance = new DBConnect();
        }
        return instance;
    }
    
    private Connection createConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo connection: " + e.getMessage());
        }
    }
    
    public synchronized Connection getConnection() throws SQLException {
        if (availableConnections.isEmpty()) {
            if (usedConnections.size() < MAX_POOL_SIZE) {
                // Tạo connection mới
                availableConnections.add(createConnection());
            } else {
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

---

### 2.3.6. Listener - Khởi tạo hệ thống

**Vị trí:** `/src/main/java/Listener/WorkerInitializer.java`

**Chức năng:** Khởi động Workers và load Vosk models khi server start

```java
@WebListener
public class WorkerInitializer implements ServletContextListener {
    private Thread worker1Thread;
    private Thread worker2Thread;
    private WorkerServiceVosk worker1;
    private WorkerServiceVosk worker2;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("========================================");
        System.out.println("🚀 Server đang khởi động...");
        System.out.println("========================================");
        
        // 1. Load Vosk models (1 lần duy nhất)
        VoskModelManager modelManager = VoskModelManager.getInstance();
        modelManager.initializeModels();
        
        // 2. Khởi động Worker threads
        worker1 = new WorkerServiceVosk("Worker-1");
        worker2 = new WorkerServiceVosk("Worker-2");
        
        worker1Thread = new Thread(worker1);
        worker2Thread = new Thread(worker2);
        
        worker1Thread.start();
        worker2Thread.start();
        
        System.out.println("✓ Worker-1 và Worker-2 đã được khởi động");
        System.out.println("✓ Hệ thống sẵn sàng xử lý tasks");
        System.out.println("========================================");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("🛑 Server đang shutdown...");
        
        // Graceful shutdown
        if (worker1 != null) worker1.shutdown();
        if (worker2 != null) worker2.shutdown();
        
        VoskModelManager.getInstance().close();
        DBConnect.getInstance().closeAllConnections();
        
        System.out.println("✓ Hệ thống đã được shutdown an toàn");
    }
}
```

**Vai trò quan trọng:**
- ✅ Khởi động tự động khi server start
- ✅ Load Vosk models 1 lần (tiết kiệm thời gian và memory)
- ✅ Khởi động 2 Worker threads chạy ngầm
- ✅ Graceful shutdown khi server stop

---

### 2.3.7. Tổng kết luồng xử lý hoàn chỉnh

**Kịch bản: User upload file "bai_giang.mp3" tiếng Việt**

```
1. User truy cập /upload → MediaController.doGet()
   → Forward to upload.jsp

2. User chọn file và submit form
   → POST /upload → MediaController.doPost()
   
3. MediaController:
   a) Nhận Part filePart = request.getPart("mediaFile")
   b) Lưu file: /uploads/1700000000_bai_giang.mp3
   c) Lấy language = "vi"
   d) Gọi taskService.taoVaDayTaskVaoQueue(userId, fileName, path, "vi")
   
4. TaskService:
   a) Gọi taskBO.taoTaskMoi() → Validate inputs
   b) taskBO gọi taskDAO.themTaskMoi()
   
5. TaskDAO:
   a) getConnection() từ Pool
   b) INSERT INTO Task (...) VALUES (...) → Trả về taskId = 123
   c) releaseConnection() về Pool
   
6. TaskService tiếp tục:
   a) queueManager.enqueue(123) → Đẩy vào BlockingQueue
   b) Return taskId = 123
   
7. MediaController:
   → response.sendRedirect("/history")
   → User thấy task mới với status "⏳ Đang chờ"

8. Worker-1 (chạy ngầm trong background):
   a) taskId = queueManager.dequeue() → Lấy 123 (blocking)
   b) taskBO.datTaskDangXuLy(123) → UPDATE status = 'PROCESSING'
   c) task = taskBO.layThongTinTask(123) → SELECT * FROM Task WHERE id=123
   d) filePath = task.getServerFilePath()
   e) language = task.getLanguage() = "vi"
   
9. Worker-1 thực hiện Speech-to-Text (TÁC VỤ LỚN - 30% ĐIỂM):
   a) model = VoskModelManager.getInstance().getModel("vi")
   b) recognizer = new Recognizer(model, 16000)
   c) Đọc file audio và xử lý từng chunk
   d) resultText = "Chào mừng các bạn đến với bài giảng..."
   e) processingTimeMs = 45000 (45 giây)
   
10. Worker-1 lưu kết quả:
    a) taskBO.hoanThanhTask(123, resultText, 45000)
    b) taskDAO.capNhatTaskHoanThanh()
    c) UPDATE Task SET status='COMPLETED', result_text=..., processing_time_ms=45000
    d) taskCache.invalidate(123)
    
11. User refresh /history:
    → HistoryController.doGet()
    → taskService.layLichSuTask(userId)
    → taskBO.layLichSuTaskTheoUser()
    → taskDAO.layDanhSachTaskTheoUser()
    → SELECT * FROM Task WHERE user_id=? ORDER BY submission_time DESC LIMIT 100
    → Return List<Task>
    → Forward to history.jsp
    → User thấy task 123 với status "✅ Hoàn thành"
    
12. User click "👁️ Xem":
    → GET /api/task/123 → TaskDetailController.doGet()
    → task = taskService.layChiTietTask(123)
    → Check cache → Nếu không có, SELECT từ DB
    → Forward to task-detail.jsp
    → Hiển thị kết quả văn bản đầy đủ
```

---

## KẾT LUẬN CHƯƠNG 2

Chương này đã phân tích chi tiết:

1. **Bài toán (2.1):** Input là file media + ngôn ngữ, Output là văn bản trích xuất + metadata
2. **Chức năng (2.2):** Các use cases chính cho user và system
3. **Kiến trúc MVC (2.3):** Phân tích sâu từng layer và vai trò của từng file quan trọng:
   - **View (JSP):** upload.jsp, history.jsp, login.jsp
   - **Controller (Servlet):** MediaController, HistoryController, TaskDetailController
   - **Service:** TaskService, QueueManager, VoskModelManager, WorkerServiceVosk
   - **Model (BO/DAO):** TaskBO, TaskDAO, DBConnect với Connection Pool
   - **Listener:** WorkerInitializer để khởi động hệ thống

Kiến trúc MVC được áp dụng nghiêm ngặt giúp code dễ bảo trì, mở rộng và phân công công việc hiệu quả.

---

*Tài liệu này được tạo dựa trên phân tích source code thực tế của dự án DUT_NET MediaVideo.*
