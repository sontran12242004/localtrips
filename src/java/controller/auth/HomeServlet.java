package controller.auth;

import dao.AdminDashboardDAO;
import dao.DashboardDAO;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.User;

/** Entry point for the two system-role dashboards: ADMIN and USER. */
@WebServlet({"/admin", "/user"})
public class HomeServlet extends HttpServlet {

    private static final String USER_VIEW = "/WEB-INF/views/dashboard.jsp";
    private static final String ADMIN_VIEW = "/WEB-INF/views/admin/dashboard.jsp";

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final AdminDashboardDAO adminDashboardDAO = new AdminDashboardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        String path = request.getServletPath();
        String expectedPath = pathForRole(user.getRole());

        if (expectedPath == null) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!expectedPath.equals(path)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            if ("USER".equals(user.getRole())) {
                loadUserDashboard(request, user);
                request.getRequestDispatcher(USER_VIEW).forward(request, response);
            } else {
                loadAdminDashboard(request);
                request.getRequestDispatcher(ADMIN_VIEW).forward(request, response);
            }
        } catch (RuntimeException e) {
            request.setAttribute("dashboardError", "Không thể tải dữ liệu dashboard. Vui lòng thử lại.");
            String view = "ADMIN".equals(user.getRole()) ? ADMIN_VIEW : USER_VIEW;
            request.getRequestDispatcher(view).forward(request, response);
        }
    }

    private void loadUserDashboard(HttpServletRequest request, User user) {
        request.setAttribute("tripCount", dashboardDAO.countUserTrips(user.getUserId()));
        request.setAttribute("ownerCount", dashboardDAO.countOwnedTrips(user.getUserId()));
        request.setAttribute("memberCount", dashboardDAO.countMemberTrips(user.getUserId()));
        request.setAttribute("trips", dashboardDAO.findRecentTrips(user.getUserId(), 5));
    }

    private void loadAdminDashboard(HttpServletRequest request) {
        request.setAttribute("userCount", adminDashboardDAO.countUsers());
        request.setAttribute("activeUserCount", adminDashboardDAO.countActiveUsers());
        request.setAttribute("tripCount", adminDashboardDAO.countTrips());
        request.setAttribute("placeCount", adminDashboardDAO.countPlaces());
        request.setAttribute("categoryCount", adminDashboardDAO.countCategories());
    }

    public static String pathForRole(String role) {
        if ("ADMIN".equals(role)) return "/admin";
        if ("USER".equals(role)) return "/user";
        return null;
    }
}
