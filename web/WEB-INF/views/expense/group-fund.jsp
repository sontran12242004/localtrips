<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Quỹ nhóm" scope="request"/>
<c:set var="currentTrip" value="${trip}" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h3 class="mb-1">Quỹ nhóm - ${trip.name}</h3>
        <p class="text-muted mb-0">Tiền đóng vào quỹ trừ các Expense được đánh dấu <b>Chi từ quỹ nhóm</b>.</p>
    </div>
    <a href="${pageContext.request.contextPath}/expenses?tripId=${trip.id}" class="btn btn-outline-secondary btn-sm">Quản lý chi phí</a>
</div>

<div class="row g-3 mb-4">
    <div class="col-md-4"><div class="app-card h-100"><div class="text-muted">Tổng tiền đã đóng</div><h3 class="mt-2"><fmt:formatNumber value="${fundSummary.totalContributed}" type="number"/> đ</h3></div></div>
    <div class="col-md-4"><div class="app-card h-100"><div class="text-muted">Tổng chi từ quỹ</div><h3 class="mt-2"><fmt:formatNumber value="${fundSummary.totalSpent}" type="number"/> đ</h3></div></div>
    <div class="col-md-4"><div class="app-card h-100"><div class="text-muted">Số dư thực tế</div><h3 class="mt-2 <c:if test='${fundSummary.balance < 0}'>text-danger</c:if>"><fmt:formatNumber value="${fundSummary.balance}" type="number"/> đ</h3><c:if test="${fundSummary.balance < 0}"><small class="text-danger">Quỹ đang âm.</small></c:if></div></div>
</div>

<c:if test="${trip.role == 'OWNER'}">
<div class="app-card mb-4">
    <h5>Ghi nhận tiền thành viên đóng vào quỹ</h5>
    <p class="text-muted">Owner ghi nhận hộ thành viên. Khoản tiền sẽ cộng ngay vào số dư quỹ.</p>
    <form method="post" action="${pageContext.request.contextPath}/group-fund/contribute" class="row g-3">
        <input type="hidden" name="tripId" value="${trip.id}">
        <div class="col-md-4">
            <label class="form-label">Thành viên</label>
            <select name="memberId" class="form-select" required>
                <c:forEach var="memberFund" items="${memberFundSummaries}"><option value="${memberFund.userId}">${memberFund.memberName} (${memberFund.role})</option></c:forEach>
            </select>
        </div>
        <div class="col-md-3">
            <label class="form-label">Số tiền (đ)</label>
            <input type="number" name="amount" class="form-control" min="1" step="1" required>
        </div>
        <div class="col-md-3">
            <label class="form-label">Ghi chú</label>
            <input type="text" name="note" maxlength="255" class="form-control" placeholder="VD: Đóng quỹ lần 1">
        </div>
        <div class="col-md-2 d-flex align-items-end"><button class="btn btn-brand w-100" type="submit">Ghi nhận</button></div>
    </form>
</div>
</c:if>

<div class="app-card mb-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
            <h5 class="mb-1">Đóng góp của từng thành viên</h5>
            <p class="text-muted mb-0">Theo dõi số tiền, số lần nạp và lần đóng gần nhất của từng thành viên.</p>
        </div>
    </div>
    <div class="table-responsive">
        <table class="table align-middle mb-0">
            <thead><tr><th>#</th><th>Thành viên</th><th>Vai trò</th><th class="text-center">Số lần nạp</th><th>Lần nạp gần nhất</th><th class="text-end">Đã nạp vào quỹ</th></tr></thead>
            <tbody>
                <c:forEach var="memberFund" items="${memberFundSummaries}" varStatus="st">
                    <tr>
                        <td>${st.count}</td>
                        <td><strong>${memberFund.memberName}</strong></td>
                        <td><c:choose><c:when test="${memberFund.role == 'OWNER'}"><span class="badge bg-primary">Owner</span></c:when><c:otherwise><span class="badge bg-secondary">Member</span></c:otherwise></c:choose></td>
                        <td class="text-center">${memberFund.contributionCount}</td>
                        <td><c:choose><c:when test="${not empty memberFund.lastContributionAt}"><fmt:formatDate value="${memberFund.lastContributionAt}" pattern="dd/MM/yyyy HH:mm"/></c:when><c:otherwise><span class="text-muted">Chưa nạp</span></c:otherwise></c:choose></td>
                        <td class="text-end"><strong><fmt:formatNumber value="${memberFund.contributed}" type="number"/> đ</strong></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty memberFundSummaries}"><tr><td colspan="6" class="text-muted text-center">Chưa có thành viên trong quỹ.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</div>

<div class="app-card">
    <h5 class="mb-3">Lịch sử giao dịch quỹ</h5>
    <c:choose>
        <c:when test="${empty fundTransactions}"><p class="text-muted mb-0">Chưa có giao dịch quỹ.</p></c:when>
        <c:otherwise>
            <div class="table-responsive"><table class="table align-middle mb-0">
                <thead><tr><th>Thời gian</th><th>Loại</th><th>Người</th><th>Mô tả</th><th class="text-end">Số tiền</th></tr></thead>
                <tbody>
                    <c:forEach var="tx" items="${fundTransactions}">
                        <tr>
                            <td><fmt:formatDate value="${tx.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                            <td><c:choose><c:when test="${tx.type == 'CONTRIBUTION'}"><span class="badge bg-success">Đóng quỹ</span></c:when><c:otherwise><span class="badge bg-danger">Chi từ quỹ</span></c:otherwise></c:choose></td>
                            <td>${tx.memberName}</td>
                            <td>${tx.description}</td>
                            <td class="text-end <c:if test='${tx.amount < 0}'>text-danger</c:if>"><fmt:formatNumber value="${tx.amount}" type="number"/> đ</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table></div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
