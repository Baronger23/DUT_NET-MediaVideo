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
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
        }
        
        .navbar {
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(10px);
            color: white;
            padding: 15px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }
        
        .navbar-brand {
            font-size: 24px;
            font-weight: bold;
        }
        
        .navbar-user {
            display: flex;
            align-items: center;
            gap: 20px;
        }
        
        .user-info {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: white;
            color: #667eea;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            font-size: 18px;
        }
        
        .btn-logout {
            padding: 8px 20px;
            background: rgba(255, 255, 255, 0.2);
            color: white;
            border: 1px solid white;
            border-radius: 5px;
            text-decoration: none;
            transition: all 0.3s;
        }
        
        .btn-logout:hover {
            background: white;
            color: #667eea;
        }
        
        .container {
            max-width: 1200px;
            margin: 60px auto;
            padding: 0 20px;
        }
        
        .welcome-card {
            background: white;
            padding: 50px;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
            text-align: center;
            margin-bottom: 40px;
        }
        
        .welcome-card h1 {
            color: #333;
            margin-bottom: 15px;
            font-size: 36px;
        }
        
        .welcome-card p {
            color: #666;
            font-size: 18px;
        }
        
        .features-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
            gap: 30px;
            margin-bottom: 40px;
        }
        
        .feature-card {
            background: white;
            padding: 40px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
            text-align: center;
            transition: all 0.3s;
        }
        
        .feature-card:hover {
            transform: translateY(-10px);
            box-shadow: 0 15px 40px rgba(0, 0, 0, 0.25);
        }
        
        .feature-icon {
            font-size: 64px;
            margin-bottom: 20px;
        }
        
        .feature-card h3 {
            color: #333;
            margin-bottom: 15px;
            font-size: 24px;
        }
        
        .feature-card p {
            color: #666;
            margin-bottom: 25px;
            font-size: 15px;
            line-height: 1.6;
        }
        
        .btn-primary {
            padding: 12px 35px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 25px;
            text-decoration: none;
            display: inline-block;
            transition: all 0.3s;
            font-size: 16px;
            font-weight: 600;
        }
        
        .btn-primary:hover {
            transform: scale(1.05);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.5);
        }
        
        .user-details {
            background: white;
            padding: 35px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
        }
        
        .user-details h2 {
            color: #333;
            margin-bottom: 25px;
            padding-bottom: 15px;
            border-bottom: 3px solid #667eea;
            font-size: 24px;
        }
        
        .detail-row {
            display: flex;
            padding: 15px 0;
            border-bottom: 1px solid #eee;
        }
        
        .detail-row:last-child {
            border-bottom: none;
        }
        
        .detail-label {
            font-weight: 600;
            color: #555;
            width: 200px;
        }
        
        .detail-value {
            color: #333;
            flex: 1;
        }
        
        .footer {
            text-align: center;
            padding: 30px;
            color: white;
            margin-top: 40px;
            font-size: 14px;
            opacity: 0.9;
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
                <a href="<%= request.getContextPath() %>/upload" class="btn-primary">Bắt đầu Upload</a>
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
