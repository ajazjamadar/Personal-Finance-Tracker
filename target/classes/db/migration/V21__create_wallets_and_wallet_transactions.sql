-- Reintroduce wallets module with soft delete and wallet transaction ledger

DROP TABLE IF EXISTS wallet_transactions;
DROP TABLE IF EXISTS wallets;

CREATE TABLE wallets
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT         NOT NULL,
    name       VARCHAR(100)   NOT NULL,
    balance    DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    currency   VARCHAR(10)    NOT NULL DEFAULT 'INR',
    is_deleted BOOLEAN        NOT NULL DEFAULT FALSE,
    version    BIGINT         NOT NULL DEFAULT 0,
    created_at TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_wallets_user_deleted
    ON wallets (user_id, is_deleted);

CREATE TABLE wallet_transactions
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    wallet_id   BIGINT         NOT NULL,
    type        VARCHAR(20)    NOT NULL,
    amount      DECIMAL(15, 2) NOT NULL,
    category    VARCHAR(100),
    description VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_transactions_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (id)
);

CREATE INDEX idx_wallet_transactions_wallet_created_at
    ON wallet_transactions (wallet_id, created_at);
