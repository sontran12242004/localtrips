USE LocalTripDB;
GO

/*
    Admin Audit Log
    ----------------
    Ghi lại các thao tác quan trọng của ADMIN:

    User:
      CREATE_USER, UPDATE_USER, RESET_PASSWORD

    Place:
      CREATE_PLACE, UPDATE_PLACE, ACTIVATE_PLACE, DEACTIVATE_PLACE

    Category:
      CREATE_CATEGORY, UPDATE_CATEGORY, DELETE_CATEGORY

    Không lưu password/passwordHash vào audit log.
*/

IF OBJECT_ID('AdminAuditLogs', 'U') IS NULL
BEGIN
    CREATE TABLE AdminAuditLogs (
        audit_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        actor_user_id INT NOT NULL,
        action VARCHAR(40) NOT NULL,
        target_user_id INT NULL,
        detail NVARCHAR(500) NULL,
        created_at DATETIME2 NOT NULL
            CONSTRAINT DF_AdminAuditLogs_CreatedAt DEFAULT SYSDATETIME(),
        CONSTRAINT FK_AdminAuditLogs_Actor
            FOREIGN KEY (actor_user_id) REFERENCES Users(user_id),
        CONSTRAINT FK_AdminAuditLogs_Target
            FOREIGN KEY (target_user_id) REFERENCES Users(user_id)
    );
END;
GO

/* Nếu database cũ đã có CHECK constraint thì xóa để cập nhật danh sách action. */
IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'CK_AdminAuditLogs_Action'
      AND parent_object_id = OBJECT_ID('AdminAuditLogs')
)
BEGIN
    ALTER TABLE AdminAuditLogs DROP CONSTRAINT CK_AdminAuditLogs_Action;
END;
GO

IF OBJECT_ID('AdminAuditLogs', 'U') IS NOT NULL
BEGIN
    ALTER TABLE AdminAuditLogs
    ADD CONSTRAINT CK_AdminAuditLogs_Action
    CHECK (action IN (
        'CREATE_USER',
        'UPDATE_USER',
        'RESET_PASSWORD',
        'CREATE_PLACE',
        'UPDATE_PLACE',
        'ACTIVATE_PLACE',
        'DEACTIVATE_PLACE',
        'CREATE_CATEGORY',
        'UPDATE_CATEGORY',
        'DELETE_CATEGORY'
    ));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_AdminAuditLogs_CreatedAt'
      AND object_id = OBJECT_ID('AdminAuditLogs')
)
BEGIN
    CREATE INDEX IX_AdminAuditLogs_CreatedAt
        ON AdminAuditLogs(created_at DESC, audit_id DESC);
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_AdminAuditLogs_Actor'
      AND object_id = OBJECT_ID('AdminAuditLogs')
)
BEGIN
    CREATE INDEX IX_AdminAuditLogs_Actor
        ON AdminAuditLogs(actor_user_id);
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_AdminAuditLogs_Action'
      AND object_id = OBJECT_ID('AdminAuditLogs')
)
BEGIN
    CREATE INDEX IX_AdminAuditLogs_Action
        ON AdminAuditLogs(action, created_at DESC);
END;
GO

SELECT TOP 20
    audit_id,
    actor_user_id,
    action,
    target_user_id,
    detail,
    created_at
FROM AdminAuditLogs
ORDER BY created_at DESC, audit_id DESC;
GO
