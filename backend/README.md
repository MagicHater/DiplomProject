# Backend module

## Run backend with PostgreSQL locally

Основная команда (поднимает PostgreSQL и запускает backend):

```bash
docker compose -f backend/docker-compose.yml up -d postgres && ./gradlew :backend:bootRun
```

Если PostgreSQL уже запущен, достаточно:

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


## Swagger / OpenAPI

Для локальной проверки Swagger без внешней PostgreSQL запустите backend с профилем `swagger`
(используется in-memory H2, без подключения к внешней БД):

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=swagger'
```

Swagger UI доступен локально:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/swagger-ui/index.html`

OpenAPI JSON:

- `http://localhost:8080/v3/api-docs`

Если запускаете без профиля `swagger`, убедитесь что корректно заданы
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, иначе приложение не поднимется из-за ошибки подключения к PostgreSQL.

Для Swagger в security-конфигурации разрешены публичные эндпоинты:
`/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`.
