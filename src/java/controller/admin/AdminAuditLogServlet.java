package controller.admin;

import dao.AdminAuditLogDAO;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.AdminAuditLog;

@WebServlet("/admin/audit-logs")
public class AdminAuditLogServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/admin/audit-log-list.jsp";
    private static final int PAGE_SIZE = 20;

    private final AdminAuditLogDAO auditLogDAO = new AdminAuditLogDAO();

    private static final List<String> ACTIONS = Arrays.asList(
            "CREATE_USER", "UPDATE_USER", "RESET_PASSWORD",
            "CREATE_PLACE", "UPDATE_PLACE", "ACTIVATE_PLACE", "DEACTIVATE_PLACE",
            "CREATE_CATEGORY", "UPDATE_CATEGORY", "DELETE_CATEGORY"
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = trim(request.getParameter("keyword"));
        String action = trim(request.getParameter("action")).toUpperCase();
        if (!ACTIONS.contains(action)) action = "";

        int page = parseInt(request.getParameter("page"), 1);
        if (page < 1) page = 1;

        try {
            int totalResults = auditLogDAO.countSearch(keyword, action);
            int totalPages = Math.max(1, (totalResults + PAGE_SIZE - 1) / PAGE_SIZE);
            if (page > totalPages) page = totalPages;

            List<AdminAuditLog> logs = auditLogDAO.searchPage(keyword, action, page, PAGE_SIZE);

            request.setAttribute("logs", logs);
            request.setAttribute("keyword", keyword);
            request.setAttribute("selectedAction", action);
            request.setAttribute("actions", ACTIONS);
            request.setAttribute("resultCount", totalResults);
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("pageSize", PAGE_SIZE);
            request.setAttribute("pageTitle", "Nhật ký quản trị");
        } catch (RuntimeException e) {
            request.setAttribute("logs", Collections.emptyList());
            request.setAttribute("keyword", keyword);
            request.setAttribute("selectedAction", action);
            request.setAttribute("actions", ACTIONS);
            request.setAttribute("errorMessage", "Không thể tải nhật ký quản trị.");
            request.setAttribute("pageTitle", "Nhật ký quản trị");
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
