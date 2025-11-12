<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Redirect về trang login khi truy cập vào trang chủ
    response.sendRedirect(request.getContextPath() + "/login");
%>
