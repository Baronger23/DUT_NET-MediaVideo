# 🗄️ HƯỚNG DẪN CÀI ĐẶT H2 DATABASE

## 📌 Tại sao chọn H2 Database?

H2 là một database nhúng (embedded) rất phù hợp cho việc demo và hướng dẫn:
- ✅ **Không cần cài đặt** - Chỉ cần thêm file JAR
- ✅ **Dễ sử dụng** - Tự động tạo database file
- ✅ **Có Web Console** - Quản lý database qua trình duyệt
- ✅ **Tương thích MySQL** - Syntax gần giống MySQL
- ✅ **Nhẹ và nhanh** - Phù hợp cho development và testing

---

## 🚀 BƯỚC 1: Chuẩn bị

### 1.1. Kiểm tra file JAR đã có
File `h2-2.2.224.jar` đã được đặt tại:
```
src/main/webapp/WEB-INF/lib/h2-2.2.224.jar
```

### 1.2. Đảm bảo DBConnect.java đã được cập nhật
File `Model/DAO/DBConnect.java` đã được cấu hình để sử dụng H2:
```java
private static final String DB_URL = "jdbc:h2:~/media_processor_db;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;MODE=MySQL";
private static final String DB_USERNAME = "sa";
private static final String DB_PASSWORD = "";
```

**Giải thích:**
- `jdbc:h2:~/media_processor_db` - Database file sẽ được tạo trong thư mục home của user
- `AUTO_SERVER=TRUE` - Cho phép nhiều connections cùng lúc
- `DB_CLOSE_DELAY=-1` - Database không tự đóng khi connection cuối cùng đóng
- `MODE=MySQL` - Sử dụng syntax tương thích MySQL

---

## 🗃️ BƯỚC 2: Khởi tạo Database

### 2.1. Chạy SQL Script

**Cách 1: Sử dụng H2 Console (Khuyến nghị)**

1. Khởi động H2 Console:
```bash
java -cp src/main/webapp/WEB-INF/lib/h2-2.2.224.jar org.h2.tools.Server -web -webPort 8082
```

2. Mở trình duyệt tại: `http://localhost:8082`

3. Cấu hình kết nối:
   - **JDBC URL:** `jdbc:h2:~/media_processor_db`
   - **User Name:** `sa`
   - **Password:** (để trống)

4. Click "Connect", sau đó copy nội dung file `H2_DATABASE_INIT.sql` và chạy

**Cách 2: Chạy từ Command Line**
```bash
java -cp src/main/webapp/WEB-INF/lib/h2-2.2.224.jar org.h2.tools.RunScript -url jdbc:h2:~/media_processor_db -user sa -script H2_DATABASE_INIT.sql
```

**Cách 3: Tự động khởi tạo khi chạy ứng dụng**

Database sẽ tự động được tạo khi ứng dụng chạy lần đầu. Tuy nhiên, bạn cần import dữ liệu mẫu thủ công.

---

## 🔍 BƯỚC 3: Kiểm tra Database

### 3.1. Sử dụng H2 Console

1. Khởi động H2 Console (nếu chưa chạy):
```bash
java -cp src/main/webapp/WEB-INF/lib/h2-2.2.224.jar org.h2.tools.Server -web -webPort 8082
```

2. Truy cập: `http://localhost:8082`

3. Thử các câu lệnh SQL:
```sql
-- Xem danh sách user
SELECT * FROM user;

-- Xem danh sách task
SELECT * FROM Task;

-- Xem task theo ngôn ngữ
SELECT * FROM Task WHERE language = 'vi';
SELECT * FROM Task WHERE language = 'en';

-- Xem task của một user
SELECT t.*, u.username 
FROM Task t 
JOIN user u ON t.user_id = u.id 
WHERE u.username = 'admin';
```

---

## 🚀 BƯỚC 4: Chạy ứng dụng

### 4.1. Build và Deploy

1. **Clean và build project:**
   - Right-click project → Clean
   - Right-click project → Build Project

2. **Chạy trên Tomcat:**
   - Right-click project → Run As → Run on Server
   - Chọn Tomcat v10.1 Server

3. **Truy cập ứng dụng:**
   ```
   http://localhost:8080/DUT_NET-MediaVideo/
   ```

### 4.2. Test đăng nhập

Sử dụng tài khoản demo:
- **Username:** `admin` / **Password:** `admin123`
- **Username:** `demo` / **Password:** `demo123`

---

## 📁 Vị trí Database File

Database file sẽ được tạo tại:
- **Windows:** `C:\Users\YourUsername\media_processor_db.mv.db`
- **Linux/Mac:** `~/media_processor_db.mv.db`

Bạn có thể backup file này để sao lưu toàn bộ dữ liệu.

---

## 🔧 Cấu hình Nâng cao

### Thay đổi vị trí Database

Nếu muốn đặt database ở thư mục khác, sửa trong `DBConnect.java`:

```java
// Đặt trong project folder
private static final String DB_URL = "jdbc:h2:./data/media_processor_db;...";

// Đặt tại đường dẫn tuyệt đối
private static final String DB_URL = "jdbc:h2:E:/databases/media_processor_db;...";
```

### Bật H2 Console trong ứng dụng

Thêm vào `web.xml`:
```xml
<servlet>
    <servlet-name>H2Console</servlet-name>
    <servlet-class>org.h2.server.web.WebServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>H2Console</servlet-name>
    <url-pattern>/console/*</url-pattern>
</servlet-mapping>
```

Sau đó truy cập: `http://localhost:8080/DUT_NET-MediaVideo/console`

---

## 🆚 So sánh với MySQL

| Tính năng | MySQL | H2 |
|-----------|-------|-----|
| Cài đặt | Cần cài MySQL Server | Không cần, chỉ cần JAR |
| Cấu hình | Phải tạo database, user | Tự động tạo |
| Quản lý | phpMyAdmin/MySQL Workbench | H2 Console (built-in) |
| Phù hợp | Production | Development/Demo |
| Tốc độ | Nhanh với dữ liệu lớn | Rất nhanh với dữ liệu nhỏ |

---

## ⚠️ Lưu ý quan trọng

1. **Backup dữ liệu:**
   ```bash
   # Export to SQL
   java -cp h2-2.2.224.jar org.h2.tools.Script -url jdbc:h2:~/media_processor_db -user sa -script backup.sql
   
   # Import from SQL
   java -cp h2-2.2.224.jar org.h2.tools.RunScript -url jdbc:h2:~/media_processor_db -user sa -script backup.sql
   ```

2. **Khi chuyển sang Production:**
   - H2 chỉ phù hợp cho demo và development
   - Với production, nên chuyển sang MySQL, PostgreSQL hoặc database chuyên nghiệp khác
   - Chỉ cần thay đổi `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` trong `DBConnect.java`

3. **Xử lý lỗi:**
   - Nếu database bị corrupt: Xóa file `.mv.db` và chạy lại script khởi tạo
   - Nếu không kết nối được: Kiểm tra đường dẫn và quyền ghi file

---

## 🎯 Kết luận

Với H2 Database, việc cài đặt và demo ứng dụng trở nên cực kỳ đơn giản:
- ✅ Không cần cài đặt MySQL Server
- ✅ Không cần cấu hình phức tạp
- ✅ Dễ dàng backup và restore
- ✅ Phù hợp cho môi trường học tập và demo

**Chúc bạn thành công! 🚀**
