<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "Đăng nhập");
    String emailValue = request.getParameter("email");
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="row justify-content-center">
    <div class="col-md-5">
        <div class="app-card">
            <h3 class="mb-4 text-center">Đăng nhập</h3>
            <form method="post" action="<%= request.getContextPath() %>/login">
                <div class="mb-3">
                    <label class="form-label">Email</label>
                    <input type="email" name="email" class="form-control"
                           value="<%= utils.HtmlUtil.escape(emailValue) %>" required autofocus autocomplete="email">
                </div>
                <div class="mb-3">
                    <label class="form-label">Mật khẩu</label>
                    <input type="password" name="password" class="form-control" required autocomplete="current-password">
                </div>
                <div class="form-check mb-3">
                    <input class="form-check-input" type="checkbox" name="rememberMe" id="rememberMe">
                    <label class="form-check-label" for="rememberMe">Ghi nhớ đăng nhập trên thiết bị này (30 ngày)</label>
                </div>
                <button type="submit" class="btn btn-brand w-100">Đăng nhập</button>
            </form>
            <div class="text-center mt-3">
                <a href="<%= request.getContextPath() %>/forgot-password">Quên mật khẩu?</a>
            </div>
            <p class="text-center mt-3 mb-0">Chưa có tài khoản? <a href="<%= request.getContextPath() %>/register">Đăng ký ngay</a></p>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
