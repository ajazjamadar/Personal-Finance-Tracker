-- noinspection SqlNoDataSourceInspectionForFile,SqlResolveForFile
CREATE TABLE wallets
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT         NOT NULL,
    wallet_name VARCHAR(100)   NOT NULL,
    balance     DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at  TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
