package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import model.ItineraryItem;

public class ItineraryDAO {

    public List<ItineraryItem> findByTrip(int tripId) {
        List<ItineraryItem> items = new ArrayList<>();

        String sql
                = "SELECT i.item_id, i.trip_id, i.place_id, "
                + "p.place_name, i.visit_date, "
                + "i.start_time, i.end_time, i.note, "
                + "i.estimated_cost, p.latitude, p.longitude, i.created_at "
                + "FROM ItineraryItems i "
                + "INNER JOIN Places p "
                + "ON p.place_id = i.place_id "
                + "WHERE i.trip_id = ? "
                + "ORDER BY i.visit_date, i.start_time, i.item_id";

        try ( Connection connection = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);

            try ( ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapItem(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải lịch trình.",
                    e
            );
        }

        return items;
    }

    public boolean add(ItineraryItem item) {
        String sql
                = "INSERT INTO ItineraryItems "
                + "(trip_id, place_id, visit_date, "
                + "start_time, end_time, note, estimated_cost) "
                + "VALUES (?, ?, ?, "
                + "CAST(? AS TIME), CAST(? AS TIME), ?, ?)";

        try ( Connection connection = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, item.getTripId());
            statement.setInt(2, item.getPlaceId());
            statement.setDate(3, item.getVisitDate());
            statement.setString(4, item.getStartTime().toString());
            statement.setString(5, item.getEndTime().toString());
            statement.setString(6, item.getNote());
            statement.setBigDecimal(
                    7,
                    item.getEstimatedCost()
            );

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể thêm địa điểm vào lịch trình.",
                    e
            );
        }
    }

    public boolean hasTimeConflict(
            int tripId,
            Date visitDate,
            Time startTime,
            Time endTime
    ) {
        String sql
                = "SELECT 1 "
                + "FROM ItineraryItems "
                + "WHERE trip_id = ? "
                + "AND visit_date = ? "
                + "AND start_time < CAST(? AS TIME) "
                + "AND end_time > CAST(? AS TIME)";

        try ( Connection connection = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);
            statement.setDate(2, visitDate);
            statement.setString(3, endTime.toString());
            statement.setString(4, startTime.toString());

            try ( ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể kiểm tra thời gian lịch trình.",
                    e
            );
        }
    }

    public boolean deleteByOwner(
            int itemId,
            int tripId,
            int ownerId
    ) {
        String sql
                = "DELETE FROM ItineraryItems "
                + "WHERE item_id = ? "
                + "AND trip_id = ? "
                + "AND EXISTS ("
                + "SELECT 1 FROM Trips t "
                + "WHERE t.trip_id = ItineraryItems.trip_id "
                + "AND t.owner_id = ?"
                + ")";

        try ( Connection connection = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);
            statement.setInt(2, tripId);
            statement.setInt(3, ownerId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể xóa mục khỏi lịch trình.",
                    e
            );
        }
    }

    private ItineraryItem mapItem(ResultSet resultSet)
            throws SQLException {

        ItineraryItem item = new ItineraryItem();

        item.setItemId(resultSet.getInt("item_id"));
        item.setTripId(resultSet.getInt("trip_id"));
        item.setPlaceId(resultSet.getInt("place_id"));
        item.setPlaceName(resultSet.getString("place_name"));
        item.setVisitDate(resultSet.getDate("visit_date"));
        item.setStartTime(resultSet.getTime("start_time"));
        item.setEndTime(resultSet.getTime("end_time"));
        item.setNote(resultSet.getString("note"));
        item.setEstimatedCost(
                resultSet.getBigDecimal("estimated_cost")
        );
        item.setLatitude(
                resultSet.getBigDecimal("latitude")
        );
        item.setLongitude(
                resultSet.getBigDecimal("longitude")
        );
        item.setCreatedAt(
                resultSet.getTimestamp("created_at")
        );

        return item;
    }
}
