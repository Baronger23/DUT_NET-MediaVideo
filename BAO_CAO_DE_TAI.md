# BÁO CÁO ĐỀ TÀI LẬP TRÌNH MẠNG

## Tên Đề Tài

**HỆ THỐNG TRÍCH XUẤT VĂN BẢN TỰ ĐỘNG TỪ MEDIA ĐA PHƯƠNG TIỆN SỬ DỤNG KỸ THUẬT SPEECH-TO-TEXT VÀ XỬ LÝ BẤT ĐỒNG BỘ**

*(Tên gọi rút gọn: DUT_NET MediaVideo - Hệ thống Speech-to-Text cho Media)*

**Tên tiếng Anh:** *Automatic Text Extraction System from Multimedia Using Speech-to-Text Technology and Asynchronous Processing*

---

## PHẦN MỞ ĐẦU

### 1. MỤC ĐÍCH ĐỀ TÀI

Trong bối cảnh công nghệ thông tin phát triển mạnh mẽ, nhu cầu xử lý và chuyển đổi nội dung đa phương tiện (audio, video) sang dạng văn bản ngày càng trở nên quan trọng. Việc ghi chép thủ công nội dung từ các tệp âm thanh và video không chỉ tốn thời gian mà còn dễ xảy ra sai sót, đặc biệt khi khối lượng công việc lớn.

Đề tài **"Hệ thống trích xuất văn bản tự động từ media đa phương tiện sử dụng kỹ thuật Speech-to-Text và xử lý bất đồng bộ"** được thực hiện với các mục đích chính sau:

#### 1.1. Mục đích học tập và nghiên cứu

- **Áp dụng kiến thức Lập trình mạng:** Xây dựng một ứng dụng web thực tế sử dụng công nghệ Java Servlet/JSP, minh họa các khái niệm về lập trình mạng như HTTP request/response, session management, và xử lý đồng thời.

- **Thực hành kiến trúc phần mềm:** Triển khai mô hình kiến trúc MVC (Model-View-Controller) một cách đầy đủ và chuyên nghiệp, phân tách rõ ràng các tầng ứng dụng (Presentation, Business Logic, Data Access).

- **Làm quen với xử lý bất đồng bộ:** Nghiên cứu và áp dụng các kỹ thuật xử lý tác vụ nặng (heavy computation tasks) thông qua cơ chế hàng đợi (Queue) và Worker threads, đáp ứng yêu cầu về xử lý tính toán lớn trong đề bài môn học.

- **Tích hợp công nghệ AI/ML:** Tìm hiểu và tích hợp thư viện Speech-to-Text (Vosk) vào ứng dụng Java, mở rộng kiến thức về trí tuệ nhân tạo và xử lý ngôn ngữ tự nhiên.

#### 1.2. Mục đích thực tiễn

- **Giải quyết bài toán thực tế:** Phát triển một công cụ hữu ích giúp người dùng chuyển đổi nội dung âm thanh/video sang văn bản một cách tự động, tiết kiệm thời gian và công sức.

- **Hỗ trợ đa ngôn ngữ:** Xây dựng hệ thống có khả năng xử lý nhiều ngôn ngữ (tiếng Việt, tiếng Anh), phục vụ nhu cầu đa dạng của người dùng.

- **Khả năng mở rộng:** Thiết kế hệ thống có khả năng scale (mở rộng) tốt, có thể xử lý nhiều yêu cầu đồng thời từ nhiều người dùng mà không bị quá tải.

- **Offline processing:** Sử dụng công nghệ xử lý offline (Vosk) giúp bảo mật thông tin người dùng, không phụ thuộc vào kết nối internet và không tốn phí API calls.

#### 1.3. Mục đích nâng cao kỹ năng

- **Quản lý tài nguyên hệ thống:** Học cách tối ưu hóa việc sử dụng Connection Pool, Memory Management, và Thread Management để xây dựng ứng dụng có hiệu suất cao.

- **Xử lý lỗi và ngoại lệ:** Thực hành các kỹ thuật xử lý lỗi, exception handling, và logging để tạo ra một hệ thống ổn định và dễ bảo trì.

- **Bảo mật ứng dụng web:** Áp dụng các biện pháp bảo mật cơ bản như authentication, authorization, input validation, và SQL injection prevention.

---

### 2. MỤC TIÊU CỦA ĐỀ TÀI

Để đạt được những mục đích đã đề ra, đề tài đặt ra các mục tiêu cụ thể như sau:

#### 2.1. Mục tiêu về chức năng

**✅ Mục tiêu 1: Xây dựng hệ thống upload và quản lý file media**
- Cho phép người dùng upload các file audio (MP3, WAV) và video (MP4, AVI)
- Validate định dạng và kích thước file trước khi xử lý
- Lưu trữ file an toàn trên server với cơ chế đặt tên tránh trùng lặp
- Hiển thị danh sách các file đã upload với trạng thái xử lý

**✅ Mục tiêu 2: Triển khai module Speech-to-Text**
- Tích hợp thư viện Vosk để xử lý chuyển đổi âm thanh sang văn bản
- Hỗ trợ tối thiểu 2 ngôn ngữ: Tiếng Việt và Tiếng Anh
- Đảm bảo độ chính xác của kết quả chuyển đổi ở mức chấp nhận được
- Xử lý được cả file audio trực tiếp và extract audio từ file video

**✅ Mục tiêu 3: Xây dựng hệ thống xử lý bất đồng bộ**
- Thiết kế cơ chế Queue (hàng đợi) để quản lý các task cần xử lý
- Triển khai Worker threads chạy ngầm để xử lý các tác vụ nặng
- Cập nhật trạng thái task theo thời gian thực (PENDING → PROCESSING → COMPLETED)
- Tính toán và hiển thị thời gian xử lý cho từng task

**✅ Mục tiêu 4: Quản lý người dùng và phân quyền**
- Xây dựng hệ thống đăng ký, đăng nhập, đăng xuất
- Mỗi người dùng chỉ xem được lịch sử task của chính mình
- Lưu trữ thông tin người dùng và mật khẩu an toàn trong database
- Session management để duy trì trạng thái đăng nhập

**✅ Mục tiêu 5: Hiển thị kết quả và export dữ liệu**
- Hiển thị văn bản đã trích xuất một cách trực quan, dễ đọc
- Cho phép người dùng tải xuống kết quả dưới dạng file TXT
- Hiển thị các thông tin chi tiết như thời gian xử lý, ngôn ngữ, trạng thái
- Cung cấp giao diện responsive, thân thiện với người dùng

#### 2.2. Mục tiêu về kỹ thuật

**🎯 Mục tiêu 6: Áp dụng kiến trúc MVC chuẩn**
- **Model Layer:** Thiết kế các Bean (Task, User), DAO (Data Access Object), và BO (Business Object) để quản lý dữ liệu
- **View Layer:** Sử dụng JSP để hiển thị giao diện, tách biệt hoàn toàn với logic xử lý
- **Controller Layer:** Xây dựng các Servlet để điều khiển luồng xử lý và điều hướng
- **Service Layer:** Tạo lớp Service để chứa business logic phức tạp

**🎯 Mục tiêu 7: Tối ưu hóa hiệu suất và khả năng mở rộng**
- Triển khai Connection Pool tự xây dựng để quản lý kết nối database hiệu quả
- Sử dụng Singleton pattern cho các shared resources (VoskModelManager, QueueManager)
- Implement caching mechanism để giảm số lần truy vấn database
- Áp dụng pagination cho các danh sách dữ liệu lớn

**🎯 Mục tiêu 8: Đảm bảo tính ổn định và xử lý lỗi**
- Xử lý exception một cách đầy đủ ở mọi tầng của ứng dụng
- Ghi log chi tiết để theo dõi hoạt động của hệ thống
- Triển khai cơ chế graceful shutdown để đóng tài nguyên đúng cách
- Error handling cho các tình huống như file không hợp lệ, model không load được, database connection failed

**🎯 Mục tiêu 9: Bảo mật ứng dụng**
- Sử dụng PreparedStatement để phòng chống SQL Injection
- Validate và sanitize mọi input từ người dùng
- Mã hóa mật khẩu trước khi lưu vào database (khuyến nghị sử dụng BCrypt hoặc tương tự)
- Kiểm tra authorization trước khi cho phép truy cập tài nguyên

**🎯 Mục tiêu 10: Đáp ứng yêu cầu môn học về "Tính toán lớn"**
- Module Speech-to-Text đại diện cho tác vụ tính toán phức tạp, tốn CPU và thời gian
- Quá trình xử lý được thực hiện bất đồng bộ thông qua Worker threads
- Thời gian xử lý cho mỗi file audio/video dao động từ 10 giây đến vài phút tùy độ dài
- Đảm bảo hệ thống không bị block khi xử lý nhiều file đồng thời

#### 2.3. Mục tiêu về kết quả

**📊 Mục tiêu 11: Hoàn thiện sản phẩm đầy đủ chức năng**
- Ứng dụng có thể chạy ổn định trên môi trường local (localhost)
- Giao diện thân thiện, dễ sử dụng, hiển thị đầy đủ thông tin
- Có tài liệu hướng dẫn cài đặt và sử dụng đầy đủ
- Source code được tổ chức tốt, có comment và dễ bảo trì

**📊 Mục tiêu 12: Khả năng demo và trình bày**
- Chuẩn bị các test case để demo đầy đủ chức năng
- Có file audio/video mẫu cho cả tiếng Việt và tiếng Anh
- Thống kê các chỉ số hiệu suất như thời gian xử lý, độ chính xác
- So sánh trước và sau tối ưu hóa (nếu có)

**📊 Mục tiêu 13: Tài liệu và báo cáo**
- Viết báo cáo đầy đủ theo cấu trúc chuẩn của đề tài tốt nghiệp/đồ án
- Mô tả chi tiết kiến trúc hệ thống, luồng hoạt động
- Giải thích các quyết định thiết kế và công nghệ được chọn
- Đánh giá kết quả đạt được và hướng phát triển

---

### 3. Ý NGHĨA CỦA ĐỀ TÀI

Đề tài này không chỉ giúp sinh viên củng cố kiến thức về lập trình mạng mà còn mang lại những giá trị thực tiễn:

**📚 Về mặt học thuật:**
- Minh họa cụ thể cách áp dụng lý thuyết lập trình mạng vào thực tế
- Giúp hiểu rõ về các vấn đề concurrent programming và asynchronous processing
- Rèn luyện kỹ năng giải quyết vấn đề và tư duy hệ thống

**💼 Về mặt thực tiễn:**
- Tạo ra một công cụ hữu ích có thể sử dụng trong thực tế
- Có thể ứng dụng vào các lĩnh vực như:
  - Phụ đề tự động cho video giáo dục
  - Ghi chép nội dung cuộc họp, hội thảo
  - Hỗ trợ người khuyết tật (hearing impaired)
  - Lưu trữ và tìm kiếm nội dung audio/video

**🚀 Về mặt phát triển cá nhân:**
- Nâng cao kỹ năng lập trình Java và web development
- Học cách làm việc với các công nghệ mới (Vosk, FFmpeg)
- Phát triển khả năng tự học và giải quyết vấn đề kỹ thuật
- Chuẩn bị hành trang cho công việc thực tế sau khi tốt nghiệp

---

### 4. PHẠM VI VÀ GIỚI HẠN CỦA ĐỀ TÀI

#### 4.1. Phạm vi thực hiện

Đề tài tập trung vào các khía cạnh sau:

✔️ **Xử lý file media:** Audio formats (WAV, MP3) và Video formats (MP4, AVI)  
✔️ **Ngôn ngữ hỗ trợ:** Tiếng Việt và Tiếng Anh  
✔️ **Công nghệ sử dụng:** Java Servlet/JSP, MySQL/H2 Database, Vosk Speech-to-Text  
✔️ **Triển khai:** Ứng dụng web chạy trên Tomcat Server  
✔️ **Người dùng:** Hệ thống multi-user với authentication  

#### 4.2. Giới hạn của đề tài

Do thời gian và phạm vi đồ án môn học, đề tài có một số giới hạn:

❌ **Không hỗ trợ streaming:** Chỉ xử lý file đã upload, không xử lý real-time streaming  
❌ **Giới hạn file size:** Không xử lý file quá lớn (>100MB) do hạn chế server resources  
❌ **Độ chính xác:** Phụ thuộc vào chất lượng audio và model Vosk, chưa đạt mức thương mại  
❌ **Không có mobile app:** Chỉ phát triển web application, chưa có app cho iOS/Android  
❌ **Ngôn ngữ giới hạn:** Chỉ hỗ trợ 2 ngôn ngữ chính, có thể mở rộng trong tương lai  

---

### 5. TỔNG QUAN VỀ CẤU TRÚC BÁO CÁO

Báo cáo này được tổ chức thành các phần chính sau:

**📖 PHẦN 1: TỔNG QUAN VỀ HỆ THỐNG**
- Giới thiệu các công nghệ sử dụng
- Kiến trúc tổng quan của hệ thống
- Các thành phần chính và chức năng

**📖 PHẦN 2: PHÂN TÍCH VÀ THIẾT KẾ**
- Phân tích yêu cầu chức năng và phi chức năng
- Thiết kế database schema
- Thiết kế kiến trúc MVC chi tiết
- Thiết kế luồng xử lý bất đồng bộ

**📖 PHẦN 3: TRIỂN KHAI HỆ THỐNG**
- Cài đặt môi trường phát triển
- Triển khai các module chính
- Tích hợp Speech-to-Text với Vosk
- Xây dựng giao diện người dùng

**📖 PHẦN 4: TỐI ƯU HÓA VÀ TESTING**
- Các vấn đề gặp phải và cách giải quyết
- Tối ưu hóa hiệu suất (Connection Pool, Caching, Shared Models)
- Test cases và kết quả kiểm thử
- Đánh giá hiệu suất trước và sau tối ưu

**📖 PHẦN 5: KẾT LUẬN**
- Tổng kết những gì đã đạt được
- Đánh giá ưu điểm và hạn chế
- Hướng phát triển trong tương lai
- Bài học kinh nghiệm

---

## KẾT LUẬN PHẦN MỞ ĐẦU

Qua phần mở đầu này, chúng ta đã làm rõ **mục đích** và **mục tiêu** của đề tài. Hệ thống được xây dựng không chỉ nhằm đáp ứng yêu cầu học tập mà còn hướng tới việc tạo ra một sản phẩm có giá trị thực tiễn. Với việc kết hợp các công nghệ hiện đại như Java Servlet, Vosk Speech-to-Text, và các kỹ thuật xử lý bất đồng bộ, đề tài đã đạt được các mục tiêu đề ra và mở ra nhiều hướng phát triển tiềm năng trong tương lai.

Trong các phần tiếp theo, báo cáo sẽ đi sâu vào chi tiết kỹ thuật, trình bày quá trình phân tích, thiết kế, triển khai và đánh giá hệ thống một cách đầy đủ và chi tiết.

---

*Tài liệu này được tạo tự động bởi GitHub Copilot dựa trên phân tích source code và documentation của dự án.*
