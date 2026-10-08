package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.AdminAuditLog;

public class AdminAuditLogDAO {

    public boolean log(int actorUserId, String action, Integer targetUserId, String detail) {
        String sql = "INSERT INTO AdminAuditLogs "
                + "(actor_user_id, action, target_user_id, detail) VALUES (?, ?, ?, ?)";

        String safeDetail = detail;
        if (safeDetail != null && safeDetail.length() > 500) {
            safeDetail = safeDetail.substring(0, 500);
        }

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, actorUserId);
            ps.setString(2, action);
            if (targetUserId == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, targetUserId);
            }
            if (safeDetail == null) {
                ps.setNull(4, Types.NVARCHAR);
            } else {
                ps.setString(4, safeDetail);
            }
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countSearch(String keyword, String action) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ")
           .append("FROM AdminAuditLogs l ")
           .append("JOIN Users actor ON actor.user_id = l.actor_user_id ")
           .append("LEFT JOIN Users target ON target.user_id = l.target_user_id ")
           .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<Object>();
        appendFilters(sql, params, keyword, action);

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể đếm audit log", e);
        }
    }

    public List<AdminAuditLog> searchPage(String keyword, String action, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT l.audit_id, l.actor_user_id, ")
           .append("actor.full_name AS actor_name, actor.email AS actor_email, ")
           .append("l.action, l.target_user_id, ")
           .append("target.full_name AS target_name, target.email AS target_email, ")
           .append("l.detail, l.created_at ")
           .append("FROM AdminAuditLogs l ")
           .append("JOIN Users actor ON actor.user_id = l.actor_user_id ")
           .append("LEFT JOIN Users target ON target.user_id = l.target_user_id ")
           .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<Object>();
        appendFilters(sql, params, keyword, action);
        sql.append("ORDER BY l.created_at DESC, l.audit_id DESC ")
           .append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        params.add((page - 1) * pageSize);
        params.add(pageSize);

        List<AdminAuditLog> logs = new ArrayList<AdminAuditLog>();
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải audit log", e);
        }
        return logs;
    }

    // Giữ lại method cũ để các phần code khác không bị ảnh hưởng.
    public List<AdminAuditLog> findRecent(int limit) {
        if (limit < 1 || limit > 200) limit = 50;
        return searchPage("", "", 1, limit);
    }

    private void appendFilters(StringBuilder sql, List<Object> params,
                               String keyword, String action) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (actor.full_name LIKE ? OR actor.email LIKE ? "
                    + "OR target.full_name LIKE ? OR target.email LIKE ? "
                    + "OR l.detail LIKE ?) ");
            String q = "%" + keyword.trim() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        if (action != null && !action.trim().isEmpty()) {
            sql.append("AND l.action = ? ");
            params.add(action.trim().toUpperCase());
        }
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);
            } else {
                ps.setString(i + 1, String.valueOf(value));
            }
        }
    }

    private AdminAuditLog map(ResultSet rs) throws SQLException {
        AdminAuditLog log = new AdminAuditLog();
        log.setAuditId(rs.getLong("audit_id"));
        log.setActorUserId(rs.getInt("actor_user_id"));
        log.setActorName(rs.getString("actor_name"));
        log.setActorEmail(rs.getString("actor_email"));
        log.setAction(rs.getString("action"));

        int targetUserId = rs.getInt("target_user_id");
        log.setTargetUserId(rs.wasNull() ? null : Integer.valueOf(targetUserId));
        log.setTargetName(rs.getString("target_name"));
        log.setTargetEmail(rs.getString("target_email"));
        log.setDetail(rs.getString("detail"));
        log.setCreatedAt(rs.getTimestamp("created_at"));
        return log;
    }
}
