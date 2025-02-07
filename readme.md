# HostelGuard™ - Apply. Approve. Arrive. So easy.

A web-based Hostel Visitor Verification System for APU Hostel Management Office.

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

2. WildFly Settings:
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
