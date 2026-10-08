package controller.trip;

import dao.TripDAO;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Trip;
import model.User;
import dao.TripMemberDAO;
import dao.UserDAO;

@WebServlet({
    "/trips",
    "/trip/create",
    "/trip/edit",
    "/trip/detail",
    "/trip/member/add",
    "/trip/member/remove"

})
public class TripServlet extends HttpServlet {

    private static final String LIST_VIEW
            = "/WEB-INF/views/trip/trip-list.jsp";

    private static final String FORM_VIEW
            = "/WEB-INF/views/trip/trip-form.jsp";

    private static final String DETAIL_VIEW
            = "/WEB-INF/views/trip/trip-detail.jsp";

    private final TripDAO tripDAO = new TripDAO();
    private final TripMemberDAO tripMemberDAO
            = new TripMemberDAO();

    private final UserDAO userDAO
            = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentUser(request, response);

        if (user == null) {
            return;
        }

        String path = request.getServletPath();

        if ("/trips".equals(path)) {
            showList(request, response, user);
            return;
        }

        if ("/trip/create".equals(path)) {
            showCreateForm(request, response);
            return;
        }
        if ("/trip/edit".equals(path)) {
            showEditForm(request, response, user);
            return;
        }
        if ("/trip/detail".equals(path)) {
            showDetail(request, response, user);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
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

        String path = request.getServletPath();

        if ("/trip/create".equals(path)) {
            createTrip(request, response, user);
            return;
        }
        if ("/trip/member/add".equals(path)) {
            addMember(request, response, user);
            return;
        }
        if ("/trip/member/remove".equals(path)) {
            removeMember(request, response, user);
            return;
        }
        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED
        );
    }

    private void showList(HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {

        try {
            request.setAttribute(
                    "trips",
                    tripDAO.findByUser(user.getUserId())
            );

            request.getRequestDispatcher(LIST_VIEW)
                    .forward(request, response);

        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể tải danh sách chuyến đi.",
                    e
            );
        }
    }

    private void showCreateForm(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(FORM_VIEW)
                .forward(request, response);
    }

    private void showEditForm(HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {

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

        if (!"OWNER".equals(trip.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        request.setAttribute("trip", trip);

        request.getRequestDispatcher(FORM_VIEW)
                .forward(request, response);
    }

    private void showDetail(HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {

        int tripId = parsePositiveInt(
                request.getParameter("tripId")
        );

        if (tripId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        Trip trip;

        try {
            trip = tripDAO.findByIdForUser(
                    tripId,
                    user.getUserId()
            );
        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể tải chuyến đi.",
                    e
            );
        }

        if (trip == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );
            return;
        }

        request.setAttribute("trip", trip);
        request.setAttribute(
                "members",
                tripMemberDAO.findByTrip(tripId)
        );

        request.getRequestDispatcher(DETAIL_VIEW)
                .forward(request, response);
    }

    private void createTrip(HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {

        int requestedTripId = parsePositiveInt(
                request.getParameter("tripId")
        );

        String name = trim(request.getParameter("name"));
        String area = trim(request.getParameter("area"));
        String startDateValue
                = trim(request.getParameter("startDate"));
        String endDateValue
                = trim(request.getParameter("endDate"));
        String budgetValue
                = trim(request.getParameter("budget"));

        if (name.isEmpty()
                || area.isEmpty()
                || startDateValue.isEmpty()
                || endDateValue.isEmpty()
                || budgetValue.isEmpty()) {

            forwardCreateError(
                    request,
                    response,
                    "Vui lòng nhập đầy đủ thông tin."
            );
            return;
        }

        if (name.length() > 150) {
            forwardCreateError(
                    request,
                    response,
                    "Tên chuyến đi không được vượt quá 150 ký tự."
            );
            return;
        }

        if (area.length() > 200) {
            forwardCreateError(
                    request,
                    response,
                    "Địa điểm không được vượt quá 200 ký tự."
            );
            return;
        }

        Date startDate;
        Date endDate;
        BigDecimal budget;

        try {
            startDate = Date.valueOf(startDateValue);
            endDate = Date.valueOf(endDateValue);
            budget = new BigDecimal(budgetValue);

        } catch (IllegalArgumentException e) {
            forwardCreateError(
                    request,
                    response,
                    "Ngày hoặc ngân sách không hợp lệ."
            );
            return;
        }

        if (endDate.before(startDate)) {
            forwardCreateError(
                    request,
                    response,
                    "Ngày kết thúc không được trước ngày bắt đầu."
            );
            return;
        }

        if (budget.compareTo(BigDecimal.ZERO) < 0) {
            forwardCreateError(
                    request,
                    response,
                    "Ngân sách không được là số âm."
            );
            return;
        }

        Trip trip = new Trip();
        trip.setTripName(name);
        trip.setDestination(area);
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        trip.setBudget(budget);

        /*
     * Có tripId nghĩa là form đang sửa Trip.
         */
        if (requestedTripId > 0) {

            if (!tripDAO.isOwner(
                    requestedTripId,
                    user.getUserId())) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN
                );
                return;
            }

            Trip existing = tripDAO.findByIdForUser(
                    requestedTripId,
                    user.getUserId()
            );

            if (existing == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );
                return;
            }

            trip.setTripId(requestedTripId);
            trip.setDescription(existing.getDescription());
            trip.setStatus(existing.getStatus());

            boolean updated = tripDAO.updateByOwner(
                    trip,
                    user.getUserId()
            );

            if (!updated) {
                forwardCreateError(
                        request,
                        response,
                        "Không thể cập nhật chuyến đi."
                );
                return;
            }

            request.getSession().setAttribute(
                    "successMessage",
                    "Cập nhật chuyến đi thành công."
            );

            redirectToDetail(
                    request,
                    response,
                    requestedTripId
            );
            return;
        }

        /*
     * Không có tripId nghĩa là tạo mới.
         */
        trip.setDescription(null);
        trip.setStatus("PLANNING");

        int createdTripId = tripDAO.create(
                trip,
                user.getUserId()
        );

        if (createdTripId <= 0) {
            forwardCreateError(
                    request,
                    response,
                    "Không thể tạo chuyến đi."
            );
            return;
        }

        request.getSession().setAttribute(
                "successMessage",
                "Tạo chuyến đi thành công."
        );

        redirectToDetail(
                request,
                response,
                createdTripId
        );
    }

    private void addMember(HttpServletRequest request,
            HttpServletResponse response,
            User currentUser)
            throws IOException, ServletException {

        int tripId = parsePositiveInt(
                request.getParameter("tripId")
        );

        String email = trim(
                request.getParameter("email")
        ).toLowerCase();

        if (tripId <= 0 || email.isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        /*
     * Kiểm tra quyền ở server, không chỉ ẩn form trên JSP.
         */
        if (!tripDAO.isOwner(
                tripId,
                currentUser.getUserId())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        User member;

        try {
            member = userDAO.findByEmail(email);

        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể tìm người dùng.",
                    e
            );
        }

        if (member == null || !member.isActive()) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "Không tìm thấy user đang hoạt động với email này."
            );

            redirectToDetail(request, response, tripId);
            return;
        }

        if (member.getUserId() == currentUser.getUserId()) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "OWNER đã nằm trong chuyến đi."
            );

            redirectToDetail(request, response, tripId);
            return;
        }

        boolean added;

        try {
            added = tripMemberDAO.addMember(
                    tripId,
                    member.getUserId()
            );

        } catch (RuntimeException e) {
            throw new ServletException(
                    "Không thể thêm thành viên.",
                    e
            );
        }

        if (!added) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "User này đã là thành viên của chuyến đi."
            );

            redirectToDetail(request, response, tripId);
            return;
        }

        request.getSession().setAttribute(
                "successMessage",
                "Thêm thành viên thành công."
        );

        redirectToDetail(request, response, tripId);
    }

    private void redirectToDetail(
            HttpServletRequest request,
            HttpServletResponse response,
            int tripId)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/trip/detail?tripId="
                + tripId
        );
    }

    private void removeMember(HttpServletRequest request,
            HttpServletResponse response,
            User currentUser)
            throws IOException {

        int tripId = parsePositiveInt(
                request.getParameter("tripId")
        );

        int memberUserId = parsePositiveInt(
                request.getParameter("userId")
        );

        if (tripId <= 0 || memberUserId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        /*
     * Chỉ OWNER của Trip mới được xóa thành viên.
         */
        if (!tripDAO.isOwner(
                tripId,
                currentUser.getUserId())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        /*
     * Không cho OWNER tự xóa chính mình.
         */
        if (memberUserId == currentUser.getUserId()) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "Không thể xóa OWNER khỏi chuyến đi."
            );

            redirectToDetail(request, response, tripId);
            return;
        }

        boolean removed = tripMemberDAO.removeMember(
                tripId,
                memberUserId,
                currentUser.getUserId()
        );

        if (!removed) {
            request.getSession().setAttribute(
                    "errorMessage",
                    "Không thể xóa thành viên này. Thành viên có thể đã liên quan đến khoản chi."
            );
            redirectToDetail(request, response, tripId);
            return;
        }

        request.getSession().setAttribute(
                "successMessage",
                "Xóa thành viên thành công."
        );

        redirectToDetail(request, response, tripId);
    }

    private User getCurrentUser(HttpServletRequest request,
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

    private void forwardCreateError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute("errorMessage", message);

        request.getRequestDispatcher(FORM_VIEW)
                .forward(request, response);
    }

    private int parsePositiveInt(String value) {
        try {
            int number = Integer.parseInt(value);
            return number > 0 ? number : -1;

        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
