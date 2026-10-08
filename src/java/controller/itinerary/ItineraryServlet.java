package controller.itinerary;

import dao.ItineraryDAO;
import dao.PlaceDAO;
import dao.TripDAO;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.ItineraryItem;
import model.Place;
import model.Trip;
import model.User;

@WebServlet(name = "ItineraryServlet", urlPatterns = {
    "/itinerary",
    "/itinerary/add",
    "/itinerary/delete"
})
public class ItineraryServlet extends HttpServlet {

    private final TripDAO tripDAO = new TripDAO();
    private final PlaceDAO placeDAO = new PlaceDAO();
    private final ItineraryDAO itineraryDAO = new ItineraryDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        User currentUser = getCurrentUser(request, response);

        if (currentUser == null) {
            return;
        }

        String path = request.getServletPath();

        try {
            if ("/itinerary/add".equals(path)) {
                showAddForm(request, response, currentUser);
                return;
            }

            /*
             * Link trong place-detail.jsp hiện đang truyền placeId
             * trực tiếp tới /itinerary. Nếu có placeId thì chuyển
             * sang form thêm lịch trình.
             */
            if (request.getParameter("placeId") != null) {
                int tripId = parsePositiveInt(
                        request.getParameter("tripId")
                );

                int placeId = parsePositiveInt(
                        request.getParameter("placeId")
                );

                response.sendRedirect(
                        request.getContextPath()
                        + "/itinerary/add?tripId=" + tripId
                        + "&placeId=" + placeId
                );
                return;
            }

            showItinerary(request, response, currentUser);

        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể xử lý lịch trình.",
                    e
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User currentUser = getCurrentUser(request, response);

        if (currentUser == null) {
            return;
        }

        String path = request.getServletPath();

        try {
            if ("/itinerary/delete".equals(path)) {
                deleteItem(request, response, currentUser);
                return;
            }

            if ("/itinerary/add".equals(path)) {
                addItem(request, response, currentUser);
                return;
            }

            /*
             * Nút "+ Thêm vào Itinerary" trong trang Recommendation
             * đang POST tới /itinerary với tripId và placeId.
             * Chuyển người dùng sang form nhập ngày và giờ.
             */
            int tripId = parsePositiveInt(
                    request.getParameter("tripId")
            );

            int placeId = parsePositiveInt(
                    request.getParameter("placeId")
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/itinerary/add?tripId=" + tripId
                    + "&placeId=" + placeId
            );

        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể cập nhật lịch trình.",
                    e
            );
        }
    }

    private void showItinerary(
            HttpServletRequest request,
            HttpServletResponse response,
            User currentUser
    ) throws ServletException, IOException {

        int tripId = parsePositiveInt(
                request.getParameter("tripId")
        );

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

        request.setAttribute("trip", trip);
        request.setAttribute(
                "items",
                itineraryDAO.findByTrip(tripId)
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/intinerary/intinerary.jsp"
        ).forward(request, response);
    }

    private void showAddForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User currentUser
    ) throws ServletException, IOException {

        int tripId = parsePositiveInt(
                request.getParameter("tripId")
        );

        int placeId = parsePositiveInt(
                request.getParameter("placeId")
        );

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

        if (!"OWNER".equalsIgnoreCase(trip.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Chỉ Owner mới có thể thêm lịch trình."
            );
            return;
        }

        Place place = placeDAO.findById(placeId);

        if (place == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy địa điểm."
            );
            return;
        }

        request.setAttribute("trip", trip);
        request.setAttribute("place", place);

        request.getRequestDispatcher(
                "/WEB-INF/views/intinerary/intinerary-form.jsp"
        ).forward(request, response);
    }

    private void addItem(
            HttpServletRequest request,
            HttpServletResponse response,
            User currentUser
    ) throws ServletException, IOException {

        int tripId = parsePositiveInt(
                request.getParameter("tripId")
        );

        int placeId = parsePositiveInt(
                request.getParameter("placeId")
        );

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

        if (!"OWNER".equalsIgnoreCase(trip.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Chỉ Owner mới có thể thêm lịch trình."
            );
            return;
        }

        Place place = placeDAO.findById(placeId);

        if (place == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy địa điểm."
            );
            return;
        }

        Date visitDate;
        Time startTime;
        Time endTime;
        BigDecimal estimatedCost;

        try {
            visitDate = Date.valueOf(
                    request.getParameter("visitDate")
            );

            startTime = parseTime(
                    request.getParameter("startTime")
            );

            endTime = parseTime(
                    request.getParameter("endTime")
            );

            estimatedCost = new BigDecimal(
                    request.getParameter("estimatedCost")
            );

        } catch (Exception e) {
            forwardFormError(
                    request,
                    response,
                    trip,
                    place,
                    "Ngày, giờ hoặc chi phí không hợp lệ."
            );
            return;
        }

        String note = request.getParameter("note");

        if (note != null) {
            note = note.trim();
        }

        if (visitDate.before(trip.getStartDate())
                || visitDate.after(trip.getEndDate())) {

            forwardFormError(
                    request,
                    response,
                    trip,
                    place,
                    "Ngày tham quan phải nằm trong thời gian của Trip."
            );
            return;
        }

        if (!endTime.after(startTime)) {
            forwardFormError(
                    request,
                    response,
                    trip,
                    place,
                    "Giờ kết thúc phải sau giờ bắt đầu."
            );
            return;
        }

        if (estimatedCost.compareTo(BigDecimal.ZERO) < 0) {
            forwardFormError(
                    request,
                    response,
                    trip,
                    place,
                    "Chi phí dự kiến không được âm."
            );
            return;
        }

        BigDecimal maximumCost
                = new BigDecimal("9999999999999999.99");

        if (estimatedCost.compareTo(maximumCost) > 0) {
            forwardFormError(
                    request,
                    response,
                    trip,
                    place,
                    "Chi phí dự kiến quá lớn."
            );
            return;
        }

        if (note != null && note.length() > 500) {
            forwardFormError(
                    request,
                    response,
                    trip,
                    place,
                    "Ghi chú không được vượt quá 500 ký tự."
            );
            return;
        }

        if (itineraryDAO.hasTimeConflict(
                tripId,
                visitDate,
                startTime,
                endTime
        )) {
            forwardFormError(
                    request,
                    response,
                    trip,
                    place,
                    "Khoảng thời gian này bị trùng với một hoạt động khác."
            );
            return;
        }

        ItineraryItem item = new ItineraryItem();

        item.setTripId(tripId);
        item.setPlaceId(placeId);
        item.setVisitDate(visitDate);
        item.setStartTime(startTime);
        item.setEndTime(endTime);
        item.setNote(note);
        item.setEstimatedCost(estimatedCost);

        itineraryDAO.add(item);

        response.sendRedirect(
                request.getContextPath()
                + "/itinerary?tripId=" + tripId
        );
    }

    private void deleteItem(
            HttpServletRequest request,
            HttpServletResponse response,
            User currentUser
    ) throws IOException {

        int tripId = parsePositiveInt(
                request.getParameter("tripId")
        );

        int itemId = parsePositiveInt(
                request.getParameter("itemId")
        );

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

        if (!"OWNER".equalsIgnoreCase(trip.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Chỉ Owner mới có thể xóa lịch trình."
            );
            return;
        }

        itineraryDAO.deleteByOwner(
                itemId,
                tripId,
                currentUser.getUserId()
        );

        response.sendRedirect(
                request.getContextPath()
                + "/itinerary?tripId=" + tripId
        );
    }

    private void forwardFormError(
            HttpServletRequest request,
            HttpServletResponse response,
            Trip trip,
            Place place,
            String error
    ) throws ServletException, IOException {

        request.setAttribute("trip", trip);
        request.setAttribute("place", place);
        request.setAttribute("error", error);

        request.getRequestDispatcher(
                "/WEB-INF/views/intinerary/intinerary-form.jsp"
        ).forward(request, response);
    }

    private User getCurrentUser(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return null;
        }

        User currentUser
                = (User) session.getAttribute("user");

        if (!"USER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền sử dụng chức năng này."
            );
            return null;
        }

        return currentUser;
    }

    private int parsePositiveInt(String value) {
        try {
            int number = Integer.parseInt(value);

            if (number <= 0) {
                throw new NumberFormatException();
            }

            return number;

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "ID không hợp lệ."
            );
        }
    }

    private Time parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }

        String cleanValue = value.trim();

        if (cleanValue.length() == 5) {
            cleanValue += ":00";
        }

        return Time.valueOf(cleanValue);
    }
}