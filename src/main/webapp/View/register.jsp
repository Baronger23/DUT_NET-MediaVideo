<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - Media Video</title>
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
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        
        .register-container {
            background: white;
            padding: 30px;
            border-radius: 8px;
            border: 1px solid #e0e0e0;
            width: 100%;
            max-width: 420px;
        }
        
        .register-header {
            text-align: center;
            margin-bottom: 25px;
        }
        
        .register-header h1 {
            color: #333;
            font-size: 22px;
            margin-bottom: 6px;
        }
        
        .register-header p {
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
        
        .form-group label .required {
            color: #c33;
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
        
        .password-hint {
            font-size: 12px;
            color: #666;
            margin-top: 4px;
        }
        
        .btn-register {
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
        
        .btn-register:hover {
            background: #2d3748;
        }
        
        .login-link {
            text-align: center;
            margin-top: 16px;
            color: #666;
            font-size: 13px;
        }
        
        .login-link a {
            color: #4a5568;
            text-decoration: none;
            font-weight: bold;
        }
        
        .login-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="register-container">
        <div class="register-header">
            <h1>Đăng ký tài khoản</h1>
            <p>Tạo tài khoản mới để sử dụng hệ thống</p>
        </div>
        
        <% 
            String error = (String) request.getAttribute("error");
            String username = (String) request.getAttribute("username");
            String email = (String) request.getAttribute("email");
            if (username == null) username = "";
            if (email == null) email = "";
        %>
        
        <% if (error != null && !error.isEmpty()) { %>
            <div class="error-message">
                <%= error %>
            </div>
        <% } %>
        
        <form action="<%= request.getContextPath() %>/register" method="post">
            <div class="form-group">
                <label for="username">Tên đăng nhập <span class="required">*</span></label>
                <input type="text" id="username" name="username" 
                       value="<%= username %>" 
                       placeholder="Nhập tên đăng nhập" 
                       required autofocus>
            </div>
            
            <div class="form-group">
                <label for="email">Email <span class="required">*</span></label>
                <input type="email" id="email" name="email" 
                       value="<%= email %>" 
                       placeholder="Nhập địa chỉ email" 
                       required>
            </div>
            
            <div class="form-group">
                <label for="password">Mật khẩu <span class="required">*</span></label>
                <input type="password" id="password" name="password" 
                       placeholder="Nhập mật khẩu" 
                       required>
                <div class="password-hint">Mật khẩu phải có ít nhất 6 ký tự</div>
            </div>
            
            <div class="form-group">
                <label for="confirmPassword">Xác nhận mật khẩu <span class="required">*</span></label>
                <input type="password" id="confirmPassword" name="confirmPassword" 
                       placeholder="Nhập lại mật khẩu" 
                       required>
            </div>
            
            <button type="submit" class="btn-register">Đăng ký</button>
        </form>
        
        <div class="login-link">
            Đã có tài khoản? <a href="<%= request.getContextPath() %>/login">Đăng nhập ngay</a>
        </div>
    </div>
</body>
</html>
