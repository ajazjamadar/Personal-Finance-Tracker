-- Improve transaction history filtering and sorting performance

CREATE INDEX idx_transactions_user_created_at
    ON transactions (user_id, created_at);

CREATE INDEX idx_transactions_user_status_created_at
    ON transactions (user_id, status, created_at);

CREATE INDEX idx_transactions_source_bank_id
    ON transactions (source_bank_id);

CREATE INDEX idx_transactions_dest_bank_id
    ON transactions (dest_bank_id);
