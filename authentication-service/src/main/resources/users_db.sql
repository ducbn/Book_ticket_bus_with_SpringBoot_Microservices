-- Xóa database nếu tồn tại (chỉ dùng cho dev/test)
DROP DATABASE IF EXISTS users_db;

-- Tạo database và sử dụng
CREATE DATABASE IF NOT EXISTS users_db;
USE users_db;

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    permission TEXT
);

ALTER TABLE roles AUTO_INCREMENT = 1;
INSERT INTO roles (name, permission) VALUES 
('ADMIN', 'FULL_ACCESS'),
('USER', 'BOOK_TICKET'),
('BUS_OWNER', 'MANAGE_BUSES'),
('COMPANY', 'MANAGE_COMPANY');


CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fullname VARCHAR(100) NOT NULL,
    phone_number VARCHAR(10) NOT NULL unique,
    email VARCHAR(50) NOT NULL unique,
    address VARCHAR(200),
    password VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    role_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id)
);
