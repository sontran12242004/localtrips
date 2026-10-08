package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Trip;

/** Data needed by the USER dashboard. */
public class DashboardDAO {

    public int countUserTrips(int userId) {
        String sql = "SELECT COUNT(*) FROM (" +
                " SELECT trip_id FROM Trips WHERE owner_id = ?" +
                " UNION" +
                " SELECT trip_id FROM TripMembers WHERE user_id = ?" +
                ") x";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Khong the dem Trip cua user", e);
        }
    }

    public int countOwnedTrips(int userId) {
        return count("SELECT COUNT(*) FROM Trips WHERE owner_id = ?", userId);
    }

    public int countMemberTrips(int userId) {
        String sql = "SELECT COUNT(*) FROM TripMembers tm " +
                "WHERE tm.user_id = ? AND NOT EXISTS " +
                "(SELECT 1 FROM Trips t WHERE t.trip_id = tm.trip_id AND t.owner_id = tm.user_id)";
        return count(sql, userId);
    }

    public List<Trip> findRecentTrips(int userId, int limit) {
        String sql =
                "SELECT TOP (?) t.trip_id, t.owner_id, t.trip_name, t.destination, " +
                "t.start_date, t.end_date, t.budget, t.description, t.status, t.created_at " +
                "FROM Trips t " +
                "WHERE t.owner_id = ? OR EXISTS " +
                "(SELECT 1 FROM TripMembers tm WHERE tm.trip_id = t.trip_id AND tm.user_id = ?) " +
                "ORDER BY t.created_at DESC, t.trip_id DESC";

        List<Trip> trips = new ArrayList<Trip>();
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) trips.add(mapTrip(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Khong the lay Trip gan day", e);
        }
        return trips;
    }

    private int count(String sql, int userId) {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Khong the truy van dashboard", e);
        }
    }

    private Trip mapTrip(ResultSet rs) throws SQLException {
        Trip t = new Trip();
        t.setTripId(rs.getInt("trip_id"));
        t.setOwnerId(rs.getInt("owner_id"));
        t.setTripName(rs.getString("trip_name"));
        t.setDestination(rs.getString("destination"));
        t.setStartDate(rs.getDate("start_date"));
        t.setEndDate(rs.getDate("end_date"));
        t.setBudget(rs.getBigDecimal("budget"));
        t.setDescription(rs.getString("description"));
        t.setStatus(rs.getString("status"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        return t;
    }
}
