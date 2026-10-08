package controller.preference;

import dao.CategoryDAO;
import dao.PreferenceDAO;
import dao.TripDAO;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Category;
import model.Trip;
import model.User;

@WebServlet({
    "/preferences",
    "/preferences/group"
})
public class PreferenceServlet extends HttpServlet {

    private static final String FORM_VIEW =
            "/WEB-INF/views/preference/preference-form.jsp";

    private static final String GROUP_VIEW =
            "/WEB-INF/views/preference/group-preferences.jsp";

    private final PreferenceDAO preferenceDAO =
            new PreferenceDAO();

    private final CategoryDAO categoryDAO =
            new CategoryDAO();

    private final TripDAO tripDAO =
            new TripDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentUser(request, response);

        if (user == null) {
            return;
        }

        int tripId = parsePositiveInt(
                request.getParameter("tripId")
        );

        if (tripId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        Trip trip = tripDAO.findByIdForUser(
                tripId,
                user.getUserId()
        );

        if (trip == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );
            return;
        }

        String path = request.getServletPath();

        if ("/preferences".equals(path)) {
            showPreferenceForm(
                    request,
                    response,
                    user,
                    trip
            );
            return;
        }

        if ("/preferences/group".equals(path)) {
            showGroupPreferences(
                    request,
                    response,
                    trip
            );
            return;
        }

        response.sendError(
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = getCurrentUser(request, response);

        if (user == null) {
            return;
        }

        if (!"/preferences".equals(
                request.getServletPath())) {

            response.sendError(
                    HttpServletResponse.SC_METHOD_NOT_ALLOWED
            );
            return;
        }

        int tripId = parsePositiveInt(
                request.getParameter("tripId")
        );

        if (tripId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        Trip trip = tripDAO.findByIdForUser(
                tripId,
                user.getUserId()
        );

        if (trip == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );
            return;
        }

        Set<Integer> selectedIds =
                parseCategoryIds(
                        request.getParameterValues(
                                "categoryIds"
                        )
                );

        /*
         * Chỉ chấp nhận ID danh mục thật trong database.
         */
        Set<Integer> validIds = new LinkedHashSet<>();

        for (Category category : categoryDAO.findAll()) {
            validIds.add(category.getCategoryId());
        }

        selectedIds.retainAll(validIds);

        try {
            preferenceDAO.savePreferences(
                    tripId,
                    user.getUserId(),
                    selectedIds
            );

        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể lưu Preferences.",
                    e
            );
        }

        request.getSession().setAttribute(
                "successMessage",
                "Lưu sở thích thành công."
        );

        response.sendRedirect(
                request.getContextPath()
                + "/preferences/group?tripId="
                + tripId
        );
    }

    private void showPreferenceForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User user,
            Trip trip)
            throws ServletException, IOException {

        try {
            request.setAttribute("trip", trip);

            request.setAttribute(
                    "categories",
                    categoryDAO.findAll()
            );

            request.setAttribute(
                    "selectedIds",
                    preferenceDAO.findSelectedCategoryIds(
                            trip.getTripId(),
                            user.getUserId()
                    )
            );

            request.getRequestDispatcher(FORM_VIEW)
                    .forward(request, response);

        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể tải Preferences.",
                    e
            );
        }
    }

    private void showGroupPreferences(
            HttpServletRequest request,
            HttpServletResponse response,
            Trip trip)
            throws ServletException, IOException {

        try {
            int tripId = trip.getTripId();

            request.setAttribute("trip", trip);

            request.setAttribute(
                    "totalMembers",
                    preferenceDAO.countTripMembers(tripId)
            );

            request.setAttribute(
                    "groupPreferences",
                    preferenceDAO.findGroupPreferences(
                            tripId
                    )
            );

            request.getRequestDispatcher(GROUP_VIEW)
                    .forward(request, response);

        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể tải sở thích nhóm.",
                    e
            );
        }
    }

    private Set<Integer> parseCategoryIds(
            String[] values) {

        Set<Integer> result = new LinkedHashSet<>();

        if (values == null) {
            return result;
        }

        for (String value : values) {
            int categoryId = parsePositiveInt(value);

            if (categoryId > 0) {
                result.add(categoryId);
            }
        }

        return result;
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