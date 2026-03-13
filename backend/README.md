# Backend module

## Run locally

1. Ensure PostgreSQL is running and create DB `adaptive_testing`.
2. Set environment variables for your local DB credentials:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/adaptive_testing
export DB_USERNAME=postgres
export DB_PASSWORD=your_real_password
```

On PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/adaptive_testing"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_real_password"
```

3. Start backend from project root:

```bash
./gradlew :backend:bootRun
```

If credentials are wrong, startup fails with PostgreSQL SQL state `28P01` (authentication failed).
