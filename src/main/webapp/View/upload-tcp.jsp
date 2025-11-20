<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="Model.Bean.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    Boolean workerHealthy = (Boolean) request.getAttribute("workerHealthy");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload Media - TCP Socket Mode</title>
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
            max-width: 900px;
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
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .tcp-badge {
            display: inline-block;
            background: #2d3748;
            color: white;
            padding: 4px 10px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: bold;
        }
        
        .user-info {
            display: flex;
            align-items: center;
            gap: 15px;
            font-size: 13px;
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
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .server-status {
            padding: 12px 15px;
            border-radius: 6px;
            margin-bottom: 25px;
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 14px;
        }
        
        .server-status.healthy {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
        }
        
        .server-status.unhealthy {
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
        }
        
        .status-icon {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            display: inline-block;
        }
        
        .status-icon.green {
            background: #28a745;
            animation: pulse 2s infinite;
        }
        
        .status-icon.red {
            background: #dc3545;
        }
        
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
        }
        
        .form-section {
            margin-bottom: 20px;
        }
        
        .form-section label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 600;
            font-size: 14px;
        }
        
        .file-upload-area {
            border: 2px dashed #cbd5e0;
            border-radius: 8px;
            padding: 30px;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s;
            background: #f7fafc;
        }
        
        .file-upload-area:hover {
            border-color: #4a5568;
            background: #edf2f7;
        }
        
        .file-upload-area.dragover {
            border-color: #4a5568;
            background: #e2e8f0;
        }
        
        .file-upload-area input[type="file"] {
            display: none;
        }
        
        .file-icon {
            font-size: 40px;
            margin-bottom: 10px;
        }
        
        .file-name {
            color: #4a5568;
            font-weight: 600;
            margin-top: 10px;
        }
        
        select {
            width: 100%;
            padding: 10px;
            border: 1px solid #cbd5e0;
            border-radius: 6px;
            font-size: 14px;
            background: white;
        }
        
        select:focus {
            outline: none;
            border-color: #4a5568;
        }
        
        .submit-btn {
            width: 100%;
            padding: 12px;
            background: #4a5568;
            color: white;
            border: none;
            border-radius: 6px;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.3s;
        }
        
        .submit-btn:hover {
            background: #2d3748;
        }
        
        .submit-btn:disabled {
            background: #cbd5e0;
            cursor: not-allowed;
        }
        
        .note {
            font-size: 12px;
            color: #999;
            margin-top: 5px;
        }
        
        .progress-section {
            display: none;
            margin-top: 20px;
        }
        
        .progress-bar {
            width: 100%;
            height: 8px;
            background: #e2e8f0;
            border-radius: 4px;
            overflow: hidden;
        }
        
        .progress-fill {
            height: 100%;
            background: #4a5568;
            width: 0%;
            transition: width 0.3s;
        }
        
        .progress-text {
            text-align: center;
            margin-top: 8px;
            color: #666;
            font-size: 13px;
        }
        
        .result-box {
            display: none;
            margin-top: 20px;
            padding: 15px;
            border-radius: 6px;
            font-size: 14px;
        }
        
        .result-box.success {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
        }
        
        .result-box.error {
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
        }
        
        .info-section {
            background: #f7fafc;
            border-left: 3px solid #4a5568;
            padding: 15px;
            margin-top: 25px;
            border-radius: 4px;
        }
        
        .info-section h4 {
            margin-bottom: 10px;
            color: #2d3748;
            font-size: 14px;
        }
        
        .info-section ul {
            margin-left: 20px;
            font-size: 13px;
            color: #666;
        }
        
        .info-section li {
            margin: 5px 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>
                🎤 Upload Media File
                <span class="tcp-badge">TCP SOCKET</span>
            </h1>
            <div class="user-info">
                <span>👤 <%= user.getUsername() %></span>
                <a href="${pageContext.request.contextPath}/history" class="btn btn-secondary">📋 Lịch Sử</a>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary">🚪 Đăng Xuất</a>
            </div>
        </div>
        
        <!-- Main Content -->
        <div class="main-content">
            <!-- Server Status -->
            <% if (workerHealthy != null && workerHealthy) { %>
                <div class="server-status healthy">
                    <span class="status-icon green"></span>
                    <strong>Worker Server: ONLINE</strong>
                    <span style="margin-left: auto;">✅ Sẵn sàng xử lý</span>
                </div>
            <% } else { %>
                <div class="server-status unhealthy">
                    <span class="status-icon red"></span>
                    <strong>Worker Server: OFFLINE</strong>
                    <span style="margin-left: auto;">⚠️ Không kết nối được server</span>
                </div>
            <% } %>
        
            <!-- Upload Form -->
            <form id="uploadForm" enctype="multipart/form-data">
                <!-- File Upload -->
                <div class="form-section">
                    <label>📁 Chọn File Media</label>
                    <div class="file-upload-area" id="fileUploadArea">
                        <input type="file" id="mediaFile" name="mediaFile" accept="audio/*,video/*" required>
                        <div class="file-icon">📎</div>
                        <p><strong>Nhấn để chọn file</strong> hoặc kéo thả vào đây</p>
                        <p class="note">Hỗ trợ: MP3, WAV, M4A, MP4, AVI, MOV (Tối đa 100MB)</p>
                        <div class="file-name" id="fileName"></div>
                    </div>
                </div>
                
                <!-- Language Select -->
                <div class="form-section">
                    <label>🌍 Chọn Ngôn Ngữ</label>
                    <select id="language" name="language">
                        <option value="vi">🇻🇳 Tiếng Việt</option>
                        <option value="en">🇺🇸 English</option>
                    </select>
                </div>
                
                <!-- Submit Button -->
                <button type="submit" class="submit-btn" id="submitBtn">
                    🚀 Upload và Xử Lý qua TCP
                </button>
            </form>
            
            <!-- Progress -->
            <div class="progress-section" id="progressSection">
                <div class="progress-bar">
                    <div class="progress-fill" id="progressFill"></div>
                </div>
                <div class="progress-text" id="progressText">Đang tải lên...</div>
            </div>
            
            <!-- Result -->
            <div class="result-box" id="resultBox"></div>
            
            <!-- Info Section -->
            <div class="info-section">
                <h4>ℹ️ Về Kiến Trúc TCP Socket</h4>
                <ul>
                    <li><strong>Web Server:</strong> Nhận file upload, tạo task trong database</li>
                    <li><strong>TCP Socket:</strong> Gửi task đến Worker Server (port 9999)</li>
                    <li><strong>Worker Server:</strong> Xử lý Speech-to-Text độc lập</li>
                    <li><strong>Ưu điểm:</strong> Phân tán, mở rộng dễ dàng, chịu lỗi tốt</li>
                </ul>
            </div>
        </div>
    </div>
    
    <script>
        const form = document.getElementById('uploadForm');
        const fileInput = document.getElementById('mediaFile');
        const fileUploadArea = document.getElementById('fileUploadArea');
        const fileName = document.getElementById('fileName');
        const submitBtn = document.getElementById('submitBtn');
        const progressSection = document.getElementById('progressSection');
        const progressFill = document.getElementById('progressFill');
        const progressText = document.getElementById('progressText');
        const resultBox = document.getElementById('resultBox');
        
        // Click to upload
        fileUploadArea.addEventListener('click', function() {
            fileInput.click();
        });
        
        // Show selected file name
        fileInput.addEventListener('change', function(e) {
            e.stopPropagation();
            if (this.files && this.files[0]) {
                const file = this.files[0];
                const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
                fileName.textContent = '✅ ' + file.name + ' (' + sizeMB + ' MB)';
            }
        });
        
        // Drag and drop
        fileUploadArea.addEventListener('dragover', function(e) {
            e.preventDefault();
            this.classList.add('dragover');
        });
        
        fileUploadArea.addEventListener('dragleave', function(e) {
            e.preventDefault();
            this.classList.remove('dragover');
        });
        
        fileUploadArea.addEventListener('drop', function(e) {
            e.preventDefault();
            this.classList.remove('dragover');
            
            if (e.dataTransfer.files.length > 0) {
                fileInput.files = e.dataTransfer.files;
                const file = e.dataTransfer.files[0];
                const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
                fileName.textContent = '✅ ' + file.name + ' (' + sizeMB + ' MB)';
            }
        });
        
        // Handle form submit
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            if (!fileInput.files || !fileInput.files[0]) {
                alert('Vui lòng chọn file');
                return;
            }
            
            // Check file size
            const fileSizeMB = fileInput.files[0].size / (1024 * 1024);
            if (fileSizeMB > 100) {
                alert('File quá lớn! Vui lòng chọn file dưới 100MB');
                return;
            }
            
            // Disable form
            submitBtn.disabled = true;
            submitBtn.textContent = '⏳ Đang xử lý...';
            
            // Show progress
            progressSection.style.display = 'block';
            resultBox.style.display = 'none';
            
            const formData = new FormData();
            formData.append('mediaFile', fileInput.files[0]);
            formData.append('language', document.getElementById('language').value);
            
            try {
                // Simulate progress
                let progress = 0;
                const progressInterval = setInterval(() => {
                    progress += 5;
                    if (progress >= 90) clearInterval(progressInterval);
                    updateProgress(progress, 'Đang tải lên...');
                }, 200);
                
                // Send request
                const response = await fetch('${pageContext.request.contextPath}/upload-tcp', {
                    method: 'POST',
                    body: formData
                });
                
                clearInterval(progressInterval);
                updateProgress(100, 'Hoàn tất!');
                
                const result = await response.json();
                
                setTimeout(() => {
                    progressSection.style.display = 'none';
                    
                    if (result.success) {
                        const langText = result.language == 'vi' ? 'Tiếng Việt' : 'English';
                        showResult('success', 
                            '✅ <strong>Upload thành công!</strong><br>' +
                            'Task ID: ' + result.taskId + '<br>' +
                            'File: ' + result.fileName + '<br>' +
                            'Ngôn ngữ: ' + langText + '<br>' +
                            '<br>Task đang được xử lý trên Worker Server. <a href="${pageContext.request.contextPath}/history">Xem lịch sử →</a>'
                        );
                        
                        // Reset form
                        form.reset();
                        fileName.textContent = '';
                    } else {
                        showResult('error', '❌ <strong>Lỗi:</strong> ' + (result.error || 'Không thể upload file'));
                    }
                    
                    // Re-enable form
                    submitBtn.disabled = false;
                    submitBtn.textContent = '🚀 Upload và Xử Lý qua TCP';
                }, 500);
                
            } catch (error) {
                console.error('Upload error:', error);
                progressSection.style.display = 'none';
                showResult('error', '❌ <strong>Lỗi kết nối:</strong> ' + error.message);
                
                submitBtn.disabled = false;
                submitBtn.textContent = '🚀 Upload và Xử Lý qua TCP';
            }
        });
        
        function updateProgress(percent, text) {
            progressFill.style.width = percent + '%';
            progressText.textContent = text + ' (' + percent + '%)';
        }
        
        function showResult(type, message) {
            resultBox.className = 'result-box ' + type;
            resultBox.innerHTML = message;
            resultBox.style.display = 'block';
        }
    </script>
</body>
</html>
