<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Server-side redirect sang Servlet /login
    response.sendRedirect(request.getContextPath() + "/login");
%>