package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.LoginLog;

public class LoginLogDAO {

    private static final String INSERT_SQL
            = "INSERT INTO LoginLogs "
            + "(user_id, email, success, failure_reason, ip_address, user_agent) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    public boolean log(Integer userId, String email, boolean success,
            String failureReason, String ipAddress, String userAgent) {

        try (Connection connection = DBContext.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            if (userId == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, userId);
            }

            statement.setString(2, email);
            statement.setBoolean(3, success);
            statement.setString(4, failureReason);
            statement.setString(5, ipAddress);

            if (userAgent != null && userAgent.length() > 500) {
                userAgent = userAgent.substring(0, 500);
            }
            statement.setString(6, userAgent);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<LoginLog> findRecent(int limit) {
        return findPage(null, null, null, null, 1, Math.max(1, limit));
    }

    public List<LoginLog> searchRecent(String emailKeyword, String result, int limit) {
        return findPage(emailKeyword, result, null, null, 1, Math.max(1, limit));
    }

    public List<LoginLog> findPage(String emailKeyword, String result,
            Date fromDate, Date toDate, int page, int pageSize) {

        List<LoginLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT login_log_id, user_id, email, success, failure_reason, "
                + "ip_address, user_agent, attempted_at "
                + "FROM LoginLogs WHERE 1 = 1 ");

        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, emailKeyword, result, fromDate, toDate);

        sql.append("ORDER BY attempted_at DESC, login_log_id DESC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        int offset = Math.max(0, page - 1) * Math.max(1, pageSize);
        parameters.add(offset);
        parameters.add(Math.max(1, pageSize));

        try (Connection connection = DBContext.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(map(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }

    public int count(String emailKeyword, String result, Date fromDate, Date toDate) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM LoginLogs WHERE 1 = 1 ");
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, emailKeyword, result, fromDate, toDate);

        try (Connection connection = DBContext.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int countRecentFailures(String email, int minutes) {

        String sql = "SELECT COUNT(*) "
                + "FROM LoginLogs "
                + "WHERE email = ? "
                + "AND success = 0 "
                + "AND failure_reason = 'INVALID_CREDENTIALS' "
                + "AND attempted_at >= DATEADD(MINUTE, ?, SYSDATETIME()) "
                + "AND attempted_at > ISNULL((" 
                + "SELECT MAX(attempted_at) FROM LoginLogs "
                + "WHERE email = ? AND success = 1" 
                + "), CAST('1900-01-01' AS DATETIME2))";

        try (Connection connection = DBContext.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setInt(2, -Math.max(1, minutes));
            statement.setString(3, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void appendFilters(StringBuilder sql, List<Object> parameters,
            String emailKeyword, String result, Date fromDate, Date toDate) {

        if (emailKeyword != null && !emailKeyword.trim().isEmpty()) {
            sql.append("AND email LIKE ? ");
            parameters.add("%" + emailKeyword.trim() + "%");
        }

        if ("success".equalsIgnoreCase(result)) {
            sql.append("AND success = ? ");
            parameters.add(true);
        } else if ("failed".equalsIgnoreCase(result)) {
            sql.append("AND success = ? ");
            parameters.add(false);
        }

        if (fromDate != null) {
            sql.append("AND attempted_at >= ? ");
            parameters.add(fromDate);
        }

        if (toDate != null) {
            sql.append("AND attempted_at < DATEADD(DAY, 1, ?) ");
            parameters.add(toDate);
        }
    }

    private void setParameters(PreparedStatement statement, List<Object> parameters)
            throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object value = parameters.get(i);
            if (value instanceof Boolean) {
                statement.setBoolean(i + 1, (Boolean) value);
            } else if (value instanceof Date) {
                statement.setDate(i + 1, (Date) value);
            } else {
                statement.setObject(i + 1, value);
            }
        }
    }

    private LoginLog map(ResultSet resultSet) throws SQLException {
        LoginLog log = new LoginLog();
        log.setLoginLogId(resultSet.getLong("login_log_id"));

        int userId = resultSet.getInt("user_id");
        log.setUserId(resultSet.wasNull() ? null : userId);

        log.setEmail(resultSet.getString("email"));
        log.setSuccess(resultSet.getBoolean("success"));
        log.setFailureReason(resultSet.getString("failure_reason"));
        log.setIpAddress(resultSet.getString("ip_address"));
        log.setUserAgent(resultSet.getString("user_agent"));
        log.setAttemptedAt(resultSet.getTimestamp("attempted_at"));
        return log;
    }
}
