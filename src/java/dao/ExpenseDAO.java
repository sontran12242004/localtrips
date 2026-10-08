package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Expense;

public class ExpenseDAO {

    public List<Expense> findByTrip(int tripId) {
        Map<Integer, Expense> expenseMap
                = new LinkedHashMap<>();

        String sql
                = "SELECT e.expense_id, e.trip_id, e.payer_id, "
                + "e.created_by, e.description, e.amount, "
                + "e.from_group_fund, e.created_at, "
                + "payer.full_name AS payer_name, "
                + "participant.full_name AS participant_name "
                + "FROM Expenses e "
                + "INNER JOIN Users payer "
                + "ON payer.user_id = e.payer_id "
                + "LEFT JOIN ExpenseParticipants ep "
                + "ON ep.expense_id = e.expense_id "
                + "LEFT JOIN Users participant "
                + "ON participant.user_id = ep.user_id "
                + "WHERE e.trip_id = ? "
                + "ORDER BY e.created_at DESC, "
                + "e.expense_id DESC, participant.full_name";

        try ( Connection connection = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);

            try ( ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int expenseId
                            = resultSet.getInt("expense_id");

                    Expense expense = expenseMap.get(expenseId);

                    if (expense == null) {
                        expense = mapExpense(resultSet);
                        expenseMap.put(expenseId, expense);
                    }

                    String participantName
                            = resultSet.getString(
                                    "participant_name"
                            );

                    if (participantName != null) {
                        expense.addParticipantName(
                                participantName
                        );
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải danh sách chi phí.",
                    e
            );
        }

        return new ArrayList<>(expenseMap.values());
    }

    public int create(
            Expense expense,
            List<Integer> participantIds
    ) {
        if (participantIds == null
                || participantIds.isEmpty()) {

            throw new IllegalArgumentException(
                    "Phải chọn ít nhất một người tham gia."
            );
        }

        Set<Integer> uniqueParticipants
                = new LinkedHashSet<>(participantIds);

        String checkMemberSql
                = "SELECT 1 FROM TripMembers "
                + "WHERE trip_id = ? AND user_id = ?";

        String insertExpenseSql
                = "INSERT INTO Expenses "
                + "(trip_id, payer_id, created_by, "
                + "description, amount, from_group_fund) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        String insertParticipantSql
                = "INSERT INTO ExpenseParticipants "
                + "(expense_id, user_id, share_amount) "
                + "VALUES (?, ?, ?)";

        try ( Connection connection = DBContext.getConnection()) {

            boolean originalAutoCommit
                    = connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {
                validateMembers(
                        connection,
                        checkMemberSql,
                        expense.getTripId(),
                        uniqueParticipants
                );

                int expenseId;

                BigDecimal normalizedAmount
                        = expense.getAmount().setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

                try ( PreparedStatement statement
                        = connection.prepareStatement(
                                insertExpenseSql,
                                Statement.RETURN_GENERATED_KEYS
                        )) {

                            statement.setInt(
                                    1,
                                    expense.getTripId()
                            );

                            statement.setInt(
                                    2,
                                    expense.getPayerId()
                            );

                            statement.setInt(
                                    3,
                                    expense.getCreatedBy()
                            );

                            statement.setString(
                                    4,
                                    expense.getDescription()
                            );

                            statement.setBigDecimal(
                                    5,
                                    normalizedAmount
                            );

                            statement.setBoolean(
                                    6,
                                    expense.isFromGroupFund()
                            );

                            if (statement.executeUpdate() == 0) {
                                connection.rollback();
                                return -1;
                            }

                            try ( ResultSet keys
                                    = statement.getGeneratedKeys()) {

                                if (!keys.next()) {
                                    connection.rollback();
                                    return -1;
                                }

                                expenseId = keys.getInt(1);
                            }
                        }

                        insertParticipants(
                                connection,
                                insertParticipantSql,
                                expenseId,
                                normalizedAmount,
                                uniqueParticipants
                        );

                        connection.commit();
                        return expenseId;

            } catch (Exception e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(
                        originalAutoCommit
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể lưu khoản chi.",
                    e
            );
        }
    }

    private void validateMembers(
            Connection connection,
            String sql,
            int tripId,
            Set<Integer> participantIds
    ) throws SQLException {

        try ( PreparedStatement statement
                = connection.prepareStatement(sql)) {

            for (Integer userId : participantIds) {
                statement.setInt(1, tripId);
                statement.setInt(2, userId);

                try ( ResultSet resultSet
                        = statement.executeQuery()) {

                    if (!resultSet.next()) {
                        throw new IllegalArgumentException(
                                "Có người tham gia không thuộc Trip."
                        );
                    }
                }
            }
        }
    }

    private void insertParticipants(
            Connection connection,
            String sql,
            int expenseId,
            BigDecimal totalAmount,
            Set<Integer> participantIds
    ) throws SQLException {

        int participantCount = participantIds.size();

        BigDecimal shareAmount = totalAmount.divide(
                BigDecimal.valueOf(participantCount),
                1,
                RoundingMode.HALF_UP
        );

        try ( PreparedStatement statement
                = connection.prepareStatement(sql)) {

            for (Integer userId : participantIds) {
                statement.setInt(1, expenseId);
                statement.setInt(2, userId);
                statement.setBigDecimal(3, shareAmount);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    public Expense findById(int tripId, int expenseId) {
        String sql
                = "SELECT e.expense_id, e.trip_id, e.payer_id, "
                + "e.created_by, e.description, e.amount, "
                + "e.from_group_fund, e.created_at, "
                + "u.full_name AS payer_name "
                + "FROM Expenses e "
                + "INNER JOIN Users u ON u.user_id = e.payer_id "
                + "WHERE e.trip_id = ? AND e.expense_id = ?";

        try ( Connection connection = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);
            statement.setInt(2, expenseId);

            try ( ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapExpense(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải khoản chi cần sửa.",
                    e
            );
        }

        return null;
    }

    public List<Integer> findParticipantIds(
            int tripId,
            int expenseId
    ) {
        List<Integer> participantIds = new ArrayList<>();

        String sql
                = "SELECT ep.user_id "
                + "FROM ExpenseParticipants ep "
                + "INNER JOIN Expenses e "
                + "ON e.expense_id = ep.expense_id "
                + "WHERE e.trip_id = ? AND e.expense_id = ? "
                + "ORDER BY ep.user_id";

        try ( Connection connection = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);
            statement.setInt(2, expenseId);

            try ( ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    participantIds.add(
                            resultSet.getInt("user_id")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải danh sách người chia khoản chi.",
                    e
            );
        }

        return participantIds;
    }

    public boolean updateAuthorized(
            Expense expense,
            List<Integer> participantIds,
            int currentUserId
    ) {
        if (participantIds == null || participantIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Phải chọn ít nhất một người tham gia."
            );
        }

        Set<Integer> uniqueParticipants
                = new LinkedHashSet<>(participantIds);

        uniqueParticipants.add(expense.getPayerId());

        String checkMemberSql
                = "SELECT 1 FROM TripMembers "
                + "WHERE trip_id = ? AND user_id = ?";

        String updateExpenseSql
                = "UPDATE e "
                + "SET payer_id = ?, description = ?, "
                + "amount = ?, from_group_fund = ? "
                + "FROM Expenses e "
                + "WHERE e.expense_id = ? "
                + "AND e.trip_id = ? "
                + "AND (e.created_by = ? OR EXISTS ("
                + "    SELECT 1 FROM Trips t "
                + "    WHERE t.trip_id = e.trip_id "
                + "    AND t.owner_id = ?"
                + "))";

        String deleteParticipantsSql
                = "DELETE FROM ExpenseParticipants "
                + "WHERE expense_id = ?";

        String insertParticipantSql
                = "INSERT INTO ExpenseParticipants "
                + "(expense_id, user_id, share_amount) "
                + "VALUES (?, ?, ?)";

        try ( Connection connection = DBContext.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                validateMembers(
                        connection,
                        checkMemberSql,
                        expense.getTripId(),
                        uniqueParticipants
                );

                BigDecimal normalizedAmount = expense.getAmount()
                        .setScale(2, RoundingMode.HALF_UP);

                int updatedRows;

                try ( PreparedStatement statement
                        = connection.prepareStatement(updateExpenseSql)) {
                    statement.setInt(1, expense.getPayerId());
                    statement.setString(2, expense.getDescription());
                    statement.setBigDecimal(3, normalizedAmount);
                    statement.setBoolean(4, expense.isFromGroupFund());
                    statement.setInt(5, expense.getExpenseId());
                    statement.setInt(6, expense.getTripId());
                    statement.setInt(7, currentUserId);
                    statement.setInt(8, currentUserId);

                    updatedRows = statement.executeUpdate();
                }

                if (updatedRows == 0) {
                    connection.rollback();
                    return false;
                }

                try ( PreparedStatement statement
                        = connection.prepareStatement(deleteParticipantsSql)) {
                    statement.setInt(1, expense.getExpenseId());
                    statement.executeUpdate();
                }

                insertParticipants(
                        connection,
                        insertParticipantSql,
                        expense.getExpenseId(),
                        normalizedAmount,
                        uniqueParticipants
                );

                connection.commit();
                return true;

            } catch (Exception e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể cập nhật khoản chi.",
                    e
            );
        }
    }

    public boolean deleteAuthorized(
            int expenseId,
            int tripId,
            int currentUserId
    ) {
        String authorizationCondition
                = "e.expense_id = ? "
                + "AND e.trip_id = ? "
                + "AND ("
                + "e.created_by = ? "
                + "OR EXISTS ("
                + "SELECT 1 FROM Trips t "
                + "WHERE t.trip_id = e.trip_id "
                + "AND t.owner_id = ?"
                + ")"
                + ")";

        String deleteParticipantsSql
                = "DELETE ep "
                + "FROM ExpenseParticipants ep "
                + "INNER JOIN Expenses e "
                + "ON e.expense_id = ep.expense_id "
                + "WHERE " + authorizationCondition;

        String deleteExpenseSql
                = "DELETE e "
                + "FROM Expenses e "
                + "WHERE " + authorizationCondition;

        try ( Connection connection = DBContext.getConnection()) {

            boolean originalAutoCommit
                    = connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {
                try ( PreparedStatement statement
                        = connection.prepareStatement(
                                deleteParticipantsSql
                        )) {

                            statement.setInt(1, expenseId);
                            statement.setInt(2, tripId);
                            statement.setInt(3, currentUserId);
                            statement.setInt(4, currentUserId);
                            statement.executeUpdate();
                        }

                        int deletedExpenses;

                        try ( PreparedStatement statement
                                = connection.prepareStatement(
                                        deleteExpenseSql
                                )) {

                                    statement.setInt(1, expenseId);
                                    statement.setInt(2, tripId);
                                    statement.setInt(3, currentUserId);
                                    statement.setInt(4, currentUserId);

                                    deletedExpenses
                                            = statement.executeUpdate();
                                }

                                if (deletedExpenses == 0) {
                                    connection.rollback();
                                    return false;
                                }

                                connection.commit();
                                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(
                        originalAutoCommit
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể xóa khoản chi.",
                    e
            );
        }
    }

    private Expense mapExpense(ResultSet resultSet)
            throws SQLException {

        Expense expense = new Expense();

        expense.setExpenseId(
                resultSet.getInt("expense_id")
        );

        expense.setTripId(
                resultSet.getInt("trip_id")
        );

        expense.setPayerId(
                resultSet.getInt("payer_id")
        );

        expense.setCreatedBy(
                resultSet.getInt("created_by")
        );

        expense.setDescription(
                resultSet.getString("description")
        );

        expense.setAmount(
                resultSet.getBigDecimal("amount")
        );

        expense.setFromGroupFund(
                resultSet.getBoolean("from_group_fund")
        );

        expense.setCreatedAt(
                resultSet.getTimestamp("created_at")
        );

        expense.setPayerName(
                resultSet.getString("payer_name")
        );

        return expense;
    }
}
