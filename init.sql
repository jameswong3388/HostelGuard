-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS request_status;
DROP TABLE IF EXISTS visitor_record;
DROP TABLE IF EXISTS visit_request;
DROP TABLE IF EXISTS security_staff_profile;
DROP TABLE IF EXISTS resident_profile;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    salt VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (role IN ('RESIDENT', 'SECURITY_STAFF', 'ADMIN', 'SUPER_ADMIN'))
);

-- Create resident_profile table
CREATE TABLE resident_profile (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    room_number VARCHAR(10) NOT NULL,
    block_number VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create security_staff_profile table
CREATE TABLE security_staff_profile (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    badge_number VARCHAR(20) NOT NULL UNIQUE,
    shift VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (shift IN ('MORNING', 'AFTERNOON', 'NIGHT'))
);

-- Create visit_request table
CREATE TABLE visit_request (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    serial_number VARCHAR(20) NOT NULL UNIQUE,
    verification_code VARCHAR(10) NOT NULL,
    visit_date DATE NOT NULL,
    visit_time TIME NOT NULL,
    purpose TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create visitor_record table
CREATE TABLE visitor_record (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_id INT UNSIGNED NOT NULL,
    security_staff_id INT UNSIGNED NOT NULL,
    visitor_name VARCHAR(100) NOT NULL,
    visitor_ic VARCHAR(20) NOT NULL,
    visitor_phone VARCHAR(20) NOT NULL,
    check_in_time TIMESTAMP NULL,
    check_out_time TIMESTAMP NULL,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES visit_request(id) ON DELETE CASCADE,
    FOREIGN KEY (security_staff_id) REFERENCES security_staff_profile(id) ON DELETE CASCADE
);

-- Create request_status table
CREATE TABLE request_status (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_id INT UNSIGNED NOT NULL,
    updated_by_user_id INT UNSIGNED NOT NULL,
    status VARCHAR(20) NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES visit_request(id) ON DELETE CASCADE,
    FOREIGN KEY (updated_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED'))
);

-- Create indexes for better query performance
CREATE INDEX idx_visit_request_user ON visit_request(user_id);
CREATE INDEX idx_visitor_record_request ON visitor_record(request_id);
CREATE INDEX idx_request_status_request ON request_status(request_id);
CREATE INDEX idx_security_staff_badge ON security_staff_profile(badge_number);
CREATE INDEX idx_visit_request_serial ON visit_request(serial_number);

-- Insert admin user, password is 'admin'
INSERT INTO users (username, salt, password, email, full_name, phone_number, is_active, role)
VALUES (
    'admin',
    'FTUa8P#OT7N8d>o3',
    '59B8A449EABC84CB0D032576DF953518B3F6028314F0F2E6FD410EF6BBAE719D',
    'admin@hvvs.com',
    'SUPER ADMIN',
    '+60123456789',
    TRUE,
    'SUPER_ADMIN'
);
