<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User,utils.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Trang chính");
    User currentUser = (User) session.getAttribute("user");
    String role = currentUser == null ? "" : currentUser.getRole();
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="row justify-content-center">
    <div class="col-md-7">
        <div class="app-card text-center">
            <h2>Đăng nhập thành công</h2>
            <p class="mt-3 mb-1">Xin chào, <strong><%= HtmlUtil.escape(currentUser == null ? "" : currentUser.getFullName()) %></strong></p>
            <p>Email: <%= HtmlUtil.escape(currentUser == null ? "" : currentUser.getEmail()) %></p>
            <div class="alert alert-info mt-4"><strong>Role hiện tại:</strong> <%= HtmlUtil.escape(role) %></div>
            <% if ("ADMIN".equals(role)) { %><p>Đây là khu vực kiểm tra tài khoản ADMIN.</p>
            <% } else if ("USER".equals(role)) { %><p>Đây là khu vực kiểm tra tài khoản USER.</p>
            <% } %>
            <p class="mt-3 text-muted">
                TRIP_OWNER được xác định bằng <code>Trips.owner_id</code> và
                MEMBER được xác định bằng <code>TripMembers</code> theo từng chuyến đi.
            </p>
            <a href="<%= request.getContextPath() %>/logout" class="btn btn-brand mt-3">Đăng xuất</a>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
