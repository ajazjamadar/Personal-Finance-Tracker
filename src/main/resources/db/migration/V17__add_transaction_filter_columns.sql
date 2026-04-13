-- Add transaction metadata columns for history filtering

ALTER TABLE transactions
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    ADD COLUMN payment_method VARCHAR(20) NULL,
    ADD COLUMN category_name VARCHAR(100) NULL,
    ADD COLUMN receiver_name VARCHAR(150) NULL;
