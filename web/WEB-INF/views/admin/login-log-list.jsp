<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lịch sử đăng nhập</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 30px; background: #f5f6fa; color: #212529; }
        h1 { margin-bottom: 6px; }
        .subtitle { color: #6c757d; margin-bottom: 20px; }
        .filter-box { background: white; padding: 16px; border-radius: 8px; margin-bottom: 18px; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
        .filter-box label { margin-right: 6px; font-weight: bold; }
        .filter-box input, .filter-box select { padding: 8px; margin-right: 12px; border: 1px solid #ced4da; border-radius: 4px; }
        button { padding: 8px 14px; border: 0; border-radius: 4px; background: #212529; color: white; cursor: pointer; }
        .clear { margin-left: 8px; color: #495057; }
        .error { background: #f8d7da; color: #842029; padding: 10px; border-radius: 4px; margin-bottom: 12px; }
        table { width: 100%; border-collapse: collapse; background: white; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
        th, td { padding: 10px; border: 1px solid #dee2e6; text-align: left; vertical-align: top; }
        th { background: #343a40; color: white; }
        .success { color: #198754; font-weight: bold; }
        .failed { color: #dc3545; font-weight: bold; }
        .muted { color: #6c757d; }
        .user-agent { max-width: 280px; word-break: break-word; font-size: 12px; color: #495057; }
        .summary { margin: 12px 0; color: #495057; }
        .pagination { margin-top: 18px; display: flex; gap: 6px; align-items: center; }
        .pagination a, .pagination span { padding: 7px 11px; border: 1px solid #ced4da; border-radius: 4px; text-decoration: none; color: #212529; background: white; }
        .pagination .current { background: #212529; color: white; }
        .pagination .disabled { color: #adb5bd; background: #f1f3f5; }
        .back-link { display: inline-block; margin-top: 20px; }
    </style>
</head>
<body>

<h1>Lịch sử đăng nhập</h1>
<div class="subtitle">Theo dõi các lần đăng nhập thành công và thất bại của tài khoản trong hệ thống.</div>

<c:if test="${not empty filterError}">
    <div class="error"><c:out value="${filterError}"/></div>
</c:if>

<form method="get" action="${pageContext.request.contextPath}/admin/login-logs" class="filter-box">
    <label for="email">Email:</label>
    <input type="text" id="email" name="email" value="<c:out value='${emailKeyword}'/>" placeholder="Nhập email">

    <label for="result">Kết quả:</label>
    <select id="result" name="result">
        <option value="" ${empty selectedResult ? 'selected' : ''}>Tất cả</option>
        <option value="success" ${selectedResult == 'success' ? 'selected' : ''}>Thành công</option>
        <option value="failed" ${selectedResult == 'failed' ? 'selected' : ''}>Thất bại</option>
    </select>

    <label for="fromDate">Từ:</label>
    <input type="date" id="fromDate" name="fromDate" value="<c:out value='${fromDate}'/>" >

    <label for="toDate">Đến:</label>
    <input type="date" id="toDate" name="toDate" value="<c:out value='${toDate}'/>" >

    <button type="submit">Lọc</button>
    <a class="clear" href="${pageContext.request.contextPath}/admin/login-logs">Xóa bộ lọc</a>
</form>

<div class="summary">
    Tổng số bản ghi: <strong><c:out value="${totalRows}"/></strong>
</div>

<c:choose>
    <c:when test="${empty loginLogs}">
        <p>Không có lịch sử đăng nhập phù hợp.</p>
    </c:when>
    <c:otherwise>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Email</th>
                <th>User ID</th>
                <th>Kết quả</th>
                <th>Lý do</th>
                <th>IP</th>
                <th>Thiết bị / trình duyệt</th>
                <th>Thời gian</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${loginLogs}" var="log">
                <tr>
                    <td><c:out value="${log.loginLogId}"/></td>
                    <td><c:out value="${log.email}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${log.userId != null}"><c:out value="${log.userId}"/></c:when>
                            <c:otherwise>—</c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${log.success}"><span class="success">Thành công</span></c:when>
                            <c:otherwise><span class="failed">Thất bại</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${empty log.failureReason}">—</c:when>
                            <c:when test="${log.failureReason == 'INVALID_CREDENTIALS'}">Sai email hoặc mật khẩu</c:when>
                            <c:when test="${log.failureReason == 'ACCOUNT_DISABLED'}">Tài khoản đã bị khóa</c:when>
                            <c:when test="${log.failureReason == 'MISSING_CREDENTIALS'}">Chưa nhập đủ thông tin</c:when>
                            <c:when test="${log.failureReason == 'SYSTEM_ERROR'}">Lỗi hệ thống</c:when>
                            <c:when test="${log.failureReason == 'RATE_LIMITED'}">Thử đăng nhập quá nhiều lần</c:when>
                            <c:otherwise><c:out value="${log.failureReason}"/></c:otherwise>
                        </c:choose>
                    </td>
                    <td><c:out value="${log.ipAddress}"/></td>
                    <td class="user-agent"><c:out value="${log.userAgent}"/></td>
                    <td><c:out value="${log.attemptedAt}"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <c:if test="${totalPages > 1}">
            <div class="pagination">
                <c:url var="baseUrl" value="/admin/login-logs">
                    <c:param name="email" value="${emailKeyword}"/>
                    <c:param name="result" value="${selectedResult}"/>
                    <c:param name="fromDate" value="${fromDate}"/>
                    <c:param name="toDate" value="${toDate}"/>
                </c:url>

                <c:choose>
                    <c:when test="${currentPage > 1}">
                        <a href="${baseUrl}&page=${currentPage - 1}">« Trước</a>
                    </c:when>
                    <c:otherwise><span class="disabled">« Trước</span></c:otherwise>
                </c:choose>

                <c:forEach begin="1" end="${totalPages}" var="p">
                    <c:choose>
                        <c:when test="${p == currentPage}">
                            <span class="current"><c:out value="${p}"/></span>
                        </c:when>
                        <c:otherwise>
                            <a href="${baseUrl}&page=${p}"><c:out value="${p}"/></a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>

                <c:choose>
                    <c:when test="${currentPage < totalPages}">
                        <a href="${baseUrl}&page=${currentPage + 1}">Sau »</a>
                    </c:when>
                    <c:otherwise><span class="disabled">Sau »</span></c:otherwise>
                </c:choose>
            </div>
        </c:if>
    </c:otherwise>
</c:choose>

<a class="back-link" href="${pageContext.request.contextPath}/admin">Quay lại trang Admin</a>

</body>
</html>
