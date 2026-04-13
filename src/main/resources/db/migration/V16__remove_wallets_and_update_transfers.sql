-- Remove wallet dependencies and add transfer destination metadata

SET @drop_fk_source_wallet := (
    SELECT IFNULL(
        (SELECT CONCAT('ALTER TABLE transactions DROP FOREIGN KEY ', kcu.CONSTRAINT_NAME)
         FROM information_schema.KEY_COLUMN_USAGE kcu
         WHERE kcu.TABLE_SCHEMA = DATABASE()
           AND kcu.TABLE_NAME = 'transactions'
           AND kcu.COLUMN_NAME = 'source_wallet_id'
           AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
         LIMIT 1),
        'SELECT 1'
    )
);

PREPARE stmt_drop_fk_source_wallet FROM @drop_fk_source_wallet;
EXECUTE stmt_drop_fk_source_wallet;
DEALLOCATE PREPARE stmt_drop_fk_source_wallet;

SET @drop_fk_dest_wallet := (
    SELECT IFNULL(
        (SELECT CONCAT('ALTER TABLE transactions DROP FOREIGN KEY ', kcu.CONSTRAINT_NAME)
         FROM information_schema.KEY_COLUMN_USAGE kcu
         WHERE kcu.TABLE_SCHEMA = DATABASE()
           AND kcu.TABLE_NAME = 'transactions'
           AND kcu.COLUMN_NAME = 'dest_wallet_id'
           AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
         LIMIT 1),
        'SELECT 1'
    )
);

PREPARE stmt_drop_fk_dest_wallet FROM @drop_fk_dest_wallet;
EXECUTE stmt_drop_fk_dest_wallet;
DEALLOCATE PREPARE stmt_drop_fk_dest_wallet;

SET @drop_source_wallet_col := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'transactions'
              AND COLUMN_NAME = 'source_wallet_id'
        ),
        'ALTER TABLE transactions DROP COLUMN source_wallet_id',
        'SELECT 1'
    )
);

PREPARE stmt_drop_source_wallet_col FROM @drop_source_wallet_col;
EXECUTE stmt_drop_source_wallet_col;
DEALLOCATE PREPARE stmt_drop_source_wallet_col;

SET @drop_dest_wallet_col := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'transactions'
              AND COLUMN_NAME = 'dest_wallet_id'
        ),
        'ALTER TABLE transactions DROP COLUMN dest_wallet_id',
        'SELECT 1'
    )
);

PREPARE stmt_drop_dest_wallet_col FROM @drop_dest_wallet_col;
EXECUTE stmt_drop_dest_wallet_col;
DEALLOCATE PREPARE stmt_drop_dest_wallet_col;

SET @add_transfer_type_col := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'transactions'
              AND COLUMN_NAME = 'transfer_type'
        ),
        'SELECT 1',
        'ALTER TABLE transactions ADD COLUMN transfer_type VARCHAR(20) NULL'
    )
);

PREPARE stmt_add_transfer_type_col FROM @add_transfer_type_col;
EXECUTE stmt_add_transfer_type_col;
DEALLOCATE PREPARE stmt_add_transfer_type_col;

SET @add_self_transfer_col := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'transactions'
              AND COLUMN_NAME = 'self_transfer'
        ),
        'SELECT 1',
        'ALTER TABLE transactions ADD COLUMN self_transfer BOOLEAN NULL'
    )
);

PREPARE stmt_add_self_transfer_col FROM @add_self_transfer_col;
EXECUTE stmt_add_self_transfer_col;
DEALLOCATE PREPARE stmt_add_self_transfer_col;

SET @add_destination_value_col := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'transactions'
              AND COLUMN_NAME = 'destination_value'
        ),
        'SELECT 1',
        'ALTER TABLE transactions ADD COLUMN destination_value VARCHAR(150) NULL'
    )
);

PREPARE stmt_add_destination_value_col FROM @add_destination_value_col;
EXECUTE stmt_add_destination_value_col;
DEALLOCATE PREPARE stmt_add_destination_value_col;

DROP TABLE IF EXISTS wallets;
