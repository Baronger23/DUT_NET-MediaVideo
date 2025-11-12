<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Xóa session
    HttpSession userSession = request.getSession(false);
    if (userSession != null) {
        userSession.invalidate();
    }
    
    // Chuyển về trang login
    response.sendRedirect(request.getContextPath() + "/login?message=logout");
%>
