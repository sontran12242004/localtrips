package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.User;

/**
 * File moi do Thanh viet de lam Login/Register (Minh chua co UserDAO). Minh
 * review va gop vao dao cua nhom neu can.
 */
public class UserDAO {

    private static final String SELECT_BASE
            = "SELECT user_id, full_name, email, password_hash, role, is_active, created_at FROM Users ";

    public List<User> findAll() {
        List<User> users = new ArrayList<User>();
        String sql = SELECT_BASE + "ORDER BY created_at DESC";

        try ( Connection con = DBContext.getConnection();  PreparedStatement ps = con.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy danh sách user", e);
        }

        return users;
    }

    public List<User> search(String keyword,
            String role,
            String status) {

        String cleanKeyword = keyword == null
                ? ""
                : keyword.trim();

        String cleanRole = role == null
                ? ""
                : role.trim().toUpperCase();

        String cleanStatus = status == null
                ? ""
                : status.trim().toUpperCase();

        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE 1 = 1 ");

        if (!cleanKeyword.isEmpty()) {
            sql.append(
                    "AND (full_name LIKE ? OR email LIKE ?) "
            );
        }

        if ("ADMIN".equals(cleanRole)
                || "USER".equals(cleanRole)) {
            sql.append("AND role = ? ");
        }

        if ("ACTIVE".equals(cleanStatus)) {
            sql.append("AND is_active = 1 ");
        } else if ("LOCKED".equals(cleanStatus)) {
            sql.append("AND is_active = 0 ");
        }

        sql.append("ORDER BY created_at DESC");

        List<User> users = new ArrayList<User>();

        try ( Connection con = DBContext.getConnection();  PreparedStatement ps
                = con.prepareStatement(sql.toString())) {

            int index = 1;

            if (!cleanKeyword.isEmpty()) {
                String pattern = "%" + cleanKeyword + "%";

                ps.setString(index++, pattern);
                ps.setString(index++, pattern);
            }

            if ("ADMIN".equals(cleanRole)
                    || "USER".equals(cleanRole)) {
                ps.setString(index++, cleanRole);
            }

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tìm kiếm danh sách user",
                    e
            );
        }

        return users;
    }

    public int countSearch(String keyword,
            String role,
            String status) {

        String cleanKeyword = keyword == null
                ? ""
                : keyword.trim();

        String cleanRole = role == null
                ? ""
                : role.trim().toUpperCase();

        String cleanStatus = status == null
                ? ""
                : status.trim().toUpperCase();

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM Users WHERE 1 = 1 "
        );

        if (!cleanKeyword.isEmpty()) {
            sql.append(
                    "AND (full_name LIKE ? OR email LIKE ?) "
            );
        }

        if ("ADMIN".equals(cleanRole)
                || "USER".equals(cleanRole)) {
            sql.append("AND role = ? ");
        }

        if ("ACTIVE".equals(cleanStatus)) {
            sql.append("AND is_active = 1 ");
        } else if ("LOCKED".equals(cleanStatus)) {
            sql.append("AND is_active = 0 ");
        }

        try ( Connection con = DBContext.getConnection();  PreparedStatement ps
                = con.prepareStatement(sql.toString())) {

            int index = 1;

            if (!cleanKeyword.isEmpty()) {
                String pattern = "%" + cleanKeyword + "%";

                ps.setString(index++, pattern);
                ps.setString(index++, pattern);
            }

            if ("ADMIN".equals(cleanRole)
                    || "USER".equals(cleanRole)) {
                ps.setString(index++, cleanRole);
            }

            try ( ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể đếm kết quả tìm kiếm user",
                    e
            );
        }
    }

    public List<User> searchPage(String keyword,
            String role,
            String status,
            int page,
            int pageSize) {

        String cleanKeyword = keyword == null
                ? ""
                : keyword.trim();

        String cleanRole = role == null
                ? ""
                : role.trim().toUpperCase();

        String cleanStatus = status == null
                ? ""
                : status.trim().toUpperCase();

        if (page < 1) {
            page = 1;
        }

        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        long offset = (long) (page - 1) * pageSize;

        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE 1 = 1 ");

        if (!cleanKeyword.isEmpty()) {
            sql.append(
                    "AND (full_name LIKE ? OR email LIKE ?) "
            );
        }

        if ("ADMIN".equals(cleanRole)
                || "USER".equals(cleanRole)) {
            sql.append("AND role = ? ");
        }

        if ("ACTIVE".equals(cleanStatus)) {
            sql.append("AND is_active = 1 ");
        } else if ("LOCKED".equals(cleanStatus)) {
            sql.append("AND is_active = 0 ");
        }

        /*
     * user_id được dùng làm điều kiện sắp xếp phụ để thứ tự
     * giữa các trang luôn ổn định nếu created_at giống nhau.
         */
        sql.append(
                "ORDER BY created_at DESC, user_id DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY"
        );

        List<User> users = new ArrayList<User>();

        try ( Connection con = DBContext.getConnection();  PreparedStatement ps
                = con.prepareStatement(sql.toString())) {

            int index = 1;

            if (!cleanKeyword.isEmpty()) {
                String pattern = "%" + cleanKeyword + "%";

                ps.setString(index++, pattern);
                ps.setString(index++, pattern);
            }

            if ("ADMIN".equals(cleanRole)
                    || "USER".equals(cleanRole)) {
                ps.setString(index++, cleanRole);
            }

            ps.setLong(index++, offset);
            ps.setInt(index, pageSize);

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(map(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải trang danh sách user",
                    e
            );
        }

        return users;
    }

    public User findById(int userId) {
        String sql = SELECT_BASE + "WHERE user_id = ?";

        try ( Connection con = DBContext.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try ( ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tìm user theo ID", e);
        }
    }

    public User findByEmail(String email) {
        String sql = SELECT_BASE + "WHERE email = ?";
        try ( Connection con = DBContext.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try ( ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Khong the tim user theo email", e);
        }
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    /**
     * Find an active user without exposing password data to callers that do not
     * need it.
     */
    public User findActiveByEmail(String email) {
        User user = findByEmail(email);
        if (user == null || !user.isActive()) {
            return null;
        }
        return user;
    }

    /**
     * @return true neu tao thanh cong; false neu email da ton tai (vi pham
     * UNIQUE, ma loi SQL Server 2627 hoac 2601 - xay ra khi 2 nguoi dang ky
     * cung email cung luc, vuot qua buoc existsByEmail).
     */
    public boolean insert(String fullName, String email, String passwordHash) {
        String sql = "INSERT INTO Users (full_name, email, password_hash, role) VALUES (?, ?, ?, 'USER')";
        try ( Connection con = DBContext.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                return false;
            }
            throw new RuntimeException("Khong the tao user", e);
        }
    }

    public boolean insertByAdmin(String fullName,
            String email,
            String passwordHash,
            String role,
            boolean active) {

        String sql = "INSERT INTO Users "
                + "(full_name, email, password_hash, role, is_active) "
                + "VALUES (?, ?, ?, ?, ?)";

        try ( Connection con = DBContext.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, role);
            ps.setBoolean(5, active);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            // SQL Server: email vi phạm UNIQUE constraint.
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                return false;
            }

            throw new RuntimeException("Không thể tạo user bởi Admin", e);
        }
    }

    public boolean emailExistsForOtherUser(String email, int userId) {
        String sql = "SELECT COUNT(*) FROM Users "
                + "WHERE email = ? AND user_id <> ?";

        try ( Connection con = DBContext.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, userId);

            try ( ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể kiểm tra email khi cập nhật user",
                    e
            );
        }
    }

    public boolean updateByAdmin(int userId,
            String fullName,
            String email,
            String role,
            boolean active) {

        String sql = "UPDATE Users "
                + "SET full_name = ?, email = ?, role = ?, is_active = ? "
                + "WHERE user_id = ?";

        try ( Connection con = DBContext.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, role);
            ps.setBoolean(4, active);
            ps.setInt(5, userId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                return false;
            }

            throw new RuntimeException(
                    "Không thể cập nhật user bởi Admin",
                    e
            );
        }
    }

    public boolean updatePasswordByAdmin(int userId,
            String passwordHash) {

        String sql = "UPDATE Users "
                + "SET password_hash = ? "
                + "WHERE user_id = ?";

        try ( Connection con = DBContext.getConnection();  PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, passwordHash);
            ps.setInt(2, userId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể đặt lại mật khẩu user",
                    e
            );
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getBoolean("is_active"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
