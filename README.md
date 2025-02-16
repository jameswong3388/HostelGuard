# HostelGuard™ - Apply. Approve. Arrive. So easy.

A web-based Visitor Verification System.

https://github.com/user-attachments/assets/94c52765-d23f-4e6f-8851-109621c8119c

## Features

### Core System Features (Shared)
- Multi-factor Authentication (TOTP/SMS/Email) with recovery codes
- Secure password storage using SHA-256 hashing with individual salts
- Multi-device session management with geolocation tracking and device fingerprinting
- Role-based access control (Resident/Security/Admin) with hierarchical permissions
- File upload handling with content validation and multiple storage collections
- Rate limiting for authentication endpoints with bulk operation protection
- Lazy loading data tables with server-side filtering, sorting and global search
- Automatic session timeout with configurable duration and warning dialog
- Real-time notification system with multi-channel delivery

### Security Features
- QR code generation/validation system for visitor verification
- IP address tracking with GeoIP2 integration
- Session invalidation on password change/role modification
- Secure cookie handling with HTTP-only flags and encryption
- Password complexity enforcement with historical password tracking
- Device fingerprinting for session validation
- Parallel session prevention

### Admin Features
- Full user lifecycle management (CRUD + activation/deactivation)
- Real-time visitor analytics with interactive Chart.js visualizations
- Shift management with staff distribution charts
- Media management with collection-based organization (profile pics, docs, etc)
- Data export capabilities (Excel, PDF) with dynamic filtering
- Security staff performance metrics dashboard

### Security Staff Features
- Visitor onboarding workflow with photo capture and validation
- Bulk check-in/check-out operations with QR verification
- Real-time visitor record updates
- Visit request status tracking with notifications

### Resident Features
- Visit request management with status tracking and comments
- QR code generation for pre-authorized visits with time slots
- Visitor scheduling calendar integration
- Historical visit records access

## Installation

### Prerequisites
- Java 21+
- Apache Maven
- Docker & Docker Compose
- WildFly 34+

### Setup

1. Database Setup:

Boot up mysql server
```bash
docker compose up -d 
```
2. Add new module in Wildfly directory
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
3. JDBC Driver Setup (Wildfly -> Configuration -> Subsystems -> Datasources & Drivers)
- Name: mysql
- Driver Module name: com.mysql
- Driver Class Name com.mysql.cj.jdbc.Driver
4. JDBC Datasource Setup
5. Create env (Optional)
- Create `application.properties` and place in `META-INF/application.properties`
- Copy below to apply
```text
# Email Configuration
mail.smtp.host=
mail.smtp.port=587
mail.smtp.username=
mail.smtp.password=
mail.smtp.from=
mail.smtp.auth=true
mail.smtp.starttls.enable=false
```
6. WildFly Settings (IntelliJ):
- Add new configuration
- Select Wildfly
- Add URL `https://localhost:8443/HVVS-1.0-SNAPSHOT/` to Server section
- Add `HVVS:war exploded` to Deployment section
7. Setup GeoLite
- Download `GeoLite2-City.mmdb` from https://github.com/P3TERX/GeoLite.mmdb
- Place `GeoLite2-City.mmdb` in `src/main/webapp/WEB-INF/GeoLite2-City.mmdb`
8. Run

## Usage

Access via: `https://your-domain:8443/HVVS-1.0-SNAPSHOT/`

| Role           | Default Landing Page             |
|----------------|----------------------------------|
| Resident       | /resident/requests.xhtml         |
| Security Staff | /security/onboard-visitors.xhtml |
| Admin          | /admin/dashboard.xhtml           |
| Super Admin    | /god/users.xhtml                 |
