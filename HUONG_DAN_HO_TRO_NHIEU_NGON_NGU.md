# HƯỚNG DẪN HỖ TRỢ NHIỀU NGÔN NGỮ (TIẾNG VIỆT + TIẾNG ANH)

## 🎯 TỔNG QUAN

Hệ thống đã được nâng cấp để hỗ trợ nhiều ngôn ngữ:
- 🇻🇳 **Tiếng Việt** (Vietnamese) - Model: vosk-model-vn-0.4
- 🇺🇸 **Tiếng Anh** (English) - Model: vosk-model-small-en-us-0.15

User có thể chọn ngôn ngữ khi upload file, và Worker sẽ tự động sử dụng model phù hợp.

---

## ✅ ĐÃ THAY ĐỔI GÌ?

### **1. Upload Form (upload.jsp)**
- ✅ Thêm dropdown chọn ngôn ngữ
- ✅ Mặc định: Tiếng Việt
- ✅ Hỗ trợ: Tiếng Việt, Tiếng Anh

### **2. Database Schema**
- ✅ Thêm cột `language` vào bảng `Task`
- ✅ Kiểu dữ liệu: VARCHAR(10)
- ✅ Giá trị mặc định: 'vi' (Tiếng Việt)

### **3. Backend Code**
- ✅ **Task.java**: Thêm field `language` + getter/setter
- ✅ **MediaController.java**: Nhận parameter `language` từ form
- ✅ **TaskService.java**: Truyền `language` khi tạo task
- ✅ **TaskBO.java**: Validate và xử lý `language`
- ✅ **TaskDAO.java**: Lưu và đọc `language` từ database
- ✅ **WorkerServiceVosk.java**: Load 2 models, tự động chọn model phù hợp

### **4. Worker Logic**
- ✅ Worker load cả 2 models khi khởi động
- ✅ Khi xử lý task, Worker đọc field `language` từ database
- ✅ Worker chọn model phù hợp (tiếng Việt hoặc tiếng Anh)
- ✅ Xử lý Speech-to-Text với model đã chọn

---

## 🔧 CÀI ĐẶT (Các bước PHẢI LÀM)

### **BƯỚC 1: Cập nhật Database Schema**

Mở **MySQL Workbench** hoặc command line, chạy các lệnh sau:

```sql
USE media_processor_db;

-- Thêm cột language
ALTER TABLE Task 
ADD COLUMN language VARCHAR(10) DEFAULT 'vi' 
COMMENT 'Ngôn ngữ: vi=Tiếng Việt, en=Tiếng Anh';

-- Cập nhật các task cũ thành tiếng Việt
UPDATE Task SET language = 'vi' WHERE language IS NULL;

-- Kiểm tra kết quả
DESCRIBE Task;
SELECT id, file_name, language, status FROM Task LIMIT 5;
```

**Kết quả mong đợi:**
```
+-------------------+--------------+------+-----+---------+
| Field             | Type         | Null | Key | Default |
+-------------------+--------------+------+-----+---------+
| id                | int          | NO   | PRI | NULL    |
| user_id           | int          | NO   |     | NULL    |
| file_name         | varchar(255) | YES  |     | NULL    |
| server_file_path  | text         | YES  |     | NULL    |
| status            | varchar(50)  | YES  |     | NULL    |
| language          | varchar(10)  | YES  |     | vi      | ← MỚI
| submission_time   | timestamp    | YES  |     | NULL    |
| completion_time   | timestamp    | YES  |     | NULL    |
| result_text       | text         | YES  |     | NULL    |
| processing_time_ms| int          | YES  |     | NULL    |
+-------------------+--------------+------+-----+---------+
```

---

### **BƯỚC 2: Tải Model Tiếng Anh từ Vosk**

#### **2.1. Tải model:**
1. Truy cập: **https://alphacephei.com/vosk/models**
2. Tìm model: **vosk-model-small-en-us-0.15** (English, US, ~40MB)
3. Hoặc dùng link trực tiếp:
   ```
   https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip
   ```
4. Tải về và giải nén

#### **2.2. Đặt model vào đúng thư mục:**
```
E:\K1N3\LTM\DUT_NET-MediaVideo\models\vosk-model-small-en-us-0.15\
```

#### **2.3. Cấu trúc thư mục models sau khi hoàn thành:**
```
DUT_NET-MediaVideo/
├── models/
│   ├── vosk-model-vn-0.4/              ← Tiếng Việt (Đã có)
│   │   ├── am/
│   │   ├── conf/
│   │   ├── graph/
│   │   ├── ivector/
│   │   └── README
│   └── vosk-model-small-en-us-0.15/    ← Tiếng Anh (TẢI MỚI)
│       ├── am/
│       ├── conf/
│       ├── graph/
│       └── README
├── src/
├── build/
└── ...
```

#### **2.4. Kiểm tra model:**
Mở file `README` trong mỗi thư mục model để xem thông tin:
- **vosk-model-vn-0.4**: Vietnamese model
- **vosk-model-small-en-us-0.15**: English US model

---

### **BƯỚC 3: Build & Restart Server**

#### **3.1. Clean & Build Project trong Eclipse:**
1. Click chuột phải vào project **DUT_NET-MediaVideo**
2. Chọn **Clean...** → OK
3. Chọn **Build Project** (hoặc tự động build)
4. Đợi build hoàn tất (không có lỗi)

#### **3.2. Clean Tomcat:**
1. Mở tab **Servers** ở dưới Eclipse
2. Click chuột phải vào **Tomcat v10.1 Server**
3. Chọn **Clean...**
4. Chọn **Clean Tomcat Work Directory...**

#### **3.3. Restart Tomcat Server:**
1. Click chuột phải vào **Tomcat v10.1 Server**
2. Chọn **Restart**
3. Đợi server khởi động

#### **3.4. Kiểm tra Console Log:**
Khi Worker khởi động, bạn sẽ thấy log như sau:

```
[Worker-1] Đang load Vosk models...
[Worker-1] Đường dẫn model tiếng Việt: E:\K1N3\LTM\DUT_NET-MediaVideo\models\vosk-model-vn-0.4
[Worker-1] ✅ Đã load model tiếng Việt thành công
[Worker-1] Đường dẫn model tiếng Anh: E:\K1N3\LTM\DUT_NET-MediaVideo\models\vosk-model-small-en-us-0.15
[Worker-1] ✅ Đã load model tiếng Anh thành công
[Worker-1] Worker đã khởi động và bắt đầu lắng nghe queue...
```

**✅ Nếu thấy cả 2 dòng "✅ Đã load model..." → THÀNH CÔNG!**

**⚠️ Nếu thấy "⚠️ Không tìm thấy model tiếng Anh":**
- Kiểm tra lại đường dẫn thư mục model
- Đảm bảo tên thư mục chính xác: `vosk-model-small-en-us-0.15`
- Trong thư mục phải có các folder: `am`, `conf`, `graph`, `README`

---

## 🎬 CÁCH SỬ DỤNG

### **Bước 1: Đăng nhập và vào trang Upload**
1. Đăng nhập vào hệ thống
2. Click **Upload** hoặc vào: `http://localhost:8080/DUT_NET-MediaVideo/upload`

### **Bước 2: Chọn ngôn ngữ và upload file**
1. Chọn ngôn ngữ từ dropdown:
   - 🇻🇳 **Tiếng Việt (Vietnamese)** - Cho file audio/video tiếng Việt
   - 🇺🇸 **Tiếng Anh (English)** - Cho file audio/video tiếng Anh
2. Click **Chọn File** và chọn file media
3. Click **🚀 Upload và Xử lý**

### **Bước 3: Theo dõi tiến trình**
1. Vào **Lịch Sử** để xem danh sách tasks
2. Task sẽ có trạng thái:
   - ⏳ **Đang chờ** - Task trong queue
   - 🔄 **Đang xử lý** - Worker đang xử lý
   - ✅ **Hoàn thành** - Đã có kết quả
   - ❌ **Thất bại** - Có lỗi xảy ra

### **Bước 4: Xem kết quả**
1. Click nút **👁️ Xem** trên task đã hoàn thành
2. Xem kết quả Speech-to-Text trong modal popup
3. Kết quả sẽ hiển thị đúng ngôn ngữ đã chọn

---

## 🧪 TEST CASES

### **Test Case 1: Upload file tiếng Việt**
- **Input**: File audio/video tiếng Việt, chọn ngôn ngữ = "Tiếng Việt"
- **Expected**: Worker dùng model `vosk-model-vn-0.4`, kết quả chính xác tiếng Việt

### **Test Case 2: Upload file tiếng Anh**
- **Input**: File audio/video tiếng Anh, chọn ngôn ngữ = "Tiếng Anh"
- **Expected**: Worker dùng model `vosk-model-small-en-us-0.15`, kết quả chính xác tiếng Anh

### **Test Case 3: Chọn sai ngôn ngữ**
- **Input**: File tiếng Việt nhưng chọn "Tiếng Anh"
- **Expected**: Kết quả không chính xác hoặc nhận dạng sai

### **Test Case 4: Upload nhiều file khác ngôn ngữ**
- **Input**: Upload lần lượt file tiếng Việt và tiếng Anh
- **Expected**: Cả 2 files đều được xử lý đúng với model phù hợp

---

## 📊 KIỂM TRA LOG

### **Log khi upload file tiếng Anh:**
```
[MediaController] Ngôn ngữ được chọn: en
[MediaController] File đã được lưu: ...
[TaskService] Task 123 (ngôn ngữ: en) đã được tạo và đẩy vào queue thành công
[Worker-1] Bắt đầu xử lý Task ID: 123
[Worker-1] Ngôn ngữ: Tiếng Anh
[Worker-1] ===== BẮT ĐẦU SPEECH-TO-TEXT với VOSK (30% ĐIỂM) =====
[Worker-1] Ngôn ngữ: en
[Worker-1] Model: Tiếng Anh
[Worker-1] ✓ Hoàn thành Task ID: 123 trong 45000ms
```

### **Log khi upload file tiếng Việt:**
```
[MediaController] Ngôn ngữ được chọn: vi
[MediaController] File đã được lưu: ...
[TaskService] Task 124 (ngôn ngữ: vi) đã được tạo và đẩy vào queue thành công
[Worker-1] Bắt đầu xử lý Task ID: 124
[Worker-1] Ngôn ngữ: Tiếng Việt
[Worker-1] ===== BẮT ĐẦU SPEECH-TO-TEXT với VOSK (30% ĐIỂM) =====
[Worker-1] Ngôn ngữ: vi
[Worker-1] Model: Tiếng Việt
[Worker-1] ✓ Hoàn thành Task ID: 124 trong 38000ms
```

---

## ⚠️ XỬ LÝ LỖI

### **Lỗi 1: "Model tiếng Anh chưa được load"**
**Nguyên nhân**: Chưa tải hoặc đặt sai thư mục model tiếng Anh

**Giải pháp**:
1. Kiểm tra thư mục: `E:\K1N3\LTM\DUT_NET-MediaVideo\models\vosk-model-small-en-us-0.15\`
2. Đảm bảo có các folder: `am`, `conf`, `graph`
3. Restart Tomcat để Worker load lại models

### **Lỗi 2: "Không có model nào được load"**
**Nguyên nhân**: Cả 2 models đều không tìm thấy

**Giải pháp**:
1. Kiểm tra lại đường dẫn thư mục `models`
2. Đảm bảo có ít nhất 1 model (tiếng Việt hoặc tiếng Anh)
3. Xem console log để biết đường dẫn Worker đang tìm

### **Lỗi 3: Kết quả không chính xác**
**Nguyên nhân**: Chọn sai ngôn ngữ (upload file tiếng Anh nhưng chọn tiếng Việt)

**Giải pháp**:
- Chọn đúng ngôn ngữ khi upload
- Nếu không chắc, thử cả 2 ngôn ngữ và so sánh kết quả

### **Lỗi 4: Worker không khởi động**
**Nguyên nhân**: Lỗi khi load models

**Giải pháp**:
1. Xem console log để biết lỗi cụ thể
2. Đảm bảo models đã được tải và giải nén đúng
3. Kiểm tra dung lượng ổ cứng (models chiếm ~200MB)

---

## 🚀 MỞ RỘNG THÊM NGÔN NGỮ

Để thêm ngôn ngữ khác (ví dụ: Tiếng Trung, Tiếng Nhật):

### **1. Tải model từ Vosk:**
- https://alphacephei.com/vosk/models
- Chọn model ngôn ngữ cần thêm

### **2. Đặt vào thư mục models:**
```
models/
  ├── vosk-model-cn-0.22/  (Tiếng Trung)
  ├── vosk-model-ja-0.22/  (Tiếng Nhật)
  └── ...
```

### **3. Cập nhật code:**
#### **upload.jsp** - Thêm option mới:
```html
<option value="zh">🇨🇳 Tiếng Trung (Chinese)</option>
<option value="ja">🇯🇵 Tiếng Nhật (Japanese)</option>
```

#### **WorkerServiceVosk.java** - Load thêm models:
```java
private Model modelChinese;
private Model modelJapanese;

// Trong constructor:
this.modelChinese = new Model("models/vosk-model-cn-0.22");
this.modelJapanese = new Model("models/vosk-model-ja-0.22");

// Trong thucHienSpeechToTextVosk:
else if (language.equals("zh")) {
    selectedModel = modelChinese;
} else if (language.equals("ja")) {
    selectedModel = modelJapanese;
}
```

### **4. Validate language trong Backend:**
Cập nhật các hàm validate để accept thêm `"zh"`, `"ja"`, etc.

---

## 📝 CHECKLIST HOÀN THÀNH

### **Database:**
- [ ] Đã chạy script SQL thêm cột `language`
- [ ] Kiểm tra `DESCRIBE Task;` thấy cột `language`

### **Models:**
- [ ] Model tiếng Việt có trong `models/vosk-model-vn-0.4/`
- [ ] Model tiếng Anh có trong `models/vosk-model-small-en-us-0.15/`
- [ ] Mỗi thư mục có đầy đủ: `am`, `conf`, `graph`, `README`

### **Code:**
- [ ] Build project thành công, không có lỗi compile
- [ ] Upload form có dropdown chọn ngôn ngữ
- [ ] Console log hiển thị "✅ Đã load model..." cho cả 2 models

### **Test:**
- [ ] Upload file tiếng Việt, chọn "Tiếng Việt" → Kết quả đúng
- [ ] Upload file tiếng Anh, chọn "Tiếng Anh" → Kết quả đúng
- [ ] Xem kết quả trong modal popup hiển thị đúng

---

## 💡 LƯU Ý QUAN TRỌNG

1. **Model size**: 
   - Model tiếng Việt: ~130MB
   - Model tiếng Anh: ~40MB
   - Tổng: ~170MB

2. **Thời gian load models**: 
   - Worker cần 5-10 giây để load cả 2 models khi khởi động
   - Chỉ load 1 lần duy nhất khi Worker start

3. **Chọn đúng ngôn ngữ**:
   - Chọn sai ngôn ngữ sẽ cho kết quả không chính xác
   - Khuyến khích user chọn đúng ngôn ngữ của file

4. **Memory**:
   - Mỗi model chiếm ~200-500MB RAM khi load
   - Đảm bảo máy có đủ RAM (tối thiểu 4GB)

---

## 🎉 KẾT QUẢ MONG ĐỢI

Sau khi hoàn thành, hệ thống của bạn sẽ:

✅ Hỗ trợ upload file tiếng Việt và tiếng Anh  
✅ User có thể chọn ngôn ngữ trước khi upload  
✅ Worker tự động chọn model phù hợp  
✅ Kết quả Speech-to-Text chính xác cho cả 2 ngôn ngữ  
✅ Dễ dàng mở rộng thêm ngôn ngữ mới trong tương lai  

---

**Ngày cập nhật:** 11/11/2025  
**Tác giả:** GitHub Copilot  
**Version:** 2.0 - Multi-language Support
