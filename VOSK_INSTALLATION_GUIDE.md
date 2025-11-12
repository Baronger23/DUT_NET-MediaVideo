# Hướng Dẫn Thêm Vosk vào Project (Eclipse)

## ✅ Bạn đã hoàn thành:
- [x] Tạo thư mục `models/`
- [x] Giải nén model vào `models/vosk-model-vn-0.4/`
- [x] Cập nhật đường dẫn model trong code

## 📦 Bước Tiếp Theo: Thêm Thư Viện Vosk

### Bước 1: Tải Vosk JAR

**Download 2 file JAR:**

1. **vosk-0.3.45.jar** (thư viện chính)
   - Link: https://repo1.maven.org/maven2/com/alphacephei/vosk/0.3.45/vosk-0.3.45.jar
   - Hoặc: https://github.com/alphacep/vosk-api/releases

2. **jna-5.13.0.jar** (dependency của Vosk)
   - Link: https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar

### Bước 2: Copy JAR vào Project

```
Copy 2 file JAR vào thư mục:
E:\K1N3\LTM\DUT_NET-MediaVideo\src\main\webapp\WEB-INF\lib\

Kết quả:
WEB-INF\lib\
├── vosk-0.3.45.jar
└── jna-5.13.0.jar
```

### Bước 3: Refresh Project trong Eclipse

```
1. Right-click vào project "DUT_NET-MediaVideo"
2. Chọn "Refresh" (hoặc F5)
3. Eclipse sẽ tự động nhận JAR trong WEB-INF/lib/
```

### Bước 4: Verify Libraries

```
1. Trong Eclipse, mở project properties
2. Java Build Path → Libraries
3. Kiểm tra vosk-0.3.45.jar và jna-5.13.0.jar đã được add
```

### Bước 5: Cập nhật WorkerInitializer để dùng Vosk

Sửa file: `Listener/WorkerInitializer.java`

```java
// Thay dòng 27, 31:
// CŨ:
worker1 = new WorkerService("Worker-1");
worker2 = new WorkerService("Worker-2");

// MỚI:
worker1 = new WorkerServiceVosk("Worker-1");
worker2 = new WorkerServiceVosk("Worker-2");
```

---

## 🎯 Download Links (Copy vào Browser)

### Link 1: Vosk JAR
```
https://repo1.maven.org/maven2/com/alphacephei/vosk/0.3.45/vosk-0.3.45.jar
```

### Link 2: JNA JAR
```
https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar
```

---

## ⚠️ Lưu Ý Quan Trọng

### 1. Vosk chỉ xử lý file WAV
Vosk yêu cầu file audio định dạng **WAV (PCM 16-bit mono)**.

Nếu upload file MP3/MP4, cần convert sang WAV trước:

**Option A: Dùng FFmpeg (Khuyến nghị)**
```java
// Thêm code convert trong WorkerServiceVosk
ProcessBuilder pb = new ProcessBuilder(
    "ffmpeg", "-i", inputFile, 
    "-ar", "16000", 
    "-ac", "1", 
    "-f", "wav", 
    outputFile
);
pb.start().waitFor();
```

**Option B: Chỉ cho phép upload file WAV**
Sửa MediaController.java:
```java
private boolean isValidMediaFile(String fileName) {
    return fileName.toLowerCase().endsWith(".wav");
}
```

### 2. Cấu Trúc Thư Mục Model
Đảm bảo cấu trúc đúng:
```
DUT_NET-MediaVideo/
└── models/
    └── vosk-model-vn-0.4/
        ├── am/
        ├── conf/
        ├── graph/
        └── ivector/
```

---

## 🚀 Sau Khi Setup Xong

### 1. Clean & Build Project
```
Project → Clean... → Clean all projects
```

### 2. Start Server
```
Run → Run As → Run on Server
```

### 3. Console sẽ hiển thị:
```
[Worker-1] Đang load Vosk model...
[Worker-1] ✅ Đã load Vosk model thành công
[Worker-2] Đang load Vosk model...
[Worker-2] ✅ Đã load Vosk model thành công
✓ Worker-1 và Worker-2 đã được khởi động
```

### 4. Test
```
1. Đăng nhập
2. Upload file WAV
3. Xem kết quả tại History page
```

---

## 🐛 Troubleshooting

### Lỗi: "UnsatisfiedLinkError"
```
Nguyên nhân: Thiếu native library
Giải pháp: Download thêm vosk-platform-specific JAR
Link: https://github.com/alphacep/vosk-api/releases
```

### Lỗi: "Model không load được"
```
Kiểm tra:
1. Đường dẫn MODEL_PATH đúng chưa
2. Thư mục model có đầy đủ file chưa (am, conf, graph, ivector)
3. Quyền đọc file
```

### Lỗi: "AudioInputStream không đọc được"
```
Nguyên nhân: File không phải WAV
Giải pháp: Convert sang WAV hoặc chỉ cho phép upload WAV
```

---

## 📊 Tóm Tắt

**Đã làm:**
- ✅ Tạo thư mục models
- ✅ Giải nén Vosk model
- ✅ Cập nhật đường dẫn trong code

**Cần làm:**
1. ⬇️ Download 2 file JAR (vosk + jna)
2. 📂 Copy vào WEB-INF/lib/
3. 🔄 Refresh project trong Eclipse
4. ✏️ Sửa WorkerInitializer dùng WorkerServiceVosk
5. ▶️ Run và test!

---

## 💡 Nếu Gặp Khó Khăn

**Option 1:** Dùng WorkerService demo (hiện tại) - đơn giản, chạy ngay
**Option 2:** Setup Vosk (như hướng dẫn trên) - STT thật

Bạn có thể demo với WorkerService trước, sau đó nâng cấp lên Vosk!
