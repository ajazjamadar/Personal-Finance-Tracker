SET @role_col_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'role'
);

SET @role_col_sql = IF(
    @role_col_exists = 0,
    'ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT ''USER''',
    'SELECT 1'
);

PREPARE role_col_stmt FROM @role_col_sql;
EXECUTE role_col_stmt;
DEALLOCATE PREPARE role_col_stmt;
