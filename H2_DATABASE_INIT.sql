-- ===================================================================
-- H2 DATABASE INITIALIZATION SCRIPT
-- Script khởi tạo database cho H2 (thay thế MySQL)
-- ===================================================================

-- Tạo bảng User
CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng Task
CREATE TABLE IF NOT EXISTS Task (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    server_file_path VARCHAR(500) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    language VARCHAR(10) DEFAULT 'vi',
    result_text TEXT,
    submission_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_time TIMESTAMP NULL,
    processing_time_ms INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Tạo index để tăng tốc truy vấn
CREATE INDEX IF NOT EXISTS idx_task_user_id ON Task(user_id);
CREATE INDEX IF NOT EXISTS idx_task_status ON Task(status);
CREATE INDEX IF NOT EXISTS idx_task_language ON Task(language);

-- Insert dữ liệu mẫu
-- User demo
INSERT INTO user (username, password, email) VALUES 
('admin', 'admin123', 'admin@example.com'),
('demo', 'demo123', 'demo@example.com');

-- Task demo
INSERT INTO Task (user_id, file_name, server_file_path, status, language, result_text, submission_time, completion_time, processing_time_ms) VALUES 
(1, 'sample_vi.mp3', '/uploads/sample_vi.mp3', 'COMPLETED', 'vi', 'Đây là bản ghi âm tiếng Việt mẫu.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5000),
(1, 'sample_en.mp3', '/uploads/sample_en.mp3', 'COMPLETED', 'en', 'This is a sample English audio recording.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4500),
(2, 'test_vi.wav', '/uploads/test_vi.wav', 'PENDING', 'vi', NULL, CURRENT_TIMESTAMP, NULL, 0);

-- Hiển thị kết quả
SELECT 'Database initialized successfully!' AS message;
SELECT * FROM user;
SELECT * FROM Task;
