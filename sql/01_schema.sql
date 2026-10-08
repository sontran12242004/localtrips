IF DB_ID('LocalTripDB') IS NULL
BEGIN
    CREATE DATABASE LocalTripDB;
END;
GO

USE LocalTripDB;
GO

/* =========================
   1. USERS
   ========================= */
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(150) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT CK_Users_Role
        CHECK (role IN ('ADMIN', 'USER'))
);
GO

/* =========================
   2. TRIPS
   ========================= */
CREATE TABLE Trips (
    trip_id INT IDENTITY(1,1) PRIMARY KEY,
    owner_id INT NOT NULL,
    trip_name NVARCHAR(150) NOT NULL,
    destination NVARCHAR(200) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    budget DECIMAL(18,2) NOT NULL DEFAULT 0,
    description NVARCHAR(1000) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_Trips_Owner
        FOREIGN KEY (owner_id) REFERENCES Users(user_id),

    CONSTRAINT CK_Trips_Date
        CHECK (end_date >= start_date),

    CONSTRAINT CK_Trips_Budget
        CHECK (budget >= 0),

    CONSTRAINT CK_Trips_Status
        CHECK (status IN (
            'PLANNING',
            'ONGOING',
            'COMPLETED',
            'CANCELLED'
        ))
);
GO

/* =========================
   3. TRIP MEMBERS
   ========================= */
CREATE TABLE TripMembers (
    trip_id INT NOT NULL,
    user_id INT NOT NULL,
    member_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT PK_TripMembers
        PRIMARY KEY (trip_id, user_id),

    CONSTRAINT FK_TripMembers_Trip
        FOREIGN KEY (trip_id) REFERENCES Trips(trip_id),

    CONSTRAINT FK_TripMembers_User
        FOREIGN KEY (user_id) REFERENCES Users(user_id),

    CONSTRAINT CK_TripMembers_Role
        CHECK (member_role IN ('OWNER', 'MEMBER'))
);
GO

/* =========================
   4. CATEGORIES
   ========================= */
CREATE TABLE Categories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL UNIQUE,
    category_name NVARCHAR(100) NOT NULL UNIQUE
);
GO

/* =========================
   5. TRIP MEMBER PREFERENCES
   ========================= */
CREATE TABLE TripMemberPreferences (
    trip_id INT NOT NULL,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT PK_TripMemberPreferences
        PRIMARY KEY (trip_id, user_id, category_id),

    CONSTRAINT FK_Preferences_TripMember
        FOREIGN KEY (trip_id, user_id)
        REFERENCES TripMembers(trip_id, user_id),

    CONSTRAINT FK_Preferences_Category
        FOREIGN KEY (category_id)
        REFERENCES Categories(category_id)
);
GO

/* =========================
   6. PLACES
   ========================= */
CREATE TABLE Places (
    place_id INT IDENTITY(1,1) PRIMARY KEY,
    category_id INT NOT NULL,
    place_name NVARCHAR(200) NOT NULL,
    address NVARCHAR(300) NOT NULL,
    description NVARCHAR(1000) NULL,
    estimated_cost DECIMAL(18,2) NOT NULL DEFAULT 0,
    rating DECIMAL(3,2) NOT NULL DEFAULT 0,
    opening_time TIME NULL,
    closing_time TIME NULL,
    latitude DECIMAL(9,6) NULL,
    longitude DECIMAL(9,6) NULL,
    place_type VARCHAR(20) NOT NULL DEFAULT 'INDOOR',
    is_active BIT NOT NULL DEFAULT 1,

    CONSTRAINT FK_Places_Category
        FOREIGN KEY (category_id)
        REFERENCES Categories(category_id),

    CONSTRAINT CK_Places_Cost
        CHECK (estimated_cost >= 0),

    CONSTRAINT CK_Places_Rating
        CHECK (rating >= 0 AND rating <= 5),

    CONSTRAINT CK_Places_Type
        CHECK (place_type IN ('INDOOR', 'OUTDOOR'))
);
GO

/* =========================
   INDEXES
   ========================= */
CREATE INDEX IX_Trips_Owner
ON Trips(owner_id);
GO

CREATE INDEX IX_TripMembers_User
ON TripMembers(user_id);
GO

CREATE INDEX IX_Places_Category
ON Places(category_id);
GO
