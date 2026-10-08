<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User" %>
<%@ page import="utils.HtmlUtil" %>

<%
    User item = (User) request.getAttribute("item");
    request.setAttribute("pageTitle", "Đặt lại mật khẩu");
%>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-lg-6">

        <div class="d-flex justify-content-between
                    align-items-center mb-4">

            <div>
                <h2 class="fw-bold mb-1">
                    Đặt lại mật khẩu
                </h2>

                <p class="text-muted mb-0">
                    Tạo mật khẩu mới cho tài khoản người dùng
                </p>
            </div>

            <a href="<%= request.getContextPath() %>/admin/users"
               class="btn btn-outline-secondary">
                Quay lại
            </a>
        </div>

        <% if (item == null) { %>

            <div class="alert alert-danger">
                Không tìm thấy người dùng.
            </div>

        <% } else { %>

            <div class="card shadow-sm">
                <div class="card-body p-4">

                    <div class="alert alert-info">
                        <strong>
                            <%= HtmlUtil.escape(item.getFullName()) %>
                        </strong>
                        <br>
                        <span>
                            <%= HtmlUtil.escape(item.getEmail()) %>
                        </span>
                    </div>

                    <form method="post"
                          action="<%= request.getContextPath() %>/admin/users/reset-password">

                        <input type="hidden"
                               name="userId"
                               value="<%= item.getUserId() %>">

                        <div class="mb-3">
                            <label for="password"
                                   class="form-label">
                                Mật khẩu mới
                            </label>

                            <input type="password"
                                   id="password"
                                   name="password"
                                   class="form-control"
                                   minlength="8"
                                   maxlength="64"
                                   pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,64}"
                                   title="Mật khẩu phải có 8–64 ký tự, gồm chữ hoa, chữ thường, chữ số và ký tự đặc biệt."
                                   autocomplete="new-password"
                                   required>

                            <div class="form-text">
                                Từ 8 đến 64 ký tự, gồm chữ hoa, chữ thường,
                                chữ số và ký tự đặc biệt.
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="confirmPassword"
                                   class="form-label">
                                Nhập lại mật khẩu mới
                            </label>

                            <input type="password"
                                   id="confirmPassword"
                                   name="confirmPassword"
                                   class="form-control"
                                   minlength="8"
                                   maxlength="64"
                                   autocomplete="new-password"
                                   required>
                        </div>

                        <button type="submit"
                                class="btn btn-warning">
                            Đặt lại mật khẩu
                        </button>
                    </form>
                </div>
            </div>

        <% } %>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>