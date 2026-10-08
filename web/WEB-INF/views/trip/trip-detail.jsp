<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Trip Detail" scope="request"/>
<c:set var="currentTrip" value="${trip}" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="app-card">
    <div class="d-flex justify-content-between align-items-start">
        <div>
            <h3 class="mb-1">${trip.name}</h3>
            <p class="text-muted mb-1">📅 ${trip.startDate} → ${trip.endDate} &nbsp;|&nbsp; 📍 ${trip.area}</p>
            <p class="text-muted mb-0">💰 Ngân sách: <fmt:formatNumber value="${trip.budget}" type="number"/> đ
                &nbsp;|&nbsp; <span class="badge bg-secondary">${trip.status}</span></p>
        </div>
        <c:if test="${trip.role == 'OWNER'}">
            <div>
                <span class="badge badge-owner">OWNER</span>

                <a href="${pageContext.request.contextPath}/trip/edit?tripId=${trip.id}"
                   class="btn btn-outline-primary btn-sm ms-2">
                    Sửa Trip
                </a>
            </div>
        </c:if>
        <c:if test="${trip.role != 'OWNER'}">
            <span class="badge badge-member">MEMBER</span>
        </c:if>
    </div>
</div>

<div class="row">
    <div class="col-md-7">
        <div class="app-card">
            <h5 class="mb-3">Thành viên</h5>
            <ul class="list-group list-group-flush">
                <c:forEach var="member" items="${members}">
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        ${member.fullName} <span class="text-muted">(${member.email})</span>
                        <c:choose>
                            <c:when test="${member.role == 'OWNER'}">
                                <span class="badge badge-owner">OWNER</span>
                            </c:when>
                            <c:otherwise>
                                <div class="d-flex align-items-center">
                                    <span class="badge badge-member">
                                        MEMBER
                                    </span>

                                    <c:if test="${trip.role == 'OWNER'}">
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/trip/member/remove"
                                              class="ms-2">

                                            <input type="hidden"
                                                   name="tripId"
                                                   value="${trip.id}">

                                            <input type="hidden"
                                                   name="userId"
                                                   value="${member.userId}">

                                            <button type="submit"
                                                    class="btn btn-outline-danger btn-sm">
                                                Xóa
                                            </button>
                                        </form>
                                    </c:if>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </div>

    <div class="col-md-5">
        <c:if test="${trip.role == 'OWNER'}">
            <div class="app-card">
                <h5 class="mb-3">Thêm thành viên</h5>
                <form method="post" action="${pageContext.request.contextPath}/trip/member/add">
                    <input type="hidden" name="tripId" value="${trip.id}">
                    <div class="mb-3">
                        <label class="form-label">Email thành viên</label>
                        <input type="email" name="email" class="form-control" required
                               placeholder="ban@example.com">
                    </div>
                    <button type="submit" class="btn btn-brand w-100">Thêm</button>
                </form>
            </div>
        </c:if>

        <div class="app-card">
            <h5 class="mb-3">Bước tiếp theo</h5>
            <div class="d-grid gap-2">
                <a href="${pageContext.request.contextPath}/preferences?tripId=${trip.id}"
                   class="btn btn-outline-secondary">Chọn Preferences</a>
                <a href="${pageContext.request.contextPath}/recommendations?tripId=${trip.id}"
                   class="btn btn-outline-secondary">Xem Recommendation</a>
                <a href="${pageContext.request.contextPath}/itinerary?tripId=${trip.id}"
                   class="btn btn-outline-secondary">Itinerary</a>
                <a href="${pageContext.request.contextPath}/expenses?tripId=${trip.id}"
                   class="btn btn-outline-secondary">
                    Quản lý chi phí
                </a>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
