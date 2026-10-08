package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.GroupFundContribution;
import model.GroupFundSummary;
import model.GroupFundTransaction;
import model.GroupFundMemberSummary;

public class GroupFundDAO {

    public GroupFundSummary findSummary(int tripId) {
        String sql =
            "SELECT " +
            "COALESCE((SELECT SUM(amount) FROM GroupFundContributions WHERE trip_id = ?), 0) AS total_contributed, " +
            "COALESCE((SELECT SUM(amount) FROM Expenses WHERE trip_id = ? AND from_group_fund = 1), 0) AS total_spent";

        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            ps.setInt(2, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    GroupFundSummary s = new GroupFundSummary();
                    s.setTotalContributed(rs.getBigDecimal("total_contributed"));
                    s.setTotalSpent(rs.getBigDecimal("total_spent"));
                    s.setBalance(s.getTotalContributed().subtract(s.getTotalSpent()));
                    return s;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải số dư quỹ nhóm.", e);
        }
        return new GroupFundSummary();
    }

    public List<GroupFundContribution> findContributions(int tripId) {
        List<GroupFundContribution> list = new ArrayList<>();
        String sql = "SELECT c.contribution_id, c.trip_id, c.user_id, u.full_name AS member_name, " +
                     "c.amount, c.note, c.created_by, c.created_at " +
                     "FROM GroupFundContributions c INNER JOIN Users u ON u.user_id = c.user_id " +
                     "WHERE c.trip_id = ? ORDER BY c.created_at DESC, c.contribution_id DESC";
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GroupFundContribution x = new GroupFundContribution();
                    x.setContributionId(rs.getInt("contribution_id"));
                    x.setTripId(rs.getInt("trip_id"));
                    x.setUserId(rs.getInt("user_id"));
                    x.setMemberName(rs.getString("member_name"));
                    x.setAmount(rs.getBigDecimal("amount"));
                    x.setNote(rs.getString("note"));
                    x.setCreatedBy(rs.getInt("created_by"));
                    x.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(x);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải lịch sử đóng quỹ.", e);
        }
        return list;
    }

    public List<GroupFundMemberSummary> findMemberSummaries(int tripId) {
        List<GroupFundMemberSummary> list = new ArrayList<>();
        String sql =
            "SELECT x.user_id, x.member_name, x.member_role, " +
            "COALESCE(SUM(c.amount), 0) AS contributed, " +
            "COUNT(c.contribution_id) AS contribution_count, " +
            "MAX(c.created_at) AS last_contribution_at " +
            "FROM (" +
            "  SELECT t.owner_id AS user_id, u.full_name AS member_name, 'OWNER' AS member_role " +
            "  FROM Trips t INNER JOIN Users u ON u.user_id = t.owner_id WHERE t.trip_id = ? " +
            "  UNION " +
            "  SELECT tm.user_id, u.full_name, tm.member_role " +
            "  FROM TripMembers tm INNER JOIN Users u ON u.user_id = tm.user_id " +
            "  WHERE tm.trip_id = ? " +
            ") x " +
            "LEFT JOIN GroupFundContributions c ON c.trip_id = ? AND c.user_id = x.user_id " +
            "GROUP BY x.user_id, x.member_name, x.member_role " +
            "ORDER BY contributed DESC, x.member_name ASC";
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            ps.setInt(2, tripId);
            ps.setInt(3, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GroupFundMemberSummary x = new GroupFundMemberSummary();
                    x.setUserId(rs.getInt("user_id"));
                    x.setMemberName(rs.getString("member_name"));
                    x.setRole(rs.getString("member_role"));
                    x.setContributed(rs.getBigDecimal("contributed"));
                    x.setContributionCount(rs.getInt("contribution_count"));
                    x.setLastContributionAt(rs.getTimestamp("last_contribution_at"));
                    list.add(x);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải tổng đóng góp của thành viên.", e);
        }
        return list;
    }

    public boolean isTripParticipant(int tripId, int userId) {
        String sql =
            "SELECT 1 FROM Trips t WHERE t.trip_id = ? AND t.owner_id = ? " +
            "UNION ALL " +
            "SELECT 1 FROM TripMembers tm WHERE tm.trip_id = ? AND tm.user_id = ?";
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            ps.setInt(2, userId);
            ps.setInt(3, tripId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra thành viên của Trip.", e);
        }
    }

    public List<GroupFundTransaction> findTransactions(int tripId) {
        List<GroupFundTransaction> list = new ArrayList<>();
        String sql =
            "SELECT transaction_type, amount, description, member_name, created_at FROM (" +
            " SELECT 'CONTRIBUTION' AS transaction_type, c.amount AS amount, " +
            " COALESCE(NULLIF(c.note, ''), 'Đóng góp vào quỹ') AS description, " +
            " u.full_name AS member_name, c.created_at AS created_at " +
            " FROM GroupFundContributions c INNER JOIN Users u ON u.user_id = c.user_id WHERE c.trip_id = ? " +
            " UNION ALL " +
            " SELECT 'EXPENSE' AS transaction_type, -e.amount AS amount, e.description AS description, " +
            " u.full_name AS member_name, e.created_at AS created_at " +
            " FROM Expenses e INNER JOIN Users u ON u.user_id = e.payer_id " +
            " WHERE e.trip_id = ? AND e.from_group_fund = 1 " +
            ") x ORDER BY created_at DESC";
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            ps.setInt(2, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GroupFundTransaction x = new GroupFundTransaction();
                    x.setType(rs.getString("transaction_type"));
                    x.setAmount(rs.getBigDecimal("amount"));
                    x.setDescription(rs.getString("description"));
                    x.setMemberName(rs.getString("member_name"));
                    x.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(x);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải lịch sử giao dịch quỹ.", e);
        }
        return list;
    }

    public boolean addContribution(int tripId, int userId, BigDecimal amount, String note, int ownerId) {
        String sql =
            "INSERT INTO GroupFundContributions (trip_id, user_id, amount, note, created_by) " +
            "SELECT ?, ?, ?, ?, ? " +
            "WHERE EXISTS (SELECT 1 FROM Trips WHERE trip_id = ? AND owner_id = ?) " +
            "AND (EXISTS (SELECT 1 FROM Trips WHERE trip_id = ? AND owner_id = ?) " +
            "     OR EXISTS (SELECT 1 FROM TripMembers WHERE trip_id = ? AND user_id = ?))";
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tripId);
            ps.setInt(2, userId);
            ps.setBigDecimal(3, amount.setScale(2, RoundingMode.HALF_UP));
            ps.setString(4, note);
            ps.setInt(5, ownerId);
            ps.setInt(6, tripId);
            ps.setInt(7, ownerId);
            ps.setInt(8, tripId);
            ps.setInt(9, ownerId);
            ps.setInt(10, tripId);
            ps.setInt(11, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể ghi nhận đóng góp vào quỹ.", e);
        }
    }
}
