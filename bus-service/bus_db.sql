-- Xóa database nếu tồn tại (chỉ dùng cho dev/test)
DROP DATABASE IF EXISTS bus_db;

-- Tạo database và sử dụng
CREATE DATABASE IF NOT EXISTS bus_db;
USE bus_db;

-- ROUTES: Tuyến đường
CREATE TABLE routes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    departure_location VARCHAR(100) NOT NULL,
    arrival_location VARCHAR(100) NOT NULL,
    distance DECIMAL(10,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP
);

-- COMPANIES: Nhà xe
CREATE TABLE companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    phone VARCHAR(50),
    email VARCHAR(100),
    address VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP
);

-- BUS TYPES: Loại xe
CREATE TABLE bus_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    seat_capacity INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP
);

-- BUSES: Chuyến xe (có giờ chạy cố định)
CREATE TABLE buses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    route_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    bus_type_id BIGINT NOT NULL,
    thumbnail VARCHAR(255),
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (route_id) REFERENCES routes(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (bus_type_id) REFERENCES bus_types(id)
);

-- SEATS: Ghế trên xe
CREATE TABLE seats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bus_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (bus_id) REFERENCES buses(id)
);

-- BUS IMAGES: Hình ảnh xe
CREATE TABLE bus_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bus_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (bus_id) REFERENCES buses(id)
);

-- BUS REVIEWS: Đánh giá nhà xe
CREATE TABLE bus_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (company_id) REFERENCES companies(id)
);
