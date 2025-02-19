-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS visitor_records;
DROP TABLE IF EXISTS visit_requests;
DROP TABLE IF EXISTS security_staff_profiles;
DROP TABLE IF EXISTS resident_profiles;
DROP TABLE IF EXISTS managing_staff_profiles;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS medias;
DROP TABLE IF EXISTS mfa_methods;

-- Create users table
CREATE TABLE users
(
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    salt            VARCHAR(255) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    phone_number    VARCHAR(20)  NOT NULL,
    identity_number VARCHAR(12)  NOT NULL,
    address         VARCHAR(100),
    gender          VARCHAR(100),
    is_active       BOOLEAN               DEFAULT FALSE,
    role            VARCHAR(20)  NOT NULL,
    is_mfa_enable   BOOLEAN               DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (role IN ('RESIDENT', 'SECURITY_STAFF', 'MANAGING_STAFF', 'SUPER_ADMIN')),
    CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

-- Create mfa_methods table
CREATE TABLE mfa_methods
(
    id             CHAR(36) PRIMARY KEY,
    user_id        INT UNSIGNED NOT NULL,
    method         VARCHAR(20)  NOT NULL,
    secret         VARCHAR(255),
    recovery_codes JSON,
    is_primary     BOOLEAN               DEFAULT FALSE,
    is_enabled     BOOLEAN               DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CHECK (method IN ('TOTP', 'SMS', 'EMAIL', 'RECOVERY_CODES'))
);

-- Create resident_profiles table
CREATE TABLE resident_profiles
(
    id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id     INT UNSIGNED NOT NULL,
    unit_number VARCHAR(10)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create security_staff_profiles table
CREATE TABLE security_staff_profiles
(
    id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id      INT UNSIGNED NOT NULL,
    badge_number VARCHAR(20)  NOT NULL UNIQUE,
    shift        VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CHECK (shift IN ('MORNING', 'AFTERNOON', 'NIGHT'))
);

-- Create managing_staff_profiles table
CREATE TABLE managing_staff_profiles
(
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id    INT UNSIGNED NOT NULL,
    department VARCHAR(50)  NOT NULL,
    position   VARCHAR(50)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create visit_requests table
CREATE TABLE visit_requests
(
    id                INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id           INT UNSIGNED NOT NULL,
    verification_code VARCHAR(36)  NOT NULL,
    visit_datetime    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    purpose           TEXT         NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    remarks           TEXT,
    unit_number       VARCHAR(10)  NOT NULL,
    number_of_entries INT UNSIGNED NOT NULL DEFAULT 1,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
    city        VARCHAR(255),
    region      VARCHAR(255),
    country     VARCHAR(255),
    user_agent  VARCHAR(512) NOT NULL,
    login_time  TIMESTAMP    NOT NULL,
    last_access TIMESTAMP    NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    device_info VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE notifications
(
    id                  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             INT UNSIGNED NOT NULL,
    type                VARCHAR(50)  NOT NULL,
    title               VARCHAR(255) NOT NULL,
    message             TEXT         NOT NULL,
    status              ENUM ('UNREAD', 'READ') DEFAULT 'UNREAD',
    related_entity_type VARCHAR(50),
    related_entity_id   CHAR(36),
    read_at             TIMESTAMP    NULL,
    created_at          TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NULL       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CHECK (type IN ('VISIT_APPROVAL', 'SECURITY_ALERT', 'SYSTEM_UPDATE', 'VISIT_REMINDER', 'ENTRY_EXIT'))
);

-- Create calendar_events table
CREATE TABLE calendar_events 
(
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    all_day BOOLEAN DEFAULT FALSE,
    url VARCHAR(512),
    border_color VARCHAR(20),
    background_color VARCHAR(20),
    display_mode ENUM('BACKGROUND', 'INVERSE', 'NORMAL') DEFAULT 'NORMAL',
    status ENUM('ACTIVE', 'CANCELLED', 'COMPLETED') DEFAULT 'ACTIVE',
    recurrence_pattern ENUM('NONE', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') DEFAULT 'NONE',
    parent_event_id INT UNSIGNED,
    time_zone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_event_id) REFERENCES calendar_events(id) ON DELETE SET NULL,
    
    CHECK (end_date >= start_date)
);

-- Create indexes for better query performance
CREATE INDEX idx_user_sessions_user_active ON user_sessions (user_id);
CREATE INDEX idx_user_sessions_expires_active ON user_sessions (expires_at);
CREATE INDEX idx_user_sessions_last_access ON user_sessions (last_access);
CREATE INDEX idx_user_sessions_user_expires ON user_sessions (user_id, expires_at);

CREATE INDEX idx_visit_requests_verification_status_entries ON visit_requests (verification_code, status, number_of_entries);

CREATE INDEX idx_resident_profiles_user_id ON resident_profiles (user_id);
CREATE INDEX idx_security_staff_profiles_user_id ON security_staff_profiles (user_id);
CREATE INDEX idx_managing_staff_profiles_user_id ON managing_staff_profiles (user_id);

CREATE INDEX idx_medias_model_id ON medias (model_id);
CREATE INDEX idx_medias_collection ON medias (collection);

CREATE INDEX idx_mfa_methods_user ON mfa_methods (user_id);
CREATE INDEX idx_mfa_methods_user_method ON mfa_methods (user_id, method);

CREATE INDEX idx_medias_model_composite ON medias (model, model_id);