-- Keep test migrations aligned with runtime safety fix for missing categories table
CREATE TABLE IF NOT EXISTS categories
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description TEXT
);
