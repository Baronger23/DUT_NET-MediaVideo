# Hướng Dẫn Tích Hợp Speech-to-Text

## 🎯 3 Giải Pháp STT (Chọn 1 trong 3)

---

## ✅ Giải Pháp 1: Vosk (KHUYẾN NGHỊ - Dễ nhất)

### Ưu điểm:
- ✅ **Offline** - Không cần internet
- ✅ **Không cần API key** - Miễn phí 100%
- ✅ **Hỗ trợ tiếng Việt** rất tốt
- ✅ **Nhanh** - Xử lý local
- ✅ **Dễ cài đặt**

### Bước 1: Thêm Dependency (Maven)

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.alphacephei</groupId>
    <artifactId>vosk</artifactId>
    <version>0.3.45</version>
</dependency>
```

### Bước 2: Tải Vosk Model (Tiếng Việt)

```bash
# Tải model tiếng Việt từ:
https://alphacephei.com/vosk/models

# Download: vosk-model-small-vi-0.4.zip (80MB)
# Hoặc: vosk-model-vi-0.4.zip (400MB - chính xác hơn)

# Giải nén và đặt vào thư mục project:
DUT_NET-MediaVideo/models/vosk-model-small-vi-0.4/
```

### Bước 3: Cập Nhật WorkerInitializer

Thay `WorkerService` bằng `WorkerServiceVosk`:

```java
// Listener/WorkerInitializer.java
worker1 = new WorkerServiceVosk("Worker-1");
worker2 = new WorkerServiceVosk("Worker-2");
```

### Bước 4: Chạy và Test

Upload file audio/video → Xem kết quả tại History page!

---

## 🌐 Giải Pháp 2: Google Cloud Speech-to-Text (Chất lượng cao nhất)

### Ưu điểm:
- ✅ **Chất lượng tốt nhất** - AI của Google
- ✅ Hỗ trợ nhiều ngôn ngữ
- ✅ Automatic punctuation
- ✅ Speaker diarization

### Nhược điểm:
- ❌ Cần API key (free 60 phút/tháng)
- ❌ Cần internet
- ❌ Phức tạp hơn

### Bước 1: Tạo Google Cloud Project

```
1. Truy cập: https://console.cloud.google.com
2. Tạo project mới
3. Enable API: Cloud Speech-to-Text API
4. Tạo Service Account và tải JSON key
```

### Bước 2: Thêm Dependency

```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-speech</artifactId>
    <version>4.14.0</version>
</dependency>
```

### Bước 3: Code Implementation

```java
private String thucHienSpeechToTextGoogle(String filePath) throws Exception {
    // Set credentials
    System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "path/to/key.json");
    
    try (SpeechClient speechClient = SpeechClient.create()) {
        byte[] data = Files.readAllBytes(Paths.get(filePath));
        ByteString audioBytes = ByteString.copyFrom(data);
        
        RecognitionConfig config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
            .setSampleRateHertz(16000)
            .setLanguageCode("vi-VN")
            .build();
        
        RecognitionAudio audio = RecognitionAudio.newBuilder()
            .setContent(audioBytes)
            .build();
        
        RecognizeResponse response = speechClient.recognize(config, audio);
        
        StringBuilder result = new StringBuilder();
        for (SpeechRecognitionResult r : response.getResultsList()) {
            result.append(r.getAlternativesList().get(0).getTranscript());
        }
        
        return result.toString();
    }
}
```

---

## 🐍 Giải Pháp 3: Python Whisper (OpenAI - Cực mạnh)

### Ưu điểm:
- ✅ **Chất lượng xuất sắc** - AI của OpenAI
- ✅ Offline
- ✅ Hỗ trợ 99 ngôn ngữ
- ✅ Tự động phát hiện ngôn ngữ

### Bước 1: Cài Whisper (Python)

```bash
pip install openai-whisper
pip install ffmpeg-python
```

### Bước 2: Tạo Python Script

```python
# stt_whisper.py
import whisper
import sys

model = whisper.load_model("base")  # tiny, base, small, medium, large
result = model.transcribe(sys.argv[1], language="vi")
print(result["text"])
```

### Bước 3: Gọi từ Java

```java
private String thucHienSpeechToTextWhisper(String filePath) throws Exception {
    ProcessBuilder pb = new ProcessBuilder(
        "python", "stt_whisper.py", filePath
    );
    
    Process process = pb.start();
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream())
    );
    
    StringBuilder result = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
        result.append(line);
    }
    
    process.waitFor();
    return result.toString();
}
```

---

## 📊 So Sánh 3 Giải Pháp

| Tiêu chí | Vosk | Google Cloud | Whisper |
|----------|------|--------------|---------|
| **Độ chính xác** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Tốc độ** | ⚡⚡⚡ | ⚡⚡ | ⚡⚡ |
| **Offline** | ✅ | ❌ | ✅ |
| **Miễn phí** | ✅ | ⚠️ (60p/tháng) | ✅ |
| **Dễ cài đặt** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| **Tiếng Việt** | ✅ Tốt | ✅ Xuất sắc | ✅ Xuất sắc |

---

## 🎯 KHUYẾN NGHỊ

### Cho Demo/Học Tập:
👉 **Dùng Vosk** - Dễ nhất, offline, không cần key

### Cho Production:
👉 **Google Cloud** - Chất lượng cao, reliable

### Cho Research:
👉 **Whisper** - State-of-the-art, mới nhất

---

## 🚀 Quick Start với Vosk (5 phút)

```bash
# 1. Thêm vào pom.xml
<dependency>
    <groupId>com.alphacephei</groupId>
    <artifactId>vosk</artifactId>
    <version>0.3.45</version>
</dependency>

# 2. Download model
wget https://alphacephei.com/vosk/models/vosk-model-small-vi-0.4.zip
unzip vosk-model-small-vi-0.4.zip -d models/

# 3. Sửa WorkerInitializer.java
worker1 = new WorkerServiceVosk("Worker-1");

# 4. Run và test!
```

---

## 📝 Test Files

Bạn có thể test với:
- File audio: .mp3, .wav
- File video: .mp4, .avi (sẽ extract audio)

**Vosk chỉ xử lý được file .wav**, nên cần convert trước:

```java
// Dùng thư viện javax.sound hoặc ffmpeg
```

---

## 💡 Tips

1. **Vosk**: Tốt cho demo, học tập
2. **Google**: Tốt cho production, cần API key
3. **Whisper**: Tốt cho research, cần Python

**Tôi khuyên dùng Vosk để test nhanh nhất!** 🚀

File `WorkerServiceVosk.java` đã sẵn sàng, chỉ cần:
- Thêm dependency
- Tải model
- Sửa WorkerInitializer
- Chạy!
