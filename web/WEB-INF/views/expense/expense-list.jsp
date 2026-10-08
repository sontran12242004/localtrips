<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Expenses" scope="request"/>
<c:set var="currentTrip" value="${trip}" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="mb-0">Expenses - ${trip.name}</h3>
    <div>
        <a href="${pageContext.request.contextPath}/expenses/create?tripId=${trip.id}" class="btn btn-brand btn-sm">+ Thêm expense</a>
        <a href="${pageContext.request.contextPath}/settlement?tripId=${trip.id}" class="btn btn-outline-secondary btn-sm">Xem Settlement</a>
    </div>
</div>

<c:choose>
    <c:when test="${empty expenses}">
        <div class="empty-state app-card">
            <div class="empty-icon">🧾</div>
            <p>Chưa có khoản chi nào được ghi nhận.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="app-card p-0">
            <table class="table mb-0 align-middle">
                <thead>
                    <tr>
                        <th>Mô tả</th>
                        <th>Người trả</th>
                        <th>Số tiền</th>
                        <th>Người tham gia</th>
                        <th>Nguồn</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="expense" items="${expenses}">
                        <tr>
                            <td>${expense.description}</td>
                            <td>${expense.payerName}</td>
                            <td><fmt:formatNumber value="${expense.amount}" type="number"/> đ</td>
                            <td>
                                <c:forEach var="p" items="${expense.participantNames}" varStatus="st">
                                    ${p}<c:if test="${!st.last}">, </c:if>
                                </c:forEach>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${expense.fromGroupFund}">
                                        <span class="badge bg-info text-dark">Quỹ nhóm</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary">Cá nhân</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${trip.role == 'OWNER'
                                                    || expense.createdBy == sessionScope.user.userId}">

                                            <div class="d-flex gap-2 align-items-center flex-wrap">

                                                <a href="${pageContext.request.contextPath}/expenses/edit?tripId=${trip.id}&amp;expenseId=${expense.id}"
                                                   class="btn btn-sm btn-outline-primary">
                                                    Chỉnh sửa
                                                </a>

                                                <form method="post"
                                                      action="${pageContext.request.contextPath}/expenses/delete"
                                                      onsubmit="return confirm('Bạn có chắc muốn xóa khoản chi này?');">

                                                    <input type="hidden"
                                                           name="tripId"
                                                           value="${trip.id}">

                                                    <input type="hidden"
                                                           name="expenseId"
                                                           value="${expense.id}">

                                                    <button type="submit"
                                                            class="btn btn-sm btn-outline-danger">
                                                        Xóa
                                                    </button>
                                                </form>

                                            </div>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="text-muted">—</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
