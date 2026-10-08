package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Place;

public class PlaceDAO {

    private static final String SELECT_BASE
            = "SELECT p.place_id, "
            + "p.category_id, "
            + "c.category_name, "
            + "p.place_name, "
            + "p.address, "
            + "p.description, "
            + "p.estimated_cost, "
            + "p.rating, "
            + "p.opening_time, "
            + "p.closing_time, "
            + "p.latitude, "
            + "p.longitude, "
            + "p.place_type, "
            + "p.is_active "
            + "FROM Places p "
            + "JOIN Categories c "
            + "ON p.category_id = c.category_id ";

    public List<Place> findAll() {
        List<Place> places
                = new ArrayList<Place>();

        String sql = SELECT_BASE
                + "WHERE p.is_active = 1 "
                + "ORDER BY p.place_name";

        try (
                 Connection connection
                = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql);  ResultSet resultSet
                = statement.executeQuery()) {
            while (resultSet.next()) {
                places.add(mapPlace(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Khong the doc danh sach dia diem",
                    e
            );
        }

        return places;
    }

    public List<Place> findByCategory(
            int categoryId
    ) {
        List<Place> places
                = new ArrayList<Place>();

        String sql = SELECT_BASE
                + "WHERE p.is_active = 1 "
                + "AND p.category_id = ? "
                + "ORDER BY p.place_name";

        try (
                 Connection connection
                = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);

            try (
                     ResultSet resultSet
                    = statement.executeQuery()) {
                while (resultSet.next()) {
                    places.add(mapPlace(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Khong the loc dia diem",
                    e
            );
        }

        return places;
    }

    public Place findById(int placeId) {

        String sql = SELECT_BASE
                + "WHERE p.place_id = ? "
                + "AND p.is_active = 1";

        try ( Connection connection
                = DBContext.getConnection();  PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setInt(1, placeId);

            try ( ResultSet resultSet
                    = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPlace(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể đọc thông tin địa điểm.",
                    e
            );
        }

        return null;
    }


    public List<Place> searchAdmin(String keyword, int categoryId, String status) {
        List<Place> places = new ArrayList<Place>();
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE 1 = 1 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.place_name LIKE ? OR p.address LIKE ?) ");
        }
        if (categoryId > 0) {
            sql.append("AND p.category_id = ? ");
        }
        if ("ACTIVE".equals(status)) {
            sql.append("AND p.is_active = 1 ");
        } else if ("INACTIVE".equals(status)) {
            sql.append("AND p.is_active = 0 ");
        }
        sql.append("ORDER BY p.place_name");

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String value = "%" + keyword.trim() + "%";
                statement.setString(index++, value);
                statement.setString(index++, value);
            }
            if (categoryId > 0) {
                statement.setInt(index++, categoryId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    places.add(mapPlace(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tìm kiếm địa điểm quản trị.", e);
        }
        return places;
    }

    public Place findByIdAdmin(int placeId) {
        String sql = SELECT_BASE + "WHERE p.place_id = ?";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, placeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return mapPlace(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể đọc địa điểm.", e);
        }
        return null;
    }

    public int insert(Place place) {
        String sql = "INSERT INTO Places "
                + "(category_id, place_name, address, description, estimated_cost, rating, "
                + "opening_time, closing_time, latitude, longitude, place_type, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, new String[]{"place_id"})) {
            setPlaceParameters(statement, place);
            statement.executeUpdate();
            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể thêm địa điểm.", e);
        }
        throw new RuntimeException("Không lấy được mã địa điểm vừa tạo.");
    }

    public boolean update(Place place) {
        String sql = "UPDATE Places SET category_id=?, place_name=?, address=?, description=?, "
                + "estimated_cost=?, rating=?, opening_time=?, closing_time=?, latitude=?, longitude=?, place_type=? "
                + "WHERE place_id=?";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setPlaceParameters(statement, place);
            statement.setInt(12, place.getPlaceId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật địa điểm.", e);
        }
    }

    public boolean setActive(int placeId, boolean active) {
        String sql = "UPDATE Places SET is_active = ? WHERE place_id = ?";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setInt(2, placeId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể thay đổi trạng thái địa điểm.", e);
        }
    }

    private void setPlaceParameters(PreparedStatement statement, Place place) throws SQLException {
        statement.setInt(1, place.getCategoryId());
        statement.setString(2, place.getPlaceName());
        statement.setString(3, place.getAddress());
        if (place.getDescription() == null || place.getDescription().isEmpty()) statement.setNull(4, java.sql.Types.NVARCHAR);
        else statement.setString(4, place.getDescription());
        statement.setBigDecimal(5, place.getEstimatedCost());
        statement.setBigDecimal(6, place.getRating());
        if (place.getOpeningTime() == null) statement.setNull(7, java.sql.Types.TIME); else statement.setTime(7, place.getOpeningTime());
        if (place.getClosingTime() == null) statement.setNull(8, java.sql.Types.TIME); else statement.setTime(8, place.getClosingTime());
        if (place.getLatitude() == null) statement.setNull(9, java.sql.Types.DECIMAL); else statement.setBigDecimal(9, place.getLatitude());
        if (place.getLongitude() == null) statement.setNull(10, java.sql.Types.DECIMAL); else statement.setBigDecimal(10, place.getLongitude());
        statement.setString(11, place.getPlaceType());
    }

    private Place mapPlace(
            ResultSet resultSet
    ) throws SQLException {

        Place place = new Place();

        place.setPlaceId(
                resultSet.getInt("place_id")
        );

        place.setCategoryId(
                resultSet.getInt("category_id")
        );

        place.setCategoryName(
                resultSet.getString("category_name")
        );

        place.setPlaceName(
                resultSet.getString("place_name")
        );

        place.setAddress(
                resultSet.getString("address")
        );

        place.setDescription(
                resultSet.getString("description")
        );

        place.setEstimatedCost(
                resultSet.getBigDecimal(
                        "estimated_cost"
                )
        );

        place.setRating(
                resultSet.getBigDecimal("rating")
        );

        place.setOpeningTime(
                resultSet.getTime("opening_time")
        );

        place.setClosingTime(
                resultSet.getTime("closing_time")
        );

        place.setLatitude(
                resultSet.getBigDecimal("latitude")
        );

        place.setLongitude(
                resultSet.getBigDecimal("longitude")
        );

        place.setPlaceType(
                resultSet.getString("place_type")
        );

        place.setActive(
                resultSet.getBoolean("is_active")
        );

        return place;
    }
}
