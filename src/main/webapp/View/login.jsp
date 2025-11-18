<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - Media Video</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: Arial, sans-serif;
            background: #f5f5f5;
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        
        .login-container {
            background: white;
            padding: 30px;
            border-radius: 8px;
            border: 1px solid #e0e0e0;
            width: 100%;
            max-width: 380px;
        }
        
        .login-header {
            text-align: center;
            margin-bottom: 25px;
        }
        
        .login-header h1 {
            color: #333;
            font-size: 22px;
            margin-bottom: 6px;
        }
        
        .login-header p {
            color: #666;
            font-size: 13px;
        }
        
        .form-group {
            margin-bottom: 16px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 6px;
            color: #333;
            font-weight: bold;
            font-size: 13px;
        }
        
        .form-group input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 13px;
        }
        
        .form-group input:focus {
            outline: none;
            border-color: #4a5568;
        }
        
        .error-message {
            background-color: #fee;
            color: #c33;
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 16px;
            font-size: 13px;
            border-left: 3px solid #c33;
        }
        
        .success-message {
            background-color: #efe;
            color: #2a7;
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 16px;
            font-size: 13px;
            border-left: 3px solid #2a7;
        }
        
        .btn-login {
            width: 100%;
            padding: 10px;
            background: #4a5568;
            color: white;
            border: none;
            border-radius: 4px;
            font-size: 14px;
            font-weight: bold;
            cursor: pointer;
        }
        
        .btn-login:hover {
            background: #2d3748;
        }
        
        .register-link {
            text-align: center;
            margin-top: 16px;
            color: #666;
            font-size: 13px;
        }
        
        .register-link a {
            color: #4a5568;
            text-decoration: none;
            font-weight: bold;
        }
        
        .register-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-header">
            <h1>Đăng nhập</h1>
            <p>Media Video Processor</p>
        </div>
        
        <% 
            String error = (String) request.getAttribute("error");
            String message = request.getParameter("message");
            String username = (String) request.getAttribute("username");
            if (username == null) username = "";
        %>
        
        <% if (error != null && !error.isEmpty()) { %>
            <div class="error-message">
                <%= error %>
            </div>
        <% } %>
        
        <% if ("logout".equals(message)) { %>
            <div class="success-message">
                Bạn đã đăng xuất thành công!
            </div>
        <% } %>
        
        <% if ("register_success".equals(message)) { %>
            <div class="success-message">
                Đăng ký thành công! Vui lòng đăng nhập.
            </div>
        <% } %>
        
        <form action="<%= request.getContextPath() %>/login" method="post">
            <div class="form-group">
                <label for="username">Tên đăng nhập</label>
                <input type="text" id="username" name="username" 
                       value="<%= username %>" 
                       placeholder="Nhập tên đăng nhập" 
                       required autofocus>
            </div>
            
            <div class="form-group">
                <label for="password">Mật khẩu</label>
                <input type="password" id="password" name="password" 
                       placeholder="Nhập mật khẩu" 
                       required>
            </div>
            
            <button type="submit" class="btn-login">Đăng nhập</button>
        </form>
        
        <div class="register-link">
            Chưa có tài khoản? <a href="<%= request.getContextPath() %>/register">Đăng ký ngay</a>
        </div>
    </div>
</body>
</html>