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