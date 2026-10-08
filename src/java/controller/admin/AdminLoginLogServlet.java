package controller.admin;

import dao.LoginLogDAO;
import java.io.IOException;
import java.sql.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "AdminLoginLogServlet", urlPatterns = {"/admin/login-logs"})
public class AdminLoginLogServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/admin/login-log-list.jsp";
    private static final int PAGE_SIZE = 20;

    private final LoginLogDAO loginLogDAO = new LoginLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = trim(request.getParameter("email"));
        String result = trim(request.getParameter("result"));
        String fromDateText = trim(request.getParameter("fromDate"));
        String toDateText = trim(request.getParameter("toDate"));

        int page = parsePositiveInt(request.getParameter("page"), 1);
        Date fromDate = parseDate(fromDateText);
        Date toDate = parseDate(toDateText);

        if (!fromDateText.isEmpty() && fromDate == null) {
            request.setAttribute("filterError", "Ngày bắt đầu không hợp lệ.");
        } else if (!toDateText.isEmpty() && toDate == null) {
            request.setAttribute("filterError", "Ngày kết thúc không hợp lệ.");
        } else if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            request.setAttribute("filterError", "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }

        if (request.getAttribute("filterError") != null) {
            page = 1;
            fromDate = null;
            toDate = null;
        }

        int totalRows = loginLogDAO.count(email, result, fromDate, toDate);
        int totalPages = Math.max(1, (int) Math.ceil(totalRows / (double) PAGE_SIZE));

        if (page > totalPages) {
            page = totalPages;
        }

        request.setAttribute("emailKeyword", email);
        request.setAttribute("selectedResult", result);
        request.setAttribute("fromDate", fromDateText);
        request.setAttribute("toDate", toDateText);
        request.setAttribute("loginLogs", loginLogDAO.findPage(email, result, fromDate, toDate, page, PAGE_SIZE));
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRows", totalRows);

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private int parsePositiveInt(String value, int defaultValue) {
        try {
            int number = Integer.parseInt(value);
            return number > 0 ? number : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Date parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Date.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
