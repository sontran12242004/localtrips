<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="My Trips" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div class="page-intro mb-0"><span class="eyebrow">SỔ TAY HÀNH TRÌNH</span><h3 class="mb-1">Chuyến đi của bạn</h3><p class="text-muted">Những kế hoạch chung, những trải nghiệm riêng.</p></div>
    <a href="${pageContext.request.contextPath}/trip/create" class="btn btn-brand">+ Tạo Trip mới</a>
</div>

<c:choose>
    <c:when test="${empty trips}">
        <div class="empty-state app-card">
            <div class="empty-icon">🧳</div>
            <p class="mb-1">Bạn chưa có chuyến đi nào.</p>
            <p class="text-muted">Bấm "Tạo Trip mới" để bắt đầu lên kế hoạch.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row">
            <c:forEach var="trip" items="${trips}">
                <div class="col-md-6 col-lg-4">
                    <div class="app-card h-100">
                        <div class="d-flex justify-content-between align-items-start">
                            <h5>${trip.name}</h5>
                            <c:choose>
                                <c:when test="${trip.role == 'OWNER'}">
                                    <span class="badge badge-owner">OWNER</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-member">MEMBER</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <p class="text-muted mb-1">
                            📅 ${trip.startDate} → ${trip.endDate}
                        </p>
                        <p class="text-muted mb-3">
                            💰 Ngân sách: <fmt:formatNumber value="${trip.budget}" type="number"/> đ
                        </p>
                        <span class="badge bg-secondary mb-3">${trip.status}</span>
                        <a href="${pageContext.request.contextPath}/trip/detail?tripId=${trip.id}"
                           class="btn btn-outline-secondary btn-sm w-100">Xem chi tiết</a>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
