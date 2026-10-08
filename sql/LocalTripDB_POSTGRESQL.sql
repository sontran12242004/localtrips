-- =============================================================
-- LocalTripDB_POSTGRESQL.sql
-- Script tao database va du lieu mau cho PostgreSQL tren Railway / Neon / Supabase
-- =============================================================

-- 1. USERS
DROP TABLE IF EXISTS LoginLogs CASCADE;
DROP TABLE IF EXISTS AdminAuditLogs CASCADE;
DROP TABLE IF EXISTS RememberMeTokens CASCADE;
DROP TABLE IF EXISTS PasswordResetTokens CASCADE;
DROP TABLE IF EXISTS ExpenseParticipants CASCADE;
DROP TABLE IF EXISTS Expenses CASCADE;
DROP TABLE IF EXISTS GroupFundContributions CASCADE;
DROP TABLE IF EXISTS ItineraryItems CASCADE;
DROP TABLE IF EXISTS TripMemberPreferences CASCADE;
DROP TABLE IF EXISTS TripMembers CASCADE;
DROP TABLE IF EXISTS Places CASCADE;
DROP TABLE IF EXISTS Categories CASCADE;
DROP TABLE IF EXISTS Trips CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT CK_Users_Role CHECK (role IN ('ADMIN', 'USER'))
);

-- 2. TRIPS
CREATE TABLE Trips (
    trip_id SERIAL PRIMARY KEY,
    owner_id INT NOT NULL REFERENCES Users(user_id),
    trip_name VARCHAR(150) NOT NULL,
    destination VARCHAR(200) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    budget NUMERIC(18,2) NOT NULL DEFAULT 0,
    description VARCHAR(1000) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT CK_Trips_Date CHECK (end_date >= start_date),
    CONSTRAINT CK_Trips_Budget CHECK (budget >= 0),
    CONSTRAINT CK_Trips_Status CHECK (status IN ('PLANNING', 'ONGOING', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX IX_Trips_Owner ON Trips(owner_id);

-- 3. TRIP MEMBERS
CREATE TABLE TripMembers (
    trip_id INT NOT NULL REFERENCES Trips(trip_id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE,
    member_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (trip_id, user_id),
    CONSTRAINT CK_TripMembers_Role CHECK (member_role IN ('OWNER', 'MEMBER'))
);

CREATE INDEX IX_TripMembers_User ON TripMembers(user_id);

-- 4. CATEGORIES
CREATE TABLE Categories (
    category_id SERIAL PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL UNIQUE,
    category_name VARCHAR(100) NOT NULL UNIQUE
);

-- 5. TRIP MEMBER PREFERENCES
CREATE TABLE TripMemberPreferences (
    trip_id INT NOT NULL,
    user_id INT NOT NULL,
    category_id INT NOT NULL REFERENCES Categories(category_id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (trip_id, user_id, category_id),
    FOREIGN KEY (trip_id, user_id) REFERENCES TripMembers(trip_id, user_id) ON DELETE CASCADE
);

-- 6. PLACES
CREATE TABLE Places (
    place_id SERIAL PRIMARY KEY,
    category_id INT NOT NULL REFERENCES Categories(category_id),
    place_name VARCHAR(200) NOT NULL,
    address VARCHAR(300) NOT NULL,
    description VARCHAR(1000) NULL,
    estimated_cost NUMERIC(18,2) NOT NULL DEFAULT 0,
    rating NUMERIC(3,2) NOT NULL DEFAULT 0,
    opening_time TIME NULL,
    closing_time TIME NULL,
    latitude NUMERIC(9,6) NULL,
    longitude NUMERIC(9,6) NULL,
    place_type VARCHAR(20) NOT NULL DEFAULT 'INDOOR',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT CK_Places_Cost CHECK (estimated_cost >= 0),
    CONSTRAINT CK_Places_Rating CHECK (rating >= 0 AND rating <= 5),
    CONSTRAINT CK_Places_Type CHECK (place_type IN ('INDOOR', 'OUTDOOR'))
);

CREATE INDEX IX_Places_Category ON Places(category_id);

-- 7. ITINERARY ITEMS
CREATE TABLE ItineraryItems (
    item_id SERIAL PRIMARY KEY,
    trip_id INT NOT NULL REFERENCES Trips(trip_id) ON DELETE CASCADE,
    place_id INT NOT NULL REFERENCES Places(place_id),
    visit_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    note VARCHAR(1000) NULL,
    estimated_cost NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT CK_ItineraryItems_Time CHECK (end_time > start_time),
    CONSTRAINT CK_ItineraryItems_Cost CHECK (estimated_cost >= 0)
);

CREATE INDEX IX_ItineraryItems_TripDateTime ON ItineraryItems(trip_id, visit_date, start_time, end_time);

-- 8. EXPENSES
CREATE TABLE Expenses (
    expense_id SERIAL PRIMARY KEY,
    trip_id INT NOT NULL REFERENCES Trips(trip_id) ON DELETE CASCADE,
    payer_id INT NOT NULL REFERENCES Users(user_id),
    created_by INT NOT NULL REFERENCES Users(user_id),
    description VARCHAR(500) NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    from_group_fund BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT CK_Expenses_Amount CHECK (amount > 0)
);

CREATE INDEX IX_Expenses_TripCreatedAt ON Expenses(trip_id, created_at DESC, expense_id DESC);

-- 9. EXPENSE PARTICIPANTS
CREATE TABLE ExpenseParticipants (
    expense_id INT NOT NULL REFERENCES Expenses(expense_id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES Users(user_id),
    share_amount NUMERIC(18,2) NOT NULL,

    PRIMARY KEY (expense_id, user_id),
    CONSTRAINT CK_ExpenseParticipants_Share CHECK (share_amount >= 0)
);

CREATE INDEX IX_ExpenseParticipants_User ON ExpenseParticipants(user_id, expense_id);

-- 10. GROUP FUND CONTRIBUTIONS
CREATE TABLE GroupFundContributions (
    contribution_id SERIAL PRIMARY KEY,
    trip_id INT NOT NULL REFERENCES Trips(trip_id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES Users(user_id),
    amount NUMERIC(18,2) NOT NULL,
    note VARCHAR(255) NULL,
    created_by INT NOT NULL REFERENCES Users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT CK_GroupFundContributions_Amount CHECK (amount > 0)
);

CREATE INDEX IX_GroupFundContributions_Trip ON GroupFundContributions(trip_id, created_at DESC);

-- 11. REMEMBER ME TOKENS
CREATE TABLE RememberMeTokens (
    token_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 12. PASSWORD RESET TOKENS
CREATE TABLE PasswordResetTokens (
    token_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 13. ADMIN AUDIT LOGS
CREATE TABLE AdminAuditLogs (
    audit_id BIGSERIAL PRIMARY KEY,
    actor_user_id INT NOT NULL REFERENCES Users(user_id),
    action VARCHAR(40) NOT NULL,
    target_user_id INT NULL REFERENCES Users(user_id),
    detail VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT CK_AdminAuditLogs_Action CHECK (action IN (
        'CREATE_USER', 'UPDATE_USER', 'RESET_PASSWORD',
        'CREATE_PLACE', 'UPDATE_PLACE', 'ACTIVATE_PLACE', 'DEACTIVATE_PLACE',
        'CREATE_CATEGORY', 'UPDATE_CATEGORY', 'DELETE_CATEGORY'
    ))
);

CREATE INDEX IX_AdminAuditLogs_CreatedAt ON AdminAuditLogs(created_at DESC, audit_id DESC);

-- 14. LOGIN LOGS
CREATE TABLE LoginLogs (
    login_log_id BIGSERIAL PRIMARY KEY,
    user_id INT NULL REFERENCES Users(user_id),
    email VARCHAR(150) NOT NULL,
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(50) NULL,
    ip_address VARCHAR(100) NULL,
    user_agent VARCHAR(500) NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT CK_LoginLogs_Result CHECK (
        (success = TRUE AND failure_reason IS NULL)
        OR
        (success = FALSE AND failure_reason IS NOT NULL)
    )
);

CREATE INDEX IX_LoginLogs_AttemptedAt ON LoginLogs(attempted_at DESC, login_log_id DESC);
CREATE INDEX IX_LoginLogs_Email ON LoginLogs(email, attempted_at DESC);


-- =============================================================
-- SEED DATA
-- Password test: 123456
-- =============================================================

INSERT INTO Users (full_name, email, password_hash, role, is_active) VALUES
('Nguyễn Ngọc Minh', 'minh@localtrip.vn', '20000:f4793f1d4b13d5693234a8bb2c2c6c79:c5da67d25cec2716b973e6495def155e9c3b1629767357145264b377361478c1', 'ADMIN', TRUE),
('Thanh', 'thanh@localtrip.vn', '20000:c5d9099a7c7a0d0fd193b2a299bf31ab:2b7eafb66e624543723032f1daeec0a7ee982cf20b830b1db9ef8b3ce4771318', 'USER', TRUE),
('Nguyễn Lan Anh', 'lananh@localtrip.vn', '20000:d5e14c10dc9cb41416fd4c3ef2deec09:b9f93bc1d5bc8ebf976dcb1583b788c16c83a256071c2b48fbb9d9ccd6cd42c6', 'USER', TRUE),
('Hoàng Nam', 'hoangnam@localtrip.vn', '20000:cab055e465dec7f70aa3b753b9fba44c:0e9bf6f4609fe6a253e63f2df3565f8e0ec0f5765234b2ca552a951402e00688', 'USER', TRUE),
('Tài khoản kiểm thử đã khóa', 'locked@localtrip.vn', '20000:10f0220a4317e460eec564f449d5f283:e278eb7f1fe11641aa0bb5f8eb14aebfb44d86e82ece798e2d91003d9162e441', 'USER', FALSE);

INSERT INTO Categories (category_code, category_name) VALUES
('FOOD', 'Ăn uống'),
('CAFE', 'Cà phê'),
('SIGHTSEEING', 'Tham quan'),
('ENTERTAINMENT', 'Giải trí'),
('SHOPPING', 'Mua sắm');

INSERT INTO Places (category_id, place_name, address, description, estimated_cost, rating, opening_time, closing_time, latitude, longitude, place_type)
SELECT c.category_id, v.place_name, v.address, v.description, v.estimated_cost, v.rating, v.opening_time::time, v.closing_time::time, v.latitude, v.longitude, v.place_type
FROM (VALUES
    ('SIGHTSEEING', 'Dinh Độc Lập', '135 Nam Kỳ Khởi Nghĩa, Quận 1, TP.HCM', 'Địa điểm lịch sử nổi tiếng tại trung tâm thành phố.', 40000::numeric, 4.60::numeric, '08:00', '16:30', 10.777000::numeric, 106.695300::numeric, 'INDOOR'),
    ('SIGHTSEEING', 'Bưu điện Trung tâm Sài Gòn', '2 Công xã Paris, Quận 1, TP.HCM', 'Công trình kiến trúc và điểm chụp ảnh nổi tiếng.', 0::numeric, 4.60::numeric, '07:00', '19:00', 10.779800::numeric, 106.699900::numeric, 'INDOOR'),
    ('SIGHTSEEING', 'Nhà thờ Đức Bà Sài Gòn', '1 Công xã Paris, Quận 1, TP.HCM', 'Công trình kiến trúc biểu tượng tại trung tâm TP.HCM.', 0::numeric, 4.50::numeric, '06:00', '20:00', 10.779700::numeric, 106.699000::numeric, 'OUTDOOR'),
    ('SIGHTSEEING', 'Phố đi bộ Nguyễn Huệ', 'Nguyễn Huệ, Quận 1, TP.HCM', 'Không gian đi bộ và sinh hoạt cộng đồng.', 0::numeric, 4.50::numeric, '00:00', '23:59', 10.773300::numeric, 106.704000::numeric, 'OUTDOOR'),
    ('CAFE', 'The Workshop Coffee', '27 Ngô Đức Kế, Quận 1, TP.HCM', 'Quán cà phê phù hợp nghỉ ngơi và trò chuyện.', 90000::numeric, 4.40::numeric, '08:00', '21:00', 10.771900::numeric, 106.704600::numeric, 'INDOOR'),
    ('CAFE', 'Bosgaurus Coffee Roasters', '92 Nguyễn Hữu Cảnh, Bình Thạnh, TP.HCM', 'Không gian cà phê hiện đại với cà phê rang xay.', 100000::numeric, 4.50::numeric, '07:00', '21:00', 10.789000::numeric, 106.716000::numeric, 'INDOOR'),
    ('CAFE', '43 Factory Coffee Roaster', '178A Pasteur, Quận 1, TP.HCM', 'Quán cà phê đặc sản trong khu vực trung tâm.', 110000::numeric, 4.50::numeric, '07:00', '22:00', 10.775700::numeric, 106.696300::numeric, 'INDOOR'),
    ('FOOD', 'Bánh mì Huỳnh Hoa', '26 Lê Thị Riêng, Quận 1, TP.HCM', 'Địa điểm bánh mì nổi tiếng.', 65000::numeric, 4.30::numeric, '06:00', '22:00', 10.771700::numeric, 106.691900::numeric, 'INDOOR'),
    ('FOOD', 'Cơm tấm Nguyễn Văn Cừ', '74 Nguyễn Văn Cừ, Quận 1, TP.HCM', 'Quán cơm tấm với khẩu phần lớn.', 120000::numeric, 4.20::numeric, '06:30', '21:30', 10.763900::numeric, 106.682700::numeric, 'INDOOR'),
    ('FOOD', 'Phở Hòa Pasteur', '260C Pasteur, Quận 3, TP.HCM', 'Quán phở lâu năm tại TP.HCM.', 100000::numeric, 4.20::numeric, '06:00', '22:00', 10.788000::numeric, 106.689900::numeric, 'INDOOR'),
    ('ENTERTAINMENT', 'Nguyễn Huệ Walking Street', 'Nguyễn Huệ, Quận 1, TP.HCM', 'Khu vực giải trí ngoài trời vào buổi tối.', 0::numeric, 4.50::numeric, '17:00', '23:59', 10.773300::numeric, 106.704000::numeric, 'OUTDOOR'),
    ('ENTERTAINMENT', 'CGV Vincom Đồng Khởi', '72 Lê Thánh Tôn, Quận 1, TP.HCM', 'Rạp chiếu phim tại trung tâm thương mại.', 120000::numeric, 4.30::numeric, '09:00', '23:00', 10.778100::numeric, 106.701900::numeric, 'INDOOR'),
    ('ENTERTAINMENT', 'Saigon Skydeck', '2 Hải Triều, Quận 1, TP.HCM', 'Đài quan sát thành phố từ tòa nhà Bitexco.', 240000::numeric, 4.40::numeric, '09:30', '21:30', 10.771600::numeric, 106.704400::numeric, 'INDOOR'),
    ('SHOPPING', 'Chợ Bến Thành', 'Lê Lợi, Quận 1, TP.HCM', 'Khu chợ truyền thống phục vụ tham quan và mua sắm.', 150000::numeric, 4.10::numeric, '06:00', '19:00', 10.772500::numeric, 106.698000::numeric, 'INDOOR'),
    ('SHOPPING', 'Vincom Center Đồng Khởi', '72 Lê Thánh Tôn, Quận 1, TP.HCM', 'Trung tâm mua sắm, ăn uống và giải trí.', 300000::numeric, 4.40::numeric, '10:00', '22:00', 10.778100::numeric, 106.701900::numeric, 'INDOOR'),
    ('FOOD', 'Bún thịt nướng Chị Tuyền', '175 Cô Giang, Quận 1, TP.HCM', 'Món bún thịt nướng phù hợp cho bữa sáng hoặc trưa.', 70000::numeric, 4.30::numeric, '06:30', '21:30', 10.765800::numeric, 106.694700::numeric, 'INDOOR'),
    ('FOOD', 'Cơm tấm Ba Ghiền', '84 Đặng Văn Ngữ, Phú Nhuận, TP.HCM', 'Cơm tấm phong cách địa phương.', 90000::numeric, 4.40::numeric, '07:00', '21:00', 10.793700::numeric, 106.674500::numeric, 'INDOOR'),
    ('FOOD', 'Hủ Tiếu Thanh Xuân', '62 Tôn Thất Thiệp, Quận 1, TP.HCM', 'Hủ tiếu truyền thống tại trung tâm thành phố.', 70000::numeric, 4.30::numeric, '07:00', '21:00', 10.771300::numeric, 106.700300::numeric, 'INDOOR'),
    ('FOOD', 'Bánh xèo Ngọc Sơn', '103 Ngô Quyền, Quận 5, TP.HCM', 'Bánh xèo và món ăn miền Nam.', 90000::numeric, 4.20::numeric, '10:00', '21:30', 10.754500::numeric, 106.666700::numeric, 'INDOOR'),
    ('FOOD', 'Ốc Oanh', '534 Vĩnh Khánh, Quận 4, TP.HCM', 'Quán ốc đa dạng món cho nhóm bạn.', 150000::numeric, 4.20::numeric, '16:00', '23:00', 10.757900::numeric, 106.701300::numeric, 'INDOOR'),
    ('FOOD', 'Bún bò Huế Đông Ba', '110A Nguyễn Du, Quận 1, TP.HCM', 'Bún bò Huế đậm vị.', 85000::numeric, 4.20::numeric, '06:30', '21:30', 10.779300::numeric, 106.695800::numeric, 'INDOOR'),
    ('FOOD', 'Pizza 4Ps Ben Thanh', '8 Thủ Khoa Huân, Quận 1, TP.HCM', 'Pizza và món Ý phù hợp nhóm bạn.', 220000::numeric, 4.50::numeric, '11:00', '23:00', 10.772700::numeric, 106.696500::numeric, 'INDOOR'),
    ('CAFE', 'Highlands Coffee Nguyễn Huệ', '26 Nguyễn Huệ, Quận 1, TP.HCM', 'Không gian cà phê trung tâm, thuận tiện nghỉ giữa lịch trình.', 70000::numeric, 4.20::numeric, '07:00', '22:00', 10.773500::numeric, 106.704300::numeric, 'INDOOR'),
    ('CAFE', 'Phúc Long Coffee & Tea Nguyễn Huệ', '42 Nguyễn Huệ, Quận 1, TP.HCM', 'Cà phê và trà tại khu trung tâm.', 70000::numeric, 4.20::numeric, '07:00', '22:00', 10.773900::numeric, 106.704500::numeric, 'INDOOR'),
    ('CAFE', 'Cong Caphe Bùi Viện', '274 Bùi Viện, Quận 1, TP.HCM', 'Không gian cà phê phong cách hoài cổ.', 80000::numeric, 4.30::numeric, '07:00', '23:00', 10.767500::numeric, 106.691000::numeric, 'INDOOR'),
    ('CAFE', 'The Running Bean', '115 Hồ Tùng Mậu, Quận 1, TP.HCM', 'Quán cà phê và brunch tại trung tâm.', 120000::numeric, 4.40::numeric, '07:30', '21:30', 10.773000::numeric, 106.702000::numeric, 'INDOOR'),
    ('CAFE', 'Every Half Coffee Roasters', '8-10 Lê Thánh Tôn, Quận 1, TP.HCM', 'Cà phê rang xay và không gian hiện đại.', 100000::numeric, 4.40::numeric, '08:00', '21:00', 10.779000::numeric, 106.704000::numeric, 'INDOOR'),
    ('CAFE', 'Blank Lounge Landmark 81', '720A Điện Biên Phủ, Bình Thạnh, TP.HCM', 'Không gian cà phê và ngắm cảnh thành phố.', 180000::numeric, 4.40::numeric, '08:00', '23:00', 10.795300::numeric, 106.721800::numeric, 'INDOOR'),
    ('CAFE', 'Cafe Cardinal', '76 Nguyễn Huệ, Quận 1, TP.HCM', 'Không gian cà phê và thư giãn cao cấp.', 180000::numeric, 4.30::numeric, '07:00', '22:00', 10.774700::numeric, 106.704900::numeric, 'INDOOR'),
    ('SIGHTSEEING', 'Bảo tàng Chứng tích Chiến tranh', '28 Võ Văn Tần, Quận 3, TP.HCM', 'Bảo tàng lịch sử nổi tiếng của thành phố.', 40000::numeric, 4.50::numeric, '07:30', '17:30', 10.779400::numeric, 106.692000::numeric, 'INDOOR'),
    ('SIGHTSEEING', 'Bảo tàng Mỹ thuật TP.HCM', '97 Phó Đức Chính, Quận 1, TP.HCM', 'Không gian nghệ thuật và kiến trúc cổ.', 30000::numeric, 4.40::numeric, '08:00', '17:00', 10.768500::numeric, 106.698100::numeric, 'INDOOR'),
    ('ENTERTAINMENT', 'BHD Star Bitexco', '2 Hải Triều, Quận 1, TP.HCM', 'Rạp phim tại trung tâm thành phố.', 140000::numeric, 4.30::numeric, '09:00', '23:00', 10.771700::numeric, 106.704000::numeric, 'INDOOR'),
    ('ENTERTAINMENT', 'Khu vui chơi Snow Town Sài Gòn', '125 Đồng Văn Cống, TP Thủ Đức, TP.HCM', 'Khu vui chơi tuyết trong nhà.', 180000::numeric, 4.20::numeric, '09:00', '21:00', 10.768500::numeric, 106.767000::numeric, 'INDOOR'),
    ('ENTERTAINMENT', 'Jump Arena Thảo Điền', '63 Xuân Thủy, TP Thủ Đức, TP.HCM', 'Khu vận động giải trí trong nhà.', 180000::numeric, 4.30::numeric, '09:00', '21:00', 10.803700::numeric, 106.735700::numeric, 'INDOOR'),
    ('SHOPPING', 'AEON Mall Tân Phú Celadon', '30 Tân Thắng, Tân Phú, TP.HCM', 'Trung tâm mua sắm, ăn uống và giải trí.', 250000::numeric, 4.40::numeric, '10:00', '22:00', 10.801800::numeric, 106.618300::numeric, 'INDOOR'),
    ('SHOPPING', 'GIGAMALL Thủ Đức', '240-242 Phạm Văn Đồng, TP Thủ Đức, TP.HCM', 'Trung tâm mua sắm và giải trí.', 220000::numeric, 4.40::numeric, '09:00', '22:00', 10.828500::numeric, 106.718900::numeric, 'INDOOR'),
    ('SHOPPING', 'Vạn Hạnh Mall', '11 Sư Vạn Hạnh, Quận 10, TP.HCM', 'Trung tâm mua sắm và vui chơi.', 220000::numeric, 4.30::numeric, '09:30', '22:00', 10.772200::numeric, 106.667300::numeric, 'INDOOR'),
    ('SHOPPING', 'Takashimaya Saigon Centre', '65 Lê Lợi, Quận 1, TP.HCM', 'Trung tâm mua sắm cao cấp tại trung tâm.', 300000::numeric, 4.50::numeric, '09:30', '21:30', 10.774000::numeric, 106.701000::numeric, 'INDOOR')
) AS v(category_code, place_name, address, description, estimated_cost, rating, opening_time, closing_time, latitude, longitude, place_type)
JOIN Categories c ON c.category_code = v.category_code;

-- Demo Trip
INSERT INTO Trips (owner_id, trip_name, destination, start_date, end_date, budget, description, status)
SELECT u.user_id, 'LocalTrip Demo - TP.HCM', 'TP. Hồ Chí Minh', CURRENT_DATE + INTERVAL '7 day', CURRENT_DATE + INTERVAL '9 day', 5000000, 'Chuyến đi mẫu để kiểm tra Trip, thành viên, lịch trình và quỹ nhóm.', 'PLANNING'
FROM Users u WHERE u.email = 'thanh@localtrip.vn';

-- Demo Trip Members
INSERT INTO TripMembers (trip_id, user_id, member_role)
SELECT t.trip_id, u.user_id, 'OWNER'
FROM Trips t CROSS JOIN Users u
WHERE t.trip_name = 'LocalTrip Demo - TP.HCM' AND u.email = 'thanh@localtrip.vn';

INSERT INTO TripMembers (trip_id, user_id, member_role)
SELECT t.trip_id, u.user_id, 'MEMBER'
FROM Trips t CROSS JOIN Users u
WHERE t.trip_name = 'LocalTrip Demo - TP.HCM' AND u.email = 'lananh@localtrip.vn';

INSERT INTO TripMembers (trip_id, user_id, member_role)
SELECT t.trip_id, u.user_id, 'MEMBER'
FROM Trips t CROSS JOIN Users u
WHERE t.trip_name = 'LocalTrip Demo - TP.HCM' AND u.email = 'hoangnam@localtrip.vn';

-- Demo Preferences
INSERT INTO TripMemberPreferences (trip_id, user_id, category_id)
SELECT tm.trip_id, tm.user_id, c.category_id
FROM TripMembers tm
CROSS JOIN Categories c
JOIN Trips t ON t.trip_id = tm.trip_id
WHERE t.trip_name = 'LocalTrip Demo - TP.HCM';
