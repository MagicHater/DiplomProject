# Backend module

## Profiles

By default backend starts with `local` profile (in-memory H2), so `:backend:bootRun` works without PostgreSQL.

- `local` (default): H2 in-memory DB, Flyway disabled.
- `postgres`: PostgreSQL + Flyway migrations.

## Quick start (default local profile)

```bash
./gradlew :backend:bootRun
```

## Run with PostgreSQL profile

Linux/macOS:

```bash
export SPRING_PROFILES_ACTIVE=postgres
export DB_URL=jdbc:postgresql://localhost:5432/adaptive_testing
export DB_USERNAME=postgres
export DB_PASSWORD=your_real_password
./gradlew :backend:bootRun
```

PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="postgres"
$env:DB_URL="jdbc:postgresql://localhost:5432/adaptive_testing"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_real_password"
./gradlew :backend:bootRun
```

If PostgreSQL credentials are wrong, startup fails with SQL state `28P01` (authentication failed).
