<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User,utils.HtmlUtil" %>
<% request.setAttribute("pageTitle", "Không đủ quyền"); %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="row justify-content-center"><div class="col-md-6"><div class="app-card text-center">
    <div class="display-5 mb-2">🔒</div>
    <h4 class="mb-2">Bạn không có quyền truy cập</h4>
    <p class="text-muted">Bạn không có quyền truy cập URL này với role hiện tại.</p>
    <a href="<%= request.getContextPath() %>/login" class="btn btn-brand">Về trang đăng nhập</a>
</div></div></div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
