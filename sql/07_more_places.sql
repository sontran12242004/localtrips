USE LocalTripDB;
GO

/* =========================================================
   LOCALTRIP - BỔ SUNG ĐỊA ĐIỂM PHỤC VỤ AUTO ITINERARY
   Mục tiêu: đủ nhiều lựa chọn để mỗi ngày có 3 cột mốc.
   Mỗi cột mốc: FOOD + CAFE + SIGHTSEEING/ENTERTAINMENT/SHOPPING.
   Script an toàn: chạy nhiều lần không tạo bản ghi trùng tên.
   ========================================================= */

INSERT INTO Places (
    category_id, place_name, address, description,
    estimated_cost, rating, opening_time, closing_time,
    latitude, longitude, place_type, is_active
)
SELECT c.category_id, v.place_name, v.address, v.description,
       v.estimated_cost, v.rating, v.opening_time, v.closing_time,
       v.latitude, v.longitude, v.place_type, 1
FROM (
    VALUES
    /* ================= FOOD ================= */
    ('FOOD', N'Bún thịt nướng Chị Tuyền', N'175 Cô Giang, Quận 1, TP.HCM', N'Món bún thịt nướng phù hợp cho bữa sáng hoặc trưa.', 70000, 4.30, '06:30', '21:30', 10.765800, 106.694700, 'INDOOR'),
    ('FOOD', N'Cơm tấm Ba Ghiền', N'84 Đặng Văn Ngữ, Phú Nhuận, TP.HCM', N'Cơm tấm phong cách địa phương.', 90000, 4.40, '07:00', '21:00', 10.793700, 106.674500, 'INDOOR'),
    ('FOOD', N'Hủ Tiếu Thanh Xuân', N'62 Tôn Thất Thiệp, Quận 1, TP.HCM', N'Hủ tiếu truyền thống tại trung tâm thành phố.', 70000, 4.30, '07:00', '21:00', 10.771300, 106.700300, 'INDOOR'),
    ('FOOD', N'Bánh xèo Ngọc Sơn', N'103 Ngô Quyền, Quận 5, TP.HCM', N'Bánh xèo và món ăn miền Nam.', 90000, 4.20, '10:00', '21:30', 10.754500, 106.666700, 'INDOOR'),
    ('FOOD', N'Ốc Oanh', N'534 Vĩnh Khánh, Quận 4, TP.HCM', N'Quán ốc đa dạng món cho nhóm bạn.', 150000, 4.20, '16:00', '23:00', 10.757900, 106.701300, 'INDOOR'),
    ('FOOD', N'Bún bò Huế Đông Ba', N'110A Nguyễn Du, Quận 1, TP.HCM', N'Bún bò Huế đậm vị.', 85000, 4.20, '06:30', '21:30', 10.779300, 106.695800, 'INDOOR'),
    ('FOOD', N'Pizza 4P''s Ben Thanh', N'8 Thủ Khoa Huân, Quận 1, TP.HCM', N'Pizza và món Ý phù hợp nhóm bạn.', 220000, 4.50, '11:00', '23:00', 10.772700, 106.696500, 'INDOOR'),

    /* ================= CAFE ================= */
    ('CAFE', N'Highlands Coffee Nguyễn Huệ', N'26 Nguyễn Huệ, Quận 1, TP.HCM', N'Không gian cà phê trung tâm, thuận tiện nghỉ giữa lịch trình.', 70000, 4.20, '07:00', '22:00', 10.773500, 106.704300, 'INDOOR'),
    ('CAFE', N'Phúc Long Coffee & Tea Nguyễn Huệ', N'42 Nguyễn Huệ, Quận 1, TP.HCM', N'Cà phê và trà tại khu trung tâm.', 70000, 4.20, '07:00', '22:00', 10.773900, 106.704500, 'INDOOR'),
    ('CAFE', N'Cong Caphe Bùi Viện', N'274 Bùi Viện, Quận 1, TP.HCM', N'Không gian cà phê phong cách hoài cổ.', 80000, 4.30, '07:00', '23:00', 10.767500, 106.691000, 'INDOOR'),
    ('CAFE', N'The Running Bean', N'115 Hồ Tùng Mậu, Quận 1, TP.HCM', N'Quán cà phê và brunch tại trung tâm.', 120000, 4.40, '07:30', '21:30', 10.773000, 106.702000, 'INDOOR'),
    ('CAFE', N'Every Half Coffee Roasters', N'8-10 Lê Thánh Tôn, Quận 1, TP.HCM', N'Cà phê rang xay và không gian hiện đại.', 100000, 4.40, '08:00', '21:00', 10.779000, 106.704000, 'INDOOR'),
    ('CAFE', N'Blank Lounge Landmark 81', N'720A Điện Biên Phủ, Bình Thạnh, TP.HCM', N'Không gian cà phê và ngắm cảnh thành phố.', 180000, 4.40, '08:00', '23:00', 10.795300, 106.721800, 'INDOOR'),
    ('CAFE', N'Cafe Cardinal', N'76 Nguyễn Huệ, Quận 1, TP.HCM', N'Không gian cà phê và thư giãn cao cấp.', 180000, 4.30, '07:00', '22:00', 10.774700, 106.704900, 'INDOOR'),

    /* ================= SIGHTSEEING ================= */
    ('SIGHTSEEING', N'Bảo tàng Chứng tích Chiến tranh', N'28 Võ Văn Tần, Quận 3, TP.HCM', N'Bảo tàng lịch sử nổi tiếng của thành phố.', 40000, 4.50, '07:30', '17:30', 10.779400, 106.692000, 'INDOOR'),
    ('SIGHTSEEING', N'Bảo tàng Mỹ thuật TP.HCM', N'97 Phó Đức Chính, Quận 1, TP.HCM', N'Không gian nghệ thuật và kiến trúc cổ.', 30000, 4.40, '08:00', '17:00', 10.768500, 106.698100, 'INDOOR'),

    /* ================= ENTERTAINMENT ================= */
    ('ENTERTAINMENT', N'BHD Star Bitexco', N'2 Hải Triều, Quận 1, TP.HCM', N'Rạp phim tại trung tâm thành phố.', 140000, 4.30, '09:00', '23:00', 10.771700, 106.704000, 'INDOOR'),
    ('ENTERTAINMENT', N'Khu vui chơi Snow Town Sài Gòn', N'125 Đồng Văn Cống, TP Thủ Đức, TP.HCM', N'Khu vui chơi tuyết trong nhà.', 180000, 4.20, '09:00', '21:00', 10.768500, 106.767000, 'INDOOR'),
    ('ENTERTAINMENT', N'Jump Arena Thảo Điền', N'63 Xuân Thủy, TP Thủ Đức, TP.HCM', N'Khu vận động giải trí trong nhà.', 180000, 4.30, '09:00', '21:00', 10.803700, 106.735700, 'INDOOR'),

    /* ================= SHOPPING ================= */
    ('SHOPPING', N'AEON Mall Tân Phú Celadon', N'30 Tân Thắng, Tân Phú, TP.HCM', N'Trung tâm mua sắm, ăn uống và giải trí.', 250000, 4.40, '10:00', '22:00', 10.801800, 106.618300, 'INDOOR'),
    ('SHOPPING', N'GIGAMALL Thủ Đức', N'240-242 Phạm Văn Đồng, TP Thủ Đức, TP.HCM', N'Trung tâm mua sắm và giải trí.', 220000, 4.40, '09:00', '22:00', 10.828500, 106.718900, 'INDOOR'),
    ('SHOPPING', N'Vạn Hạnh Mall', N'11 Sư Vạn Hạnh, Quận 10, TP.HCM', N'Trung tâm mua sắm và vui chơi.', 220000, 4.30, '09:30', '22:00', 10.772200, 106.667300, 'INDOOR'),
    ('SHOPPING', N'Takashimaya Saigon Centre', N'65 Lê Lợi, Quận 1, TP.HCM', N'Trung tâm mua sắm cao cấp tại trung tâm.', 300000, 4.50, '09:30', '21:30', 10.774000, 106.701000, 'INDOOR')
) AS v(category_code, place_name, address, description, estimated_cost, rating, opening_time, closing_time, latitude, longitude, place_type)
INNER JOIN Categories c ON c.category_code = v.category_code
WHERE NOT EXISTS (
    SELECT 1 FROM Places p
    WHERE p.place_name = v.place_name
);
GO

/* Kiểm tra nhanh số lượng theo nhóm */
SELECT c.category_code, c.category_name, COUNT(p.place_id) AS total_places
FROM Categories c
LEFT JOIN Places p ON p.category_id = c.category_id AND p.is_active = 1
GROUP BY c.category_code, c.category_name
ORDER BY c.category_code;
GO
