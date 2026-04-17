-- noinspection SqlNoDataSourceInspectionForFile,SqlResolveForFile
CREATE TABLE categories
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description TEXT
);
