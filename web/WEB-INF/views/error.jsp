<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ page import="utils.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Có lỗi xảy ra");
    Object statusObject = request.getAttribute("javax.servlet.error.status_code");
    String status = statusObject == null ? "" : String.valueOf(statusObject);
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="row justify-content-center"><div class="col-md-6"><div class="app-card text-center">
    <div class="display-5 mb-2">⚠️</div>
    <h4 class="mb-2"><%= "404".equals(status) ? "Không tìm thấy trang" : "Đã có lỗi xảy ra" %></h4>
    <p class="text-muted"><% Object detail = request.getAttribute("errorDetail"); if (detail != null) { %><%= HtmlUtil.escape(String.valueOf(detail)) %><% } else if ("404".equals(status)) { %>Trang hoặc dữ liệu bạn yêu cầu không tồn tại.<% } else { %>Hệ thống gặp sự cố, vui lòng thử lại sau.<% } %></p>
    <a href="<%= request.getContextPath() %>/login" class="btn btn-brand">Về trang đăng nhập</a>
</div></div></div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
