# 🚀 HƯỚNG DẪN CÀI ĐẶT SAU TỐI ƯU HÓA

## ✅ KHÔNG CẦN CÀI ĐẶT GÌ THÊM!

Dự án đã được tối ưu với **Basic Connection Pool tự build** (không cần thư viện ngoài).

---

## Bước 1: Clean và Build lại project

```bash
# Maven
mvn clean install

# Gradle
gradle clean build

# Eclipse
Project → Clean → Clean all projects → OK
```

---

## Bước 2: Restart Server

1. Stop Tomcat server
2. Start lại
3. Kiểm tra console logs để thấy:
   - ✅ Connection Pool đã được khởi tạo (3 connections ban đầu, max 10)
   - ✅ Vosk models được load 1 lần
   - ✅ Worker-1 và Worker-2 khởi động

---

## Bước 3: Kiểm tra logs

Bạn sẽ thấy logs như sau:
```
========================================
🚀 Server đang khởi động...
========================================
✅ Connection Pool đã được khởi tạo
   - Initial Pool Size: 3 connections
   - Max Pool Size: 10 connections
✓ Connection Pool: Pool Stats - Available: 3, Used: 0, Total: 3
🚀 Đang load Vosk models...
✅ Model tiếng Việt đã được load: E:\K1N3\LTM\DUT_NET-MediaVideo\models\vosk-model-vn-0.4
✅ Model tiếng Anh đã được load: E:\K1N3\LTM\DUT_NET-MediaVideo\models\vosk-model-small-en-us-0.15
✓ Vosk Models - Vietnamese: ✓, English: ✓, Initialized: ✓
[Worker-1] ✅ Worker đã được khởi tạo (sử dụng shared Vosk models)
[Worker-2] ✅ Worker đã được khởi tạo (sử dụng shared Vosk models)
✓ Worker-1 và Worker-2 đã được khởi động
✓ Hệ thống sẵn sàng xử lý tasks
========================================
```

---

## Kiểm tra tối ưu có hoạt động

### Test 1: Connection Pool
Upload nhiều file cùng lúc → Không bị crash

### Test 2: Memory Usage
Kiểm tra Task Manager:
- Trước: ~1GB RAM (mỗi Worker load riêng model)
- Sau: ~500MB RAM (share model)

### Test 3: Cache
Refresh trang task detail nhiều lần → Chỉ query DB 1 lần trong 30s

---

## Nếu gặp lỗi

### Lỗi: Model không load được
→ Kiểm tra đường dẫn thư mục `models/`

### Lỗi: Connection pool đã đầy
→ Tăng MAX_POOL_SIZE trong DBConnect.java (hiện tại: 10)

### Lỗi khác
→ Gửi log chi tiết để được hỗ trợ
