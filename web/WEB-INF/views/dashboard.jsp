<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Trip" %>
<%@ page import="utils.HtmlUtil" %>
<%
    model.User currentUser = (model.User) session.getAttribute("user");
    Integer tripCount = (Integer) request.getAttribute("tripCount");
    Integer ownerCount = (Integer) request.getAttribute("ownerCount");
    Integer memberCount = (Integer) request.getAttribute("memberCount");
    List<Trip> trips = (List<Trip>) request.getAttribute("trips");
    String dashboardError = (String) request.getAttribute("dashboardError");
%>
<% request.setAttribute("pageTitle", "Tổng quan"); %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
    <section class="dashboard-hero">
        <div class="hero-copy">
            <span class="eyebrow">LOCALTRIP / SỔ TAY HÀNH TRÌNH</span>
            <h1>Chuyến đi đẹp hơn<br>khi có nhau.</h1>
            <p class="mt-3 mb-0">Xin chào, <strong><%= HtmlUtil.escape(currentUser.getFullName()) %></strong>.<br>Lên kế hoạch, khám phá địa điểm và chia sẻ chi phí cùng nhóm của bạn.</p>
            <div class="hero-actions">
                <a class="btn btn-brand" href="<%= request.getContextPath() %>/trip/create">+ Lên kế hoạch chuyến đi</a>
                <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/places">Khám phá địa điểm ↗</a>
            </div>
        </div>
    </section>

    <% if (dashboardError != null) { %>
        <div class="alert alert-warning"><%= HtmlUtil.escape(dashboardError) %></div>
    <% } %>

    <div class="row g-4 mb-4">
        <div class="col-md-4"><div class="card shadow-sm h-100"><div class="card-body"><div class="text-muted">Tổng số Trip</div><div class="display-6 fw-bold"><%= tripCount == null ? 0 : tripCount %></div></div></div></div>
        <div class="col-md-4"><div class="card shadow-sm h-100"><div class="card-body"><div class="text-muted">Trip bạn làm Owner</div><div class="display-6 fw-bold"><%= ownerCount == null ? 0 : ownerCount %></div></div></div></div>
        <div class="col-md-4"><div class="card shadow-sm h-100"><div class="card-body"><div class="text-muted">Trip bạn là Member</div><div class="display-6 fw-bold"><%= memberCount == null ? 0 : memberCount %></div></div></div></div>
    </div>

    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold mb-0">Trip gần đây</h5>
                <a class="btn btn-primary btn-sm" href="<%= request.getContextPath() %>/trip/create">+ Tạo Trip mới</a>
            </div>
            <% if (trips == null || trips.isEmpty()) { %>
                <div class="text-center py-5 text-muted">
                    <div class="fs-1">🧳</div>
                    <p class="mb-0">Bạn chưa có chuyến đi nào.</p>
                </div>
            <% } else { %>
                <div class="list-group list-group-flush">
                <% for (Trip trip : trips) {
                       boolean owner = trip.getOwnerId() == currentUser.getUserId(); %>
                    <div class="list-group-item px-0 d-flex justify-content-between align-items-center">
                        <div>
                            <a class="fw-semibold text-decoration-none" href="<%= request.getContextPath() %>/trip/detail?tripId=<%= trip.getTripId() %>">
                                <%= HtmlUtil.escape(trip.getTripName()) %>
                            </a>
                            <div class="small text-muted">
                                <%= HtmlUtil.escape(trip.getDestination()) %>
                                &nbsp;|&nbsp; <%= trip.getStartDate() %> → <%= trip.getEndDate() %>
                                &nbsp;|&nbsp; <%= trip.getBudget() %> đ
                            </div>
                        </div>
                        <span class="badge <%= owner ? "text-bg-primary" : "text-bg-secondary" %>"><%= owner ? "OWNER" : "MEMBER" %></span>
                    </div>
                <% } %>
                </div>
            <% } %>
        </div>
    </div>

    <div class="card shadow-sm">
        <div class="card-body">
            <h5 class="fw-bold mb-3">Lối tắt</h5>
            <a href="<%= request.getContextPath() %>/places" class="btn btn-outline-secondary me-2">Khám phá địa điểm</a>
            <a href="<%= request.getContextPath() %>/trips" class="btn btn-outline-secondary">My Trips</a>
        </div>
    </div>
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
