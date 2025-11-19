# HƯỚNG DẪN KHẮC PHỤC LỖI ENCODING TIẾNG VIỆT

## ❌ VẤN ĐỀ
Text từ Speech-to-Text bị hiển thị sai:
```
chÃ o má»«ng quÃ½ vá»‹ vÃ  cÃ¡c báº¡n...
```

Thay vì:
```
chào mừng quý vị và các bạn...
```

## 🔍 NGUYÊN NHÂN
Database cũ đã được tạo và lưu dữ liệu TRƯỚC KHI code xử lý UTF-8 đúng cách. Dữ liệu cũ đã bị lỗi encoding và không thể sửa được.

## ✅ GIẢI PHÁP ĐÃ THỰC HIỆN

### 1. Đã sửa code để xử lý UTF-8 đúng:
- ✅ `WorkerServiceVosk.java`: Xóa hàm `fixVoskEncoding()` gây lỗi
- ✅ `DBConnect.java`: Xóa parameter `CHARSET=UTF-8` không được H2 v2.x hỗ trợ
- ✅ `HistoryController.java`: Thêm set UTF-8 encoding cho request/response
- ✅ `TaskDetailController.java`: Đã có UTF-8 encoding đúng

### 2. H2 Database v2.x TỰ ĐỘNG dùng UTF-8
H2 Database phiên bản 2.x tự động sử dụng UTF-8 encoding mà KHÔNG CẦN parameter `CHARSET`.

## 📋 CÁC BƯỚC KHẮC PHỤC (QUAN TRỌNG!)

### BƯỚC 1: Dừng Tomcat Server
- Trong Eclipse: Click nút **Stop** (hình vuông đỏ) ở tab Servers
- Hoặc: Click chuột phải vào server → **Stop**

### BƯỚC 2: Xóa Database Cũ
Tìm và xóa các file database cũ:

**Cách 1: Sử dụng script tự động**
```cmd
cd /d E:\K1N3\LTM\DUT_NET-MediaVideo
reset_database_utf8.bat
```

**Cách 2: Xóa thủ công**
Mở File Explorer và xóa các file sau:
```
C:\Users\[Tên_User_Của_Bạn]\media_processor_db.mv.db
C:\Users\[Tên_User_Của_Bạn]\media_processor_db.trace.db
```

Ví dụ:
```
C:\Users\Admin\media_processor_db.mv.db
C:\Users\Admin\media_processor_db.trace.db
```

### BƯỚC 3: Khởi động lại Tomcat
- Trong Eclipse: Click nút **Start** (hình tam giác xanh) ở tab Servers
- Database mới sẽ tự động được tạo với UTF-8 đúng

### BƯỚC 4: Truy cập ứng dụng và test
1. Mở trình duyệt: `http://localhost:8080/DUT_NET-MediaVideo/`
2. Đăng nhập
3. Upload file audio/video tiếng Việt
4. Chờ xử lý xong
5. Xem kết quả tại trang **History**

## ✅ KẾT QUẢ MONG MUỐN
Sau khi làm theo các bước trên, text tiếng Việt sẽ hiển thị đúng:
```
chào mừng quý vị và các bạn đến với buổi thảo luận chuyên sâu...
```

## 🔧 NẾU VẪN BỊ LỖI

### Kiểm tra 1: Database đã bị xóa chưa?
```cmd
dir %USERPROFILE%\media_processor_db.*
```
Nếu vẫn thấy file → xóa thủ công

### Kiểm tra 2: Tomcat Console có lỗi không?
Xem tab **Console** trong Eclipse, tìm dòng:
```
✅ H2 Database Connection Pool đã được khởi tạo
```

### Kiểm tra 3: Browser encoding
- Nhấn F12 trong Chrome/Edge
- Tab **Console**, gõ: `document.characterSet`
- Phải hiển thị: `UTF-8`

### Kiểm tra 4: JSP encoding
Mở file `history.jsp`, dòng đầu tiên phải có:
```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
```

## 📝 LƯU Ý QUAN TRỌNG

### ⚠️ KHÔNG được:
- ❌ Thêm `CHARSET=UTF-8` vào H2 connection string (gây lỗi)
- ❌ Dùng hàm `fixVoskEncoding()` (gây double encoding)
- ❌ Convert encoding thủ công bằng `new String(bytes, "UTF-8")`

### ✅ NÊN:
- ✅ Để H2 v2.x tự động xử lý UTF-8
- ✅ Set `response.setCharacterEncoding("UTF-8")` ở servlet
- ✅ Dùng `<%@ page contentType="..." pageEncoding="UTF-8"%>` ở JSP
- ✅ Xóa database cũ khi có vấn đề encoding

## 🎯 NGUYÊN TẮC UTF-8 TRONG DỰ ÁN

### Luồng dữ liệu:
```
Vosk (UTF-8) 
  → Java String (UTF-8) 
  → JDBC PreparedStatement (UTF-8) 
  → H2 Database (UTF-8) 
  → ResultSet.getString() (UTF-8) 
  → Servlet response (UTF-8) 
  → Browser (UTF-8)
```

Tất cả các khâu đã được xử lý đúng UTF-8. Vấn đề chỉ là **database cũ có dữ liệu lỗi**.

## 📞 HỖ TRỢ
Nếu vẫn gặp vấn đề, kiểm tra:
1. Console log trong Tomcat
2. Browser Console (F12)
3. H2 Console: `http://localhost:8082` (nếu đã bật)

---
**Ngày cập nhật**: 19/11/2025
**Trạng thái**: Code đã được sửa xong, chỉ cần xóa database cũ
