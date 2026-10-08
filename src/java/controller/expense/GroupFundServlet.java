package controller.expense;

import dao.GroupFundDAO;
import dao.TripDAO;
import dao.TripMemberDAO;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Trip;
import model.User;

@WebServlet(name = "GroupFundServlet", urlPatterns = {
    "/group-fund",
    "/group-fund/contribute"
})
public class GroupFundServlet extends HttpServlet {
    private final TripDAO tripDAO = new TripDAO();
    private final TripMemberDAO tripMemberDAO = new TripMemberDAO();
    private final GroupFundDAO groupFundDAO = new GroupFundDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request, response);
        if (user == null) return;
        int tripId;
        try { tripId = positiveInt(request.getParameter("tripId")); }
        catch (IllegalArgumentException e) { response.sendError(400, e.getMessage()); return; }

        Trip trip = tripDAO.findByIdForUser(tripId, user.getUserId());
        if (trip == null) { response.sendError(404, "Không tìm thấy Trip hoặc bạn không có quyền truy cập."); return; }

        request.setAttribute("trip", trip);
        request.setAttribute("fundSummary", groupFundDAO.findSummary(tripId));
        request.setAttribute("fundTransactions", groupFundDAO.findTransactions(tripId));
        request.setAttribute("members", tripMemberDAO.findByTrip(tripId));
        request.setAttribute("memberFundSummaries", groupFundDAO.findMemberSummaries(tripId));
        request.getRequestDispatcher("/WEB-INF/views/expense/group-fund.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = currentUser(request, response);
        if (user == null) return;
        int tripId;
        try { tripId = positiveInt(request.getParameter("tripId")); }
        catch (IllegalArgumentException e) { response.sendError(400, e.getMessage()); return; }

        if (!tripDAO.isOwner(tripId, user.getUserId())) {
            response.sendError(403, "Chỉ Owner mới được ghi nhận tiền đóng vào quỹ.");
            return;
        }

        int memberId;
        BigDecimal amount;
        try {
            memberId = positiveInt(request.getParameter("memberId"));
            amount = new BigDecimal(request.getParameter("amount")).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            setError(request, "Thành viên hoặc số tiền không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/group-fund?tripId=" + tripId);
            return;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            setError(request, "Số tiền đóng quỹ phải lớn hơn 0.");
            response.sendRedirect(request.getContextPath() + "/group-fund?tripId=" + tripId);
            return;
        }
        if (!groupFundDAO.isTripParticipant(tripId, memberId)) {
            setError(request, "Người đóng tiền không thuộc Trip.");
            response.sendRedirect(request.getContextPath() + "/group-fund?tripId=" + tripId);
            return;
        }

        String note = request.getParameter("note");
        if (note != null) note = note.trim();
        if (note != null && note.length() > 255) {
            setError(request, "Ghi chú không được vượt quá 255 ký tự.");
            response.sendRedirect(request.getContextPath() + "/group-fund?tripId=" + tripId);
            return;
        }

        if (!groupFundDAO.addContribution(tripId, memberId, amount, note, user.getUserId())) {
            setError(request, "Không thể ghi nhận khoản đóng quỹ.");
            response.sendRedirect(request.getContextPath() + "/group-fund?tripId=" + tripId);
            return;
        }
        request.getSession().setAttribute("successMessage", "Đã ghi nhận tiền đóng vào quỹ nhóm.");
        response.sendRedirect(request.getContextPath() + "/group-fund?tripId=" + tripId);
    }

    private User currentUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        User user = (User) session.getAttribute("user");
        if (!"USER".equalsIgnoreCase(user.getRole())) {
            response.sendError(403, "Bạn không có quyền sử dụng chức năng này.");
            return null;
        }
        return user;
    }

    private int positiveInt(String value) {
        try { int x = Integer.parseInt(value); if (x > 0) return x; } catch (Exception ignored) {}
        throw new IllegalArgumentException("ID không hợp lệ.");
    }

    private void setError(HttpServletRequest request, String message) {
        request.getSession().setAttribute("errorMessage", message);
    }
}
