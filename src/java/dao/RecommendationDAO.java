package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import model.Place;
import model.Recommendation;

public class RecommendationDAO {

    public List<Recommendation> findForTrip(int tripId) {

        List<Recommendation> recommendations =
                new ArrayList<>();

        String sql =
                "SELECT p.place_id, p.category_id, "
                + "c.category_name, p.place_name, "
                + "p.address, p.description, "
                + "p.estimated_cost, p.rating, "
                + "p.opening_time, p.closing_time, "
                + "p.latitude, p.longitude, "
                + "p.place_type, p.is_active, "
                + "COUNT(DISTINCT pref.user_id) "
                + "AS preference_count, "
                + "t.budget, "
                + "(SELECT COUNT(*) "
                + " FROM TripMembers tm "
                + " WHERE tm.trip_id = t.trip_id) "
                + "AS total_members "
                + "FROM Trips t "
                + "INNER JOIN TripMemberPreferences pref "
                + "ON pref.trip_id = t.trip_id "
                + "INNER JOIN Places p "
                + "ON p.category_id = pref.category_id "
                + "AND p.is_active = 1 "
                + "INNER JOIN Categories c "
                + "ON c.category_id = p.category_id "
                + "WHERE t.trip_id = ? "
                + "GROUP BY p.place_id, p.category_id, "
                + "c.category_name, p.place_name, "
                + "p.address, p.description, "
                + "p.estimated_cost, p.rating, "
                + "p.opening_time, p.closing_time, "
                + "p.latitude, p.longitude, "
                + "p.place_type, p.is_active, "
                + "t.budget, t.trip_id";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    Place place = mapPlace(resultSet);

                    int preferenceCount =
                            resultSet.getInt(
                                    "preference_count"
                            );

                    int totalMembers =
                            resultSet.getInt(
                                    "total_members"
                            );

                    BigDecimal tripBudget =
                            resultSet.getBigDecimal("budget");

                    Recommendation recommendation =
                            calculateRecommendation(
                                    place,
                                    preferenceCount,
                                    totalMembers,
                                    tripBudget
                            );

                    recommendations.add(recommendation);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tạo danh sách gợi ý.",
                    e
            );
        }

        Collections.sort(
                recommendations,
                new Comparator<Recommendation>() {
                    @Override
                    public int compare(
                            Recommendation first,
                            Recommendation second) {

                        return Double.compare(
                                second.getScore(),
                                first.getScore()
                        );
                    }
                }
        );

        return recommendations;
    }



    /**
     * Lấy toàn bộ địa điểm thuộc một danh mục cho một Trip, nhưng vẫn
     * tính preference của các thành viên trong Trip cho danh mục đó.
     * Dùng cho Auto Itinerary để luôn có đủ FOOD + CAFE + hoạt động cuối.
     */
    public List<Recommendation> findForTripByCategory(int tripId, String categoryCode) {
        List<Recommendation> recommendations = new ArrayList<>();

        String sql =
                "SELECT p.place_id, p.category_id, c.category_name, "
                + "p.place_name, p.address, p.description, "
                + "p.estimated_cost, p.rating, p.opening_time, p.closing_time, "
                + "p.latitude, p.longitude, p.place_type, p.is_active, "
                + "COUNT(DISTINCT pref.user_id) AS preference_count, "
                + "t.budget, "
                + "(SELECT COUNT(*) FROM TripMembers tm "
                + " WHERE tm.trip_id = t.trip_id) AS total_members "
                + "FROM Trips t "
                + "CROSS JOIN Places p "
                + "INNER JOIN Categories c ON c.category_id = p.category_id "
                + "LEFT JOIN TripMemberPreferences pref "
                + "ON pref.trip_id = t.trip_id "
                + "AND pref.category_id = p.category_id "
                + "WHERE t.trip_id = ? "
                + "AND p.is_active = 1 "
                + "AND c.category_code = ? "
                + "GROUP BY p.place_id, p.category_id, c.category_name, "
                + "p.place_name, p.address, p.description, p.estimated_cost, "
                + "p.rating, p.opening_time, p.closing_time, p.latitude, "
                + "p.longitude, p.place_type, p.is_active, t.budget, t.trip_id";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);
            statement.setString(2, categoryCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Place place = mapPlace(resultSet);
                    int preferenceCount = resultSet.getInt("preference_count");
                    int totalMembers = resultSet.getInt("total_members");
                    BigDecimal tripBudget = resultSet.getBigDecimal("budget");

                    recommendations.add(calculateRecommendation(
                            place, preferenceCount, totalMembers, tripBudget));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tạo danh sách gợi ý theo danh mục.", e);
        }

        Collections.sort(recommendations, new Comparator<Recommendation>() {
            @Override
            public int compare(Recommendation first, Recommendation second) {
                return Double.compare(second.getScore(), first.getScore());
            }
        });

        return recommendations;
    }

    private Recommendation calculateRecommendation(
            Place place,
            int preferenceCount,
            int totalMembers,
            BigDecimal tripBudget) {

        double preferenceMatch =
                totalMembers <= 0
                ? 0
                : preferenceCount * 100.0
                  / totalMembers;

        double ratingScore =
                place.getRating() == null
                ? 0
                : place.getRating().doubleValue()
                  / 5.0 * 100.0;

        double budgetMatch = calculateBudgetMatch(
                place.getEstimatedCost(),
                tripBudget,
                totalMembers
        );

        double score =
                preferenceMatch * 0.50
                + ratingScore * 0.30
                + budgetMatch * 0.20;

        Recommendation recommendation =
                new Recommendation();

        recommendation.setPlace(place);
        recommendation.setPreferenceMatch(
                preferenceMatch
        );
        recommendation.setBudgetMatch(budgetMatch);
        recommendation.setScore(score);

        recommendation.setReason(
                preferenceCount
                + " thành viên chọn danh mục "
                + place.getCategoryName()
        );

        return recommendation;
    }

    private double calculateBudgetMatch(
            BigDecimal estimatedCost,
            BigDecimal tripBudget,
            int totalMembers) {

        if (estimatedCost == null
                || estimatedCost.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {
            return 100;
        }

        if (tripBudget == null
                || tripBudget.compareTo(
                        BigDecimal.ZERO
                ) <= 0
                || totalMembers <= 0) {
            return 50;
        }

        BigDecimal budgetPerMember =
                tripBudget.divide(
                        BigDecimal.valueOf(totalMembers),
                        2,
                        RoundingMode.HALF_UP
                );

        if (estimatedCost.compareTo(
                budgetPerMember) <= 0) {
            return 100;
        }

        double percentage =
                budgetPerMember
                        .divide(
                                estimatedCost,
                                4,
                                RoundingMode.HALF_UP
                        )
                        .doubleValue()
                * 100.0;

        return Math.max(
                0,
                Math.min(100, percentage)
        );
    }

    private Place mapPlace(ResultSet resultSet)
            throws SQLException {

        Place place = new Place();

        place.setPlaceId(
                resultSet.getInt("place_id")
        );
        place.setCategoryId(
                resultSet.getInt("category_id")
        );
        place.setCategoryName(
                resultSet.getString("category_name")
        );
        place.setPlaceName(
                resultSet.getString("place_name")
        );
        place.setAddress(
                resultSet.getString("address")
        );
        place.setDescription(
                resultSet.getString("description")
        );
        place.setEstimatedCost(
                resultSet.getBigDecimal(
                        "estimated_cost"
                )
        );
        place.setRating(
                resultSet.getBigDecimal("rating")
        );
        place.setOpeningTime(
                resultSet.getTime("opening_time")
        );
        place.setClosingTime(
                resultSet.getTime("closing_time")
        );
        place.setLatitude(
                resultSet.getBigDecimal("latitude")
        );
        place.setLongitude(
                resultSet.getBigDecimal("longitude")
        );
        place.setPlaceType(
                resultSet.getString("place_type")
        );
        place.setActive(
                resultSet.getBoolean("is_active")
        );

        return place;
    }
}