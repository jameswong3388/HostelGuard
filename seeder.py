import mysql.connector
from mysql.connector import errorcode
from faker import Faker
import random
import string
from datetime import timedelta
import uuid  # Add UUID import

# Configuration for MySQL connection
DB_CONFIG = {
    'user': 'root',  # Replace with your MySQL username
    'password': 'password',  # Replace with your MySQL password
    'host': '127.0.0.1',  # Replace with your MySQL host
    'database': 'app',  # Replace with your MySQL database name
    'raise_on_warnings': True,
    'port': 3308
}

# Initialize Faker with Malaysian locale
fake = Faker('en_MS')

# Fixed salt and password values
FIXED_SALT = 'FTUa8P#OT7N8d>o3'
FIXED_PASSWORD = 'F3A90A00BF8D619364F2059DED59AB2DD1FE4EA7B527A2713D2F876403A863E1'


def generate_unique_username(existing_usernames, first_name, last_name):
    base_username = f"{first_name.lower()}.{last_name.lower()}"
    username = base_username
    counter = 1
    while username in existing_usernames:
        username = f"{base_username}{counter}"
        counter += 1
    existing_usernames.add(username)
    return username


def generate_unique_email(existing_emails, username):
    domain = fake.free_email_domain()
    email = f"{username}@{domain}"
    counter = 1
    while email in existing_emails:
        email = f"{username}{counter}@{domain}"
        counter += 1
    existing_emails.add(email)
    return email


def generate_unique_identity(existing_ids):
    while True:
        identity = ''.join(random.choices(string.digits, k=12))
        if identity not in existing_ids:
            existing_ids.add(identity)
            return identity


def generate_unit_number():
    building = random.choice(string.ascii_uppercase)
    floor = random.randint(1, 20)
    unit = random.randint(1, 30)
    return f"{building}-{floor:02d}-{unit:02d}"


def generate_badge_number(existing_badges):
    while True:
        badge = f"SEC{random.randint(100, 999)}"
        if badge not in existing_badges:
            existing_badges.add(badge)
            return badge


def generate_verification_code(existing_codes):
    while True:
        code = str(uuid.uuid4())  # Generate a full UUID
        if code not in existing_codes:
            existing_codes.add(code)
            return code


def generate_malaysia_phone_number():
    """
    Generates a Malaysian mobile phone number in the format +60XXXXXXXXXX
    where X is a digit and the first digit after +60 is 1, 3, 4, 5, 6, 7, or 9.
    """
    prefix = '+60'
    second_digit = random.choice(['1', '3', '4', '5', '6', '7', '9'])
    remaining_digits = ''.join(random.choices(string.digits, k=8))
    return f"{prefix}{second_digit}{remaining_digits}"


def main():
    try:
        # Connect to the MySQL database
        cnx = mysql.connector.connect(**DB_CONFIG)
        cursor = cnx.cursor()

        # Define the DROP TABLE statements in reverse order of dependencies
        drop_tables = """
        DROP TABLE IF EXISTS visitor_records;
        DROP TABLE IF EXISTS visit_requests;
        DROP TABLE IF EXISTS security_staff_profiles;
        DROP TABLE IF EXISTS resident_profiles;
        DROP TABLE IF EXISTS managing_staff_profiles;
        DROP TABLE IF EXISTS mfa_methods;
        DROP TABLE IF EXISTS user_sessions;
        DROP TABLE IF EXISTS users;
        DROP TABLE IF EXISTS medias;
        """

        # Execute DROP TABLE statements
        print("Dropping existing tables...")
        for statement in drop_tables.strip().split(';'):
            stmt = statement.strip()
            if stmt:
                cursor.execute(stmt)
        cnx.commit()

         # Define the CREATE TABLE statements
        create_tables = """
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
            CHECK (role IN ('RESIDENT', 'SECURITY_STAFF', 'MANAGING_STAFF'))
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

        -- Create indexes for better query performance
        CREATE INDEX idx_user_sessions_user ON user_sessions (user_id);
        CREATE INDEX idx_user_sessions_expires ON user_sessions (expires_at);
        CREATE INDEX idx_user_sessions_last_access ON user_sessions (last_access);

        CREATE INDEX idx_visit_requests_verification_code ON visit_requests (verification_code);
        CREATE INDEX idx_visit_requests_status ON visit_requests (status);
        CREATE INDEX idx_visit_requests_user_status ON visit_requests (user_id, status);

        CREATE INDEX idx_resident_profiles_user_id ON resident_profiles (user_id);
        CREATE INDEX idx_security_staff_profiles_user_id ON security_staff_profiles (user_id);
        CREATE INDEX idx_managing_staff_profiles_user_id ON managing_staff_profiles (user_id);

        CREATE INDEX idx_medias_model_id ON medias (model_id);
        CREATE INDEX idx_medias_collection ON medias (collection);

        CREATE INDEX idx_mfa_methods_user ON mfa_methods (user_id);
        CREATE INDEX idx_mfa_methods_user_method ON mfa_methods (user_id, method);
        """

        # Execute CREATE TABLE statements
        print("Creating tables...")
        for statement in create_tables.strip().split(';'):
            stmt = statement.strip()
            if stmt:
                cursor.execute(stmt)
        cnx.commit()

        # Define the INSERT statement for users including new columns
        add_user = ("INSERT INTO users "
                    "(username, salt, password, email, first_name, last_name, phone_number, identity_number, address, gender, role, is_mfa_enable, is_active) "
                    "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)")

        # Prepare to track unique fields
        existing_usernames = set()
        existing_emails = set()
        existing_identity_numbers = set()
        existing_badge_numbers = set()
        existing_verification_codes = set()

        # Insert Managing Staff
        print("Inserting Managing Staff...")
        # Define fixed credentials for testing
        managing_username = "manager.test"
        managing_email = "jameswong0517@gmail.com"
        existing_usernames.add(managing_username)
        existing_emails.add(managing_email)

        first_name = "TestManager"
        last_name = "User"
        username = managing_username  # Fixed username
        email = managing_email        # Fixed email
        phone_number = generate_malaysia_phone_number()
        identity_number = generate_unique_identity(existing_identity_numbers)
        address = "123 Test Street, Test City, Test State"
        gender = "Other"  # Fixed gender for testing
        # Fixed salt and password
        salt = FIXED_SALT
        password = FIXED_PASSWORD
        is_mfa_enable = False

        managing_staff_data = (
            username, salt, password, email, first_name, last_name,
            phone_number, identity_number, address, gender, 'MANAGING_STAFF',
            is_mfa_enable, False
        )
        cursor.execute(add_user, managing_staff_data)
        managing_staff_id = cursor.lastrowid

        # Insert into managing_staff_profiles
        add_managing_profile = ("INSERT INTO managing_staff_profiles "
                                "(user_id, department, position) "
                                "VALUES (%s, %s, %s)")
        department = "Management"
        position = "Manager"
        managing_profile_data = (managing_staff_id, department, position)
        cursor.execute(add_managing_profile, managing_profile_data)

        # Insert Security Staff
        print("Inserting Security Staff...")
        security_staff_ids = []

        # Insert fixed Security Staff
        security_username = "security.test"
        security_email = "security.test@example.com"
        existing_usernames.add(security_username)
        existing_emails.add(security_email)

        first_name = "TestSecurity"
        last_name = "User"
        username = security_username  # Fixed username
        email = security_email        # Fixed email
        phone_number = generate_malaysia_phone_number()
        identity_number = generate_unique_identity(existing_identity_numbers)
        address = "456 Security Avenue, Secure City, Secure State"
        gender = "Male"  # Fixed gender for testing
        # Fixed salt and password
        salt = FIXED_SALT
        password = FIXED_PASSWORD
        is_mfa_enable = False

        security_staff_data = (
            username, salt, password, email, first_name, last_name,
            phone_number, identity_number, address, gender, 'SECURITY_STAFF',
            is_mfa_enable, False
        )
        cursor.execute(add_user, security_staff_data)
        security_staff_id = cursor.lastrowid
        security_staff_ids.append(security_staff_id)

        # Insert into security_staff_profiles
        badge_number = generate_badge_number(existing_badge_numbers)
        shift = 'MORNING'  # Fixed shift for testing
        add_security_profile = ("INSERT INTO security_staff_profiles "
                                "(user_id, badge_number, shift) "
                                "VALUES (%s, %s, %s)")
        security_profile_data = (security_staff_id, badge_number, shift)
        cursor.execute(add_security_profile, security_profile_data)

        # Insert additional random Security Staff (2 total including fixed)
        for i in range(2):
            first_name = fake.first_name()
            last_name = fake.last_name()
            username = generate_unique_username(existing_usernames, first_name, last_name)
            email = generate_unique_email(existing_emails, username)
            phone_number = generate_malaysia_phone_number()
            identity_number = generate_unique_identity(existing_identity_numbers)
            address = fake.address().replace('\n', ', ')
            gender = random.choice(['Male', 'Female', 'Other'])
            # Fixed salt and password
            salt = FIXED_SALT
            password = FIXED_PASSWORD
            is_mfa_enable = False

            security_staff_data = (
                username, salt, password, email, first_name, last_name,
                phone_number, identity_number, address, gender, 'SECURITY_STAFF',
                is_mfa_enable, False
            )
            cursor.execute(add_user, security_staff_data)
            new_security_id = cursor.lastrowid
            security_staff_ids.append(new_security_id)

            # Insert into security_staff_profiles
            badge_number = generate_badge_number(existing_badge_numbers)
            shift = random.choice(['MORNING', 'AFTERNOON', 'NIGHT'])
            security_profile_data = (new_security_id, badge_number, shift)
            cursor.execute(add_security_profile, security_profile_data)

        # Insert Residents
        print("Inserting Residents...")
        resident_ids = []
        resident_unit_map = {}

        # Insert fixed Resident
        resident_username = "resident.test"
        resident_email = "resident.test@example.com"
        existing_usernames.add(resident_username)
        existing_emails.add(resident_email)

        first_name = "TestResident"
        last_name = "User"
        username = resident_username  # Fixed username
        email = resident_email        # Fixed email
        phone_number = generate_malaysia_phone_number()
        identity_number = generate_unique_identity(existing_identity_numbers)
        address = "789 Resident Lane, Resident City, Resident State"
        gender = "Female"  # Fixed gender for testing
        # Fixed salt and password
        salt = FIXED_SALT
        password = FIXED_PASSWORD
        is_mfa_enable = False  

        resident_data = (
            username, salt, password, email, first_name, last_name,
            phone_number, identity_number, address, gender, 'RESIDENT',
            is_mfa_enable, False
        )
        cursor.execute(add_user, resident_data)
        resident_id = cursor.lastrowid
        resident_ids.append(resident_id)

        # Insert into resident_profiles
        add_resident_profile = ("INSERT INTO resident_profiles "
                                "(user_id, unit_number) "
                                "VALUES (%s, %s)")
        unit_number = generate_unit_number()
        resident_unit_map[resident_id] = unit_number
        resident_profile_data = (resident_id, unit_number)
        cursor.execute(add_resident_profile, resident_profile_data)

        # Insert additional random Residents (19 total including fixed)
        for i in range(19):
            first_name = fake.first_name()
            last_name = fake.last_name()
            username = generate_unique_username(existing_usernames, first_name, last_name)
            email = generate_unique_email(existing_emails, username)
            phone_number = generate_malaysia_phone_number()
            identity_number = generate_unique_identity(existing_identity_numbers)
            address = fake.address().replace('\n', ', ')
            gender = random.choice(['Male', 'Female', 'Other'])
            # Fixed salt and password
            salt = FIXED_SALT
            password = FIXED_PASSWORD
            is_mfa_enable = False

            resident_data = (
                username, salt, password, email, first_name, last_name,
                phone_number, identity_number, address, gender, 'RESIDENT',
                is_mfa_enable, False
            )
            cursor.execute(add_user, resident_data)
            new_resident_id = cursor.lastrowid
            resident_ids.append(new_resident_id)

            # Insert into resident_profiles
            unit_number = generate_unit_number()
            resident_unit_map[new_resident_id] = unit_number
            resident_profile_data = (new_resident_id, unit_number)
            cursor.execute(add_resident_profile, resident_profile_data)

        # Commit users and profiles
        cnx.commit()

        # Insert Visit Requests and Visitor Records
        print("Inserting Visit Requests and Visitor Records...")
        add_visit_request = ("INSERT INTO visit_requests "
                             "(user_id, verification_code, visit_datetime, purpose, status, remarks, unit_number, number_of_entries) "
                             "VALUES (%s, %s, %s, %s, %s, %s, %s, %s)")
        add_visitor_record = ("INSERT INTO visitor_records "
                              "(request_id, security_staff_id, visitor_name, visitor_ic, visitor_phone, check_in_time, check_out_time, remarks) "
                              "VALUES (%s, %s, %s, %s, %s, %s, %s, %s)")

        purposes = [
            'Friend Gathering', 'Package Delivery', 'Birthday Visit', 'Maintenance Check',
            'Friend Visit', 'Food Delivery', 'Family Reunion', 'Discussion', 'Party Visit', 'Friend Hangout'
        ]

        for i in range(10):
            user_id = random.choice(resident_ids)
            verification_code = generate_verification_code(existing_verification_codes)
            # Random visit datetime within the past 30 days
            visit_datetime = fake.date_time_between(start_date='-30d', end_date='now')
            purpose = random.choice(purposes)
            status = random.choice(['PENDING', 'APPROVED', 'REJECTED', 'PROGRESS', 'COMPLETED', 'CANCELLED'])
            remarks = fake.sentence(nb_words=6)
            unit_number = resident_unit_map[user_id]

            # Random number of visitors (1-5)
            num_visitors = random.randint(1, 5)

            visit_request_data = (
                user_id, verification_code, visit_datetime, purpose, status, remarks, unit_number, num_visitors
            )
            cursor.execute(add_visit_request, visit_request_data)
            request_id = cursor.lastrowid

            # Then loop num_visitors times to create visitor records
            for _ in range(num_visitors):
                security_staff_id = random.choice(security_staff_ids)
                visitor_name = fake.name()
                visitor_ic = ''.join(random.choices(string.ascii_uppercase + string.digits, k=12))
                visitor_phone = generate_malaysia_phone_number()
                # Check-in and check-out times around visit_datetime
                check_in_time = visit_datetime + timedelta(minutes=random.randint(0, 30))
                check_out_time = check_in_time + timedelta(hours=random.randint(1, 3))
                visitor_remarks = fake.sentence(nb_words=8)

                visitor_record_data = (
                    request_id,
                    security_staff_id,
                    visitor_name,
                    visitor_ic,
                    visitor_phone,
                    check_in_time,
                    check_out_time,
                    visitor_remarks
                )
                cursor.execute(add_visitor_record, visitor_record_data)

        # Commit all inserts
        cnx.commit()

        print("Data generation and insertion completed successfully.")
        print("\n--- Test Users ---")
        print(f"Managing Staff:\n  Username: {managing_username}\n  Email: {managing_email}")
        print(f"Security Staff:")
        print(f"  Username: {security_username}\n  Email: {security_email}")
        print(f"  (Additional Security Staff added with random data)")
        print(f"Residents:")
        print(f"  Username: {resident_username}\n  Email: {resident_email}")
        print(f"  (Additional Residents added with random data)")

    except mysql.connector.Error as err:
        print(f"Error: {err}")
    finally:
        cursor.close()
        cnx.close()


if __name__ == "__main__":
    main()
