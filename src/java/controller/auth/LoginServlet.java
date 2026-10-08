package controller.auth;

import dao.UserDAO;
import model.User;
import utils.PasswordUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import dao.LoginLogDAO;

/**
 * Login with optional 30-day remember-me token.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String LOGIN_VIEW = "/WEB-INF/views/auth/login.jsp";
    private final UserDAO userDAO = new UserDAO();
    private final LoginLogDAO loginLogDAO = new LoginLogDAO();
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOGIN_LOCK_MINUTES = 15;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            redirectByRole(request, response, (User) session.getAttribute("user"));
            return;
        }
        forwardToLogin(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        boolean rememberMe
                = "on".equalsIgnoreCase(request.getParameter("rememberMe"))
                || "true".equalsIgnoreCase(request.getParameter("rememberMe"));

        // Trường hợp chưa nhập đủ thông tin
        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            writeLoginLog(
                    request,
                    null,
                    email,
                    false,
                    "MISSING_CREDENTIALS"
            );

            forwardToLogin(
                    request,
                    response,
                    "Vui lòng nhập đầy đủ email và mật khẩu."
            );
            return;
        }

        String cleanEmail = email.trim().toLowerCase();
        int recentFailures = loginLogDAO.countRecentFailures(
                cleanEmail,
                LOGIN_LOCK_MINUTES
        );

        if (recentFailures >= MAX_FAILED_ATTEMPTS) {

            writeLoginLog(
                    request,
                    null,
                    cleanEmail,
                    false,
                    "RATE_LIMITED"
            );

            forwardToLogin(
                    request,
                    response,
                    "Bạn đã đăng nhập sai quá nhiều lần. "
                    + "Vui lòng thử lại sau 15 phút."
            );
            return;
        }
        User user;

        try {
            user = userDAO.findByEmail(cleanEmail);
        } catch (Exception e) {

            writeLoginLog(
                    request,
                    null,
                    cleanEmail,
                    false,
                    "SYSTEM_ERROR"
            );

            forwardToLogin(
                    request,
                    response,
                    "Không thể kết nối hệ thống, vui lòng thử lại."
            );
            return;
        }

        // Không tìm thấy email
        if (user == null) {
            writeLoginLog(
                    request,
                    null,
                    cleanEmail,
                    false,
                    "INVALID_CREDENTIALS"
            );

            forwardToLogin(
                    request,
                    response,
                    "Email hoặc mật khẩu không đúng."
            );
            return;
        }

        // Tài khoản đã bị khóa
        if (!user.isActive()) {
            writeLoginLog(
                    request,
                    user,
                    cleanEmail,
                    false,
                    "ACCOUNT_DISABLED"
            );

            forwardToLogin(
                    request,
                    response,
                    "Email hoặc mật khẩu không đúng."
            );
            return;
        }

        // Sai mật khẩu
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            writeLoginLog(
                    request,
                    user,
                    cleanEmail,
                    false,
                    "INVALID_CREDENTIALS"
            );

            forwardToLogin(
                    request,
                    response,
                    "Email hoặc mật khẩu không đúng."
            );
            return;
        }

        // Đăng nhập thành công
        HttpSession oldSession = request.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        user.setPasswordHash(null);

        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);

        writeLoginLog(
                request,
                user,
                cleanEmail,
                true,
                null
        );

        if (rememberMe) {
            RememberMeUtil.create(request, response, user);
        }

        redirectByRole(request, response, user);
    }

    private void redirectByRole(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String path = HomeServlet.pathForRole(user.getRole());
        if (path == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        response.sendRedirect(request.getContextPath() + path);
    }

    private void forwardToLogin(HttpServletRequest request, HttpServletResponse response,
            String errorMessage) throws ServletException, IOException {
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
        }
        request.setAttribute("pageTitle", "Đăng nhập");
        RequestDispatcher dispatcher = request.getRequestDispatcher(LOGIN_VIEW);
        dispatcher.forward(request, response);
    }

    private void writeLoginLog(HttpServletRequest request,
            User user,
            String email,
            boolean success,
            String failureReason) {

        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            ipAddress = request.getRemoteAddr();
        } else if (ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        String safeEmail = email == null ? "" : email.trim();
        String userAgent = request.getHeader("User-Agent");

        loginLogDAO.log(
                user == null ? null : user.getUserId(),
                safeEmail,
                success,
                failureReason,
                ipAddress,
                userAgent
        );
    }
}
