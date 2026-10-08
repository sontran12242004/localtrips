<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "Quên mật khẩu");
    String emailValue = request.getParameter("email");
    String devResetUrl = (String) request.getAttribute("devResetUrl");
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="row justify-content-center">
    <div class="col-md-6">
        <div class="app-card">
            <h3 class="mb-3 text-center">Quên mật khẩu</h3>
            <p class="text-muted">Nhập email đã đăng ký. Nếu tài khoản tồn tại, hệ thống sẽ tạo liên kết đặt lại mật khẩu có hiệu lực trong 30 phút.</p>
            <form method="post" action="<%= request.getContextPath() %>/forgot-password">
                <div class="mb-3">
                    <label class="form-label">Email</label>
                    <input type="email" name="email" class="form-control"
                           value="<%= utils.HtmlUtil.escape(emailValue) %>" required autocomplete="email">
                </div>
                <button type="submit" class="btn btn-brand w-100">Tạo liên kết đặt lại mật khẩu</button>
            </form>

            <% if (devResetUrl != null) { %>
                <div class="alert alert-warning mt-4 mb-0">
                    <strong>Development mode:</strong> project hiện chưa tích hợp SMTP/email service.
                    Bạn có thể dùng link dưới đây để test chức năng reset:
                    <div class="mt-2"><a href="<%= utils.HtmlUtil.escape(devResetUrl) %>"><%= utils.HtmlUtil.escape(devResetUrl) %></a></div>
                </div>
            <% } %>

            <div class="text-center mt-3">
                <a href="<%= request.getContextPath() %>/login">← Quay lại đăng nhập</a>
            </div>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
