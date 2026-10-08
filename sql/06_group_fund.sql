USE LocalTripDB;
GO

/* Quỹ nhóm: chỉ lưu các khoản đóng góp.
   Số dư thực tế = tổng đóng góp - tổng Expenses có from_group_fund = 1.
   Vì số dư được tính động nên Expense thêm/sửa/xóa sẽ tự cập nhật số dư. */

IF OBJECT_ID('dbo.GroupFundContributions', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.GroupFundContributions (
        contribution_id INT IDENTITY(1,1) PRIMARY KEY,
        trip_id INT NOT NULL,
        user_id INT NOT NULL,
        amount DECIMAL(18,2) NOT NULL,
        note NVARCHAR(255) NULL,
        created_by INT NOT NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_GroupFundContributions_CreatedAt DEFAULT SYSDATETIME(),

        CONSTRAINT FK_GroupFundContributions_Trip
            FOREIGN KEY (trip_id) REFERENCES dbo.Trips(trip_id),
        CONSTRAINT FK_GroupFundContributions_User
            FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id),
        CONSTRAINT FK_GroupFundContributions_CreatedBy
            FOREIGN KEY (created_by) REFERENCES dbo.Users(user_id),
        CONSTRAINT CK_GroupFundContributions_Amount
            CHECK (amount > 0)
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_GroupFundContributions_Trip' AND object_id = OBJECT_ID('dbo.GroupFundContributions'))
BEGIN
    CREATE INDEX IX_GroupFundContributions_Trip
        ON dbo.GroupFundContributions(trip_id, created_at DESC);
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_GroupFundContributions_User' AND object_id = OBJECT_ID('dbo.GroupFundContributions'))
BEGIN
    CREATE INDEX IX_GroupFundContributions_User
        ON dbo.GroupFundContributions(user_id);
END;
GO
