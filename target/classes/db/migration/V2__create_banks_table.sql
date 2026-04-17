-- noinspection SqlNoDataSourceInspectionForFile,SqlResolveForFile
CREATE TABLE banks
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_name  VARCHAR(100) NOT NULL UNIQUE,
    bank_code  VARCHAR(20)  NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO banks (bank_name, bank_code)
VALUES ('HDFC Bank', 'HDFC'),
       ('State Bank of India', 'SBI'),
       ('ICICI Bank', 'ICICI'),
       ('Axis Bank', 'AXIS'),
       ('Kotak Mahindra Bank', 'KOTAK');
