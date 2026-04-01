-- noinspection SqlNoDataSourceInspectionForFile,SqlResolveForFile
CREATE TABLE bank_accounts
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT          NOT NULL,
    bank_id        BIGINT          NOT NULL,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    balance        DECIMAL(15, 2)  NOT NULL DEFAULT 0.00,
    created_at     TIMESTAMP                DEFAULT CURRENT_TIMESTAMP,
    version        BIGINT                   DEFAULT 0 NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (bank_id) REFERENCES banks (id)
);
