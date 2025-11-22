# HƯỚNG DẪN CÀI ĐẶT VÀ SỬ DỤNG CHƯƠNG TRÌNH

## MỤC LỤC

1. [Hướng dẫn cài đặt Tomcat](#1-hướng-dẫn-cài-đặt-tomcat)
2. [Hướng dẫn cài đặt Database](#2-hướng-dẫn-cài-đặt-database)
3. [Hướng dẫn cài đặt ứng dụng](#3-hướng-dẫn-cài-đặt-ứng-dụng)
4. [Hướng dẫn sử dụng chương trình](#4-hướng-dẫn-sử-dụng-chương-trình)
5. [Xử lý sự cố](#5-xử-lý-sự-cố)

---

## 1. HƯỚNG DẪN CÀI ĐẶT TOMCAT

### 1.1. Yêu cầu hệ thống

| Thành phần | Yêu cầu tối thiểu | Khuyến nghị |
|------------|-------------------|-------------|
| **Hệ điều hành** | Windows 7+, macOS 10.12+, Ubuntu 18.04+ | Windows 10/11, Ubuntu 20.04+ |
| **RAM** | 2GB | 4GB hoặc cao hơn |
| **Ổ cứng trống** | 2GB | 5GB |
| **Java** | JDK 11 | JDK 17 |

### 1.2. Cài đặt Java JDK

#### Windows:

**Bước 1:** Download JDK 17
- Truy cập: https://www.oracle.com/java/technologies/downloads/
- Chọn: Windows → x64 Installer
- Download file: `jdk-17_windows-x64_bin.exe`

**Bước 2:** Cài đặt JDK
1. Double-click file `.exe` vừa download
2. Click "Next" → Chọn thư mục cài đặt (mặc định: `C:\Program Files\Java\jdk-17`)
3. Click "Next" → "Close"

**Bước 3:** Thiết lập biến môi trường

1. Mở "System Properties":
   - Right-click "This PC" → Properties
   - Click "Advanced system settings"
   - Click "Environment Variables"

2. Thêm JAVA_HOME:
   - Trong "System variables", click "New"
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-17`
   - Click "OK"

3. Cập nhật PATH:
   - Tìm biến "Path" trong "System variables"
   - Click "Edit" → "New"
   - Thêm: `%JAVA_HOME%\bin`
   - Click "OK"

**Bước 4:** Kiểm tra cài đặt

Mở Command Prompt và chạy:

```bash
java -version
```

Kết quả mong đợi:
```
java version "17.0.x" 2023-xx-xx LTS
Java(TM) SE Runtime Environment (build 17.0.x+x-LTS-xxx)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.x+x-LTS-xxx, mixed mode, sharing)
```

#### Ubuntu/Linux:

```bash
# Update package list
sudo apt update

# Cài đặt OpenJDK 17
sudo apt install openjdk-17-jdk -y

# Kiểm tra
java -version
```

### 1.3. Cài đặt Apache Tomcat 10.1

#### Windows:

**Bước 1:** Download Tomcat
- Truy cập: https://tomcat.apache.org/download-10.cgi
- Chọn: "32-bit/64-bit Windows Service Installer"
- Download file: `apache-tomcat-10.1.xx.exe`

**Bước 2:** Cài đặt Tomcat

1. Double-click file `.exe`
2. Click "Next" tại Welcome screen
3. Click "I Agree" cho License
4. Chọn các components:
   - ☑ Tomcat
   - ☑ Service Startup
   - ☑ Native (Optional)
5. Click "Next"

6. Cấu hình:
   - HTTP/1.1 Connector Port: `8080` (mặc định)
   - Server Shutdown Port: `8005` (mặc định)
   - Administrator login: `admin`
   - Password: `admin123` (ghi nhớ password này)
7. Click "Next"

8. Chọn thư mục cài đặt:
   - Mặc định: `C:\Program Files\Apache Software Foundation\Tomcat 10.1`
9. Click "Install"

10. Click "Finish"

**Bước 3:** Khởi động Tomcat

**Cách 1: Dùng Services**
1. Mở "Services" (Win + R → `services.msc`)
2. Tìm "Apache Tomcat 10.1 Tomcat10"
3. Right-click → "Start"

**Cách 2: Dùng Command Line**
```bash
cd "C:\Program Files\Apache Software Foundation\Tomcat 10.1\bin"
startup.bat
```

**Bước 4:** Kiểm tra Tomcat

Mở trình duyệt và truy cập:
```
http://localhost:8080
```

Nếu thấy trang "Apache Tomcat" → Cài đặt thành công! ✅

#### Ubuntu/Linux:

```bash
# Download Tomcat
cd /opt
sudo wget https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.18/bin/apache-tomcat-10.1.18.tar.gz

# Giải nén
sudo tar -xvzf apache-tomcat-10.1.18.tar.gz
sudo mv apache-tomcat-10.1.18 tomcat

# Cấp quyền
sudo chmod +x /opt/tomcat/bin/*.sh

# Khởi động
sudo /opt/tomcat/bin/startup.sh

# Kiểm tra
curl http://localhost:8080
```

### 1.4. Cấu hình Tomcat cho Eclipse (Nếu dùng Eclipse)

**Bước 1:** Thêm Tomcat Server vào Eclipse

1. Mở Eclipse
2. Vào Window → Show View → Servers
3. Trong tab "Servers", click "No servers are available. Click this link to create a new server..."
4. Chọn:
   - Server type: Apache → Tomcat v10.1 Server
   - Tomcat installation directory: Chọn thư mục đã cài Tomcat
   - Click "Next"
5. Click "Finish"

**Bước 2:** Cấu hình Server

1. Double-click "Tomcat v10.1 Server" trong tab Servers
2. Trong phần "Server Locations", chọn:
   - ☑ Use Tomcat installation (takes control of Tomcat installation)
3. Trong phần "Server Options":
   - ☑ Publish module contexts to separate XML files
4. Save (Ctrl+S)

---

## 2. HƯỚNG DẪN CÀI ĐẶT DATABASE

Dự án hỗ trợ 2 loại database: **MySQL** (khuyến nghị cho production) và **H2** (dùng cho testing).

### 2.1. Cài đặt MySQL

#### Windows:

**Bước 1:** Download MySQL

- Truy cập: https://dev.mysql.com/downloads/installer/
- Chọn: "Windows (x86, 32-bit), MSI Installer"
- Download file: `mysql-installer-community-8.0.xx.msi`

**Bước 2:** Cài đặt MySQL

1. Double-click file `.msi`
2. Chọn Setup Type: "Developer Default"
3. Click "Next" → "Execute" để download các components
4. Sau khi download xong, click "Next"

5. **MySQL Server Configuration:**
   - Config Type: Development Computer
   - Connectivity:
     - Port: `3306` (mặc định)
     - ☑ Open Windows Firewall ports for network access
   - Authentication Method: Use Strong Password Encryption (Recommended)
   - Root Password: `root123` (ghi nhớ password)
   - Click "Next"

6. Click "Execute" → "Finish" → "Next" → "Finish"

**Bước 3:** Kiểm tra MySQL

Mở Command Prompt:

```bash
mysql -u root -p
# Nhập password: root123
```

Nếu vào được MySQL prompt → Cài đặt thành công! ✅

```sql
mysql> SHOW DATABASES;
mysql> EXIT;
```

#### Ubuntu/Linux:

```bash
# Cài đặt MySQL
sudo apt update
sudo apt install mysql-server -y

# Khởi động MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# Cấu hình bảo mật
sudo mysql_secure_installation

# Đăng nhập
sudo mysql -u root -p
```

### 2.2. Tạo Database và Tables

**Bước 1:** Tạo Database

```sql
-- Mở MySQL Command Line hoặc MySQL Workbench

-- Tạo database
CREATE DATABASE IF NOT EXISTS media_processor_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Sử dụng database
USE media_processor_db;
```

**Bước 2:** Tạo bảng User

```sql
CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Bước 3:** Tạo bảng Task

```sql
CREATE TABLE IF NOT EXISTS Task (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    server_file_path TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    language VARCHAR(10) DEFAULT 'vi',
    submission_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_time TIMESTAMP NULL,
    result_text TEXT,
    processing_time_ms INT,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_status (user_id, status),
    INDEX idx_submission_time (submission_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Bước 4:** Tạo user test

```sql
-- Tạo user test để login
INSERT INTO user (username, password, email) 
VALUES ('testuser', 'pass123', 'test@example.com');

-- Kiểm tra
SELECT * FROM user;
```

**Bước 5:** Kiểm tra tables

```sql
SHOW TABLES;

-- Kết quả mong đợi:
-- +-------------------------------+
-- | Tables_in_media_processor_db  |
-- +-------------------------------+
-- | Task                          |
-- | user                          |
-- +-------------------------------+
```

### 2.3. Cấu hình kết nối Database trong code

Mở file `src/main/java/Model/DAO/DBConnect.java` và cập nhật:

```java
// Thông tin kết nối MySQL
private static final String URL = "jdbc:mysql://localhost:3306/media_processor_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
private static final String USER = "root";
private static final String PASSWORD = "root123";  // Password bạn đã đặt
```

### 2.4. (Tùy chọn) Sử dụng H2 Database

Nếu không muốn cài MySQL, có thể dùng H2 (embedded database):

**Bước 1:** File SQL đã có sẵn trong project:
- `H2_DATABASE_INIT.sql`

**Bước 2:** Cấu hình trong `DBConnect.java`:

```java
private static final String URL = "jdbc:h2:./database/media_processor;AUTO_SERVER=TRUE";
private static final String USER = "sa";
private static final String PASSWORD = "";
```

**Bước 3:** Chạy script khởi tạo:

```bash
# Windows
init_database.bat

# Linux
./init_database.sh
```

---

## 3. HƯỚNG DẪN CÀI ĐẶT ỨNG DỤNG

### 3.1. Tải source code

**Cách 1: Clone từ GitHub**

```bash
git clone https://github.com/Baronger23/DUT_NET-MediaVideo.git
cd DUT_NET-MediaVideo
```

**Cách 2: Download ZIP**

1. Truy cập: https://github.com/Baronger23/DUT_NET-MediaVideo
2. Click "Code" → "Download ZIP"
3. Giải nén vào thư mục làm việc

### 3.2. Import project vào Eclipse

**Bước 1:** Mở Eclipse

**Bước 2:** Import project

1. File → Import
2. Chọn: General → Existing Projects into Workspace
3. Click "Next"
4. Select root directory: Chọn thư mục `DUT_NET-MediaVideo`
5. Click "Finish"

**Bước 3:** Cấu hình Build Path

1. Right-click project → Properties
2. Chọn Java Build Path → Libraries
3. Nếu thiếu JRE:
   - Click "Add Library" → JRE System Library
   - Chọn JDK 17
   - Click "Finish"

### 3.3. Cài đặt Vosk Models (Bắt buộc)

**Bước 1:** Tạo thư mục models

```bash
# Trong thư mục project
mkdir models
cd models
```

**Bước 2:** Download models

**Model tiếng Việt:**
- URL: https://alphacephei.com/vosk/models/vosk-model-vn-0.4.zip
- Size: ~130MB
- Giải nén vào: `models/vosk-model-vn-0.4/`

**Model tiếng Anh:**
- URL: https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip
- Size: ~40MB
- Giải nén vào: `models/vosk-model-small-en-us-0.15/`

**Bước 3:** Kiểm tra cấu trúc thư mục

```
DUT_NET-MediaVideo/
├── models/
│   ├── vosk-model-vn-0.4/
│   │   ├── am/
│   │   ├── conf/
│   │   ├── graph/
│   │   ├── ivector/
│   │   └── README
│   └── vosk-model-small-en-us-0.15/
│       ├── am/
│       ├── conf/
│       ├── graph/
│       └── README
├── src/
├── build/
└── ...
```

### 3.4. Build và Deploy

#### Trong Eclipse:

**Bước 1:** Clean project

1. Project → Clean
2. Chọn project DUT_NET-MediaVideo
3. Click "OK"

**Bước 2:** Deploy vào Tomcat

1. Right-click project → Run As → Run on Server
2. Chọn "Tomcat v10.1 Server at localhost"
3. Click "Finish"

**Bước 3:** Kiểm tra Console

Quan sát Eclipse Console, bạn sẽ thấy:

```
========================================
🚀 Server đang khởi động...
========================================
✅ Connection Pool đã được khởi tạo
   - Initial Pool Size: 3 connections
   - Max Pool Size: 10 connections
🚀 Đang load Vosk models...
✅ Model tiếng Việt đã được load: .../models/vosk-model-vn-0.4
✅ Model tiếng Anh đã được load: .../models/vosk-model-small-en-us-0.15
[Worker-1] ✅ Worker đã được khởi tạo
[Worker-2] ✅ Worker đã được khởi tạo
✓ Worker-1 và Worker-2 đã được khởi động
✓ Hệ thống sẵn sàng xử lý tasks
========================================
```

**Bước 4:** Truy cập ứng dụng

Mở trình duyệt:
```
http://localhost:8080/DUT_NET-MediaVideo/
```

#### Sử dụng Command Line:

```bash
# Build với Maven (nếu có)
mvn clean package

# Copy WAR file vào Tomcat
cp target/DUT_NET-MediaVideo.war /opt/tomcat/webapps/

# Restart Tomcat
sudo /opt/tomcat/bin/shutdown.sh
sudo /opt/tomcat/bin/startup.sh
```

---

## 4. HƯỚNG DẪN SỬ DỤNG CHƯƠNG TRÌNH

### 4.1. Đăng ký tài khoản

**Bước 1:** Truy cập trang đăng ký

URL: `http://localhost:8080/DUT_NET-MediaVideo/register`

**Bước 2:** Điền thông tin

```
Username: testuser123
Password: pass123456  (tối thiểu 6 ký tự)
Email: test@example.com
```

**Bước 3:** Click nút "Đăng ký"

**Kết quả:**
- ✅ Thành công → Chuyển đến trang login
- ❌ Username đã tồn tại → Chọn username khác

---

### 4.2. Đăng nhập

**Bước 1:** Truy cập trang login

URL: `http://localhost:8080/DUT_NET-MediaVideo/login`

**Bước 2:** Nhập thông tin

```
Username: testuser123
Password: pass123456
```

**Bước 3:** Click nút "Đăng nhập"

**Kết quả:**
- ✅ Thành công → Chuyển đến trang Home
- ❌ Sai thông tin → Hiển thị lỗi "Sai username hoặc password"

---

### 4.3. Upload file và xử lý Speech-to-Text

**Bước 1:** Truy cập trang Upload

Sau khi đăng nhập, click menu "Upload" hoặc truy cập:
```
http://localhost:8080/DUT_NET-MediaVideo/upload
```

**Bước 2:** Chuẩn bị file test

Hệ thống hỗ trợ:
- **Audio formats:** MP3, WAV, M4A, OGG
- **Video formats:** MP4, AVI, MKV, MOV
- **Kích thước tối đa:** 100MB
- **Độ dài:** Không giới hạn (nhưng khuyến nghị < 10 phút)

**Bước 3:** Upload file

1. Click nút "Chọn File"
2. Chọn file audio hoặc video từ máy tính
3. Chọn ngôn ngữ:
   - 🇻🇳 **Tiếng Việt (Vietnamese)** - Dùng cho file tiếng Việt
   - 🇺🇸 **Tiếng Anh (English)** - Dùng cho file tiếng Anh
4. Click nút "🚀 Upload và Xử lý"

**Bước 4:** Chờ xử lý

Sau khi upload:
- File được lưu vào server
- Task được tạo với status **"⏳ Đang chờ"**
- Hệ thống tự động chuyển đến trang Lịch sử

**Thời gian xử lý:** (tham khảo)
- File 1 phút: ~15-25 giây
- File 2 phút: ~30-50 giây
- File 5 phút: ~75-130 giây

**Trạng thái task:**
- **⏳ Đang chờ** - Task trong queue, chờ Worker
- **🔄 Đang xử lý** - Worker đang thực hiện Speech-to-Text
- **✅ Hoàn thành** - Đã có kết quả
- **❌ Thất bại** - Có lỗi xảy ra

---

### 4.4. Xem lịch sử tasks

**Bước 1:** Truy cập trang Lịch sử

Click menu "Lịch sử" hoặc:
```
http://localhost:8080/DUT_NET-MediaVideo/history
```

**Bước 2:** Xem danh sách tasks

Bảng hiển thị:
- **ID**: Task ID
- **Tên file**: File gốc đã upload
- **Trạng thái**: ⏳/🔄/✅/❌
- **Ngôn ngữ**: 🇻🇳 hoặc 🇺🇸
- **Thời gian upload**: Ngày giờ upload
- **Hành động**: Nút "👁️ Xem" (chỉ với tasks đã hoàn thành)

**Bước 3:** Refresh để cập nhật trạng thái

- Nhấn F5 hoặc click "Làm mới" trên trình duyệt
- Status sẽ tự động cập nhật

---

### 4.5. Xem chi tiết kết quả

**Bước 1:** Click nút "👁️ Xem"

Tại trang Lịch sử, click nút "Xem" trên task đã hoàn thành

**Bước 2:** Xem thông tin chi tiết

Modal/Trang hiển thị:

```
==========================================
CHI TIẾT TASK #123
==========================================

📁 File: bai_giang_ltm.mp3
🌐 Ngôn ngữ: Tiếng Việt
📅 Thời gian upload: 2024-11-22 14:30:00
✅ Thời gian hoàn thành: 2024-11-22 14:31:15
⏱️ Thời gian xử lý: 75.5 giây

📝 KẾT QUẢ SPEECH-TO-TEXT:
─────────────────────────────────────────
[Văn bản trích xuất từ audio...]

chào mừng các bạn đến với bài giảng lập 
trình mạng hôm nay chúng ta sẽ tìm hiểu 
về mô hình client server...
─────────────────────────────────────────

[Nút: 📥 Tải xuống TXT] [Nút: ← Quay lại]
```

**Bước 3:** Tải xuống kết quả (Optional)

Click nút "📥 Tải xuống TXT" để download file text:
- File name: `task_123_result.txt`
- Encoding: UTF-8
- Format: Plain text với metadata

---

### 4.6. Đăng xuất

**Cách 1:** Click menu "Đăng xuất"

**Cách 2:** Truy cập:
```
http://localhost:8080/DUT_NET-MediaVideo/logout
```

Session sẽ bị hủy và redirect về trang login.

---

## 5. XỬ LÝ SỰ CỐ

### 5.1. Lỗi khi khởi động server

**Lỗi:** "Port 8080 already in use"

**Nguyên nhân:** Port 8080 đang được sử dụng bởi process khác

**Giải pháp:**

```bash
# Windows - Tìm và kill process
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux
sudo lsof -i :8080
sudo kill -9 <PID>

# Hoặc đổi port trong Tomcat
# Mở: conf/server.xml
# Tìm: <Connector port="8080" ...
# Đổi thành: <Connector port="8081" ...
```

---

### 5.2. Lỗi "Model không load được"

**Lỗi trong console:**
```
❌ Không tìm thấy model tiếng Việt: .../models/vosk-model-vn-0.4
```

**Nguyên nhân:** 
- Chưa download models
- Đường dẫn sai
- Cấu trúc thư mục không đúng

**Giải pháp:**

1. Kiểm tra thư mục models có tồn tại không:
```bash
ls models/
# Phải thấy: vosk-model-vn-0.4, vosk-model-small-en-us-0.15
```

2. Kiểm tra cấu trúc bên trong:
```bash
ls models/vosk-model-vn-0.4/
# Phải thấy: am, conf, graph, ivector, README
```

3. Nếu chưa có, download lại:
   - Tiếng Việt: https://alphacephei.com/vosk/models/vosk-model-vn-0.4.zip
   - Tiếng Anh: https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip

---

### 5.3. Lỗi kết nối Database

**Lỗi:** "Connection refused" hoặc "Access denied for user 'root'@'localhost'"

**Giải pháp:**

1. Kiểm tra MySQL đang chạy:
```bash
# Windows - Services
services.msc → MySQL80 → Start

# Linux
sudo systemctl status mysql
sudo systemctl start mysql
```

2. Kiểm tra thông tin đăng nhập trong `DBConnect.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/media_processor_db";
private static final String USER = "root";
private static final String PASSWORD = "root123";  // Đúng password?
```

3. Test kết nối bằng MySQL Command Line:
```bash
mysql -u root -p
# Nhập password và kiểm tra
```

---

### 5.4. Lỗi "File quá lớn"

**Lỗi:** "The field mediaFile exceeds its maximum permitted size of 104857600 bytes"

**Nguyên nhân:** File vượt quá 100MB

**Giải pháp:**

**Cách 1:** Nén file xuống dưới 100MB

**Cách 2:** Tăng giới hạn file size

Mở `MediaController.java`, sửa annotation:

```java
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 200,      // Tăng lên 200MB
    maxRequestSize = 1024 * 1024 * 250    // 250MB
)
```

---

### 5.5. Lỗi "Connection Pool đã đầy"

**Lỗi trong console:** "Connection pool đã đầy!"

**Nguyên nhân:** Quá nhiều requests đồng thời

**Giải pháp:**

Tăng MAX_POOL_SIZE trong `DBConnect.java`:

```java
private static final int MAX_POOL_SIZE = 20;  // Tăng từ 10 lên 20
```

Sau đó rebuild và restart server.

---

### 5.6. Lỗi "Task bị FAILED"

**Các nguyên nhân phổ biến:**

1. **File audio bị lỗi hoặc corrupt**
   - Giải pháp: Upload file khác

2. **Format không hỗ trợ**
   - Giải pháp: Convert sang MP3 hoặc WAV

3. **File video không extract được audio**
   - Nguyên nhân: Chưa cài FFmpeg
   - Giải pháp: Cài FFmpeg (xem `FFMPEG_SETUP.md`)

4. **Worker bị crash**
   - Kiểm tra console logs để biết lỗi cụ thể
   - Restart server

---

### 5.7. Độ chính xác thấp

**Vấn đề:** Kết quả Speech-to-Text không chính xác

**Các yếu tố ảnh hưởng:**

1. **Chất lượng audio kém**
   - Giải pháp: Dùng file audio chất lượng cao hơn (WAV lossless)

2. **Nhiễu nền, echo**
   - Giải pháp: Dùng phần mềm loại nhiễu (Audacity)

3. **Giọng địa phương mạnh**
   - Giải pháo: Không có, Vosk model không hỗ trợ tốt

4. **Chọn sai ngôn ngữ**
   - Giải pháp: Chọn đúng ngôn ngữ khi upload

5. **Nhiều người nói xen kẽ**
   - Vosk không hỗ trợ tốt trường hợp này

**Khuyến nghị:**
- Dùng file audio rõ ràng, 1 người nói
- Tốc độ nói vừa phải
- Format WAV 16kHz mono (tốt nhất)

---

## 6. CÂU HỎI THƯỜNG GẶP (FAQ)

### Q1: Tôi có thể upload video không?

**A:** Có! Hệ thống hỗ trợ video (MP4, AVI, MKV). Audio sẽ được extract tự động.

**Lưu ý:** Cần cài FFmpeg để extract audio từ video. Xem `FFMPEG_SETUP.md`.

---

### Q2: Hệ thống có hỗ trợ tiếng Việt miền Nam/Bắc không?

**A:** Vosk model hỗ trợ cả 3 miền nhưng độ chính xác giảm nếu giọng địa phương quá mạnh.

Độ chính xác:
- Giọng chuẩn: ~85-90%
- Giọng miền (nhẹ): ~80-85%
- Giọng miền (nặng): ~70-75%

---

### Q3: Tôi có thể thêm ngôn ngữ khác không?

**A:** Có! Xem hướng dẫn trong `HUONG_DAN_HO_TRO_NHIEU_NGON_NGU.md`.

Các ngôn ngữ Vosk hỗ trợ:
- Tiếng Trung, Nhật, Hàn, Nga, Pháp, Đức, Tây Ban Nha...
- Xem đầy đủ: https://alphacephei.com/vosk/models

---

### Q4: Xử lý mất bao lâu?

**A:** Trung bình ~25-40% thời lượng audio.

Ví dụ:
- File 1 phút → ~15-25 giây
- File 5 phút → ~75-130 giây
- File 10 phút → ~150-240 giây

Phụ thuộc vào:
- Cấu hình máy (CPU)
- Format file (WAV nhanh hơn MP3)
- Video hay audio (video chậm hơn do cần extract)

---

### Q5: Hệ thống có lưu file của tôi không?

**A:** Có. File được lưu trong thư mục `uploads/` trên server.

Nếu muốn xóa:
1. Admin có thể xóa file trong `uploads/`
2. Hoặc thêm chức năng "Xóa task" (chưa implement)

---

### Q6: Tôi có thể chạy trên máy chủ online không?

**A:** Có! Deploy lên VPS/Cloud server:

1. Cài Tomcat + MySQL trên server
2. Copy project lên
3. Cấu hình firewall mở port 8080
4. Đổi `localhost` thành IP public/domain

**Lưu ý:** 
- Cần RAM tối thiểu 2GB
- Vosk models chiếm ~500MB RAM

---

### Q7: Có giới hạn số lượng file upload không?

**A:** Không giới hạn số lượng.

Giới hạn đồng thời:
- Có 2 Workers → Xử lý đồng thời 2 tasks
- Tasks còn lại chờ trong queue
- Có thể tăng số Workers nếu cần

---

### Q8: Kết quả có hỗ trợ dấu câu không?

**A:** Không. Vosk không tự động thêm dấu câu (punctuation).

Nếu cần dấu câu, có thể:
- Dùng thêm punctuation restoration model (advanced)
- Hoặc dùng Google Cloud STT (có punctuation)

---

## 7. LIÊN HỆ VÀ HỖ TRỢ

Nếu gặp vấn đề không thể tự giải quyết:

1. Kiểm tra lại các bước cài đặt
2. Xem logs trong Eclipse Console
3. Kiểm tra file `OPTIMIZATION_REPORT.md` để hiểu cách hệ thống hoạt động
4. Tham khảo các file SETUP trong project:
   - `FFMPEG_SETUP.md`
   - `VOSK_INSTALLATION_GUIDE.md`
   - `H2_DATABASE_SETUP.md`
   - `RABBITMQ_SETUP.md`

---

*Hướng dẫn được tạo cho dự án DUT_NET MediaVideo - Speech-to-Text System*
