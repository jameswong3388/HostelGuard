To use Database

1. Create a `secrets` directory:
```bash
mkdir secrets
```

2. Create secret files:
```bash
echo "your_root_password" > secrets/mysql_root_password.txt
echo "your_app_password" > secrets/mysql_password.txt
```

3. Create a custom MySQL configuration file (custom.cnf) if needed:
```bash
touch custom.cnf
```

4. Create an initialization SQL script if needed:
```bash
touch init.sql
```

erDiagram
Users ||--|| UserRoles : has
Users ||--o{ VisitRequests : creates
Users ||--o{ RequestStatus : updates
Residents ||--|{ VisitRequests : receives
SecurityStaff ||--o{ VisitorRecords : records
VisitRequests ||--|{ VisitorRecords : has

    Users {
        int user_id PK
        string username
        string password
        string email
        string full_name
        string phone_number
        datetime created_at
        datetime updated_at
        boolean is_active
    }

    UserRoles {
        int role_id PK
        string role_name
        string description
    }

    Residents {
        int resident_id PK
        int user_id FK
        string room_number
        string block_number
        boolean is_approved
        datetime approval_date
    }

    SecurityStaff {
        int staff_id PK
        int user_id FK
        string badge_number
        string shift
    }

    VisitRequests {
        int request_id PK
        int requester_id FK
        int resident_id FK
        string verification_code
        datetime visit_date
        string purpose
        string status
        datetime created_at
        datetime updated_at
    }

    VisitorRecords {
        int record_id PK
        int request_id FK
        int security_staff_id FK
        string visitor_name
        string visitor_ic
        string visitor_phone
        datetime check_in_time
        datetime check_out_time
        string remarks
    }

    RequestStatus {
        int status_id PK
        int request_id FK
        int updated_by_user_id FK
        string status
        string remarks
        datetime updated_at
    }