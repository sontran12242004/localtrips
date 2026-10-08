<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Category" %>
<%@ page import="utils.HtmlUtil" %>
<%
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    String keyword = (String) request.getAttribute("keyword");
    if (keyword == null) keyword = "";

    String successMessage = (String) session.getAttribute("successMessage");
    String errorMessage = (String) session.getAttribute("errorMessage");
    session.removeAttribute("successMessage");
    session.removeAttribute("errorMessage");
    request.setAttribute("pageTitle", "Quản lý danh mục");
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h2 class="fw-bold mb-1">Quản lý Categories</h2>
        <p class="text-muted mb-0">Quản lý các danh mục địa điểm được sử dụng trong LocalTrip.</p>
    </div>
    <div class="d-flex gap-2">
        <a href="<%= request.getContextPath()%>/admin/categories/create" class="btn btn-primary">+ Thêm Category</a>
        <a href="<%= request.getContextPath()%>/admin" class="btn btn-outline-secondary">Về Admin Dashboard</a>
    </div>
</div>

<% if (successMessage != null) { %>
<div class="alert alert-success"><%= HtmlUtil.escape(successMessage) %></div>
<% } %>
<% if (errorMessage != null) { %>
<div class="alert alert-danger"><%= HtmlUtil.escape(errorMessage) %></div>
<% } %>
<% if (request.getAttribute("errorMessage") != null) { %>
<div class="alert alert-danger"><%= HtmlUtil.escape((String) request.getAttribute("errorMessage")) %></div>
<% } %>

<div class="card shadow-sm mb-4">
    <div class="card-body">
        <form method="get" action="<%= request.getContextPath()%>/admin/categories">
            <div class="row g-3 align-items-end">
                <div class="col-lg-9">
                    <label class="form-label">Tìm kiếm</label>
                    <input type="text" name="keyword" class="form-control"
                           value="<%= HtmlUtil.escape(keyword) %>"
                           placeholder="Tìm theo mã hoặc tên category...">
                </div>
                <div class="col-lg-3">
                    <button type="submit" class="btn btn-primary">Tìm kiếm</button>
                    <a href="<%= request.getContextPath()%>/admin/categories" class="btn btn-outline-secondary">Xóa lọc</a>
                </div>
            </div>
        </form>
    </div>
</div>

<div class="card shadow-sm">
    <div class="card-body">
        <div class="d-flex justify-content-between mb-3">
            <strong>Danh sách Categories</strong>
            <span class="text-muted"><%= categories == null ? 0 : categories.size() %> kết quả</span>
        </div>

        <% if (categories == null || categories.isEmpty()) { %>
        <div class="alert alert-info mb-0">Không tìm thấy category phù hợp.</div>
        <% } else { %>
        <div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Mã</th>
                        <th>Tên Category</th>
                        <th>Số Places</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Category category : categories) { %>
                    <tr>
                        <td><%= category.getCategoryId() %></td>
                        <td><span class="badge text-bg-light border"><%= HtmlUtil.escape(category.getCategoryCode()) %></span></td>
                        <td><strong><%= HtmlUtil.escape(category.getIcon()) %> <%= HtmlUtil.escape(category.getCategoryName()) %></strong></td>
                        <td><%= category.getPlaceCount() %></td>
                        <td>
                            <div class="d-flex flex-wrap gap-2">
                                <a href="<%= request.getContextPath()%>/admin/categories/edit?id=<%= category.getCategoryId() %>"
                                   class="btn btn-sm btn-outline-primary">Sửa</a>
                                <% if (category.getPlaceCount() == 0) { %>
                                <form method="post" action="<%= request.getContextPath()%>/admin/categories/delete"
                                      onsubmit="return confirm('Bạn có chắc muốn xóa category này không?');" class="d-inline">
                                    <input type="hidden" name="id" value="<%= category.getCategoryId() %>">
                                    <button type="submit" class="btn btn-sm btn-outline-danger">Xóa</button>
                                </form>
                                <% } else { %>
                                <button type="button" class="btn btn-sm btn-outline-secondary" disabled>Đang được sử dụng</button>
                                <% } %>
                            </div>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
        <% } %>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
