-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS visitor_record;
DROP TABLE IF EXISTS visit_request;
DROP TABLE IF EXISTS security_staff_profile;
DROP TABLE IF EXISTS resident_profile;
DROP TABLE IF EXISTS managing_staff_profile;
DROP TABLE IF EXISTS user;

-- Create user table
CREATE TABLE user
(
    id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    salt         VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    first_name   VARCHAR(100),
    last_name    VARCHAR(100),
    phone_number VARCHAR(20)  NOT NULL,
    is_active    BOOLEAN               DEFAULT TRUE,
    role         VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (role IN ('RESIDENT', 'SECURITY_STAFF', 'MANAGING_STAFF'))
);

-- Create resident_profile table
CREATE TABLE resident_profile
(
    id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id      INT UNSIGNED NOT NULL,
    room_number  VARCHAR(10) NOT NULL,
    block_number VARCHAR(10) NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

-- Create security_staff_profile table
CREATE TABLE security_staff_profile
(
    id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id      INT UNSIGNED NOT NULL,
    badge_number VARCHAR(20) NOT NULL UNIQUE,
    shift        VARCHAR(20) NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    CHECK (shift IN ('MORNING', 'AFTERNOON', 'NIGHT'))
);

-- Create managing_staff_profile table
CREATE TABLE managing_staff_profile
(
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id    INT UNSIGNED NOT NULL,
    department VARCHAR(50) NOT NULL,
    position   VARCHAR(50) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

-- Create visit_request table
CREATE TABLE visit_request
(
    id                INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id           INT UNSIGNED NOT NULL,
    verification_code VARCHAR(10) NOT NULL,
    visit_datetime    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    purpose           TEXT        NOT NULL,
    status            VARCHAR(20) NOT NULL,
    remarks           TEXT,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED'))
);

-- Create visitor_record table
CREATE TABLE visitor_record
(
    id                INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_id        INT UNSIGNED NOT NULL,
    security_staff_id INT UNSIGNED NOT NULL,
    visitor_name      VARCHAR(100) NOT NULL,
    visitor_ic        VARCHAR(20)  NOT NULL,
    visitor_phone     VARCHAR(20)  NOT NULL,
    check_in_time     TIMESTAMP NULL,
    check_out_time    TIMESTAMP NULL,
    remarks           TEXT,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES visit_request (id) ON DELETE CASCADE,
    FOREIGN KEY (security_staff_id) REFERENCES security_staff_profile (id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_visit_request_user ON visit_request (user_id);
CREATE INDEX idx_visitor_record_request ON visitor_record (request_id);
CREATE INDEX idx_security_staff_badge ON security_staff_profile (badge_number);

-- Insert initial users, all pass: admin
INSERT INTO user (username, salt, password, email, first_name, last_name, phone_number, is_active, role)
VALUES ('managing_staff', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'managingstaff@hvvs.com', 'MANAGING', 'STAFF', '+60123456789', TRUE, 'MANAGING_STAFF'),
       ('resident_user', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident@hvvs.com', 'John', 'Doe', '+60123456780', TRUE, 'RESIDENT'),
       ('security_staff', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'securitystaff@hvvs.com', 'SECURITY', 'STAFF', '+60123456781', TRUE, 'SECURITY_STAFF');

-- Insert managing staff profile
INSERT INTO managing_staff_profile (user_id, department, position)
SELECT id, 'Administration', 'Senior Manager'
FROM user
WHERE username = 'managing_staff';

-- Insert resident profile
INSERT INTO resident_profile (user_id, room_number, block_number)
SELECT id, 'A-123', 'Block A'
FROM user
WHERE username = 'resident_user';

-- Insert security staff profile
INSERT INTO security_staff_profile (user_id, badge_number, shift)
SELECT id, 'SEC001', 'MORNING'
FROM user
WHERE username = 'security_staff';
