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
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 800px;
            margin: 0 auto;
        }
        
        .header {
            background: white;
            padding: 20px;
            border-radius: 10px 10px 0 0;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .header h1 {
            color: #667eea;
            font-size: 24px;
        }
        
        .user-info {
            display: flex;
            align-items: center;
            gap: 15px;
        }
        
        .nav-links {
            display: flex;
            gap: 10px;
        }
        
        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            text-decoration: none;
            display: inline-block;
            transition: all 0.3s;
        }
        
        .btn-primary {
            background: #667eea;
            color: white;
        }
        
        .btn-primary:hover {
            background: #5568d3;
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #5a6268;
        }
        
        .main-content {
            background: white;
            padding: 40px;
            border-radius: 0 0 10px 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        
        .alert {
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
            font-weight: 500;
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
            padding: 30px;
            border-radius: 10px;
            border: 2px dashed #667eea;
            text-align: center;
            margin-top: 20px;
        }
        
        .upload-icon {
            font-size: 64px;
            color: #667eea;
            margin-bottom: 20px;
        }
        
        .file-input-wrapper {
            position: relative;
            overflow: hidden;
            display: inline-block;
            margin: 20px 0;
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
            padding: 15px 30px;
            background: #667eea;
            color: white;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            transition: all 0.3s;
        }
        
        .file-input-label:hover {
            background: #5568d3;
        }
        
        .file-name {
            margin-top: 10px;
            color: #666;
            font-size: 14px;
        }
        
        .submit-btn {
            padding: 15px 40px;
            background: #28a745;
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
            margin-top: 20px;
            transition: all 0.3s;
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
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
            border-left: 4px solid #667eea;
        }
        
        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-top: 30px;
        }
        
        .info-card {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }
        
        .info-card h3 {
            color: #667eea;
            margin-bottom: 10px;
        }
        
        .info-card p {
            color: #666;
            font-size: 14px;
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
