<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Preferences" scope="request"/>
<%-- selectedIds phai la mot List/Set<Integer> tu Servlet (vd Set<Integer>).
     EL 2.2+ ho tro goi method truc tiep nen dung selectedIds.contains(category.id),
     khong dung fn:contains vi ham do chi ap dung cho String. --%>
<c:set var="currentTrip" value="${trip}" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-md-8">
        <div class="app-card">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <h4 class="mb-0">Sở thích của bạn cho "${trip.name}"</h4>
                <span class="text-muted">Đã chọn: <span id="selectedCount">0</span></span>
            </div>
            <p class="text-muted">Chọn các loại địa điểm bạn quan tâm cho chuyến đi này.</p>

            <form method="post" action="${pageContext.request.contextPath}/preferences">
                <input type="hidden" name="tripId" value="${trip.id}">

                <div class="row mb-4">
                    <c:forEach var="category" items="${categories}">
                        <div class="col-md-4 col-6 mb-2">
                            <div class="form-check">
                                <input class="form-check-input preference-checkbox" type="checkbox"
                                       name="categoryIds" value="${category.id}" id="cat${category.id}"
                                       <c:if test="${selectedIds.contains(category.id)}">checked</c:if>>
                                <label class="form-check-label" for="cat${category.id}">
                                    ${category.icon} ${category.name}
                                </label>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <button type="submit" class="btn btn-brand">Lưu Preferences</button>
                <a href="${pageContext.request.contextPath}/preferences/group?tripId=${trip.id}"
                   class="btn btn-outline-secondary">Xem Group Preferences</a>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
