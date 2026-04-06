-- Add version column for optimistic locking to wallets table
ALTER TABLE wallets ADD COLUMN version BIGINT DEFAULT 0;
