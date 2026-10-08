package controller.expense;

import dao.SettlementDAO;
import dao.TripDAO;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.SettlementBalance;
import model.SettlementTransfer;
import model.Trip;
import model.User;

@WebServlet(name = "SettlementServlet", urlPatterns = {
    "/settlement"
})
public class SettlementServlet extends HttpServlet {

    private final TripDAO tripDAO = new TripDAO();
    private final SettlementDAO settlementDAO
            = new SettlementDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        User currentUser
                = (User) session.getAttribute("user");

        if (!"USER".equalsIgnoreCase(
                currentUser.getRole()
        )) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền sử dụng chức năng này."
            );
            return;
        }

        int tripId;

        try {
            tripId = Integer.parseInt(
                    request.getParameter("tripId")
            );

            if (tripId <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Trip ID không hợp lệ."
            );
            return;
        }

        try {
            Trip trip = tripDAO.findByIdForUser(
                    tripId,
                    currentUser.getUserId()
            );

            if (trip == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy Trip hoặc bạn không có quyền truy cập."
                );
                return;
            }

            List<SettlementBalance> balances
                    = settlementDAO.findBalances(tripId);

            List<SettlementTransfer> transfers
                    = settlementDAO.calculateTransfers(
                            balances
                    );

            request.setAttribute("trip", trip);
            request.setAttribute(
                    "balances",
                    balances
            );
            request.setAttribute(
                    "transfers",
                    transfers
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/expense/settlement.jsp"
            ).forward(request, response);

        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể tính toán Settlement.",
                    e
            );
        }
    }
}