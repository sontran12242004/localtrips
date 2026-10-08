package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Category;

public class CategoryDAO {

    public List<Category> findAll() {
        return searchAdmin("");
    }

    public List<Category> searchAdmin(String keyword) {
        List<Category> categories = new ArrayList<Category>();

        String sql = "SELECT c.category_id, c.category_code, c.category_name, "
                + "(SELECT COUNT(*) FROM Places p WHERE p.category_id = c.category_id) AS place_count "
                + "FROM Categories c ";

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += "WHERE c.category_code LIKE ? OR c.category_name LIKE ? ";
        }

        sql += "ORDER BY c.category_name";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (keyword != null && !keyword.trim().isEmpty()) {
                String value = "%" + keyword.trim() + "%";
                statement.setString(1, value);
                statement.setString(2, value);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Category category = mapCategory(resultSet);
                    category.setPlaceCount(resultSet.getInt("place_count"));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải danh sách danh mục.", e);
        }

        return categories;
    }

    public Category findById(int categoryId) {
        String sql = "SELECT category_id, category_code, category_name "
                + "FROM Categories WHERE category_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể đọc danh mục.", e);
        }
        return null;
    }

    public int insert(Category category) {
        String sql = "INSERT INTO Categories (category_code, category_name) VALUES (?, ?)";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, new String[]{"category_id"})) {
            statement.setString(1, category.getCategoryCode());
            statement.setString(2, category.getCategoryName());
            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể thêm danh mục. Mã hoặc tên danh mục có thể đã tồn tại.", e);
        }

        throw new RuntimeException("Không lấy được mã danh mục vừa tạo.");
    }

    public boolean update(Category category) {
        String sql = "UPDATE Categories SET category_code = ?, category_name = ? "
                + "WHERE category_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getCategoryCode());
            statement.setString(2, category.getCategoryName());
            statement.setInt(3, category.getCategoryId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật danh mục. Mã hoặc tên danh mục có thể đã tồn tại.", e);
        }
    }

    public boolean delete(int categoryId) {
        String sql = "DELETE FROM Categories WHERE category_id = ?";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể xóa danh mục. Danh mục có thể đang được sử dụng bởi địa điểm hoặc sở thích thành viên.", e);
        }
    }

    private Category mapCategory(ResultSet resultSet) throws SQLException {
        Category category = new Category();
        category.setCategoryId(resultSet.getInt("category_id"));
        category.setCategoryCode(resultSet.getString("category_code"));
        category.setCategoryName(resultSet.getString("category_name"));
        return category;
    }
}
