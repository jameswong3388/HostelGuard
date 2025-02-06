# HostelGuard™ - Apply. Approve. Arrive. So easy.

A web-based Hostel Visitor Verification System designed for APU Hostel Management Office.

https://github.com/user-attachments/assets/440058af-d7d9-472f-9d3f-5a79bf7ae4eb

## Setup Instructions

### To use Database

Boot up mysql server
```bash
docker compose up -d 
```

### Setup Wildfly server (IntelliJ Idea COnfiguration)

1. Add new configuration
2. Select Wildfly
3. Add URL `https://localhost:8443/HVVS-1.0-SNAPSHOT/` to Server section
4. Add `HVVS:war exploded` to Deployment section
5. Run

### Start Wildfly Server (Optional)
/opt/homebrew/opt/wildfly-as/libexec/bin/standalone.sh

mvn package verify
