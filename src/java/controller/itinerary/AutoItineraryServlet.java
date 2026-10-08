package controller.itinerary;

import dao.ItineraryDAO;
import dao.RecommendationDAO;
import dao.TripDAO;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.ItineraryItem;
import model.Place;
import model.Recommendation;
import model.Trip;
import model.User;

/**
 * Tự động tạo lịch trình.
 *
 * Mỗi ngày có 3 cột mốc:
 * 07:00 - 11:00
 * 11:00 - 17:00
 * 17:00 - 22:00
 *
 * Mỗi cột mốc ưu tiên:
 * FOOD + CAFE + ACTIVITY
 *
 * ACTIVITY gồm:
 * SIGHTSEEING / ENTERTAINMENT / SHOPPING
 *
 * Ưu tiên:
 * 1. Số cột mốc hoàn chỉnh.
 * 2. Tổng số địa điểm.
 * 3. Tổng Recommendation Score.
 *
 * QUAN TRỌNG:
 * Không gọi ItineraryDAO.hasTimeConflict() trong quá trình
 * tìm phương án. Conflict được kiểm tra trực tiếp trong RAM
 * để tránh mở quá nhiều DB connection.
 */
@WebServlet(name = "AutoItineraryServlet", urlPatterns = {"/itinerary/auto"})
public class AutoItineraryServlet extends HttpServlet {

    private static final int FOOD_MINUTES = 60;
    private static final int CAFE_MINUTES = 60;
    private static final int ACTIVITY_MINUTES = 90;
    private static final int BREAK_MINUTES = 15;

    private final TripDAO tripDAO = new TripDAO();
    private final ItineraryDAO itineraryDAO = new ItineraryDAO();
    private final RecommendationDAO recommendationDAO = new RecommendationDAO();

    private static final class Slot {

        final LocalTime start;
        final LocalTime end;

        Slot(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }
    }

    private static final class ScheduledPlan {

        Recommendation food;
        Recommendation cafe;
        Recommendation activity;

        LocalTime foodStart;
        LocalTime cafeStart;
        LocalTime activityStart;

        double score;

        boolean isComplete() {
            return food != null
                    && cafe != null
                    && activity != null;
        }

        int count() {
            int count = 0;

            if (food != null) {
                count++;
            }

            if (cafe != null) {
                count++;
            }

            if (activity != null) {
                count++;
            }

            return count;
        }
    }

    private static final class DayPlan {

        final List<ScheduledPlan> milestones
                = new ArrayList<ScheduledPlan>();

        int completeMilestones;
        int scheduledItems;
        double totalScore;
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login");

            return;
        }

        User currentUser =
                (User) session.getAttribute("user");

        if (!"USER".equalsIgnoreCase(currentUser.getRole())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền sử dụng chức năng này.");

            return;
        }

        int tripId;

        try {

            tripId = Integer.parseInt(
                    request.getParameter("tripId"));

            if (tripId <= 0) {
                throw new NumberFormatException();
            }

        } catch (Exception e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Trip ID không hợp lệ.");

            return;
        }

        try {

            Trip trip =
                    tripDAO.findByIdForUser(
                            tripId,
                            currentUser.getUserId());

            if (trip == null) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy Trip hoặc bạn không có quyền truy cập.");

                return;
            }

            if (!"OWNER".equalsIgnoreCase(trip.getRole())) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Chỉ Owner mới có thể tự động tạo lịch trình.");

                return;
            }

            /*
             * =========================================================
             * 1. LẤY RECOMMENDATION
             * =========================================================
             */

            List<Recommendation> foods =
                    recommendationDAO.findForTripByCategory(
                            tripId,
                            "FOOD");

            List<Recommendation> cafes =
                    recommendationDAO.findForTripByCategory(
                            tripId,
                            "CAFE");

            List<Recommendation> activities =
                    new ArrayList<Recommendation>();

            activities.addAll(
                    recommendationDAO.findForTripByCategory(
                            tripId,
                            "SIGHTSEEING"));

            activities.addAll(
                    recommendationDAO.findForTripByCategory(
                            tripId,
                            "ENTERTAINMENT"));

            activities.addAll(
                    recommendationDAO.findForTripByCategory(
                            tripId,
                            "SHOPPING"));

            sortByScore(activities);

            /*
             * =========================================================
             * 2. KIỂM TRA DỮ LIỆU
             * =========================================================
             */

            if (foods.isEmpty()
                    || cafes.isEmpty()
                    || activities.isEmpty()) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/itinerary?tripId="
                        + tripId
                        + "&auto=empty");

                return;
            }

            /*
             * =========================================================
             * 3. LẤY ITINERARY HIỆN TẠI CHỈ 1 LẦN
             *
             * Đây là phần quan trọng nhất.
             *
             * Không gọi:
             *
             * itineraryDAO.hasTimeConflict()
             *
             * trong hàng trăm / hàng nghìn vòng lặp nữa.
             * =========================================================
             */

            List<ItineraryItem> existing =
                    itineraryDAO.findByTrip(tripId);

            /*
             * =========================================================
             * 4. LƯU PLACE ID ĐÃ DÙNG
             * =========================================================
             */

            Set<Integer> scheduledPlaceIds =
                    new HashSet<Integer>();

            for (ItineraryItem item : existing) {

                if (item == null) {
                    continue;
                }

                scheduledPlaceIds.add(
                        item.getPlaceId());
            }

            /*
             * =========================================================
             * 5. CÁC MILESTONE
             * =========================================================
             */

            Slot[] milestones = {

                new Slot(
                        LocalTime.of(7, 0),
                        LocalTime.of(11, 0)),

                new Slot(
                        LocalTime.of(11, 0),
                        LocalTime.of(17, 0)),

                new Slot(
                        LocalTime.of(17, 0),
                        LocalTime.of(22, 0))
            };

            int added = 0;

            LocalDate currentDate =
                    trip.getStartDate().toLocalDate();

            LocalDate endDate =
                    trip.getEndDate().toLocalDate();

            /*
             * =========================================================
             * 6. TẠO LỊCH THEO TỪNG NGÀY
             * =========================================================
             */

            while (!currentDate.isAfter(endDate)) {

                DayPlan dayPlan =
                        buildBestDayPlan(
                                currentDate,
                                milestones,
                                foods,
                                cafes,
                                activities,
                                scheduledPlaceIds,
                                existing);

                /*
                 * =====================================================
                 * 7. LƯU KẾT QUẢ
                 * =====================================================
                 */

                for (ScheduledPlan plan
                        : dayPlan.milestones) {

                    if (plan == null) {
                        continue;
                    }

                    /*
                     * FOOD
                     */

                    if (plan.food != null
                            && addItem(
                                    tripId,
                                    currentDate,
                                    plan.food,
                                    plan.foodStart,
                                    FOOD_MINUTES,
                                    "Bữa ăn - cột mốc "
                                    + milestoneLabel(plan))) {

                        scheduledPlaceIds.add(
                                plan.food.getPlace().getId());

                        added++;
                    }

                    /*
                     * CAFE
                     */

                    if (plan.cafe != null
                            && addItem(
                                    tripId,
                                    currentDate,
                                    plan.cafe,
                                    plan.cafeStart,
                                    CAFE_MINUTES,
                                    "Cà phê - cột mốc "
                                    + milestoneLabel(plan))) {

                        scheduledPlaceIds.add(
                                plan.cafe.getPlace().getId());

                        added++;
                    }

                    /*
                     * ACTIVITY
                     */

                    if (plan.activity != null
                            && addItem(
                                    tripId,
                                    currentDate,
                                    plan.activity,
                                    plan.activityStart,
                                    ACTIVITY_MINUTES,
                                    "Hoạt động "
                                    + plan.activity
                                            .getPlace()
                                            .getCategoryName()
                                    + " - cột mốc "
                                    + milestoneLabel(plan))) {

                        scheduledPlaceIds.add(
                                plan.activity.getPlace().getId());

                        added++;
                    }
                }

                currentDate =
                        currentDate.plusDays(1);
            }

            /*
             * =========================================================
             * 8. THÔNG BÁO THÀNH CÔNG
             * =========================================================
             */

            response.sendRedirect(
                    request.getContextPath()
                    + "/itinerary?tripId="
                    + tripId
                    + "&auto=success&count="
                    + added);

        } catch (RuntimeException e) {

            throw new ServletException(
                    "Không thể tự động tạo lịch trình.",
                    e);
        }
    }

    /**
     * Tìm DayPlan tốt nhất.
     */
    private DayPlan buildBestDayPlan(
            LocalDate date,
            Slot[] milestones,
            List<Recommendation> foods,
            List<Recommendation> cafes,
            List<Recommendation> activities,
            Set<Integer> globallyScheduled,
            List<ItineraryItem> existing) {

        DayPlan best =
                new DayPlan();

        searchDayPlans(
                date,
                milestones,
                0,
                foods,
                cafes,
                activities,
                new HashSet<Integer>(
                        globallyScheduled),
                new ArrayList<ScheduledPlan>(),
                best,
                existing);

        return best;
    }

    /**
     * Đệ quy tìm phương án.
     */
    private void searchDayPlans(
            LocalDate date,
            Slot[] milestones,
            int milestoneIndex,
            List<Recommendation> foods,
            List<Recommendation> cafes,
            List<Recommendation> activities,
            Set<Integer> usedIds,
            List<ScheduledPlan> current,
            DayPlan best,
            List<ItineraryItem> existing) {

        if (milestoneIndex
                >= milestones.length) {

            evaluatePlan(
                    current,
                    best);

            return;
        }

        Slot slot =
                milestones[milestoneIndex];

        /*
         * =============================================================
         * ƯU TIÊN PHƯƠNG ÁN ĐỦ 3 LOẠI
         * =============================================================
         */

        List<ScheduledPlan> completeOptions =
                buildCompleteOptions(
                        date,
                        slot,
                        foods,
                        cafes,
                        activities,
                        usedIds,
                        existing);

        if (!completeOptions.isEmpty()) {

            limitPlans(
                    completeOptions,
                    20);

            for (ScheduledPlan option
                    : completeOptions) {

                Set<Integer> nextUsed =
                        new HashSet<Integer>(
                                usedIds);

                nextUsed.add(
                        option.food
                                .getPlace()
                                .getId());

                nextUsed.add(
                        option.cafe
                                .getPlace()
                                .getId());

                nextUsed.add(
                        option.activity
                                .getPlace()
                                .getId());

                current.add(option);

                searchDayPlans(
                        date,
                        milestones,
                        milestoneIndex + 1,
                        foods,
                        cafes,
                        activities,
                        nextUsed,
                        current,
                        best,
                        existing);

                current.remove(
                        current.size() - 1);
            }

            /*
             * Có phương án đủ 3 loại thì không cho
             * phương án thiếu cạnh tranh.
             */

            return;
        }

        /*
         * =============================================================
         * KHÔNG ĐỦ 3 LOẠI -> THỬ PARTIAL
         * =============================================================
         */

        List<ScheduledPlan> partialOptions =
                buildBestPartialOptions(
                        date,
                        slot,
                        foods,
                        cafes,
                        activities,
                        usedIds,
                        existing);

        limitPlans(
                partialOptions,
                5);

        for (ScheduledPlan option
                : partialOptions) {

            Set<Integer> nextUsed =
                    new HashSet<Integer>(
                            usedIds);

            if (option.food != null) {

                nextUsed.add(
                        option.food
                                .getPlace()
                                .getId());
            }

            if (option.cafe != null) {

                nextUsed.add(
                        option.cafe
                                .getPlace()
                                .getId());
            }

            if (option.activity != null) {

                nextUsed.add(
                        option.activity
                                .getPlace()
                                .getId());
            }

            current.add(option);

            searchDayPlans(
                    date,
                    milestones,
                    milestoneIndex + 1,
                    foods,
                    cafes,
                    activities,
                    nextUsed,
                    current,
                    best,
                    existing);

            current.remove(
                    current.size() - 1);
        }

        /*
         * =============================================================
         * BỎ QUA MILESTONE
         * =============================================================
         */

        current.add(
                new ScheduledPlan());

        searchDayPlans(
                date,
                milestones,
                milestoneIndex + 1,
                foods,
                cafes,
                activities,
                new HashSet<Integer>(
                        usedIds),
                current,
                best,
                existing);

        current.remove(
                current.size() - 1);
    }

    /**
     * Tìm phương án hoàn chỉnh:
     *
     * FOOD + CAFE + ACTIVITY
     */
    private List<ScheduledPlan> buildCompleteOptions(
            LocalDate date,
            Slot slot,
            List<Recommendation> foods,
            List<Recommendation> cafes,
            List<Recommendation> activities,
            Set<Integer> usedIds,
            List<ItineraryItem> existing) {

        List<ScheduledPlan> options =
                new ArrayList<ScheduledPlan>();

        for (Recommendation food : foods) {

            if (isUsed(food, usedIds)) {
                continue;
            }

            LocalTime foodStart =
                    getValidStart(
                            food,
                            slot.start);

            LocalTime foodEnd =
                    foodStart.plusMinutes(
                            FOOD_MINUTES);

            if (!fits(
                    food,
                    foodStart,
                    foodEnd,
                    slot.end)) {

                continue;
            }

            if (hasConflict(
                    date,
                    foodStart,
                    foodEnd,
                    existing)) {

                continue;
            }

            LocalTime cafeEarliest =
                    foodEnd.plusMinutes(
                            BREAK_MINUTES);

            for (Recommendation cafe : cafes) {

                if (isUsed(cafe, usedIds)
                        || samePlace(food, cafe)) {

                    continue;
                }

                LocalTime cafeStart =
                        getValidStart(
                                cafe,
                                cafeEarliest);

                LocalTime cafeEnd =
                        cafeStart.plusMinutes(
                                CAFE_MINUTES);

                if (!fits(
                        cafe,
                        cafeStart,
                        cafeEnd,
                        slot.end)) {

                    continue;
                }

                if (hasConflict(
                        date,
                        cafeStart,
                        cafeEnd,
                        existing)) {

                    continue;
                }

                /*
                 * Kiểm tra conflict với FOOD
                 * trong chính phương án hiện tại.
                 */

                if (isTimeOverlap(
                        foodStart,
                        foodEnd,
                        cafeStart,
                        cafeEnd)) {

                    continue;
                }

                LocalTime activityEarliest =
                        cafeEnd.plusMinutes(
                                BREAK_MINUTES);

                for (Recommendation activity
                        : activities) {

                    if (isUsed(
                            activity,
                            usedIds)
                            || samePlace(
                                    food,
                                    activity)
                            || samePlace(
                                    cafe,
                                    activity)) {

                        continue;
                    }

                    LocalTime activityStart =
                            getValidStart(
                                    activity,
                                    activityEarliest);

                    LocalTime activityEnd =
                            activityStart.plusMinutes(
                                    ACTIVITY_MINUTES);

                    if (!fits(
                            activity,
                            activityStart,
                            activityEnd,
                            slot.end)) {

                        continue;
                    }

                    if (hasConflict(
                            date,
                            activityStart,
                            activityEnd,
                            existing)) {

                        continue;
                    }

                    /*
                     * Kiểm tra conflict trong
                     * chính phương án hiện tại.
                     */

                    if (isTimeOverlap(
                            foodStart,
                            foodEnd,
                            activityStart,
                            activityEnd)) {

                        continue;
                    }

                    if (isTimeOverlap(
                            cafeStart,
                            cafeEnd,
                            activityStart,
                            activityEnd)) {

                        continue;
                    }

                    ScheduledPlan plan =
                            new ScheduledPlan();

                    plan.food = food;
                    plan.cafe = cafe;
                    plan.activity = activity;

                    plan.foodStart = foodStart;
                    plan.cafeStart = cafeStart;
                    plan.activityStart =
                            activityStart;

                    plan.score =
                            food.getScore()
                            + cafe.getScore()
                            + activity.getScore();

                    options.add(plan);
                }
            }
        }

        Collections.sort(
                options,
                PLAN_SCORE_DESC);

        return options;
    }

    /**
     * Tìm phương án từng phần.
     */
    private List<ScheduledPlan> buildBestPartialOptions(
            LocalDate date,
            Slot slot,
            List<Recommendation> foods,
            List<Recommendation> cafes,
            List<Recommendation> activities,
            Set<Integer> usedIds,
            List<ItineraryItem> existing) {

        List<ScheduledPlan> options =
                new ArrayList<ScheduledPlan>();

        /*
         * =============================================================
         * FOOD
         * =============================================================
         */

        for (Recommendation food : foods) {

            if (isUsed(food, usedIds)) {
                continue;
            }

            LocalTime start =
                    getValidStart(
                            food,
                            slot.start);

            LocalTime end =
                    start.plusMinutes(
                            FOOD_MINUTES);

            if (fits(
                    food,
                    start,
                    end,
                    slot.end)
                    && !hasConflict(
                            date,
                            start,
                            end,
                            existing)) {

                ScheduledPlan plan =
                        new ScheduledPlan();

                plan.food = food;
                plan.foodStart = start;
                plan.score =
                        food.getScore();

                options.add(plan);
            }
        }

        /*
         * =============================================================
         * FOOD + CAFE
         * =============================================================
         */

        for (Recommendation food : foods) {

            if (isUsed(food, usedIds)) {
                continue;
            }

            LocalTime foodStart =
                    getValidStart(
                            food,
                            slot.start);

            LocalTime foodEnd =
                    foodStart.plusMinutes(
                            FOOD_MINUTES);

            if (!fits(
                    food,
                    foodStart,
                    foodEnd,
                    slot.end)) {

                continue;
            }

            if (hasConflict(
                    date,
                    foodStart,
                    foodEnd,
                    existing)) {

                continue;
            }

            for (Recommendation cafe : cafes) {

                if (isUsed(cafe, usedIds)
                        || samePlace(food, cafe)) {

                    continue;
                }

                LocalTime cafeStart =
                        getValidStart(
                                cafe,
                                foodEnd.plusMinutes(
                                        BREAK_MINUTES));

                LocalTime cafeEnd =
                        cafeStart.plusMinutes(
                                CAFE_MINUTES);

                if (!fits(
                        cafe,
                        cafeStart,
                        cafeEnd,
                        slot.end)) {

                    continue;
                }

                if (hasConflict(
                        date,
                        cafeStart,
                        cafeEnd,
                        existing)) {

                    continue;
                }

                if (isTimeOverlap(
                        foodStart,
                        foodEnd,
                        cafeStart,
                        cafeEnd)) {

                    continue;
                }

                ScheduledPlan plan =
                        new ScheduledPlan();

                plan.food = food;
                plan.cafe = cafe;

                plan.foodStart =
                        foodStart;

                plan.cafeStart =
                        cafeStart;

                plan.score =
                        food.getScore()
                        + cafe.getScore();

                options.add(plan);
            }
        }

        Collections.sort(
                options,
                PLAN_PRIORITY_DESC);

        return options;
    }

    /**
     * Kiểm tra địa điểm đã được dùng.
     */
    private boolean isUsed(
            Recommendation recommendation,
            Set<Integer> usedIds) {

        return recommendation == null
                || recommendation.getPlace() == null
                || usedIds.contains(
                        recommendation
                                .getPlace()
                                .getId());
    }

    /**
     * Hai recommendation có cùng địa điểm hay không.
     */
    private boolean samePlace(
            Recommendation first,
            Recommendation second) {

        return first != null
                && second != null
                && first.getPlace() != null
                && second.getPlace() != null
                && first.getPlace().getId()
                == second.getPlace().getId();
    }

    /**
     * Tính giờ bắt đầu hợp lệ dựa trên
     * giờ mở cửa của địa điểm.
     */
    private LocalTime getValidStart(
            Recommendation recommendation,
            LocalTime earliest) {

        Place place =
                recommendation.getPlace();

        if (place == null
                || place.getOpeningTime() == null) {

            return earliest;
        }

        LocalTime opening =
                place.getOpeningTime()
                        .toLocalTime();

        return opening.isAfter(earliest)
                ? opening
                : earliest;
    }

    /**
     * Kiểm tra địa điểm có hoạt động được
     * trong khoảng thời gian hay không.
     */
    private boolean fits(
            Recommendation recommendation,
            LocalTime start,
            LocalTime end,
            LocalTime milestoneEnd) {

        Place place =
                recommendation.getPlace();

        if (place == null) {
            return false;
        }

        if (end.isAfter(milestoneEnd)) {
            return false;
        }

        if (place.getClosingTime() != null
                && end.isAfter(
                        place.getClosingTime()
                                .toLocalTime())) {

            return false;
        }

        return true;
    }

    /**
     * ================================================================
     * KIỂM TRA CONFLICT TRONG RAM
     * ================================================================
     *
     * Không gọi DB.
     */
    private boolean hasConflict(
            LocalDate date,
            LocalTime start,
            LocalTime end,
            List<ItineraryItem> existing) {

        if (existing == null
                || existing.isEmpty()) {

            return false;
        }

        for (ItineraryItem item : existing) {

            if (item == null) {
                continue;
            }

            if (item.getVisitDate() == null
                    || item.getStartTime() == null
                    || item.getEndTime() == null) {

                continue;
            }

            LocalDate itemDate =
                    item.getVisitDate()
                            .toLocalDate();

            if (!date.equals(itemDate)) {
                continue;
            }

            LocalTime itemStart =
                    item.getStartTime()
                            .toLocalTime();

            LocalTime itemEnd =
                    item.getEndTime()
                            .toLocalTime();

            /*
             * Hai khoảng bị overlap nếu:
             *
             * start < itemEnd
             * &&
             * end > itemStart
             */

            if (isTimeOverlap(
                    start,
                    end,
                    itemStart,
                    itemEnd)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Kiểm tra hai khoảng thời gian có giao nhau.
     */
    private boolean isTimeOverlap(
            LocalTime start1,
            LocalTime end1,
            LocalTime start2,
            LocalTime end2) {

        return start1.isBefore(end2)
                && end1.isAfter(start2);
    }

    /**
     * Đánh giá DayPlan.
     *
     * Ưu tiên:
     * 1. Số milestone hoàn chỉnh.
     * 2. Tổng số item.
     * 3. Tổng score.
     */
    private void evaluatePlan(
            List<ScheduledPlan> current,
            DayPlan best) {

        int complete = 0;
        int items = 0;
        double score = 0;

        for (ScheduledPlan plan : current) {

            if (plan == null) {
                continue;
            }

            if (plan.isComplete()) {
                complete++;
            }

            items += plan.count();

            score += plan.score;
        }

        boolean better =
                complete > best.completeMilestones

                || (complete
                == best.completeMilestones
                && items > best.scheduledItems)

                || (complete
                == best.completeMilestones
                && items == best.scheduledItems
                && score > best.totalScore);

        if (better) {

            best.milestones.clear();

            best.milestones.addAll(
                    current);

            best.completeMilestones =
                    complete;

            best.scheduledItems =
                    items;

            best.totalScore =
                    score;
        }
    }

    /**
     * Giới hạn số phương án được xét.
     */
    private void limitPlans(
            List<ScheduledPlan> plans,
            int limit) {

        if (plans.size() > limit) {

            plans.subList(
                    limit,
                    plans.size()).clear();
        }
    }

    /**
     * Lưu itinerary item.
     */
    private boolean addItem(
            int tripId,
            LocalDate date,
            Recommendation recommendation,
            LocalTime start,
            int minutes,
            String note) {

        if (recommendation == null
                || recommendation.getPlace() == null
                || start == null) {

            return false;
        }

        LocalTime actualStart =
                getValidStart(
                        recommendation,
                        start);

        LocalTime actualEnd =
                actualStart.plusMinutes(
                        minutes);

        ItineraryItem item =
                new ItineraryItem();

        item.setTripId(tripId);

        item.setPlaceId(
                recommendation
                        .getPlace()
                        .getId());

        item.setVisitDate(
                Date.valueOf(date));

        item.setStartTime(
                Time.valueOf(actualStart));

        item.setEndTime(
                Time.valueOf(actualEnd));

        item.setEstimatedCost(
                recommendation
                        .getPlace()
                        .getEstimatedPrice());

        item.setNote(
                note
                + " - ưu tiên đủ cấu trúc cột mốc; "
                + "điểm phù hợp: "
                + Math.round(
                        recommendation.getScore())
                + ".");

        return itineraryDAO.add(item);
    }

    /**
     * Sắp xếp recommendation theo score giảm dần.
     */
    private void sortByScore(
            List<Recommendation> list) {

        Collections.sort(
                list,
                new Comparator<Recommendation>() {

            @Override
            public int compare(
                    Recommendation first,
                    Recommendation second) {

                return Double.compare(
                        second.getScore(),
                        first.getScore());
            }
        });
    }

    /**
     * Comparator score giảm dần.
     */
    private static final Comparator<ScheduledPlan>
            PLAN_SCORE_DESC =
            new Comparator<ScheduledPlan>() {

        @Override
        public int compare(
                ScheduledPlan first,
                ScheduledPlan second) {

            return Double.compare(
                    second.score,
                    first.score);
        }
    };

    /**
     * Comparator:
     * nhiều item hơn trước,
     * sau đó score cao hơn.
     */
    private static final Comparator<ScheduledPlan>
            PLAN_PRIORITY_DESC =
            new Comparator<ScheduledPlan>() {

        @Override
        public int compare(
                ScheduledPlan first,
                ScheduledPlan second) {

            int countCompare =
                    Integer.compare(
                            second.count(),
                            first.count());

            if (countCompare != 0) {
                return countCompare;
            }

            return Double.compare(
                    second.score,
                    first.score);
        }
    };

    /**
     * Hiển thị tên milestone.
     */
    private String milestoneLabel(
            ScheduledPlan plan) {

        if (plan.foodStart == null) {
            return "không xác định";
        }

        LocalTime start =
                plan.foodStart;

        if (start.isBefore(
                LocalTime.of(11, 0))) {

            return "07:00 - 11:00";
        }

        if (start.isBefore(
                LocalTime.of(17, 0))) {

            return "11:00 - 17:00";
        }

        return "17:00 - 22:00";
    }
}