package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.User;

/** Persistence for single-use, short-lived password reset tokens. */
public class PasswordResetDAO {

    public void createToken(int userId, String tokenHash, long expiresAtMillis) {
        String deleteOld = "DELETE FROM PasswordResetTokens WHERE user_id = ? AND used_at IS NULL";
        String insert = "INSERT INTO PasswordResetTokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)";
        try (Connection con = DBContext.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(deleteOld)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setInt(1, userId);
                ps.setString(2, tokenHash);
                ps.setTimestamp(3, new java.sql.Timestamp(expiresAtMillis));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Khong the tao password reset token", e);
        }
    }

    public User findUserByValidToken(String tokenHash) {
        String sql = "SELECT u.user_id, u.full_name, u.email, u.role, u.is_active, u.created_at "
                + "FROM PasswordResetTokens p JOIN Users u ON u.user_id = p.user_id "
                + "WHERE p.token_hash = ? AND p.expires_at > CURRENT_TIMESTAMP "
                + "AND p.used_at IS NULL AND u.is_active = 1";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setActive(rs.getBoolean("is_active"));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                return u;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Khong the kiem tra password reset token", e);
        }
    }

    public boolean resetPassword(String tokenHash, String newPasswordHash) {
        String select = "SELECT user_id FROM PasswordResetTokens "
                + "WHERE token_hash = ? AND expires_at > CURRENT_TIMESTAMP AND used_at IS NULL";
        String updateUser = "UPDATE Users SET password_hash = ? WHERE user_id = ? AND is_active = 1";
        String consume = "UPDATE PasswordResetTokens SET used_at = CURRENT_TIMESTAMP "
                + "WHERE token_hash = ? AND used_at IS NULL";

        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = con.prepareStatement(select)) {
                    ps.setString(1, tokenHash);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        userId = rs.getInt("user_id");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(updateUser)) {
                    ps.setString(1, newPasswordHash);
                    ps.setInt(2, userId);
                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(consume)) {
                    ps.setString(1, tokenHash);
                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                try { con.rollback(); } catch (SQLException ignored) { }
                throw e;
            } finally {
                try { con.setAutoCommit(true); } catch (SQLException ignored) { }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Khong the reset password", e);
        }
    }

    public void deleteTokensForUser(int userId) {
        String sql = "DELETE FROM PasswordResetTokens WHERE user_id = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Khong the xoa password reset tokens", e);
        }
    }
}
