ALTER TABLE transactions DROP COLUMN IF EXISTS source_wallet_id;
ALTER TABLE transactions DROP COLUMN IF EXISTS dest_wallet_id;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS transfer_type VARCHAR(20);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS self_transfer BOOLEAN;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS destination_value VARCHAR(150);
DROP TABLE IF EXISTS wallets;
