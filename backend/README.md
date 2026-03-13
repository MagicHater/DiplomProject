# Backend module

## Run backend with PostgreSQL locally

1. Start PostgreSQL (example Docker command below).
2. Run backend:

```bash
./gradlew :backend:bootRun
```

By default, backend uses PostgreSQL from `application.yml`:

- `jdbc:postgresql://localhost:5432/adaptive_testing`
- user: `postgres`
- password: `postgres`

You can override with env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

## Start PostgreSQL in Docker

### PowerShell (Windows)

> В PowerShell перенос строк делается символом `` ` `` (backtick), а не `\`.

```powershell
docker run --name adaptive-testing-postgres `
  -e POSTGRES_DB=adaptive_testing `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_PASSWORD=postgres `
  -p 5432:5432 `
  -d postgres:16
```

### Bash (Linux/macOS/Git Bash)

```bash
docker run --name adaptive-testing-postgres \
  -e POSTGRES_DB=adaptive_testing \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

### One-line command (works in any shell)

```bash
docker run --name adaptive-testing-postgres -e POSTGRES_DB=adaptive_testing -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16
```

If container already exists:

```bash
docker rm -f adaptive-testing-postgres
```

## Verify Flyway migrations

When backend starts, Flyway runs automatically and creates tables from `db/migration/V1__init.sql`.

Check in DB:

```bash
psql "postgresql://postgres:postgres@localhost:5432/adaptive_testing" -c "\dt"
psql "postgresql://postgres:postgres@localhost:5432/adaptive_testing" -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank;"
psql "postgresql://postgres:postgres@localhost:5432/adaptive_testing" -c "SELECT id, name FROM roles ORDER BY name;"
```
