-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS request_status;
DROP TABLE IF EXISTS visitor_record;
DROP TABLE IF EXISTS visit_request;
DROP TABLE IF EXISTS security_staff_profile;
DROP TABLE IF EXISTS resident_profile;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
                       user_id VARCHAR(36) PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       salt VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       full_name VARCHAR(100) NOT NULL,
                       phone_number VARCHAR(20) NOT NULL,
                       is_active BOOLEAN DEFAULT true,
                       role VARCHAR(20) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CHECK (role IN ('RESIDENT', 'SECURITY_STAFF', 'ADMIN', 'SUPER_ADMIN'))
);

-- Create resident_profile table
CREATE TABLE resident_profile (
                                  user_id VARCHAR(36) PRIMARY KEY,
                                  room_number VARCHAR(10) NOT NULL,
                                  block_number VARCHAR(10) NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create security_staff_profile table
CREATE TABLE security_staff_profile (
                                        user_id VARCHAR(36) PRIMARY KEY,
                                        badge_number VARCHAR(20) NOT NULL UNIQUE,
                                        shift VARCHAR(20) NOT NULL,
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                        CHECK (shift IN ('MORNING', 'AFTERNOON', 'NIGHT'))
);

-- Create visit_request table
CREATE TABLE visit_request (
                               visit_request_id VARCHAR(36) PRIMARY KEY,
                               user_id VARCHAR(36) NOT NULL,
                               verification_code VARCHAR(10) NOT NULL,
                               visit_date DATE NOT NULL,
                               visit_time TIME NOT NULL,
                               purpose TEXT NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create visitor_record table
CREATE TABLE visitor_record (
                                record_id SERIAL PRIMARY KEY,
                                request_id VARCHAR(36) NOT NULL,
                                security_staff_id VARCHAR(36) NOT NULL,
                                visitor_name VARCHAR(100) NOT NULL,
                                visitor_ic VARCHAR(20) NOT NULL,
                                visitor_phone VARCHAR(20) NOT NULL,
                                check_in_time TIMESTAMP,
                                check_out_time TIMESTAMP,
                                remarks TEXT,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (request_id) REFERENCES visit_request(visit_request_id) ON DELETE CASCADE,
                                FOREIGN KEY (security_staff_id) REFERENCES security_staff_profile(user_id) ON DELETE CASCADE
);

-- Create request_status table
CREATE TABLE request_status (
                                status_id SERIAL PRIMARY KEY,
                                request_id VARCHAR(36) NOT NULL,
                                updated_by_user_id VARCHAR(36) NOT NULL,
                                status VARCHAR(20) NOT NULL,
                                remarks TEXT,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (request_id) REFERENCES visit_request(visit_request_id) ON DELETE CASCADE,
                                FOREIGN KEY (updated_by_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED'))
);

-- Create indexes for better query performance
CREATE INDEX idx_visit_request_user ON visit_request(user_id);
CREATE INDEX idx_visitor_record_request ON visitor_record(request_id);
CREATE INDEX idx_request_status_request ON request_status(request_id);
CREATE INDEX idx_security_staff_badge ON security_staff_profile(badge_number);

-- Insert admin user, password is admin
INSERT INTO users (user_id, username, salt, password, email, full_name, phone_number, is_active, role)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'admin', 'FTUa8P#OT7N8d>o3', '59B8A449EABC84CB0D032576DF953518B3F6028314F0F2E6FD410EF6BBAE719D', 'admin@hvvs.com', 'SUPER ADMIN', '+60123456789', true, 'SUPER_ADMIN');
