import mysql.connector
from faker import Faker
import random
import string
from datetime import timedelta
import uuid

# ------------------------------
# Global Configuration & Setup
# ------------------------------

DB_CONFIG = {
    'user': 'root',
    'password': 'password',
    'host': '127.0.0.1',
    'database': 'app',
    'raise_on_warnings': True,
    'port': 3308
}

# Initialize Faker (English - Malaysia)
fake = Faker('en_MS')

# Fixed salt and password values
FIXED_SALT = 'FTUa8P#OT7N8d>o3'
FIXED_PASSWORD = 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1'

# Data structures to ensure uniqueness
existing_usernames = set()
existing_emails = set()
existing_identity_numbers = set()
existing_badge_numbers = set()
existing_verification_codes = set()

# ------------------------------
# SQL Statements
# ------------------------------

DROP_TABLES_SCRIPT = """
DROP TABLE IF EXISTS visitor_records;
DROP TABLE IF EXISTS visit_requests;
DROP TABLE IF EXISTS security_staff_profiles;
DROP TABLE IF EXISTS resident_profiles;
DROP TABLE IF EXISTS managing_staff_profiles;
DROP TABLE IF EXISTS mfa_methods;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS medias;
DROP TABLE IF EXISTS calendar_events;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS users;
"""

CREATE_TABLES_SCRIPT = """
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
    gender          VARCHAR(10),
    is_active       BOOLEAN               DEFAULT FALSE,
    role            VARCHAR(20)  NOT NULL,
    is_mfa_enable   BOOLEAN               DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (role IN ('RESIDENT', 'SECURITY_STAFF', 'MANAGING_STAFF', 'SUPER_ADMIN')),
    CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
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
    verification_code VARCHAR(36)  NOT NULL UNIQUE,
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

-- Create password_reset_tokens table
CREATE TABLE password_reset_tokens
(
    id         CHAR(36) PRIMARY KEY,
    user_id    INT UNSIGNED NOT NULL,
    token      VARCHAR(255) NOT NULL UNIQUE,
    used       BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create notifications table
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

-- Create indexes
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

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens (token);
CREATE INDEX idx_password_reset_tokens_user ON password_reset_tokens (user_id);
"""


# ------------------------------
# Helper Functions
# ------------------------------

def execute_sql_script(cursor, script):
    """
    Execute a multi-statement SQL script by splitting on semicolons.
    """
    for statement in script.strip().split(';'):
        stmt = statement.strip()
        if stmt:
            cursor.execute(stmt)


def generate_unique_username(first_name: str, last_name: str) -> str:
    """
    Generate a unique username based on first_name and last_name.
    """
    base_username = f"{first_name.lower()}.{last_name.lower()}"
    username = base_username
    counter = 1

    while username in existing_usernames:
        username = f"{base_username}{counter}"
        counter += 1

    existing_usernames.add(username)
    return username


def generate_unique_email(username: str) -> str:
    """
    Generate a unique email based on the given username.
    """
    domain = fake.free_email_domain()
    email = f"{username}@{domain}"
    counter = 1

    while email in existing_emails:
        email = f"{username}{counter}@{domain}"
        counter += 1

    existing_emails.add(email)
    return email


def generate_unique_identity() -> str:
    """
    Generate a random 12-digit identity number and ensure it's unique.
    """
    while True:
        identity = ''.join(random.choices(string.digits, k=12))
        if identity not in existing_identity_numbers:
            existing_identity_numbers.add(identity)
            return identity


def generate_unit_number() -> str:
    """
    Generate a random unit number in the format: {Building}-{Floor}-{Unit}.
    Example: A-07-12
    """
    building = random.choice(string.ascii_uppercase)
    floor = random.randint(1, 20)
    unit = random.randint(1, 30)
    return f"{building}-{floor:02d}-{unit:02d}"


def generate_badge_number() -> str:
    """
    Generate a badge number in the format: SECXXX.
    """
    while True:
        badge = f"SEC{random.randint(100, 999)}"
        if badge not in existing_badge_numbers:
            existing_badge_numbers.add(badge)
            return badge


def generate_verification_code() -> str:
    """
    Generate a unique UUID as a verification code.
    """
    while True:
        code = str(uuid.uuid4())
        if code not in existing_verification_codes:
            existing_verification_codes.add(code)
            return code


def generate_malaysia_phone_number() -> str:
    """
    Generates a Malaysian mobile phone number in the format +60XXXXXXXXXX
    where X is a digit and the first digit after +60 is 1, 3, 4, 5, 6, 7, or 9.
    """
    prefix = '+60'
    second_digit = random.choice(['1', '3', '4', '5', '6', '7', '9'])
    remaining_digits = ''.join(random.choices(string.digits, k=8))
    return f"{prefix}{second_digit}{remaining_digits}"


def insert_user(cursor,
                username: str,
                salt: str,
                password: str,
                email: str,
                first_name: str,
                last_name: str,
                phone_number: str,
                identity_number: str,
                address: str,
                gender: str,
                role: str,
                is_mfa_enable: bool = False,
                is_active: bool = False) -> int:
    """
    Inserts a user into the users table and returns the user ID.
    """
    add_user_sql = """
        INSERT INTO users
            (username, salt, password, email, first_name, last_name,
             phone_number, identity_number, address, gender, role,
             is_mfa_enable, is_active)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """
    user_data = (
        username, salt, password, email, first_name, last_name,
        phone_number, identity_number, address, gender, role,
        is_mfa_enable, is_active
    )
    cursor.execute(add_user_sql, user_data)
    return cursor.lastrowid


def insert_managing_staff_profile(cursor, user_id: int, department: str, position: str):
    """
    Inserts a managing_staff_profiles record.
    """
    sql = """
        INSERT INTO managing_staff_profiles
            (user_id, department, position)
        VALUES (%s, %s, %s)
    """
    cursor.execute(sql, (user_id, department, position))


def insert_security_staff_profile(cursor, user_id: int, badge_number: str, shift: str):
    """
    Inserts a security_staff_profiles record.
    """
    sql = """
        INSERT INTO security_staff_profiles
            (user_id, badge_number, shift)
        VALUES (%s, %s, %s)
    """
    cursor.execute(sql, (user_id, badge_number, shift))


def insert_resident_profile(cursor, user_id: int, unit_number: str):
    """
    Inserts a resident_profiles record.
    """
    sql = """
        INSERT INTO resident_profiles
            (user_id, unit_number)
        VALUES (%s, %s)
    """
    cursor.execute(sql, (user_id, unit_number))


def insert_visit_request(cursor, user_id: int, verification_code: str, visit_datetime,
                         purpose: str, status: str, remarks: str, unit_number: str,
                         number_of_entries: int) -> int:
    """
    Inserts a visit_requests record and returns the generated request ID.
    """
    sql = """
        INSERT INTO visit_requests
            (user_id, verification_code, visit_datetime, purpose, status, remarks,
             unit_number, number_of_entries)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
    """
    data = (
        user_id, verification_code, visit_datetime, purpose, status, remarks,
        unit_number, number_of_entries
    )
    cursor.execute(sql, data)
    return cursor.lastrowid


def insert_visitor_record(cursor, request_id: int, security_staff_id: int,
                          visitor_name: str, visitor_ic: str, visitor_phone: str,
                          check_in_time, check_out_time, remarks: str):
    """
    Inserts a visitor_records record.
    """
    sql = """
        INSERT INTO visitor_records
            (request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone,
             check_in_time, check_out_time, remarks)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
    """
    data = (
        request_id,
        security_staff_id,
        visitor_name,
        visitor_ic,
        visitor_phone,
        check_in_time,
        check_out_time,
        remarks
    )
    cursor.execute(sql, data)


def insert_calendar_event(cursor, user_id: int, title: str, description: str,
                          start_date, end_date, all_day: bool, url: str,
                          border_color: str, background_color: str,
                          display_mode: str, status: str):
    """
    Inserts a calendar_events record.
    """
    sql = """
        INSERT INTO calendar_events
            (user_id, title, description, start_date, end_date, all_day, url,
             border_color, background_color, display_mode, status)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """
    data = (
        user_id,
        title,
        description,
        start_date,
        end_date,
        all_day,
        url,
        border_color,
        background_color,
        display_mode,
        status
    )
    cursor.execute(sql, data)


# ------------------------------
# Main Data Generation & Insertion
# ------------------------------

def main():
    try:
        # Connect to the MySQL database
        cnx = mysql.connector.connect(**DB_CONFIG)
        cursor = cnx.cursor()

        # 1. Drop existing tables
        print("Dropping existing tables...")
        execute_sql_script(cursor, DROP_TABLES_SCRIPT)
        cnx.commit()

        # 2. Create tables
        print("Creating tables...")
        execute_sql_script(cursor, CREATE_TABLES_SCRIPT)
        cnx.commit()

        # 3. Insert Managing Staff
        print("Inserting Managing Staff...")
        managing_username = "manager.test"
        managing_email = "jameswong0517@gmail.com"

        # Mark them as existing so no collisions
        existing_usernames.add(managing_username)
        existing_emails.add(managing_email)

        managing_staff_id = insert_user(
            cursor,
            username=managing_username,
            salt=FIXED_SALT,
            password=FIXED_PASSWORD,
            email=managing_email,
            first_name="TestManager",
            last_name="User",
            phone_number=generate_malaysia_phone_number(),
            identity_number=generate_unique_identity(),
            address="123 Test Street, Test City, Test State",
            gender="OTHER",
            role='MANAGING_STAFF'
        )
        insert_managing_staff_profile(cursor, managing_staff_id, "Management", "Manager")

        # 4. Insert Super Admin
        print("Inserting Super Admin...")
        super_username = "super.admin"
        super_email = "super.admin@example.com"

        existing_usernames.add(super_username)
        existing_emails.add(super_email)

        super_admin_id = insert_user(
            cursor,
            username=super_username,
            salt=FIXED_SALT,
            password=FIXED_PASSWORD,
            email=super_email,
            first_name="Super",
            last_name="Admin",
            phone_number=generate_malaysia_phone_number(),
            identity_number=generate_unique_identity(),
            address="789 Admin Tower, Admin City",
            gender="OTHER",
            role='SUPER_ADMIN'
        )
        insert_managing_staff_profile(cursor, super_admin_id, 'Administration', 'Super Admin')

        # 5. Insert Security Staff
        print("Inserting Security Staff...")
        security_staff_ids = []

        # Fixed Security Staff
        security_username = "security.test"
        security_email = "security.test@example.com"

        existing_usernames.add(security_username)
        existing_emails.add(security_email)

        fixed_security_id = insert_user(
            cursor,
            username=security_username,
            salt=FIXED_SALT,
            password=FIXED_PASSWORD,
            email=security_email,
            first_name="TestSecurity",
            last_name="User",
            phone_number=generate_malaysia_phone_number(),
            identity_number=generate_unique_identity(),
            address="456 Security Avenue, Secure City, Secure State",
            gender="MALE",
            role='SECURITY_STAFF'
        )
        security_staff_ids.append(fixed_security_id)

        insert_security_staff_profile(
            cursor,
            fixed_security_id,
            generate_badge_number(),
            'MORNING'
        )

        # Additional Random Security Staff
        for _ in range(2):
            first_name = fake.first_name()
            last_name = fake.last_name()
            username = generate_unique_username(first_name, last_name)
            email = generate_unique_email(username)

            new_security_id = insert_user(
                cursor,
                username=username,
                salt=FIXED_SALT,
                password=FIXED_PASSWORD,
                email=email,
                first_name=first_name,
                last_name=last_name,
                phone_number=generate_malaysia_phone_number(),
                identity_number=generate_unique_identity(),
                address=fake.address().replace('\n', ', ')[:100],  # Truncate to 100 chars
                gender=random.choice(['MALE', 'FEMALE', 'OTHER']),
                role='SECURITY_STAFF'
            )
            security_staff_ids.append(new_security_id)
            insert_security_staff_profile(
                cursor,
                new_security_id,
                generate_badge_number(),
                random.choice(['MORNING', 'AFTERNOON', 'NIGHT'])
            )

        # 6. Insert Residents
        print("Inserting Residents...")
        resident_ids = []
        resident_unit_map = {}

        # Fixed Resident
        resident_username = "resident.test"
        resident_email = "resident.test@example.com"

        existing_usernames.add(resident_username)
        existing_emails.add(resident_email)

        fixed_resident_id = insert_user(
            cursor,
            username=resident_username,
            salt=FIXED_SALT,
            password=FIXED_PASSWORD,
            email=resident_email,
            first_name="TestResident",
            last_name="User",
            phone_number=generate_malaysia_phone_number(),
            identity_number=generate_unique_identity(),
            address="789 Resident Lane, Resident City, Resident State",
            gender="FEMALE",
            role='RESIDENT'
        )
        resident_ids.append(fixed_resident_id)

        unit_number = generate_unit_number()
        resident_unit_map[fixed_resident_id] = unit_number
        insert_resident_profile(cursor, fixed_resident_id, unit_number)

        # Additional Random Residents
        for _ in range(19):
            first_name = fake.first_name()
            last_name = fake.last_name()
            username = generate_unique_username(first_name, last_name)
            email = generate_unique_email(username)

            new_resident_id = insert_user(
                cursor,
                username=username,
                salt=FIXED_SALT,
                password=FIXED_PASSWORD,
                email=email,
                first_name=first_name,
                last_name=last_name,
                phone_number=generate_malaysia_phone_number(),
                identity_number=generate_unique_identity(),
                address=fake.address().replace('\n', ', ')[:100],
                gender=random.choice(['MALE', 'FEMALE', 'OTHER']),
                role='RESIDENT'
            )
            resident_ids.append(new_resident_id)

            unit_number = generate_unit_number()
            resident_unit_map[new_resident_id] = unit_number
            insert_resident_profile(cursor, new_resident_id, unit_number)

        cnx.commit()

        # 7. Insert Visit Requests & Visitor Records
        print("Inserting Visit Requests and Visitor Records...")
        purposes = [
            "Package Delivery", "Family Visit", "Friend Visit", "Maintenance Check",
            "Appliance Repair", "Furniture Delivery", "Courier Pickup", "Cleaning Service",
            "Pest Control Service", "Landscaping Consultation", "Housewarming Party",
            "Electrical Service Visit", "Plumbing Service Visit", "Internet Installation",
            "Cable Installation", "Home Security Setup", "Fire Alarm Testing",
            "Property Inspection", "Utility Meter Reading", "Waste Collection Assistance",
            "Mailbox Installation", "Renovation Contractor Visit", "Interior Design Consultation",
            "Painting Service", "Gardening Service", "Home Appliance Installation",
            "Furniture Assembly", "Security System Maintenance", "Water Supply Repair",
            "Gas Leak Inspection", "Health and Safety Audit", "Post Office Pickup",
            "Grocery Delivery", "Prescription Delivery", "Mobile Service Appointment",
            "IT Support Visit", "Software Installation Assistance", "Computer Repair Service",
            "Printer Setup Service", "Telephone Service Activation", "Financial Advisory Meeting",
            "Legal Consultation", "Real Estate Inquiry", "Mortgage Consultation",
            "Insurance Claim Visit", "Tax Consultation", "Doctor's Home Visit",
            "Nursing Service Visit", "Childcare Pickup", "Tutoring Session", "Language Lesson",
            "Music Lesson", "Art Class Session", "Dance Lesson", "Fitness Training Session",
            "Yoga Class", "Meditation Session", "Business Meeting", "Job Interview",
            "Client Presentation", "Contract Signing", "Networking Event",
            "Community Association Meeting", "Homeowner Association Meeting",
            "Neighborhood Watch Meeting", "School Pickup", "After-School Activity Pickup",
            "Sports Coaching Session", "Personal Training Session", "Driver's License Appointment",
            "Vehicle Inspection Coordination", "Recycling Collection", "Courier Return Pickup",
            "Subscription Service Delivery", "Document Courier", "Library Book Delivery",
            "Art Exhibition Visit", "Cultural Event Participation", "Concert Visit",
            "Theater Group Visit", "Museum Tour", "Historical Site Visit",
            "Religious Service Visit", "Community Service Engagement", "Volunteer Coordination",
            "Charity Event Setup", "Fundraising Meeting", "Political Campaign Visit",
            "Press Conference Attendance", "Media Interview Setup", "Marketing Event Coordination",
            "Product Launch Meeting", "Sales Presentation", "Customer Service Follow-Up",
            "Warranty Claim Visit", "Quality Assurance Inspection", "Trade Show Setup",
            "Conference Room Booking", "Employee Training Session", "Corporate Event Visit"
        ]

        for _ in range(10):
            user_id = random.choice(resident_ids)
            verification_code = generate_verification_code()
            visit_datetime = fake.date_time_between(start_date='-30d', end_date='now')
            purpose = random.choice(purposes)
            status = random.choice(['PENDING', 'APPROVED', 'REJECTED', 'PROGRESS', 'COMPLETED', 'CANCELLED'])
            remarks = fake.sentence(nb_words=6)
            unit_number = resident_unit_map[user_id]

            num_visitors = random.randint(1, 5)
            request_id = insert_visit_request(
                cursor,
                user_id,
                verification_code,
                visit_datetime,
                purpose,
                status,
                remarks,
                unit_number,
                num_visitors
            )

            # Insert visitor records
            for _ in range(num_visitors):
                sec_id = random.choice(security_staff_ids)
                visitor_name = fake.name()
                visitor_ic = ''.join(random.choices(string.ascii_uppercase + string.digits, k=12))
                visitor_phone = generate_malaysia_phone_number()
                check_in_time = visit_datetime + timedelta(minutes=random.randint(0, 30))
                check_out_time = check_in_time + timedelta(hours=random.randint(1, 3))
                visitor_remarks = fake.sentence(nb_words=8)

                insert_visitor_record(
                    cursor,
                    request_id,
                    sec_id,
                    visitor_name,
                    visitor_ic,
                    visitor_phone,
                    check_in_time,
                    check_out_time,
                    visitor_remarks
                )

        cnx.commit()

        # 8. Insert Calendar Events
        print("Inserting Calendar Events...")
        for user_id in resident_ids[:5]:
            # Example event 1
            start1 = fake.date_time_between(start_date='-7d', end_date='now')
            insert_calendar_event(
                cursor,
                user_id,
                "Family Dinner",
                "Monthly family gathering",
                start1,
                start1 + timedelta(hours=3),
                False,
                "https://family-calendar.com",
                "#CB4335",
                None,
                "NORMAL",
                "ACTIVE"
            )

            # Example event 2 (background)
            start2 = fake.date_time_between(start_date='-14d', end_date='-7d')
            insert_calendar_event(
                cursor,
                user_id,
                "Vacation Period",
                "Out of town vacation",
                start2,
                start2 + timedelta(days=5),
                True,
                None,
                None,
                "lightgreen",
                "BACKGROUND",
                "COMPLETED"
            )

            # Example event 3
            start3 = fake.date_time_between(start_date='+3d', end_date='+7d')
            insert_calendar_event(
                cursor,
                user_id,
                "Maintenance Appointment",
                "Annual HVAC maintenance check",
                start3,
                start3 + timedelta(hours=2),
                False,
                None,
                "#27AE60",
                None,
                "NORMAL",
                "ACTIVE"
            )

        cnx.commit()

        # Final Output
        print("Data generation and insertion completed successfully.")
        print("\n--- Test Users ---")
        print(
            f"Super Admin:\n  Username: {super_username}\n  Email: {super_email}\n  Department: Administration\n  Position: Super Admin")
        print(f"Managing Staff:\n  Username: {managing_username}\n  Email: {managing_email}")
        print("Security Staff:")
        print(f"  Username: {security_username}\n  Email: {security_email}")
        print("  (Additional Security Staff added with random data)")
        print("Residents:")
        print(f"  Username: {resident_username}\n  Email: {resident_email}")
        print("  (Additional Residents added with random data)")

    except mysql.connector.Error as err:
        print(f"Error: {err}")
    finally:
        cursor.close()
        cnx.close()


if __name__ == "__main__":
    main()
