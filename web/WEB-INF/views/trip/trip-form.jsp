<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Tạo Trip" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-md-7">
        <div class="app-card">
            <h3 class="mb-4">
                <c:choose>
                    <c:when test="${not empty trip}">Cập nhật Trip</c:when>
                    <c:otherwise>Tạo Trip mới</c:otherwise>
                </c:choose>
            </h3>

            <form method="post" action="${pageContext.request.contextPath}/trip/create">
                <c:if test="${not empty trip}">
                    <input type="hidden" name="tripId" value="${trip.id}">
                </c:if>

                <div class="mb-3">
                    <label class="form-label">Tên chuyến đi</label>
                    <input type="text" name="name" class="form-control"
                           value="${trip.name}" placeholder="VD: Ho Chi Minh Weekend" required>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Ngày bắt đầu</label>
                        <input type="date" name="startDate" class="form-control"
                               value="${trip.startDate}" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Ngày kết thúc</label>
                        <input type="date" name="endDate" class="form-control"
                               value="${trip.endDate}" required>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Khu vực / Quận</label>
                    <input type="text" name="area" class="form-control"
                           value="${trip.area}" placeholder="VD: Quận 1, TP.HCM" required>
                </div>

                <div class="mb-3">
                    <label class="form-label">Ngân sách dự kiến (đ)</label>
                    <input type="number" name="budget" class="form-control"
                           value="${trip.budget}" min="0" step="1000" required>
                </div>

                <button type="submit" class="btn btn-brand">Lưu Trip</button>
                <a href="${pageContext.request.contextPath}/trips" class="btn btn-outline-secondary">Hủy</a>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
