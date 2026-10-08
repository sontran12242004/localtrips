<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Settlement" scope="request"/>
<c:set var="currentTrip" value="${trip}" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<h3 class="mb-3">Settlement - ${trip.name}</h3>

<div class="app-card p-0 mb-4">
    <table class="table mb-0 align-middle">
        <thead>
        <tr>
            <th>Thành viên</th>
            <th>Paid</th>
            <th>Owed</th>
            <th>Balance</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="b" items="${balances}">
            <tr>
                <td>${b.memberName}</td>
                <td><fmt:formatNumber value="${b.paid}" type="number"/> đ</td>
                <td><fmt:formatNumber value="${b.owed}" type="number"/> đ</td>
                <td>
                    <c:choose>
                        <c:when test="${b.balance >= 0}">
                            <span class="balance-positive">+<fmt:formatNumber value="${b.balance}" type="number"/> đ</span>
                        </c:when>
                        <c:otherwise>
                            <span class="balance-negative"><fmt:formatNumber value="${b.balance}" type="number"/> đ</span>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<div class="app-card">
    <h5 class="mb-3">Suggested Transfers</h5>
    <c:choose>
        <c:when test="${empty transfers}">
            <p class="text-muted mb-0">Mọi người đã cân bằng chi phí, không cần chuyển khoản.</p>
        </c:when>
        <c:otherwise>
            <ul class="list-group list-group-flush">
                <c:forEach var="t" items="${transfers}">
                    <li class="list-group-item">
                        <strong>${t.fromMemberName}</strong> trả cho <strong>${t.toMemberName}</strong>:
                        <fmt:formatNumber value="${t.amount}" type="number"/> đ
                    </li>
                </c:forEach>
            </ul>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
