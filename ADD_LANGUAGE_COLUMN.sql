-- ===================================================================
-- THÊM CỘT LANGUAGE VÀO BẢNG TASK
-- Để hỗ trợ nhiều ngôn ngữ (Tiếng Việt, Tiếng Anh, etc.)
-- ===================================================================

USE media_processor_db;

-- Thêm cột language vào bảng Task
ALTER TABLE Task 
ADD COLUMN language VARCHAR(10) DEFAULT 'vi' COMMENT 'Ngôn ngữ: vi=Tiếng Việt, en=Tiếng Anh';

-- Cập nhật các task cũ thành tiếng Việt
UPDATE Task SET language = 'vi' WHERE language IS NULL;

-- Kiểm tra kết quả
DESCRIBE Task;

SELECT * FROM Task LIMIT 5;
