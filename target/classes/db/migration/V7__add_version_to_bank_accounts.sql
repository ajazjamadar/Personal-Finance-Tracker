-- Add version column for optimistic locking (@Version annotation in BankAccount entity)
ALTER TABLE bank_accounts ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;
