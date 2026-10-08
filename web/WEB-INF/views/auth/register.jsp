<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    request.setAttribute("pageTitle", "Đăng ký");
%>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-md-5">

        <div class="app-card">
            <h3 class="mb-4 text-center">
                Tạo tài khoản
            </h3>

            <form method="post"
                  action="<%= request.getContextPath() %>/register">

                <div class="mb-3">
                    <label for="fullName" class="form-label">
                        Họ tên
                    </label>

                    <input type="text"
                           id="fullName"
                           name="fullName"
                           class="form-control"
                           maxlength="100"
                           autocomplete="name"
                           required
                           autofocus>
                </div>

                <div class="mb-3">
                    <label for="email" class="form-label">
                        Email
                    </label>

                    <input type="email"
                           id="email"
                           name="email"
                           class="form-control"
                           maxlength="150"
                           autocomplete="email"
                           required>
                </div>

                <div class="mb-3">
                    <label for="password" class="form-label">
                        Mật khẩu
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

                <div class="mb-3">
                    <label for="confirmPassword" class="form-label">
                        Nhập lại mật khẩu
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

                <p class="text-muted small">
                    Tài khoản đăng ký mới sẽ được cấp role USER mặc định.
                </p>

                <button type="submit"
                        class="btn btn-brand w-100">
                    Đăng ký
                </button>
            </form>

            <p class="text-center mt-3 mb-0">
                Đã có tài khoản?
                <a href="<%= request.getContextPath() %>/login">
                    Đăng nhập
                </a>
            </p>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>