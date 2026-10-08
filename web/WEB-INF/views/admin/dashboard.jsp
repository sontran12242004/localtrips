<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="utils.HtmlUtil" %>
<%
    model.User currentUser = (model.User) session.getAttribute("user");
    Integer userCount = (Integer) request.getAttribute("userCount");
    Integer activeUserCount = (Integer) request.getAttribute("activeUserCount");
    Integer tripCount = (Integer) request.getAttribute("tripCount");
    Integer placeCount = (Integer) request.getAttribute("placeCount");
    Integer categoryCount = (Integer) request.getAttribute("categoryCount");
    String dashboardError = (String) request.getAttribute("dashboardError");
%>
<% request.setAttribute("pageTitle", "Tổng quan quản trị"); %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
            <div class="page-intro">
                <span class="eyebrow">LOCALTRIP / KHÔNG GIAN QUẢN TRỊ</span>
                <h2 class="fw-bold">Xin chào, <%= HtmlUtil.escape(currentUser.getFullName())%> 👋</h2>
                <p class="text-muted">Tổng quan dữ liệu hệ thống LocalTrip.</p>
            </div>

            <% if (dashboardError != null) {%>
            <div class="alert alert-warning"><%= HtmlUtil.escape(dashboardError)%></div>
            <% }%>

            <div class="row g-4 mb-4">
                <div class="col-md-4"><div class="card shadow-sm h-100"><div class="card-body"><div class="text-muted">Tổng Users</div><div class="display-6 fw-bold"><%= userCount == null ? 0 : userCount%></div></div></div></div>
                <div class="col-md-4"><div class="card shadow-sm h-100"><div class="card-body"><div class="text-muted">Users đang hoạt động</div><div class="display-6 fw-bold"><%= activeUserCount == null ? 0 : activeUserCount%></div></div></div></div>
                <div class="col-md-4"><div class="card shadow-sm h-100"><div class="card-body"><div class="text-muted">Tổng Trips</div><div class="display-6 fw-bold"><%= tripCount == null ? 0 : tripCount%></div></div></div></div>
                <div class="col-md-6"><div class="card shadow-sm h-100"><div class="card-body"><div class="text-muted">Địa điểm đang hoạt động</div><div class="display-6 fw-bold"><%= placeCount == null ? 0 : placeCount%></div></div></div></div>
                <div class="col-md-6"><div class="card shadow-sm h-100"><div class="card-body"><div class="text-muted">Danh mục</div><div class="display-6 fw-bold"><%= categoryCount == null ? 0 : categoryCount%></div></div></div></div>
            </div>

            <div class="card shadow-sm">
                <div class="card-body">
                    <h5 class="fw-bold mb-3">Quản trị</h5>
                    <div class="d-flex flex-wrap gap-2">
                        <a href="<%= request.getContextPath()%>/admin/users" class="btn btn-outline-primary">Quản lý Users</a>
                        <a href="<%= request.getContextPath()%>/admin/places" class="btn btn-outline-primary">Quản lý Places</a>
                        <a href="<%= request.getContextPath()%>/admin/categories" class="btn btn-outline-primary">Quản lý Categories</a>
                        <a href="<%= request.getContextPath()%>/admin/audit-logs" class="btn btn-outline-dark"> Nhật ký quản trị </a>
                        <a href="${pageContext.request.contextPath}/admin/login-logs" class="btn btn-outline-dark"> Lịch sử đăng nhập </a>
                    </div>
                    <div class="alert alert-info mt-4 mb-0">
                        Các nút trên là điểm vào cho module quản trị tiếp theo; hiện tại authorization đã được bảo vệ theo role ADMIN.
                    </div>
                </div>
            </div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
