<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Recommendation" scope="request"/>
<c:set var="currentTrip" value="${trip}" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
    <h3 class="mb-0">Gợi ý địa điểm cho "${trip.name}"</h3>
    <div class="d-flex gap-2">
        <a href="${pageContext.request.contextPath}/preferences/group?tripId=${trip.id}"
           class="btn btn-outline-secondary btn-sm">Xem Group Preferences</a>
        <a href="${pageContext.request.contextPath}/itinerary?tripId=${trip.id}"
           class="btn btn-outline-secondary btn-sm">Xem Itinerary</a>
    </div>
</div>

<c:choose>
    <c:when test="${empty recommendations}">
        <div class="empty-state app-card">
            <div class="empty-icon">🤔</div>
            <p class="mb-1">Chưa có gợi ý nào.</p>
            <p class="text-muted">Trip chưa có thành viên nào chọn Preferences. Hãy chọn Preferences trước.</p>
            <a href="${pageContext.request.contextPath}/preferences?tripId=${trip.id}"
               class="btn btn-brand mt-2">Chọn Preferences</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row">
            <c:forEach var="rec" items="${recommendations}">
                <div class="col-md-6">
                    <div class="app-card">
                        <div class="d-flex justify-content-between align-items-start">
                            <h5>${rec.place.name}</h5>
                            <span class="score-pill"><fmt:formatNumber value="${rec.score}" maxFractionDigits="0"/></span>
                        </div>
                        <p class="text-muted mb-1">${rec.place.categoryIcon} ${rec.place.categoryName} &nbsp;|&nbsp; ⭐ ${rec.place.rating}</p>
                        <p class="text-muted mb-2">💰 ~${rec.place.estimatedPrice} đ</p>
                        <p class="mb-3"><small class="text-muted">
                            Lý do: ${rec.reason}
                            (Preference match <fmt:formatNumber value="${rec.preferenceMatch}" maxFractionDigits="0"/>%,
                            Rating ${rec.place.rating}/5,
                            Budget match <fmt:formatNumber value="${rec.budgetMatch}" maxFractionDigits="0"/>%)
                        </small></p>
                        <c:if test="${trip.role == 'OWNER'}">
                            <form method="post" action="${pageContext.request.contextPath}/itinerary">
                                <input type="hidden" name="tripId" value="${trip.id}">
                                <input type="hidden" name="placeId" value="${rec.place.id}">
                                <button type="submit" class="btn btn-brand btn-sm">+ Thêm vào Itinerary</button>
                            </form>
                        </c:if>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
