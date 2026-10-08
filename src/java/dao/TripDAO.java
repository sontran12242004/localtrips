package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Trip;

public class TripDAO {

    public List<Trip> findByUser(int userId) {
        List<Trip> trips = new ArrayList<>();

        String sql =
                "SELECT t.trip_id, t.owner_id, t.trip_name, "
                + "t.destination, t.start_date, t.end_date, "
                + "t.budget, t.description, t.status, t.created_at, "
                + "CASE WHEN t.owner_id = ? "
                + "THEN 'OWNER' ELSE tm.member_role END AS user_role "
                + "FROM Trips t "
                + "LEFT JOIN TripMembers tm "
                + "ON tm.trip_id = t.trip_id AND tm.user_id = ? "
                + "WHERE t.owner_id = ? OR tm.user_id IS NOT NULL "
                + "ORDER BY t.created_at DESC, t.trip_id DESC";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, userId);
            statement.setInt(3, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    trips.add(mapTrip(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải danh sách chuyến đi.",
                    e
            );
        }

        return trips;
    }

    public Trip findByIdForUser(int tripId, int userId) {

        String sql =
                "SELECT t.trip_id, t.owner_id, t.trip_name, "
                + "t.destination, t.start_date, t.end_date, "
                + "t.budget, t.description, t.status, t.created_at, "
                + "CASE WHEN t.owner_id = ? "
                + "THEN 'OWNER' ELSE tm.member_role END AS user_role "
                + "FROM Trips t "
                + "LEFT JOIN TripMembers tm "
                + "ON tm.trip_id = t.trip_id AND tm.user_id = ? "
                + "WHERE t.trip_id = ? "
                + "AND (t.owner_id = ? OR tm.user_id IS NOT NULL)";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, userId);
            statement.setInt(3, tripId);
            statement.setInt(4, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTrip(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải chuyến đi.",
                    e
            );
        }

        return null;
    }

    public int create(Trip trip, int ownerId) {

        String insertTrip =
                "INSERT INTO Trips "
                + "(owner_id, trip_name, destination, start_date, "
                + "end_date, budget, description, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String insertOwner =
                "INSERT INTO TripMembers "
                + "(trip_id, user_id, member_role) "
                + "VALUES (?, ?, 'OWNER')";

        try (Connection connection = DBContext.getConnection()) {

            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int tripId;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     insertTrip,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    statement.setInt(1, ownerId);
                    statement.setString(2, trip.getTripName());
                    statement.setString(3, trip.getDestination());
                    statement.setDate(4, trip.getStartDate());
                    statement.setDate(5, trip.getEndDate());
                    statement.setBigDecimal(6, trip.getBudget());
                    statement.setString(7, trip.getDescription());
                    statement.setString(8, trip.getStatus());

                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return -1;
                    }

                    try (ResultSet keys =
                                 statement.getGeneratedKeys()) {

                        if (!keys.next()) {
                            connection.rollback();
                            return -1;
                        }

                        tripId = keys.getInt(1);
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(insertOwner)) {

                    statement.setInt(1, tripId);
                    statement.setInt(2, ownerId);
                    statement.executeUpdate();
                }

                connection.commit();
                return tripId;

            } catch (SQLException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tạo chuyến đi.",
                    e
            );
        }
    }

    public boolean updateByOwner(Trip trip, int ownerId) {

        String sql =
                "UPDATE Trips SET "
                + "trip_name = ?, "
                + "destination = ?, "
                + "start_date = ?, "
                + "end_date = ?, "
                + "budget = ?, "
                + "description = ?, "
                + "status = ? "
                + "WHERE trip_id = ? AND owner_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, trip.getTripName());
            statement.setString(2, trip.getDestination());
            statement.setDate(3, trip.getStartDate());
            statement.setDate(4, trip.getEndDate());
            statement.setBigDecimal(5, trip.getBudget());
            statement.setString(6, trip.getDescription());
            statement.setString(7, trip.getStatus());
            statement.setInt(8, trip.getTripId());
            statement.setInt(9, ownerId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể cập nhật chuyến đi.",
                    e
            );
        }
    }

    public boolean isOwner(int tripId, int userId) {

        String sql =
                "SELECT 1 FROM Trips "
                + "WHERE trip_id = ? AND owner_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể kiểm tra quyền sở hữu Trip.",
                    e
            );
        }
    }

    private Trip mapTrip(ResultSet resultSet)
            throws SQLException {

        Trip trip = new Trip();

        trip.setTripId(resultSet.getInt("trip_id"));
        trip.setOwnerId(resultSet.getInt("owner_id"));
        trip.setTripName(resultSet.getString("trip_name"));
        trip.setDestination(resultSet.getString("destination"));
        trip.setStartDate(resultSet.getDate("start_date"));
        trip.setEndDate(resultSet.getDate("end_date"));
        trip.setBudget(resultSet.getBigDecimal("budget"));
        trip.setDescription(resultSet.getString("description"));
        trip.setStatus(resultSet.getString("status"));
        trip.setCreatedAt(resultSet.getTimestamp("created_at"));
        trip.setRole(resultSet.getString("user_role"));

        return trip;
    }
}