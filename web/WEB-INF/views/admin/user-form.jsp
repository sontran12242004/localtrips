<%@page import="utils.HtmlUtil"%>
<%@page import="utils.HtmlUtil"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User" %>
<%@ page import="utils.HtmlUtil" %>

<%
    User item = (User) request.getAttribute("item");
    boolean editMode = item != null;

    String fullNameValue = request.getParameter("fullName");
    String emailValue = request.getParameter("email");
    String roleValue = request.getParameter("role");

    if (fullNameValue == null) {
        fullNameValue = editMode ? item.getFullName() : "";
    }

    if (emailValue == null) {
        emailValue = editMode ? item.getEmail() : "";
    }

    if (roleValue == null || roleValue.trim().isEmpty()) {
        roleValue = editMode ? item.getRole() : "USER";
    }

    boolean activeValue;

    if ("POST".equalsIgnoreCase(request.getMethod())) {
        activeValue = request.getParameter("active") != null;
    } else {
        activeValue = editMode ? item.isActive() : true;
    }

    String formTitle = editMode
            ? "Sửa người dùng"
            : "Thêm người dùng";

    String formAction = editMode
            ? "/admin/users/edit"
            : "/admin/users/create";

    request.setAttribute("pageTitle", formTitle);
%>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-lg-7">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="fw-bold mb-1">
                    <%= formTitle%>
                </h2>

                <p class="text-muted mb-0">
                    <%= editMode
                            ? "Cập nhật thông tin, vai trò và trạng thái tài khoản"
                            : "Tạo tài khoản mới và phân vai trò trong LocalTrip"%>
                </p>
            </div>

            <a href="<%= request.getContextPath()%>/admin/users"
               class="btn btn-outline-secondary">
                Quay lại
            </a>
        </div>

        <div class="card shadow-sm">
            <div class="card-body p-4">
                <form method="post"
                      action="<%= request.getContextPath() + formAction%>">

                    <% if (editMode) {%>
                    <input type="hidden"
                           name="userId"
                           value="<%= item.getUserId()%>">
                    <% }%>

                    <div class="mb-3">
                        <label class="form-label">Họ tên</label>

                        <input type="text"
                               name="fullName"
                               class="form-control"
                               maxlength="100"
                               value="<%= HtmlUtil.escape(fullNameValue)%>"
                               required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Email</label>

                        <input type="email"
                               name="email"
                               class="form-control"
                               maxlength="150"
                               value="<%= HtmlUtil.escape(emailValue)%>"
                               required>
                    </div>

                    <% if (!editMode) { %>
                    <div class="mb-3">
                        <label class="form-label">Mật khẩu</label>

                        <input type="password"
                               name="password"
                               class="form-control"
                               minlength="8"
                               maxlength="64"
                               pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,64}"
                               title="Mật khẩu phải có 8–64 ký tự, gồm chữ hoa, chữ thường, chữ số và ký tự đặc biệt."
                               autocomplete="new-password"
                               required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">
                            Nhập lại mật khẩu
                        </label>

                        <input type="password"
                               name="confirmPassword"
                               class="form-control"
                               minlength="8"
                               maxlength="64"
                               required>
                    </div>
                    <% }%>

                    <div class="mb-3">
                        <label class="form-label">Vai trò</label>

                        <select name="role"
                                class="form-select"
                                required>

                            <option value="USER"
                                    <%= "USER".equals(roleValue)
                                        ? "selected" : ""%>>
                                USER
                            </option>

                            <option value="ADMIN"
                                    <%= "ADMIN".equals(roleValue)
                                        ? "selected" : ""%>>
                                ADMIN
                            </option>
                        </select>
                    </div>

                    <div class="form-check mb-4">
                        <input type="checkbox"
                               name="active"
                               id="active"
                               class="form-check-input"
                               <%= activeValue ? "checked" : ""%>>

                        <label for="active" class="form-check-label">
                            Tài khoản đang hoạt động
                        </label>
                    </div>

                    <button type="submit" class="btn btn-primary">
                        <%= editMode
                                ? "Lưu thay đổi"
                                : "Tạo người dùng"%>
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>