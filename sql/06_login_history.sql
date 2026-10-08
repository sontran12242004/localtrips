USE LocalTripDB;
GO

/* ============================================================
   LOGIN HISTORY
   - Records every login attempt from LoginServlet.
   - user_id is NULL when the email does not match a user.
   - Failed attempts keep a reason such as INVALID_CREDENTIALS.
   ============================================================ */

IF OBJECT_ID('dbo.LoginLogs', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.LoginLogs (
        login_log_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        user_id INT NULL,
        email NVARCHAR(150) NOT NULL,
        success BIT NOT NULL,
        failure_reason VARCHAR(50) NULL,
        ip_address VARCHAR(100) NULL,
        user_agent NVARCHAR(500) NULL,
        attempted_at DATETIME2 NOT NULL
            CONSTRAINT DF_LoginLogs_AttemptedAt DEFAULT SYSDATETIME(),

        CONSTRAINT FK_LoginLogs_User
            FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id),

        CONSTRAINT CK_LoginLogs_Result
            CHECK (
                (success = 1 AND failure_reason IS NULL)
                OR
                (success = 0 AND failure_reason IS NOT NULL)
            )
    );
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_LoginLogs_AttemptedAt'
      AND object_id = OBJECT_ID('dbo.LoginLogs')
)
BEGIN
    CREATE INDEX IX_LoginLogs_AttemptedAt
    ON dbo.LoginLogs(attempted_at DESC, login_log_id DESC);
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_LoginLogs_Email'
      AND object_id = OBJECT_ID('dbo.LoginLogs')
)
BEGIN
    CREATE INDEX IX_LoginLogs_Email
    ON dbo.LoginLogs(email, attempted_at DESC);
END;
GO
