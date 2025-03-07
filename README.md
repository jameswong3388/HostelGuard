# HostelGuard™ - Apply. Approve. Arrive. So easy.

A web-based Visitor Verification System.

https://github.com/user-attachments/assets/94c52765-d23f-4e6f-8851-109621c8119c

## Features

### Core System Features (Shared)
- Multi-factor Authentication (TOTP/SMS/Email) with recovery codes
- Secure password storage using HMAC-SHA256 with individual salts and server-side pepper
- Secure password reset with email verification, time-limited tokens and progressive rate limiting
- Multi-device session management with IP address tracking with GeoLite integration
- Role-based access control (Resident/Security/Managing staff/Super admin) with hierarchical permissions
- File upload handling with content validation and multiple storage collections
- Rate limiting for authentication endpoints with bulk operation protection
- Lazy loading data tables with server-side filtering, sorting and global search
- Real-time notification system with multi-channel delivery

### Admin Features
- User management (CRUD)
- Visit Request management (CRUD)
- Visitor Records management (CRUD)
- Dashboard with user statistics, visit request metrics, and status visualizations
- Calendar (Coming soon)
- Media management with collection-based organization (profile pics, docs, etc)
- Data export capabilities (CSV, Excel, PDF)

### Security Staff Features
- Visitor onboarding workflow with photo capture and validation
- check-in/check-out operations with QR verification

### Resident Features
- Visit request management with status tracking and comments
- QR code generation for pre-authorized visits with time slots

## Installation

### Prerequisites
- Java 21+
- Apache Maven
- Docker & Docker Compose
- WildFly 34+

### Setup

1. Database Setup:

Boot mysql server up and down 
```bash
docker compose up -d
docker compose down 
```
2. Seed data (Optional)
```shell
python seeder.py
```
3. Add new module in Wildfly directory
- Create `/mysql/main` in `/opt/homebrew/Cellar/wildfly-as/35.0.1/libexec/modules/system/layers/base/com`
- Download `mysql-connector-j-8.0.33.jar` and place in `/opt/homebrew/Cellar/wildfly-as/35.0.1/libexec/modules/system/layers/base/com/mysql/main/mysql-connector-j-8.0.33.jar`
- Create `module.xml` and place in `/opt/homebrew/Cellar/wildfly-as/35.0.1/libexec/modules/system/layers/base/com/mysql/main/`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.5" name="com.mysql">
    <resources>
        <resource-root path="mysql-connector-j-8.0.33.jar"/>
    </resources>
    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>
```
4. JDBC Driver Setup (Wildfly -> Configuration -> Subsystems -> Datasources & Drivers)
- Name: mysql
- Driver Module name: com.mysql
- Driver Class Name com.mysql.cj.jdbc.Driver
5. JDBC Datasource Setup
6. Create env (Optional)
- Create `application.properties` and place in `META-INF/application.properties`
- Copy below to apply
```text
# Email Configuration (Required for password reset functionality)
mail.smtp.host=
mail.smtp.port=587
mail.smtp.username=
mail.smtp.password=
mail.smtp.from=
mail.smtp.auth=true
mail.smtp.starttls.enable=false
```
7. WildFly Settings (IntelliJ):
- Add new configuration
- Select Wildfly
- Add URL `https://localhost:8443/HVVS-1.0-SNAPSHOT/` to Server section
- Add `HVVS:war exploded` to Deployment section
8. Setup GeoLite
- Download `GeoLite2-City.mmdb` from https://github.com/P3TERX/GeoLite.mmdb
- Place `GeoLite2-City.mmdb` in `src/main/webapp/WEB-INF/GeoLite2-City.mmdb`
9. Run

## Usage

Access via: `https://your-domain:8443/HVVS-1.0-SNAPSHOT/`

| Role           | Default Landing Page             |
|----------------|----------------------------------|
| Resident       | /resident/requests.xhtml         |
| Security Staff | /security/onboard-visitors.xhtml |
| Managing Staff | /admin/dashboard.xhtml           |
| Super Admin    | /god/users.xhtml                 |

### Password Reset
Access the password reset functionality at: `https://your-domain:8443/HVVS-1.0-SNAPSHOT/auth/password-reset`

Users can request a password reset by entering their email address. A secure time-limited token will be sent to their email with instructions to complete the reset process.
