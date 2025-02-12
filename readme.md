# HostelGuard™ - Apply. Approve. Arrive. So easy.

A web-based Visitor Verification System.

https://github.com/user-attachments/assets/440058af-d7d9-472f-9d3f-5a79bf7ae4eb

## Features

### General Features
- Multi-factor Authentication (TOTP/SMS/Email)
- Password management & reset functionality
- Session management with device tracking
- Profile management with photo uploads
- Role-based access control (Resident/Security/Admin)

### Admin Features
- User management (CRUD operations)
- Role assignment & profile configuration
- Visitor activity analytics dashboard
- Security staff performance monitoring
- Shift distribution visualization
- Visit request status tracking

### Security Staff Features
- Visitor onboarding workflow
- QR code verification system
- Shift schedule management
- Real-time visitor records
- Bulk check-in/check-out operations

### Resident Features
- Visit request creation/management
- QR code generation for visitors
- Unit number management
- Visitor access time scheduling
- Request status notifications

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
3. JDBC Setup (Wildfly -> Configuration -> Subsystems -> Datasources & Drivers) 
- Name: mysql
- Driver Module name: com.mysql
- Driver Class Name com.mysql.cj.jdbc.Driver
4. Datasource (setup)
5. WildFly Settings (IntelliJ):
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
