<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Group Preferences" scope="request"/>
<c:set var="currentTrip" value="${trip}" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="app-card">
    <h4 class="mb-3">Group Preferences - ${trip.name}</h4>

    <c:choose>
        <c:when test="${empty groupPreferences}">
            <div class="empty-state">
                <div class="empty-icon">🗳️</div>
                <p>Chưa có thành viên nào chọn preferences.</p>
            </div>
        </c:when>
        <c:otherwise>
            <table class="table align-middle">
                <thead>
                <tr>
                    <th>Category</th>
                    <th>Số người chọn</th>
                    <th style="width:40%;">Tỷ lệ nhóm</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="gp" items="${groupPreferences}">
                    <tr>
                        <td>${gp.icon} ${gp.categoryName}</td>
                        <td>${gp.memberCount} / ${totalMembers}</td>
                        <td>
                            <div class="progress" style="height: 1.2rem;">
                                <div class="progress-bar btn-brand"
                                     style="width: ${gp.percentage}%; background-color: var(--brand-primary);">
                                    <fmt:formatNumber value="${gp.percentage}" maxFractionDigits="0"/>%
                                </div>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

    <a href="${pageContext.request.contextPath}/recommendations?tripId=${trip.id}"
       class="btn btn-brand mt-2">Xem Recommendation</a>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
