-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS visitor_records;
DROP TABLE IF EXISTS visit_requests;
DROP TABLE IF EXISTS security_staff_profiles;
DROP TABLE IF EXISTS resident_profiles;
DROP TABLE IF EXISTS managing_staff_profiles;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS medias;

-- Create users table
CREATE TABLE users
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

-- Create resident_profiles table
CREATE TABLE resident_profiles
(
    id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id     INT UNSIGNED NOT NULL,
    unit_number VARCHAR(10) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create security_staff_profiles table
CREATE TABLE security_staff_profiles
(
    id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id      INT UNSIGNED NOT NULL,
    badge_number VARCHAR(20) NOT NULL UNIQUE,
    shift        VARCHAR(20) NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CHECK (shift IN ('MORNING', 'AFTERNOON', 'NIGHT'))
);

-- Create managing_staff_profiles table
CREATE TABLE managing_staff_profiles
(
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id    INT UNSIGNED NOT NULL,
    department VARCHAR(50) NOT NULL,
    position   VARCHAR(50) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create visit_requests table
CREATE TABLE visit_requests
(
    id                INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id           INT UNSIGNED NOT NULL,
    verification_code VARCHAR(10) NOT NULL,
    visit_datetime    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    purpose           TEXT        NOT NULL,
    status            VARCHAR(20) NOT NULL,
    remarks           TEXT,
    unit_number       VARCHAR(10) NOT NULL,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'PROGRESS', 'COMPLETED', 'CANCELLED'))
);

-- Create visitor_records table
CREATE TABLE visitor_records
(
    id                INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_id        INT UNSIGNED NOT NULL,
    security_staff_id INT UNSIGNED NOT NULL,
    visitor_name      VARCHAR(100) NOT NULL,
    visitor_ic        VARCHAR(20)  NOT NULL,
    visitor_phone     VARCHAR(20)  NOT NULL,
    check_in_time     TIMESTAMP    NULL,
    check_out_time    TIMESTAMP    NULL,
    remarks           TEXT,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES visit_requests (id) ON DELETE CASCADE,
    FOREIGN KEY (security_staff_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE medias
(
    id         CHAR(36) PRIMARY KEY,
    model      VARCHAR(255) NOT NULL,
    model_id   CHAR(36),
    collection VARCHAR(255),
    file_name  VARCHAR(255) NOT NULL,
    mime_type  VARCHAR(255),
    disk       VARCHAR(255),
    size       DOUBLE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_sessions
(
    session_id  CHAR(36) PRIMARY KEY,
    user_id     INT UNSIGNED NOT NULL,
    ip_address  VARCHAR(45)  NOT NULL,
    user_agent  VARCHAR(512) NOT NULL,
    login_time  TIMESTAMP    NOT NULL,
    last_access TIMESTAMP    NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    is_active   BOOLEAN DEFAULT true,
    device_info VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Create indexes for better query performance
CREATE INDEX idx_visit_request_user ON visit_requests (user_id);
CREATE INDEX idx_visitor_record_request ON visitor_records (request_id);
CREATE INDEX idx_security_staff_badge ON security_staff_profiles (badge_number);

-- Insert initial users, all pass: admin
INSERT INTO users (username, salt, password, email, first_name, last_name, phone_number, is_active, role)
VALUES ('managing_staff', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'managingstaff@hvvs.com', 'MANAGING', 'STAFF', '+60123456789', TRUE, 'MANAGING_STAFF'),
       ('security_staff1', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'security1@hvvs.com', 'Security', 'One', '+60123456781', TRUE, 'SECURITY_STAFF'),
       ('security_staff2', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'security2@hvvs.com', 'Security', 'Two', '+60123456782', TRUE, 'SECURITY_STAFF'),
       ('security_staff3', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'security3@hvvs.com', 'Security', 'Three', '+60123456783', TRUE, 'SECURITY_STAFF'),
       ('resident1', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident1@hvvs.com', 'John', 'Doe', '+60123456001', TRUE, 'RESIDENT'),
       ('resident2', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident2@hvvs.com', 'Jane', 'Smith', '+60123456002', TRUE, 'RESIDENT'),
       ('resident3', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident3@hvvs.com', 'Robert', 'Johnson', '+60123456003', TRUE, 'RESIDENT'),
       ('resident4', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident4@hvvs.com', 'Mary', 'Williams', '+60123456004', TRUE, 'RESIDENT'),
       ('resident5', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident5@hvvs.com', 'Michael', 'Brown', '+60123456005', TRUE, 'RESIDENT'),
       ('resident6', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident6@hvvs.com', 'Elizabeth', 'Jones', '+60123456006', TRUE, 'RESIDENT'),
       ('resident7', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident7@hvvs.com', 'David', 'Garcia', '+60123456007', TRUE, 'RESIDENT'),
       ('resident8', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident8@hvvs.com', 'Sarah', 'Miller', '+60123456008', TRUE, 'RESIDENT'),
       ('resident9', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident9@hvvs.com', 'James', 'Davis', '+60123456009', TRUE, 'RESIDENT'),
       ('resident10', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident10@hvvs.com', 'Jennifer', 'Rodriguez', '+60123456010', TRUE, 'RESIDENT'),
       ('resident11', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident11@hvvs.com', 'William', 'Martinez', '+60123456011', TRUE, 'RESIDENT'),
       ('resident12', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident12@hvvs.com', 'Linda', 'Anderson', '+60123456012', TRUE, 'RESIDENT'),
       ('resident13', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident13@hvvs.com', 'Richard', 'Taylor', '+60123456013', TRUE, 'RESIDENT'),
       ('resident14', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident14@hvvs.com', 'Patricia', 'Thomas', '+60123456014', TRUE, 'RESIDENT'),
       ('resident15', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident15@hvvs.com', 'Joseph', 'Moore', '+60123456015', TRUE, 'RESIDENT'),
       ('resident16', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident16@hvvs.com', 'Margaret', 'Jackson', '+60123456016', TRUE, 'RESIDENT'),
       ('resident17', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident17@hvvs.com', 'Charles', 'White', '+60123456017', TRUE, 'RESIDENT'),
       ('resident18', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident18@hvvs.com', 'Susan', 'Harris', '+60123456018', TRUE, 'RESIDENT'),
       ('resident19', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident19@hvvs.com', 'Thomas', 'Martin', '+60123456019', TRUE, 'RESIDENT'),
       ('resident20', 'FTUa8P#OT7N8d>o3', 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1',
        'resident20@hvvs.com', 'Jessica', 'Thompson', '+60123456020', TRUE, 'RESIDENT');


INSERT INTO visit_requests (user_id, verification_code, visit_datetime, purpose, status, remarks, unit_number)
VALUES
    -- Request #1 by resident1 (user_id=5)
    (5, 'VERIF001', '2025-01-10 10:00:00', 'Friend Gathering', 'COMPLETED', 'No issues', 'A-12-3'),
    -- Request #2 by resident2 (user_id=6)
    (6, 'VERIF002', '2025-01-11 09:30:00', 'Package Delivery', 'COMPLETED', 'Package from courier', 'A-11-7'),
    -- Request #3 by resident3 (user_id=7)
    (7, 'VERIF003', '2025-01-12 15:00:00', 'Birthday Visit', 'COMPLETED', 'Birthday celebration', 'B-09-3'),
    -- Request #4 by resident4 (user_id=8)
    (8, 'VERIF004', '2025-01-13 14:15:00', 'Maintenance Check', 'COMPLETED', 'AC maintenance', 'C-18-15'),
    -- Request #5 by resident5 (user_id=9)
    (9, 'VERIF005', '2025-01-14 08:45:00', 'Friend Visit', 'COMPLETED', 'Visitor from out of town', 'A-01-2'),
    -- Request #6 by resident10 (user_id=14)
    (14, 'VERIF006', '2025-01-15 10:30:00', 'Food Delivery', 'COMPLETED', 'Restaurant delivery', 'D-12-3'),
    -- Request #7 by resident11 (user_id=15)
    (15, 'VERIF007', '2025-01-16 16:00:00', 'Family Reunion', 'COMPLETED', 'Large group', 'B-12-3'),
    -- Request #8 by resident12 (user_id=16)
    (16, 'VERIF008', '2025-01-17 11:00:00', 'Discussion', 'COMPLETED', 'Discussion about upcoming event', 'C-15-3'),
    -- Request #9 by resident19 (user_id=23)
    (23, 'VERIF009', '2025-01-18 18:45:00', 'Party Visit', 'COMPLETED', 'Small gathering', 'A-17-6'),
    -- Request #10 by resident20 (user_id=24)
    (24, 'VERIF010', '2025-01-19 13:20:00', 'Friend Hangout', 'COMPLETED', 'Lunch and hangout', 'A-04-8');


-- Request #1 (2 visitors)
INSERT INTO visitor_records
(request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone, check_in_time, check_out_time, remarks)
VALUES (1, 2, 'Alice Lim', 'IC100001', '60100001111', '2025-01-10 10:05:00', '2025-01-10 12:00:00', 'No issues'),
       (1, 3, 'Ben Chen', 'IC100002', '60100002222', '2025-01-10 10:10:00', '2025-01-10 12:05:00', 'Friend of Alice');

-- Request #2 (1 visitor)
INSERT INTO visitor_records
(request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone, check_in_time, check_out_time, remarks)
VALUES (2, 4, 'DHL Courier', 'IC200001', '60100003333', '2025-01-11 09:35:00', '2025-01-11 09:50:00',
        'Delivered package');

-- Request #3 (3 visitors)
INSERT INTO visitor_records
(request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone, check_in_time, check_out_time, remarks)
VALUES (3, 2, 'Sarah Wong', 'IC300001', '60100004444', '2025-01-12 15:05:00', '2025-01-12 17:00:00', 'Birthday friend'),
       (3, 2, 'Tom Lee', 'IC300002', '60100005555', '2025-01-12 15:10:00', '2025-01-12 17:10:00', 'Close friend'),
       (3, 3, 'Monica Tan', 'IC300003', '60100006666', '2025-01-12 15:15:00', '2025-01-12 17:20:00', 'Bringing cake');

-- Request #4 (2 visitors)
INSERT INTO visitor_records
(request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone, check_in_time, check_out_time, remarks)
VALUES (4, 3, 'Maintenance Tech1', 'IC400001', '60100007777', '2025-01-13 14:20:00', '2025-01-13 15:20:00',
        'Checked AC'),
       (4, 3, 'Maintenance Tech2', 'IC400002', '60100008888', '2025-01-13 14:22:00', '2025-01-13 15:25:00',
        'Assisted AC check');

-- Request #5 (1 visitor)
INSERT INTO visitor_records
(request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone, check_in_time, check_out_time, remarks)
VALUES (5, 4, 'Emily Davis', 'IC500001', '60100009999', '2025-01-14 08:50:00', '2025-01-14 10:00:00',
        'Old college friend');

-- Request #6 (1 visitor)
INSERT INTO visitor_records
(request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone, check_in_time, check_out_time, remarks)
VALUES (6, 2, 'FoodPanda Rider', 'IC600001', '60100000001', '2025-01-15 10:35:00', '2025-01-15 10:45:00',
        'Delivered lunch');

-- Request #7 (10 visitors)
INSERT INTO visitor_records
(request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone, check_in_time, check_out_time, remarks)
VALUES (7, 2, 'Family Member 1', 'IC700001', '60100000002', '2025-01-16 16:05:00', '2025-01-16 19:00:00',
        'Close relative'),
       (7, 2, 'Family Member 2', 'IC700002', '60100000003', '2025-01-16 16:07:00', '2025-01-16 19:05:00',
        'Close relative'),
       (7, 3, 'Family Member 3', 'IC700003', '60100000004', '2025-01-16 16:09:00', '2025-01-16 19:10:00',
        'Close relative'),
       (7, 3, 'Family Member 4', 'IC700004', '60100000005', '2025-01-16 16:10:00', '2025-01-16 19:15:00',
        'Close relative'),
       (7, 4, 'Family Member 5', 'IC700005', '60100000006', '2025-01-16 16:12:00', '2025-01-16 19:20:00',
        'Close relative'),
       (7, 4, 'Family Member 6', 'IC700006', '60100000007', '2025-01-16 16:15:00', '2025-01-16 19:25:00',
        'Close relative'),
       (7, 2, 'Family Member 7', 'IC700007', '60100000008', '2025-01-16 16:17:00', '2025-01-16 19:30:00',
        'Close relative'),
       (7, 3, 'Family Member 8', 'IC700008', '60100000009', '2025-01-16 16:20:00', '2025-01-16 19:35:00',
        'Close relative'),
       (7, 4, 'Family Member 9', 'IC700009', '60100000010', '2025-01-16 16:25:00', '2025-01-16 19:40:00',
        'Close relative'),
       (7, 2, 'Family Member 10', 'IC700010', '60100000011', '2025-01-16 16:27:00', '2025-01-16 19:45:00',
        'Close relative');

-- Request #8 (2 visitors)
INSERT INTO visitor_records
(request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone, check_in_time, check_out_time, remarks)
VALUES (8, 3, 'Jessica Brown', 'IC800001', '60100000111', '2025-01-17 11:05:00', '2025-01-17 12:30:00',
        'Project discussion'),
       (8, 4, 'Kevin White', 'IC800002', '60100000112', '2025-01-17 11:10:00', '2025-01-17 12:35:00',
        'Meeting resident12');

-- Request #9 (1 visitor)
INSERT INTO visitor_records
(request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone, check_in_time, check_out_time, remarks)
VALUES (9, 2, 'Chris Black', 'IC900001', '60100000113', '2025-01-18 18:50:00', '2025-01-18 22:00:00',
        'Invited for party');

-- Request #10 (3 visitors)
INSERT INTO visitor_records
(request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone, check_in_time, check_out_time, remarks)
VALUES (10, 3, 'George Wilson', 'IC100001', '60100000114', '2025-01-19 13:25:00', '2025-01-19 15:00:00', 'Lunch buddy'),
       (10, 3, 'Hannah Kim', 'IC100002', '60100000115', '2025-01-19 13:30:00', '2025-01-19 15:05:00',
        'Neighbor friend'),
       (10, 4, 'Ian Campbell', 'IC100003', '60100000116', '2025-01-19 13:35:00', '2025-01-19 16:00:00',
        'Group hangout');


-- Insert profile for the managing staff (user_id = 1)
INSERT INTO managing_staff_profiles (user_id, department, position, created_at, updated_at)
VALUES (1, 'Administration', 'Manager', NOW(), NOW());

-- Insert profiles for security staff (user_id = 2, 3, 4)
INSERT INTO security_staff_profiles (user_id, badge_number, shift, created_at, updated_at)
VALUES (2, 'SEC001', 'MORNING', NOW(), NOW()),
       (3, 'SEC002', 'AFTERNOON', NOW(), NOW()),
       (4, 'SEC003', 'NIGHT', NOW(), NOW());

-- Insert profiles for residents (user_id = 5 to 24)
INSERT INTO resident_profiles (user_id, unit_number, created_at, updated_at)
VALUES (5, 'A-12-3', NOW(), NOW()),
       (6, 'A-11-7', NOW(), NOW()),
       (7, 'B-09-3', NOW(), NOW()),
       (8, 'C-18-15', NOW(), NOW()),
       (9, 'A-01-2', NOW(), NOW()),
       (10, 'D-12-3', NOW(), NOW()),
       (11, 'B-12-3', NOW(), NOW()),
       (12, 'C-15-3', NOW(), NOW()),
       (13, 'A-17-6', NOW(), NOW()),
       (14, 'A-04-8', NOW(), NOW()),
       (15, 'A-05-4', NOW(), NOW()),
       (16, 'B-07-2', NOW(), NOW()),
       (17, 'C-10-9', NOW(), NOW()),
       (18, 'D-08-5', NOW(), NOW()),
       (19, 'E-03-1', NOW(), NOW()),
       (20, 'F-06-4', NOW(), NOW()),
       (21, 'G-02-7', NOW(), NOW()),
       (22, 'H-09-3', NOW(), NOW()),
       (23, 'I-04-6', NOW(), NOW()),
       (24, 'J-11-8', NOW(), NOW());
