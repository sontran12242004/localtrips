package controller.admin;

import dao.UserDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.PasswordUtil;
import utils.PasswordValidator;
import utils.ValidationUtil;
import model.User;
import dao.RememberMeDAO;
import dao.AdminAuditLogDAO;

@WebServlet({
    "/admin/users",
    "/admin/users/create",
    "/admin/users/edit",
    "/admin/users/reset-password"
})
public class AdminUserServlet extends HttpServlet {

    private static final String LIST_VIEW
            = "/WEB-INF/views/admin/user-list.jsp";
    private static final int PAGE_SIZE = 10;
    private static final String FORM_VIEW
            = "/WEB-INF/views/admin/user-form.jsp";
    private static final String RESET_PASSWORD_VIEW
            = "/WEB-INF/views/admin/user-reset-password.jsp";
    private final UserDAO userDAO = new UserDAO();
    private final RememberMeDAO rememberMeDAO
            = new RememberMeDAO();
    private final AdminAuditLogDAO auditLogDAO
            = new AdminAuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/admin/users/create".equals(path)) {
            showCreateForm(request, response);
            return;
        }
        if ("/admin/users/edit".equals(path)) {
            showEditForm(request, response);
            return;
        }
        if ("/admin/users/reset-password".equals(path)) {
            showResetPasswordForm(request, response);
            return;
        }
        showList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        if ("/admin/users/create".equals(path)) {
            createUser(request, response);
            return;
        }
        if ("/admin/users/edit".equals(path)) {
            updateUser(request, response);
            return;
        }
        if ("/admin/users/reset-password".equals(path)) {
            resetPassword(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private void showList(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String role = request.getParameter("role");
        String status = request.getParameter("status");

        keyword = keyword == null ? "" : keyword.trim();
        role = role == null ? "" : role.trim().toUpperCase();
        status = status == null ? "" : status.trim().toUpperCase();

        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            role = "";
        }

        if (!"ACTIVE".equals(status)
                && !"LOCKED".equals(status)) {
            status = "";
        }

        int page = 1;

        try {
            String pageValue = request.getParameter("page");

            if (pageValue != null) {
                page = Integer.parseInt(pageValue);
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        if (page < 1) {
            page = 1;
        }

        try {
            int totalResults
                    = userDAO.countSearch(keyword, role, status);

            int totalPages = Math.max(
                    1,
                    (totalResults + PAGE_SIZE - 1) / PAGE_SIZE
            );

            /*
         * Nếu người dùng nhập page quá lớn trên URL,
         * đưa về trang cuối thay vì hiển thị bảng trống.
             */
            if (page > totalPages) {
                page = totalPages;
            }

            java.util.List<User> users
                    = userDAO.searchPage(
                            keyword,
                            role,
                            status,
                            page,
                            PAGE_SIZE
                    );

            request.setAttribute("users", users);
            request.setAttribute("resultCount", totalResults);
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("pageSize", PAGE_SIZE);

            request.setAttribute("keyword", keyword);
            request.setAttribute("selectedRole", role);
            request.setAttribute("selectedStatus", status);

            request.setAttribute(
                    "pageTitle",
                    "Quản lý người dùng"
            );

            request.getRequestDispatcher(LIST_VIEW)
                    .forward(request, response);

        } catch (RuntimeException e) {
            request.setAttribute(
                    "errorMessage",
                    "Không thể tải danh sách người dùng."
            );

            request.setAttribute("keyword", keyword);
            request.setAttribute("selectedRole", role);
            request.setAttribute("selectedStatus", status);

            request.getRequestDispatcher(LIST_VIEW)
                    .forward(request, response);
        }
    }

    private void showCreateForm(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("pageTitle", "Thêm người dùng");

        request.getRequestDispatcher(FORM_VIEW)
                .forward(request, response);
    }

    private void showEditForm(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int userId;

        try {
            userId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "Mã người dùng không hợp lệ."
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );
            return;
        }

        try {
            model.User item = userDAO.findById(userId);

            if (item == null) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Không tìm thấy người dùng."
                );

                response.sendRedirect(
                        request.getContextPath() + "/admin/users"
                );
                return;
            }

            request.setAttribute("item", item);
            request.setAttribute("pageTitle", "Sửa người dùng");

            request.getRequestDispatcher(FORM_VIEW)
                    .forward(request, response);

        } catch (RuntimeException e) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "Không thể tải thông tin người dùng."
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );
        }
    }

    private void showResetPasswordForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int userId;

        try {
            userId = Integer.parseInt(
                    request.getParameter("id")
            );
        } catch (NumberFormatException e) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "Mã người dùng không hợp lệ."
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );
            return;
        }

        try {
            User item = userDAO.findById(userId);

            if (item == null) {
                request.getSession().setAttribute(
                        "errorMessage",
                        "Không tìm thấy người dùng."
                );

                response.sendRedirect(
                        request.getContextPath() + "/admin/users"
                );
                return;
            }

            request.setAttribute("item", item);

            request.getRequestDispatcher(RESET_PASSWORD_VIEW)
                    .forward(request, response);

        } catch (RuntimeException e) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "Không thể tải thông tin người dùng."
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );
        }
    }

    private void createUser(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword
                = request.getParameter("confirmPassword");
        String role = request.getParameter("role");
        boolean active = request.getParameter("active") != null;

        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        role = role == null ? "" : role.trim().toUpperCase();

        String error = validateCreate(
                fullName,
                email,
                password,
                confirmPassword,
                role
        );

        if (error != null) {
            forwardWithError(request, response, error);
            return;
        }

        try {
            if (userDAO.existsByEmail(email)) {
                forwardWithError(
                        request,
                        response,
                        "Email này đã được sử dụng."
                );
                return;
            }

            boolean created = userDAO.insertByAdmin(
                    fullName,
                    email,
                    PasswordUtil.hash(password),
                    role,
                    active
            );

            if (!created) {
                forwardWithError(
                        request,
                        response,
                        "Email này đã được sử dụng."
                );
                return;
            }
            /*
 * Audit được thực hiện sau khi user đã tạo thành công.
 * Không ghi password hoặc passwordHash vào log.
             */
            try {
                User actor = (User) request.getSession()
                        .getAttribute("user");

                User createdUser = userDAO.findByEmail(email);

                if (actor != null && createdUser != null) {
                    auditLogDAO.log(
                            actor.getUserId(),
                            "CREATE_USER",
                            Integer.valueOf(createdUser.getUserId()),
                            "Tạo user " + email
                            + ", role=" + role
                            + ", status="
                            + (active ? "ACTIVE" : "LOCKED")
                    );
                }
            } catch (RuntimeException ignored) {
                /*
     * User đã tạo thành công; lỗi audit không được
     * biến kết quả thành thất bại.
                 */
            }

            request.getSession().setAttribute(
                    "successMessage",
                    "Đã tạo người dùng " + email
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );

        } catch (RuntimeException e) {
            forwardWithError(
                    request,
                    response,
                    "Không thể tạo người dùng. Vui lòng thử lại."
            );
        }
    }

    private void updateUser(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int userId;

        try {
            userId = Integer.parseInt(
                    request.getParameter("userId")
            );
        } catch (NumberFormatException e) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "Mã người dùng không hợp lệ."
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );
            return;
        }

        User item;

        try {
            item = userDAO.findById(userId);
        } catch (RuntimeException e) {
            item = null;
        }

        if (item == null) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "Không tìm thấy người dùng."
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );
            return;
        }

        request.setAttribute("item", item);

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        boolean active = request.getParameter("active") != null;

        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        role = role == null ? "" : role.trim().toUpperCase();

        String error = validateUpdate(fullName, email, role);

        if (error != null) {
            forwardEditWithError(
                    request,
                    response,
                    item,
                    error
            );
            return;
        }

        User currentUser = (User) request.getSession()
                .getAttribute("user");

        if (currentUser != null
                && currentUser.getUserId() == userId
                && (!"ADMIN".equals(role) || !active)) {

            forwardEditWithError(
                    request,
                    response,
                    item,
                    "Bạn không thể tự hạ quyền hoặc khóa tài khoản đang đăng nhập."
            );
            return;
        }

        try {
            if (userDAO.emailExistsForOtherUser(email, userId)) {
                forwardEditWithError(
                        request,
                        response,
                        item,
                        "Email này đã được tài khoản khác sử dụng."
                );
                return;
            }

            boolean updated = userDAO.updateByAdmin(
                    userId,
                    fullName,
                    email,
                    role,
                    active
            );

            if (!updated) {
                forwardEditWithError(
                        request,
                        response,
                        item,
                        "Không thể cập nhật người dùng."
                );
                return;
            }
            try {
                User actor = (User) request.getSession()
                        .getAttribute("user");

                if (actor != null) {
                    auditLogDAO.log(
                            actor.getUserId(),
                            "UPDATE_USER",
                            Integer.valueOf(userId),
                            "Cập nhật user "
                            + item.getEmail()
                            + " -> " + email
                            + ", role="
                            + item.getRole()
                            + " -> " + role
                            + ", status="
                            + (item.isActive()
                            ? "ACTIVE" : "LOCKED")
                            + " -> "
                            + (active
                                    ? "ACTIVE" : "LOCKED")
                    );
                }
            } catch (RuntimeException ignored) {
                // Audit không làm thất bại thao tác cập nhật.
            }

            /*
         * Nếu Admin sửa chính tên/email của mình, cập nhật luôn object
         * trong session để header hiển thị dữ liệu mới.
             */
            if (currentUser != null
                    && currentUser.getUserId() == userId) {

                currentUser.setFullName(fullName);
                currentUser.setEmail(email);
                currentUser.setRole(role);
                currentUser.setActive(active);
            }

            request.getSession().setAttribute(
                    "successMessage",
                    "Đã cập nhật người dùng " + email
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );

        } catch (RuntimeException e) {
            forwardEditWithError(
                    request,
                    response,
                    item,
                    "Không thể cập nhật người dùng. Vui lòng thử lại."
            );
        }
    }

    private void resetPassword(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int userId;

        try {
            userId = Integer.parseInt(
                    request.getParameter("userId")
            );
        } catch (NumberFormatException e) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "Mã người dùng không hợp lệ."
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );
            return;
        }

        User item;

        try {
            item = userDAO.findById(userId);
        } catch (RuntimeException e) {
            item = null;
        }

        if (item == null) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "Không tìm thấy người dùng."
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );
            return;
        }

        String password = request.getParameter("password");
        String confirmPassword
                = request.getParameter("confirmPassword");

        if (ValidationUtil.isBlank(password)
                || ValidationUtil.isBlank(confirmPassword)) {

            forwardResetError(
                    request,
                    response,
                    item,
                    "Vui lòng nhập đầy đủ mật khẩu mới."
            );
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            forwardResetError(
                    request,
                    response,
                    item,
                    "Mật khẩu phải từ "
                    + ValidationUtil.PASSWORD_MIN
                    + " đến "
                    + ValidationUtil.PASSWORD_MAX
                    + " ký tự."
            );
            return;
        }

        if (!password.equals(confirmPassword)) {
            forwardResetError(
                    request,
                    response,
                    item,
                    "Mật khẩu nhập lại không khớp."
            );
            return;
        }

        try {
            String passwordHash
                    = PasswordUtil.hash(password);

            boolean updated
                    = userDAO.updatePasswordByAdmin(
                            userId,
                            passwordHash
                    );

            if (!updated) {
                forwardResetError(
                        request,
                        response,
                        item,
                        "Không thể đặt lại mật khẩu."
                );
                return;
            }

            /*
         * Xóa các remember-me token cũ để trình duyệt khác
         * không tự đăng nhập lại bằng token đã cấp trước đó.
             */
            try {
                rememberMeDAO.deleteAllForUser(userId);
            } catch (RuntimeException ignored) {
                // Mật khẩu đã đổi thành công; lỗi thu hồi token
                // không được làm mất kết quả cập nhật mật khẩu.
            }
            try {
                User actor = (User) request.getSession()
                        .getAttribute("user");

                if (actor != null) {
                    auditLogDAO.log(
                            actor.getUserId(),
                            "RESET_PASSWORD",
                            Integer.valueOf(userId),
                            "Đặt lại mật khẩu cho user "
                            + item.getEmail()
                    );
                }
            } catch (RuntimeException ignored) {
                /*
     * Mật khẩu đã được cập nhật thành công;
     * lỗi audit không làm thất bại thao tác.
                 */
            }
            request.getSession().setAttribute(
                    "successMessage",
                    "Đã đặt lại mật khẩu cho "
                    + item.getEmail()
            );

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );

        } catch (RuntimeException e) {
            forwardResetError(
                    request,
                    response,
                    item,
                    "Không thể đặt lại mật khẩu. Vui lòng thử lại."
            );
        }
    }

    private String validateUpdate(String fullName,
            String email,
            String role) {

        if (ValidationUtil.isBlank(fullName)
                || ValidationUtil.isBlank(email)) {
            return "Vui lòng nhập đầy đủ họ tên và email.";
        }

        if (!ValidationUtil.isValidName(fullName)) {
            return "Họ tên không được vượt quá "
                    + ValidationUtil.NAME_MAX + " ký tự.";
        }

        if (!ValidationUtil.isValidEmail(email)) {
            return "Email không đúng định dạng.";
        }

        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            return "Vai trò không hợp lệ.";
        }

        return null;
    }

    private void forwardEditWithError(HttpServletRequest request,
            HttpServletResponse response,
            User item,
            String message)
            throws ServletException, IOException {

        request.setAttribute("item", item);
        request.setAttribute("errorMessage", message);
        request.setAttribute("pageTitle", "Sửa người dùng");

        request.getRequestDispatcher(FORM_VIEW)
                .forward(request, response);
    }

    private String validateCreate(String fullName,
            String email,
            String password,
            String confirmPassword,
            String role) {

        if (ValidationUtil.isBlank(fullName)
                || ValidationUtil.isBlank(email)
                || ValidationUtil.isBlank(password)
                || ValidationUtil.isBlank(confirmPassword)) {

            return "Vui lòng nhập đầy đủ thông tin.";
        }

        if (!ValidationUtil.isValidName(fullName)) {
            return "Họ tên không được vượt quá "
                    + ValidationUtil.NAME_MAX + " ký tự.";
        }

        if (!ValidationUtil.isValidEmail(email)) {
            return "Email không đúng định dạng.";
        }

        String passwordError = PasswordValidator.validate(password);

        if (passwordError != null) {
            return passwordError;
        }

        if (!password.equals(confirmPassword)) {
            return "Mật khẩu nhập lại không khớp.";
        }

        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            return "Vai trò không hợp lệ.";
        }

        return null;
    }

    private void forwardResetError(
            HttpServletRequest request,
            HttpServletResponse response,
            User item,
            String message)
            throws ServletException, IOException {

        request.setAttribute("item", item);
        request.setAttribute("errorMessage", message);
        request.setAttribute(
                "pageTitle",
                "Đặt lại mật khẩu"
        );

        request.getRequestDispatcher(RESET_PASSWORD_VIEW)
                .forward(request, response);
    }

    private void forwardWithError(HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute("errorMessage", message);
        request.setAttribute("pageTitle", "Thêm người dùng");

        request.getRequestDispatcher(FORM_VIEW)
                .forward(request, response);
    }
}
