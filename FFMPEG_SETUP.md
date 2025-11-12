# Hướng Dẫn Cài Đặt FFmpeg cho Windows

## ✅ Vosk đã hoạt động! Nhưng cần FFmpeg để xử lý video

Hiện tại hệ thống của bạn:
- ✅ Vosk model đã load thành công
- ✅ Worker đã khởi động
- ❌ Thiếu FFmpeg để chuyển đổi MP4 → WAV

## 📦 Cài Đặt FFmpeg (5 phút)

### Cách 1: Tải Bản Build Có Sẵn (Khuyến nghị)

1. **Tải FFmpeg:**
   - Link: https://github.com/BtbN/FFmpeg-Builds/releases
   - Chọn file: `ffmpeg-master-latest-win64-gpl.zip`
   - Hoặc: https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip

2. **Giải nén:**
   ```
   Giải nén vào: C:\ffmpeg\
   
   Kết quả:
   C:\ffmpeg\bin\ffmpeg.exe
   C:\ffmpeg\bin\ffprobe.exe
   ```

3. **Thêm vào PATH:**
   
   **Bước 3.1:** Mở System Properties
   - Nhấn `Windows + R`
   - Gõ: `sysdm.cpl` → Enter
   
   **Bước 3.2:** Environment Variables
   - Tab "Advanced" → Click "Environment Variables"
   
   **Bước 3.3:** Sửa PATH
   - Trong "System variables", tìm `Path` → Click "Edit"
   - Click "New"
   - Thêm: `C:\ffmpeg\bin`
   - Click "OK" → "OK" → "OK"

4. **Kiểm tra:**
   ```cmd
   # Đóng và mở lại CMD/Terminal mới
   ffmpeg -version
   ```
   
   Nếu thấy version info → Thành công! ✅

### Cách 2: Dùng Chocolatey (Nếu đã cài Chocolatey)

```cmd
choco install ffmpeg
```

### Cách 3: Dùng Scoop (Nếu đã cài Scoop)

```cmd
scoop install ffmpeg
```

## 🧪 Test Sau Khi Cài

1. Mở CMD mới và chạy:
   ```cmd
   ffmpeg -version
   ```

2. Nếu thấy output như này là OK:
   ```
   ffmpeg version N-... Copyright (c) 2000-2025 the FFmpeg developers
   ```

3. Restart Eclipse (để Eclipse nhận PATH mới)

4. Restart server trong Eclipse

5. Upload lại file MP4 và kiểm tra log:
   ```
   [Worker-1] File là video, đang chuyển đổi sang WAV...
   [Worker-1] Đang chạy FFmpeg để chuyển đổi...
   [Worker-1] ✓ Đã chuyển đổi video sang WAV: ...
   [Worker-1] Đang nhận dạng giọng nói...
   ```

## 🎯 Sau Khi Cài FFmpeg

Hệ thống sẽ:
1. ✅ Tự động phát hiện file MP4/AVI/MOV/MKV
2. ✅ Chuyển đổi video → WAV (16kHz, mono)
3. ✅ Xử lý Speech-to-Text với Vosk
4. ✅ Xóa file WAV tạm sau khi xử lý
5. ✅ Trả về kết quả text

## 📝 Lưu Ý

- FFmpeg chỉ cần cài 1 lần
- Sau khi thêm vào PATH, phải restart Eclipse
- Nếu không muốn cài FFmpeg, có thể upload trực tiếp file WAV thay vì MP4

## 🔧 Nếu Gặp Lỗi

**Lỗi: "ffmpeg is not recognized"**
- PATH chưa được cấu hình đúng
- Chưa restart CMD/Eclipse sau khi thêm PATH
- Kiểm tra lại đường dẫn: `C:\ffmpeg\bin\ffmpeg.exe` có tồn tại không

**Lỗi: "Exit code: 1"**
- File video bị hỏng hoặc định dạng không hỗ trợ
- Thử upload file khác

## 🎓 Giải Thích Kỹ Thuật (Cho Báo Cáo)

**Vì sao cần FFmpeg?**
- Vosk chỉ xử lý audio WAV format (PCM)
- Video MP4 chứa audio nén (AAC/MP3)
- FFmpeg giải nén và chuyển đổi audio về format Vosk cần

**Quy trình xử lý:**
```
MP4 Video → FFmpeg → WAV Audio → Vosk STT → Text Result
(Tính toán lớn 30% điểm)
```

**Sample rate 16kHz:**
- Đủ cho nhận dạng giọng nói (speech: 0-8kHz)
- Giảm kích thước file và tăng tốc độ xử lý
- Standard cho hầu hết STT systems

