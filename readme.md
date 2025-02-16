# HostelGuard™ - Apply. Approve. Arrive. So easy.

A web-based Visitor Verification System.

https://github.com/user-attachments/assets/94c52765-d23f-4e6f-8851-109621c8119c

## Features

### Core System Features
- Multi-factor Authentication (TOTP/SMS/Email) with recovery codes
- Secure password storage using SHA-256 hashing with individual salts
- Multi-device session management with geolocation tracking and device fingerprinting
- Role-based access control (Resident/Security/Admin) with hierarchical permissions
- File upload handling with content validation
- Rate limiting for authentication endpoints
- Lazy loading data tables with server-side filtering and sorting

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
- LazyDataModel implementation for efficient data retrieval
- Server-side filtering and sorting for improved performance

## Installation

### Prerequisites
- Java 17+
- Apache Maven
- Docker & Docker Compose
- WildFly 27+

### Setup

1. Database Setup:

Boot up mysql server
