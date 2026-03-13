# Backend module

## Run backend with PostgreSQL locally

Запускать из корня репозитория (`.../DiplomProject`).

### Основная команда (Windows PowerShell)

```powershell
./backend/scripts/start-local-backend.ps1
```

### Основная команда (Linux/macOS/Git Bash)

```bash
./backend/scripts/start-local-backend.sh
```

Скрипт делает 3 вещи:
1. Удаляет legacy-контейнер `adaptive-testing-postgres` (если остался от старых команд).
2. Поднимает PostgreSQL через `backend/docker-compose.yml`.
3. Запускает backend с явными `DB_URL/DB_USERNAME/DB_PASSWORD`.

> Почему у тебя падало раньше: в логе `SQL State 28P01` — это ошибка авторизации Postgres (неверный пароль для пользователя `postgres`).

## Manual commands (if needed)

### 1) Start PostgreSQL

```bash
docker compose -f backend/docker-compose.yml up -d postgres
```

### 2) Start backend with explicit DB env

```bash
DB_URL=jdbc:postgresql://localhost:5432/adaptive_testing DB_USERNAME=postgres DB_PASSWORD=postgres ./gradlew :backend:bootRun
```

PowerShell equivalent:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/adaptive_testing"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
./gradlew :backend:bootRun
```

## If port 5432 is busy

Проверь, не запущен ли другой Postgres, и останови его:

```bash
docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Ports}}"
```

Если видишь лишний контейнер на `0.0.0.0:5432->5432/tcp`, останови/удали его.

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
