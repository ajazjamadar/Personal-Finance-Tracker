CREATE TABLE bank_accounts (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               user_id BIGINT NOT NULL,
                               bank_name VARCHAR(100) NOT NULL,
                               account_number VARCHAR(50) UNIQUE NOT NULL,
                               balance DECIMAL(15,2) DEFAULT 0.00 NOT NULL,
                               version INT DEFAULT 0,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(id)
);