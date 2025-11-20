<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="Model.Bean.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang chủ - Speech to Text</title>
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
        }
        
        .navbar {
            background: #4a5568;
            color: white;
            padding: 15px 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .navbar-brand {
            font-size: 18px;
            font-weight: bold;
        }
        
        .navbar-user {
            display: flex;
            align-items: center;
            gap: 15px;
        }
        
        .user-info {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
        }
        
        .user-avatar {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: white;
            color: #4a5568;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            font-size: 14px;
        }
        
        .btn-logout {
            padding: 6px 14px;
            background: white;
            color: #4a5568;
            border: none;
            border-radius: 4px;
            text-decoration: none;
            font-size: 13px;
        }
        
        .btn-logout:hover {
            background: #e2e8f0;
        }
        
        .container {
            max-width: 1000px;
            margin: 20px auto;
            padding: 0 20px;
        }
        
        .welcome-card {
            background: white;
            padding: 25px;
            border-radius: 8px;
            border: 1px solid #e0e0e0;
            text-align: center;
            margin-bottom: 20px;
        }
        
        .welcome-card h1 {
            color: #333;
            margin-bottom: 8px;
            font-size: 24px;
        }
        
        .welcome-card p {
            color: #666;
            font-size: 14px;
        }
        
        .features-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 15px;
            margin-bottom: 20px;
        }
        
        .feature-card {
            background: white;
            padding: 25px 20px;
            border-radius: 8px;
            border: 1px solid #e0e0e0;
            text-align: center;
        }
        
        .feature-card:hover {
            border-color: #4a5568;
        }
        
        .feature-icon {
            font-size: 40px;
            margin-bottom: 12px;
        }
        
        .feature-card h3 {
            color: #333;
            margin-bottom: 8px;
            font-size: 18px;
        }
        
        .feature-card p {
            color: #666;
            margin-bottom: 15px;
            font-size: 13px;
            line-height: 1.5;
        }
        
        .btn-primary {
            padding: 8px 20px;
            background: #4a5568;
            color: white;
            border: none;
            border-radius: 4px;
            text-decoration: none;
            display: inline-block;
            font-size: 13px;
        }
        
        .btn-primary:hover {
            background: #2d3748;
        }
        
        .user-details {
            background: white;
            padding: 25px;
            border-radius: 8px;
            border: 1px solid #e0e0e0;
        }
        
        .user-details h2 {
            color: #333;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 2px solid #4a5568;
            font-size: 18px;
        }
        
        .detail-row {
            display: flex;
            padding: 10px 0;
            border-bottom: 1px solid #f0f0f0;
        }
        
        .detail-row:last-child {
            border-bottom: none;
        }
        
        .detail-label {
            font-weight: bold;
            color: #555;
            width: 150px;
            font-size: 13px;
        }
        
        .detail-value {
            color: #333;
            flex: 1;
            font-size: 13px;
        }
        
        .footer {
            text-align: center;
            padding: 15px;
            color: #666;
            margin-top: 20px;
            font-size: 12px;
        }
        
        @media (max-width: 768px) {
            .navbar {
                flex-direction: column;
                gap: 10px;
            }
            
            .welcome-card h1 {
                font-size: 20px;
            }
            
            .features-grid {
                grid-template-columns: 1fr;
            }
            
            .detail-row {
                flex-direction: column;
                gap: 5px;
            }
            
            .detail-label {
                width: 100%;
            }
        }
    </style>
</head>
<body>
    <%
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String firstLetter = user.getUsername().substring(0, 1).toUpperCase();
    %>
    
    <nav class="navbar">
        <div class="navbar-brand">
            🎬 Speech to Text System
        </div>
        <div class="navbar-user">
            <div class="user-info">
                <div class="user-avatar"><%= firstLetter %></div>
                <span><%= user.getUsername() %></span>
            </div>
            <a href="<%= request.getContextPath() %>/View/logout.jsp" class="btn-logout">Đăng xuất</a>
        </div>
    </nav>
    
    <div class="container">
        <div class="welcome-card">
            <h1>👋 Chào mừng, <%= user.getUsername() %>!</h1>
            <p>Hệ thống chuyển đổi giọng nói thành văn bản - Hỗ trợ Tiếng Việt & Tiếng Anh</p>
        </div>
        
        <div class="features-grid">
            <div class="feature-card">
                <div class="feature-icon">📤</div>
                <h3>Upload Media</h3>
                <p>Upload file video hoặc audio để chuyển đổi thành văn bản. Hỗ trợ nhiều định dạng: MP4, MP3, WAV, M4A...</p>
                <a href="<%= request.getContextPath() %>/upload-tcp" class="btn-primary">Bắt đầu Upload</a>
            </div>
            
            <div class="feature-card">
                <div class="feature-icon">📊</div>
                <h3>Lịch Sử Tasks</h3>
                <p>Xem lịch sử và kết quả xử lý của tất cả các file đã upload. Theo dõi tiến trình xử lý real-time.</p>
                <a href="<%= request.getContextPath() %>/history" class="btn-primary">Xem Lịch Sử</a>
            </div>
        </div>
        
        <div class="user-details">
            <h2>📋 Thông tin tài khoản</h2>
            <div class="detail-row">
                <div class="detail-label">🆔 User ID:</div>
                <div class="detail-value"><%= user.getId() %></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">👤 Tên đăng nhập:</div>
                <div class="detail-value"><%= user.getUsername() %></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">📧 Email:</div>
                <div class="detail-value"><%= user.getEmail() != null ? user.getEmail() : "Chưa cập nhật" %></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">📅 Ngày tạo:</div>
                <div class="detail-value"><%= user.getCreatedAt() != null ? user.getCreatedAt() : "N/A" %></div>
            </div>
        </div>
        
        <div class="footer">
            <p>🚀 Speech to Text System - Powered by Vosk AI | © 2025</p>
        </div>
    </div>
</body>
</html>
