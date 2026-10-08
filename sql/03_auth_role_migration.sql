USE LocalTripDB;
GO

/* Migration cho Authentication:
   Users.role chi co 2 system role: ADMIN, USER.
   TRIP_OWNER va MEMBER khong phai role cua tai khoan.
   - TRIP_OWNER: Users.user_id = Trips.owner_id
   - MEMBER: user_id nam trong TripMembers cua mot Trip.
*/

IF COL_LENGTH('Users', 'role') IS NULL
BEGIN
    ALTER TABLE Users
    ADD role VARCHAR(20) NOT NULL
        CONSTRAINT DF_Users_Role DEFAULT 'USER';
END;
GO

/* Neu constraint cu ton tai thi xoa truoc khi tao constraint moi. */
IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_Users_Role'
      AND parent_object_id = OBJECT_ID('Users')
)
BEGIN
    ALTER TABLE Users DROP CONSTRAINT CK_Users_Role;
END;
GO

ALTER TABLE Users
ADD CONSTRAINT CK_Users_Role
    CHECK (role IN ('ADMIN', 'USER'));
GO

/* Dam bao 2 tai khoan mau dung system role. */
UPDATE Users SET role = 'ADMIN'
WHERE email = N'minh@localtrip.vn';

UPDATE Users SET role = 'USER'
WHERE email = N'thanh@localtrip.vn';
GO

/* Neu database cu da co 2 tai khoan nay thi dua ve USER.
   Sau migration, khong con TRIP_OWNER/MEMBER trong Users.role. */
UPDATE Users
SET role = 'USER'
WHERE role IN ('TRIP_OWNER', 'MEMBER');
GO
