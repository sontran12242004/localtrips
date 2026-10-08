package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.TripMember;

public class TripMemberDAO {

    public List<TripMember> findByTrip(int tripId) {
        List<TripMember> members = new ArrayList<>();

        String sql
                = "SELECT tm.trip_id, tm.user_id, "
                + "tm.member_role, tm.joined_at, "
                + "u.full_name, u.email "
                + "FROM TripMembers tm "
                + "INNER JOIN Users u "
                + "ON u.user_id = tm.user_id "
                + "WHERE tm.trip_id = ? "
                + "ORDER BY "
                + "CASE WHEN tm.member_role = 'OWNER' "
                + "THEN 0 ELSE 1 END, "
                + "tm.joined_at, tm.user_id";

        try ( Connection connection = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);

            try ( ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    TripMember member = new TripMember();

                    member.setTripId(
                            resultSet.getInt("trip_id")
                    );
                    member.setUserId(
                            resultSet.getInt("user_id")
                    );
                    member.setMemberRole(
                            resultSet.getString("member_role")
                    );
                    member.setJoinedAt(
                            resultSet.getTimestamp("joined_at")
                    );
                    member.setFullName(
                            resultSet.getString("full_name")
                    );
                    member.setEmail(
                            resultSet.getString("email")
                    );

                    members.add(member);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải danh sách thành viên.",
                    e
            );
        }

        return members;
    }

    public boolean exists(int tripId, int userId) {

        String sql
                = "SELECT 1 FROM TripMembers "
                + "WHERE trip_id = ? AND user_id = ?";

        try ( Connection connection = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);
            statement.setInt(2, userId);

            try ( ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể kiểm tra thành viên.",
                    e
            );
        }
    }

    public boolean addMember(int tripId, int userId) {

        if (exists(tripId, userId)) {
            return false;
        }

        String sql
                = "INSERT INTO TripMembers "
                + "(trip_id, user_id, member_role) "
                + "VALUES (?, ?, 'MEMBER')";

        try ( Connection connection = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            /*
             * Trường hợp hai request cùng thêm một user:
             * khóa chính của database sẽ chặn bản ghi trùng.
             */
            if (e.getErrorCode() == 2627
                    || e.getErrorCode() == 2601
                    || "23000".equals(e.getSQLState())) {
                return false;
            }

            throw new RuntimeException(
                    "Không thể thêm thành viên.",
                    e
            );
        }
    }

    public boolean removeMember(int tripId,
            int userId,
            int ownerId) {

        String sql
                = "DELETE FROM TripMembers "
                + "WHERE trip_id = ? "
                + "AND user_id = ? "
                + "AND member_role = 'MEMBER' "
                + "AND EXISTS ("
                + "    SELECT 1 FROM Trips t "
                + "    WHERE t.trip_id = tm.trip_id "
                + "    AND t.owner_id = ?"
                + ") "
                + "AND NOT EXISTS ("
                + "    SELECT 1 FROM Expenses e "
                + "    WHERE e.trip_id = tm.trip_id "
                + "    AND ("
                + "        e.payer_id = tm.user_id "
                + "        OR e.created_by = tm.user_id "
                + "        OR EXISTS ("
                + "            SELECT 1 FROM ExpenseParticipants ep "
                + "            WHERE ep.expense_id = e.expense_id "
                + "            AND ep.user_id = tm.user_id"
                + "        )"
                + "    )"
                + ")";
        try ( Connection connection = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);
            statement.setInt(2, userId);
            statement.setInt(3, ownerId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể xóa thành viên.",
                    e
            );
        }
    }
}
