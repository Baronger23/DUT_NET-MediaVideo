<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="Model.Bean.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
    Integer taskId = (Integer) request.getAttribute("taskId");
    Integer queueSize = (Integer) request.getAttribute("queueSize");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload Media - Speech to Text</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: Arial, sans-serif;
            background: #f5f5f5;
            min-height: 100vh;
            padding: 15px;
        }
        
        .container {
            max-width: 800px;
            margin: 0 auto;
        }
        
        .header {
            background: #4a5568;
            color: white;
            padding: 15px 20px;
            border-radius: 8px 8px 0 0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .header h1 {
            font-size: 20px;
        }
        
        .user-info {
            display: flex;
            align-items: center;
            gap: 15px;
            font-size: 13px;
        }
        
        .nav-links {
            display: flex;
            gap: 8px;
        }
        
        .btn {
            padding: 6px 14px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 13px;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn-primary {
            background: white;
            color: #4a5568;
        }
        
        .btn-primary:hover {
            background: #e2e8f0;
        }
        
        .btn-secondary {
            background: #2d3748;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #1a202c;
        }
        
        .main-content {
            background: white;
            padding: 25px;
            border-radius: 0 0 8px 8px;
            border: 1px solid #e0e0e0;
            border-top: none;
        }
        
        .alert {
            padding: 12px;
            border-radius: 4px;
            margin-bottom: 15px;
            font-size: 13px;
        }
        
        .alert-success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        
        .alert-error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        
        .alert-info {
            background: #d1ecf1;
            color: #0c5460;
            border: 1px solid #bee5eb;
        }
        
        .upload-section {
            background: #f8f9fa;
            padding: 25px;
            border-radius: 8px;
            border: 2px dashed #ccc;
            text-align: center;
            margin-top: 15px;
        }
        
        .upload-icon {
            font-size: 48px;
            margin-bottom: 15px;
        }
        
        .upload-section h2 {
            font-size: 18px;
            margin-bottom: 10px;
            color: #333;
        }
        
        .upload-section p {
            color: #666;
            font-size: 13px;
            margin: 8px 0;
        }
        
        .file-input-wrapper {
            position: relative;
            overflow: hidden;
            display: inline-block;
            margin: 15px 0;
        }
        
        .file-input-wrapper input[type=file] {
            font-size: 100px;
            position: absolute;
            left: 0;
            top: 0;
            opacity: 0;
            cursor: pointer;
        }
        
        .file-input-label {
            display: inline-block;
            padding: 10px 24px;
            background: #4a5568;
            color: white;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
        }
        
        .file-input-label:hover {
            background: #2d3748;
        }
        
        .file-name {
            margin-top: 8px;
            color: #666;
            font-size: 13px;
        }
        
        .submit-btn {
            padding: 10px 30px;
            background: #28a745;
            color: white;
            border: none;
            border-radius: 4px;
            font-size: 14px;
            cursor: pointer;
            margin-top: 15px;
        }
        
        .submit-btn:hover {
            background: #218838;
        }
        
        .submit-btn:disabled {
            background: #6c757d;
            cursor: not-allowed;
        }
        
        .queue-info {
            background: #e7f3ff;
            padding: 12px;
            border-radius: 4px;
            margin-bottom: 15px;
            border-left: 3px solid #4a5568;
            font-size: 13px;
        }
        
        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-top: 20px;
        }
        
        .info-card {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            border: 1px solid #e0e0e0;
            text-align: center;
        }
        
        .info-card h3 {
            color: #333;
            margin-bottom: 8px;
            font-size: 16px;
        }
        
        .info-card p {
            color: #666;
            font-size: 13px;
        }
        
        select {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 13px;
            background: white;
            cursor: pointer;
        }
        
        select:focus {
            outline: none;
            border-color: #4a5568;
        }
        
        label {
            display: block;
            margin-bottom: 6px;
            font-weight: bold;
            color: #333;
            font-size: 13px;
        }
        
        @media (max-width: 768px) {
            .header {
                flex-direction: column;
                gap: 10px;
            }
            
            .user-info {
                flex-direction: column;
                gap: 8px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎬 Upload Media File</h1>
            <div class="user-info">
                <span>👤 <%= user.getUsername() %></span>
                <div class="nav-links">
                    <a href="<%= request.getContextPath() %>/home" class="btn btn-secondary">Home</a>
                    <a href="<%= request.getContextPath() %>/history" class="btn btn-primary">Lịch sử</a>
                    <a href="<%= request.getContextPath() %>/logout" class="btn btn-secondary">Đăng xuất</a>
                </div>
            </div>
        </div>
        
        <div class="main-content">
            <% if (success != null) { %>
                <div class="alert alert-success">
                    ✅ <%= success %>
                    <% if (taskId != null) { %>
                        <br><br>
                        <a href="<%= request.getContextPath() %>/history" class="btn btn-primary">Xem lịch sử →</a>
                    <% } %>
                </div>
            <% } %>
            
            <% if (error != null) { %>
                <div class="alert alert-error">
                    ❌ <%= error %>
                </div>
            <% } %>
            
            <% if (queueSize != null) { %>
                <div class="queue-info">
                    📊 <strong>Số task đang chờ trong Queue:</strong> <%= queueSize %> task(s)
                </div>
            <% } %>
            
            <div class="alert alert-info">
                ℹ️ <strong>Hướng dẫn:</strong> Upload file video hoặc audio để chuyển đổi thành văn bản (Speech-to-Text)
            </div>
            
            <form action="<%= request.getContextPath() %>/upload" method="post" enctype="multipart/form-data" id="uploadForm">
                <div class="upload-section">
                    <div class="upload-icon">📁</div>
                    <h2>Chọn file media để upload</h2>
                    <p style="color: #666; margin: 10px 0;">Hỗ trợ: MP4, AVI, MOV, MP3, WAV, M4A</p>
                    
                    <!-- Chọn ngôn ngữ -->
                    <div style="margin: 20px 0; text-align: left; max-width: 400px; margin-left: auto; margin-right: auto;">
                        <label for="language" style="display: block; margin-bottom: 8px; font-weight: 600; color: #333;">
                            🌍 Chọn ngôn ngữ của file audio/video:
                        </label>
                        <select name="language" id="language" required style="width: 100%; padding: 12px; border: 2px solid #667eea; border-radius: 5px; font-size: 15px; background: white; cursor: pointer;">
                            <option value="vi">🇻🇳 Tiếng Việt (Vietnamese)</option>
                            <option value="en">🇺🇸 Tiếng Anh (English)</option>
                        </select>
                        <p style="font-size: 12px; color: #666; margin-top: 8px;">
                            💡 Chọn đúng ngôn ngữ để có kết quả chính xác nhất
                        </p>
                    </div>
                    
                    <div class="file-input-wrapper">
                        <label class="file-input-label">
                            Chọn File
                            <input type="file" name="mediaFile" id="mediaFile" accept="video/*,audio/*" required>
                        </label>
                    </div>
                    
                    <div class="file-name" id="fileName">Chưa chọn file</div>
                    
                    <button type="submit" class="submit-btn" id="submitBtn" disabled>
                        🚀 Upload và Xử lý
                    </button>
                </div>
            </form>
            
            <div class="info-grid">
                <div class="info-card">
                    <h3>📤 Bước 1</h3>
                    <p>Upload file video/audio của bạn</p>
                </div>
                <div class="info-card">
                    <h3>⏳ Bước 2</h3>
                    <p>Hệ thống xử lý Speech-to-Text</p>
                </div>
                <div class="info-card">
                    <h3>✅ Bước 3</h3>
                    <p>Xem kết quả tại trang Lịch sử</p>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        const fileInput = document.getElementById('mediaFile');
        const fileName = document.getElementById('fileName');
        const submitBtn = document.getElementById('submitBtn');
        const uploadForm = document.getElementById('uploadForm');
        
        fileInput.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                const file = this.files[0];
                const fileSize = (file.size / 1024 / 1024).toFixed(2); // MB
                fileName.textContent = file.name + ' (' + fileSize + ' MB)';
                fileName.style.color = '#28a745';
                submitBtn.disabled = false;
            } else {
                fileName.textContent = 'Chưa chọn file';
                fileName.style.color = '#666';
                submitBtn.disabled = true;
            }
        });
        
        uploadForm.addEventListener('submit', function() {
            submitBtn.disabled = true;
            submitBtn.textContent = '⏳ Đang upload...';
        });
    </script>
</body>
</html>
