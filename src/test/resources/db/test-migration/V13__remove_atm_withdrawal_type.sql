-- Remove deprecated ATM_WITHDRAWAL transaction type by normalizing to EXPENSE

UPDATE transactions
SET transaction_type = 'EXPENSE',
    category_name = CASE
        WHEN category_name IS NULL OR TRIM(category_name) = '' THEN 'ATM'
        ELSE category_name
        END
WHERE transaction_type = 'ATM_WITHDRAWAL';
