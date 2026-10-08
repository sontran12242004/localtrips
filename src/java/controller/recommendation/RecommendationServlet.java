package controller.recommendation;

import dao.RecommendationDAO;
import dao.TripDAO;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Recommendation;
import model.Trip;
import model.User;


@WebServlet(name = "RecommendationServlet", urlPatterns = {
    "/recommendations"
})
public class RecommendationServlet extends HttpServlet {

    private final TripDAO tripDAO = new TripDAO();
    private final RecommendationDAO recommendationDAO
            = new RecommendationDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        if (!"USER".equalsIgnoreCase(currentUser.getRole())) {
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
                        "Không tìm thấy chuyến đi hoặc bạn không có quyền truy cập."
                );
                return;
            }

            List<Recommendation> recommendations
                    = recommendationDAO.findForTrip(tripId);

            request.setAttribute("trip", trip);
            request.setAttribute(
                    "recommendations",
                    recommendations
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/recommendation/recommendation.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            throw new ServletException(
                    "Không thể tải danh sách địa điểm gợi ý.",
                    e
            );
        }
    }
}