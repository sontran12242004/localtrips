<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Category" %>
<%@ page import="utils.HtmlUtil" %>
<%
    Category item = (Category) request.getAttribute("item");
    boolean editing = item != null;
    String errorMessage = (String) request.getAttribute("errorMessage");
    request.setAttribute("pageTitle", editing ? "Sửa Category" : "Thêm Category");
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h2 class="fw-bold mb-1"><%= editing ? "Sửa Category" : "Thêm Category" %></h2>
        <p class="text-muted mb-0">Nhập thông tin danh mục địa điểm.</p>
    </div>
    <a href="<%= request.getContextPath()%>/admin/categories" class="btn btn-outline-secondary">Quay lại</a>
</div>

<% if (errorMessage != null) { %>
<div class="alert alert-danger"><%= HtmlUtil.escape(errorMessage) %></div>
<% } %>

<div class="card shadow-sm">
    <div class="card-body">
        <form method="post"
              action="<%= request.getContextPath()%>/admin/categories/<%= editing ? "edit" : "create" %>">

            <% if (editing) { %>
            <input type="hidden" name="categoryId" value="<%= item.getCategoryId() %>">
            <% } %>

            <div class="mb-3">
                <label class="form-label">Mã Category <span class="text-danger">*</span></label>
                <input type="text" name="categoryCode" class="form-control"
                       maxlength="50" required
                       value="<%= HtmlUtil.escape(item == null ? "" : item.getCategoryCode()) %>"
                       placeholder="Ví dụ: FOOD, NATURE, SHOPPING">
                <div class="form-text">Chỉ dùng chữ cái, số, dấu - hoặc _.</div>
            </div>

            <div class="mb-4">
                <label class="form-label">Tên Category <span class="text-danger">*</span></label>
                <input type="text" name="categoryName" class="form-control"
                       maxlength="100" required
                       value="<%= HtmlUtil.escape(item == null ? "" : item.getCategoryName()) %>"
                       placeholder="Ví dụ: Ăn uống">
            </div>

            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary"><%= editing ? "Lưu thay đổi" : "Thêm Category" %></button>
                <a href="<%= request.getContextPath()%>/admin/categories" class="btn btn-outline-secondary">Hủy</a>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
