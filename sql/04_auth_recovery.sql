USE LocalTripDB;
GO

/*
   Authentication recovery migration.
   - RememberMeTokens: long-lived browser tokens (30 days).
   - PasswordResetTokens: single-use reset tokens (30 minutes).
   Raw tokens are NEVER stored; Java stores SHA-256 hashes.
*/

IF OBJECT_ID('dbo.RememberMeTokens', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.RememberMeTokens (
        token_id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        user_id INT NOT NULL,
        token_hash VARCHAR(64) NOT NULL UNIQUE,
        expires_at DATETIME2 NOT NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_RememberMeTokens_CreatedAt DEFAULT SYSDATETIME(),
        CONSTRAINT FK_RememberMeTokens_User FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id)
    );
END;
GO

IF OBJECT_ID('dbo.PasswordResetTokens', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.PasswordResetTokens (
        token_id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        user_id INT NOT NULL,
        token_hash VARCHAR(64) NOT NULL UNIQUE,
        expires_at DATETIME2 NOT NULL,
        used_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_PasswordResetTokens_CreatedAt DEFAULT SYSDATETIME(),
        CONSTRAINT FK_PasswordResetTokens_User FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id)
    );
END;
GO

/* Optional cleanup of expired one-time tokens. */
DELETE FROM dbo.PasswordResetTokens
WHERE expires_at <= SYSDATETIME() OR used_at IS NOT NULL;
GO
DELETE FROM dbo.RememberMeTokens
WHERE expires_at <= SYSDATETIME();
GO
