CREATE TABLE transactions (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              user_id BIGINT NOT NULL,
                              source_bank_id BIGINT,
                              source_wallet_id BIGINT,
                              dest_bank_id BIGINT,
                              dest_wallet_id BIGINT,
                              category_id BIGINT,
                              transaction_type VARCHAR(20) NOT NULL,
                              amount DECIMAL(15,2) NOT NULL,
                              description TEXT,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (user_id) REFERENCES users(id),
                              FOREIGN KEY (source_bank_id) REFERENCES bank_accounts(id),
                              FOREIGN KEY (source_wallet_id) REFERENCES wallets(id),
                              FOREIGN KEY (dest_bank_id) REFERENCES bank_accounts(id),
                              FOREIGN KEY (dest_wallet_id) REFERENCES wallets(id),
                              FOREIGN KEY (category_id) REFERENCES categories(id)
);