package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.User;

/** Persistence for long-lived remember-me browser tokens. */
public class RememberMeDAO {

    public void createToken(int userId, String tokenHash, long expiresAtMillis) {
        String sql = "INSERT INTO RememberMeTokens (user_id, token_hash, expires_at) "
                + "VALUES (?, ?, ?)";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, tokenHash);
            ps.setTimestamp(3, new java.sql.Timestamp(expiresAtMillis));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Khong the luu remember-me token", e);
        }
    }

    public User findUserByTokenHash(String tokenHash) {
        String sql = "SELECT u.user_id, u.full_name, u.email, u.password_hash, "
                + "u.role, u.is_active, u.created_at "
                + "FROM RememberMeTokens r "
                + "JOIN Users u ON u.user_id = r.user_id "
                + "WHERE r.token_hash = ? AND r.expires_at > SYSDATETIME() "
                + "AND u.is_active = 1";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                // Never put the password hash into the authenticated session.
                u.setPasswordHash(null);
                u.setRole(rs.getString("role"));
                u.setActive(rs.getBoolean("is_active"));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                return u;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Khong the kiem tra remember-me token", e);
        }
    }

    public void deleteToken(String tokenHash) {
        String sql = "DELETE FROM RememberMeTokens WHERE token_hash = ?";
        executeDelete(sql, tokenHash);
    }

    public void deleteAllForUser(int userId) {
        String sql = "DELETE FROM RememberMeTokens WHERE user_id = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Khong the xoa remember-me tokens", e);
        }
    }

    private void executeDelete(String sql, String value) {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Khong the xoa remember-me token", e);
        }
    }
}
