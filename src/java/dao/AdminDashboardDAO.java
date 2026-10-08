package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Data needed by the ADMIN dashboard. */
public class AdminDashboardDAO {

    public int countUsers() {
        return count("SELECT COUNT(*) FROM Users");
    }

    public int countActiveUsers() {
        return count("SELECT COUNT(*) FROM Users WHERE is_active = 1");
    }

    public int countTrips() {
        return count("SELECT COUNT(*) FROM Trips");
    }

    public int countPlaces() {
        return count("SELECT COUNT(*) FROM Places WHERE is_active = 1");
    }

    public int countCategories() {
        return count("SELECT COUNT(*) FROM Categories");
    }

    private int count(String sql) {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Khong the lay so lieu Admin Dashboard", e);
        }
    }
}
