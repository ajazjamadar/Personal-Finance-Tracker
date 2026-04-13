CREATE TABLE login_otps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    purpose VARCHAR(32) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_login_otps_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_login_otps_user_purpose_created
    ON login_otps(user_id, purpose, created_at DESC);
