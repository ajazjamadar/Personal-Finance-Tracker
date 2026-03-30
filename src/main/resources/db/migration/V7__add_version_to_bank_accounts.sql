-- Add version column for optimistic locking (@Version annotation in BankAccount entity)
SET @column_exists := (
		SELECT COUNT(*)
		FROM INFORMATION_SCHEMA.COLUMNS
		WHERE TABLE_SCHEMA = DATABASE()
			AND TABLE_NAME = 'bank_accounts'
			AND COLUMN_NAME = 'version'
);

SET @ddl := IF(@column_exists = 0,
							 'ALTER TABLE bank_accounts ADD COLUMN version BIGINT DEFAULT 0 NOT NULL',
							 'SELECT 1');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
