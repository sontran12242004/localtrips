package filter;

import controller.auth.HomeServlet;
import controller.auth.RememberMeUtil;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.User;
import dao.UserDAO;

/**
 * Authentication + role authorization + remember-me restoration.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(contextPath.length());

        // Never auto-login while explicitly logging out.
        if (!"/logout".equals(path)) {
            restoreRememberedUser(req);
        }

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user == null) {
            HttpSession newSession = req.getSession(true);
            newSession.setAttribute("authMessage", "Vui lòng đăng nhập để tiếp tục.");
            resp.sendRedirect(contextPath + "/login");
            return;
        }
        /*
 * Nạp lại user từ database để trạng thái Active và role
 * mới có hiệu lực ngay, không phải chờ session hết hạn.
         */
        try {
            User freshUser = userDAO.findById(user.getUserId());

            if (freshUser == null || !freshUser.isActive()) {

                try {
                    RememberMeUtil.revoke(req, resp);
                } catch (RuntimeException ignored) {
                    // Vẫn tiếp tục hủy session.
                }

                session.invalidate();

                HttpSession newSession = req.getSession(true);
                newSession.setAttribute(
                        "authMessage",
                        "Tài khoản đã bị khóa hoặc không còn tồn tại."
                );

                resp.sendRedirect(contextPath + "/login");
                return;
            }

            /*
     * Không giữ password hash trong session.
             */
            freshUser.setPasswordHash(null);

            session.setAttribute("user", freshUser);
            user = freshUser;

        } catch (RuntimeException e) {
            /*
     * Không tiếp tục bằng quyền cũ nếu không xác minh được
     * trạng thái mới nhất của tài khoản.
             */
            resp.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE
            );
            return;
        }   
        boolean adminPath = "/admin".equals(path) || path.startsWith("/admin/");
        boolean userPath = "/user".equals(path) || path.startsWith("/user/");

        if (adminPath && !"ADMIN".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (userPath && !"USER".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (adminPath || userPath) {
            String expected = HomeServlet.pathForRole(user.getRole());

            if (expected == null
                    || (adminPath && !"/admin".equals(expected))
                    || (userPath && !"/user".equals(expected))) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void restoreRememberedUser(HttpServletRequest request) {
        HttpSession existing = request.getSession(false);
        if (existing != null && existing.getAttribute("user") != null) {
            return;
        }

        try {
            User user = RememberMeUtil.restore(request);
            if (user != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
            }

        } catch (RuntimeException ignored) {
            // If the remember-me table is not migrated yet, normal login still works.
        }
    }

    private boolean isPublic(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return true;
        }
        if ("/index.jsp".equals(path)) {
            return true;
        }
        if ("/login".equals(path) || "/register".equals(path) || "/logout".equals(path)
                || "/forgot-password".equals(path) || "/reset-password".equals(path)) {
            return true;
        }
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")) {
            return true;
        }
        if (path.startsWith("/WEB-INF/")) {
            return true;
        }
        return false;
    }
}
