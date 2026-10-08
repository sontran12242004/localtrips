<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Thêm vào lịch trình" scope="request"/>
<c:set var="currentTrip" value="${trip}" scope="request"/>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-lg-7">

        <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
                <h3 class="mb-1">Thêm vào lịch trình</h3>
                <p class="text-muted mb-0">
                    Chuyến đi:
                    <strong><c:out value="${trip.name}"/></strong>
                </p>
            </div>

            <a href="${pageContext.request.contextPath}/recommendations?tripId=${trip.id}"
               class="btn btn-outline-secondary">
                Quay lại
            </a>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">
                <c:out value="${error}"/>
            </div>
        </c:if>

        <div class="app-card">

            <div class="alert alert-info">
                <strong>Địa điểm:</strong>
                <c:out value="${place.name}"/>
                <br>

                <strong>Chi phí tham khảo:</strong>
                <c:out value="${place.estimatedPrice}"/> đ
            </div>

            <form method="post"
                  action="${pageContext.request.contextPath}/itinerary/add">

                <input type="hidden"
                       name="tripId"
                       value="${trip.id}">

                <input type="hidden"
                       name="placeId"
                       value="${place.id}">

                <div class="mb-3">
                    <label class="form-label">
                        Ngày tham quan
                    </label>

                    <input type="date"
                           name="visitDate"
                           class="form-control"
                           min="${trip.startDate}"
                           max="${trip.endDate}"
                           value="${param.visitDate}"
                           required>

                    <div class="form-text">
                        Ngày phải nằm trong khoảng từ
                        ${trip.startDate} đến ${trip.endDate}.
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">
                            Giờ bắt đầu
                        </label>

                        <input type="time"
                               name="startTime"
                               class="form-control"
                               value="${param.startTime}"
                               required>
                    </div>

                    <div class="col-md-6 mb-3">
                        <label class="form-label">
                            Giờ kết thúc
                        </label>

                        <input type="time"
                               name="endTime"
                               class="form-control"
                               value="${param.endTime}"
                               required>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">
                        Chi phí dự kiến
                    </label>

                    <input type="number"
                           name="estimatedCost"
                           class="form-control"
                           min="0"
                           step="1000"
                           value="${not empty param.estimatedCost
                                    ? param.estimatedCost
                                    : place.estimatedPrice}"
                           required>
                </div>

                <div class="mb-4">
                    <label class="form-label">
                        Ghi chú
                    </label>

                    <textarea name="note"
                              class="form-control"
                              rows="3"
                              maxlength="500"
                              placeholder="Ví dụ: Chuẩn bị vé, tập trung trước 15 phút..."><c:out value="${param.note}"/></textarea>
                </div>

                <button type="submit"
                        class="btn btn-brand">
                    Thêm vào lịch trình
                </button>

                <a href="${pageContext.request.contextPath}/itinerary?tripId=${trip.id}"
                   class="btn btn-outline-secondary">
                    Hủy
                </a>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>