<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "Đặt lại mật khẩu");
    String token = (String) request.getAttribute("token");
    String error = (String) request.getAttribute("errorMessage");
    String success = (String) request.getAttribute("successMessage");
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="row justify-content-center">
    <div class="col-md-6">
        <div class="app-card">
            <h3 class="mb-3 text-center">Đặt lại mật khẩu</h3>

            <% if (error != null) {%>
            <div class="msg-error"><%= utils.HtmlUtil.escape(error)%></div>
            <% } %>

            <% if (success != null) {%>
            <div class="msg-success"><%= utils.HtmlUtil.escape(success)%></div>
            <div class="text-center mt-3">
                <a class="btn btn-brand" href="<%= request.getContextPath()%>/login">Đăng nhập</a>
            </div>
            <% } else if (token != null && !token.isEmpty()) {%>
            <form method="post" action="<%= request.getContextPath()%>/reset-password">
                <input type="hidden" name="token" value="<%= utils.HtmlUtil.escape(token)%>">
                <div class="mb-3">
                    <label class="form-label">Mật khẩu mới</label>
                    <input type="password"
                           name="password"
                           class="form-control"
                           minlength="8"
                           maxlength="64"
                           pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,64}"
                           title="Mật khẩu phải có 8–64 ký tự, gồm chữ hoa, chữ thường, chữ số và ký tự đặc biệt."
                           autocomplete="new-password"
                           required>
                </div>
                <div class="mb-3">
                    <label class="form-label">Xác nhận mật khẩu mới</label>
                    <input type="password" name="confirmPassword" class="form-control" minlength="8" required autocomplete="new-password">
                </div>
                <button type="submit" class="btn btn-brand w-100">Đặt lại mật khẩu</button>
            </form>
            <% } else {%>
            <div class="text-center mt-3">
                <a class="btn btn-brand" href="<%= request.getContextPath()%>/forgot-password">Yêu cầu liên kết mới</a>
            </div>
            <% }%>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
