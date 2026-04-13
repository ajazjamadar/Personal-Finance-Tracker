CREATE TABLE IF NOT EXISTS login_otps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    purpose VARCHAR(32) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_login_otps_user FOREIGN KEY (user_id) REFERENCES users(id)
);

SET @otp_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'login_otps'
      AND index_name = 'idx_login_otps_user_purpose_created'
);

SET @otp_idx_sql = IF(
    @otp_idx_exists = 0,
    'CREATE INDEX idx_login_otps_user_purpose_created ON login_otps(user_id, purpose, created_at)',
    'SELECT 1'
);

PREPARE otp_idx_stmt FROM @otp_idx_sql;
EXECUTE otp_idx_stmt;
DEALLOCATE PREPARE otp_idx_stmt;
