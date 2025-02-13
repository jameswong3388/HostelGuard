# HostelGuard™ - Apply. Approve. Arrive. So easy.

A web-based Visitor Verification System.

![](assets/Safari.mp4)

## Features

### Core System Features
- Multi-factor Authentication (TOTP/SMS/Email) with recovery codes
- Secure password storage using SHA-256 hashing with individual salts
- Multi-device session management with geolocation tracking and device fingerprinting
- Role-based access control (Resident/Security/Admin) with hierarchical permissions
- File upload handling with content validation
- Rate limiting for authentication endpoints

### Security Features
- QR code generation/validation system for visitor verification
- IP address whitelisting/blacklisting capabilities
- Automated session expiration and idle timeout
- Secure cookie handling with HTTP-only flags
- Password complexity enforcement and history tracking
- Bulk operation rate limiting for security endpoints

### Admin Features
- User lifecycle management (CRUD + activation/deactivation)
- Real-time visitor analytics with Chart.js integration
- Shift management with visual distribution charts
- System configuration management (email, security policies)
- Media management with collection organization
- Data export capabilities (Excel, PDF)
- Security staff performance metrics

### Security Staff Features
- Visitor onboarding workflow with photo capture
- Bulk check-in/check-out operations

### Resident Features
- Visit request management with status tracking
- QR code generation for pre-authorized visits
- Visitor time slot scheduling

### Technical Features
- PrimeFaces component library integration
- Mobile-responsive UI with adaptive layouts
- Database connection pooling with WildFly
- File storage abstraction with multiple collection support
- GeoIP2 integration for location tracking
- User agent parsing for device identification
- Caffeine caching for frequent queries

## Installation

### Prerequisites
- Java 17+
- Apache Maven
- Docker & Docker Compose
- WildFly 27+

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
- Run

## Usage

Access via: `https://your-domain:8443/HVVS-1.0-SNAPSHOT/`

| Role              | Default Landing Page       |
|-------------------|-----------------------------|
| Resident          | /resident/requests.xhtml    |
| Security Staff    | /security/onboard-visitors.xhtml |
| Admin             | /admin/dashboard.xhtml      |
