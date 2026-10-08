package controller.auth;

import dao.PasswordResetDAO;
import dao.UserDAO;
import model.User;
import utils.TokenUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Creates a short-lived password reset link. In this student project the link is shown for local testing. */
@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/auth/forgot-password.jsp";
    private final UserDAO userDAO = new UserDAO();
    private final PasswordResetDAO resetDAO = new PasswordResetDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Quên mật khẩu");
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();

        // Always show the same message to avoid revealing whether an email exists.
        String message = "Nếu email tồn tại trong hệ thống, một liên kết đặt lại mật khẩu đã được tạo.";
        request.setAttribute("successMessage", message);

        if (!cleanEmail.isEmpty()) {
            User user = userDAO.findActiveByEmail(cleanEmail);
            if (user != null) {
                String rawToken = TokenUtil.generateToken();
                long expires = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30);
                resetDAO.createToken(user.getUserId(), TokenUtil.sha256(rawToken), expires);

                // No SMTP dependency is bundled in the current project. For local development,
                // expose the generated link so it can be tested immediately.
                String resetUrl = request.getScheme() + "://" + request.getServerName()
                        + ":" + request.getServerPort() + request.getContextPath()
                        + "/reset-password?token=" + rawToken;
                request.setAttribute("devResetUrl", resetUrl);
            }
        }

        request.setAttribute("pageTitle", "Quên mật khẩu");
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
