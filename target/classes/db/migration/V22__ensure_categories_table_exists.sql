-- Ensure categories table exists for environments where it was dropped after earlier migrations
CREATE TABLE IF NOT EXISTS categories
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description TEXT
);
