package controller.place;

import dao.CategoryDAO;
import dao.PlaceDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Place;
import model.User;

@WebServlet({
    "/places",
    "/places/detail"
})
public class PlaceServlet extends HttpServlet {

    private static final String LIST_VIEW
            = "/WEB-INF/views/place/place-list.jsp";

    private static final String DETAIL_VIEW
            = "/WEB-INF/views/place/place-detail.jsp";

    private final PlaceDAO placeDAO = new PlaceDAO();
    private final CategoryDAO categoryDAO
            = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentUser(request, response);

        if (user == null) {
            return;
        }

        String path = request.getServletPath();

        if ("/places".equals(path)) {
            showList(request, response);
            return;
        }

        if ("/places/detail".equals(path)) {
            showDetail(request, response);
            return;
        }

        response.sendError(
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    private void showList(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int categoryId = parsePositiveInt(
                request.getParameter("categoryId")
        );

        try {
            request.setAttribute(
                    "categories",
                    categoryDAO.findAll()
            );

            if (categoryId > 0) {
                request.setAttribute(
                        "places",
                        placeDAO.findByCategory(categoryId)
                );
            } else {
                request.setAttribute(
                        "places",
                        placeDAO.findAll()
                );
            }

            request.getRequestDispatcher(LIST_VIEW)
                    .forward(request, response);

        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể tải danh sách địa điểm.",
                    e
            );
        }
    }

    private void showDetail(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        int placeId = parsePositiveInt(
                request.getParameter("placeId")
        );

        if (placeId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        Place place;

        try {
            place = placeDAO.findById(placeId);

        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể tải thông tin địa điểm.",
                    e
            );
        }

        if (place == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );
            return;
        }

        request.setAttribute("place", place);

        request.getRequestDispatcher(DETAIL_VIEW)
                .forward(request, response);
    }

    private User getCurrentUser(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        User user = session == null
                ? null
                : (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return null;
        }

        if (!"USER".equals(user.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return null;
        }

        return user;
    }

    private int parsePositiveInt(String value) {
        try {
            int number = Integer.parseInt(value);
            return number > 0 ? number : -1;

        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
