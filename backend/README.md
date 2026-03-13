# Backend module

## Run backend with PostgreSQL locally (WORKING RESET FLOW)

Запускать из корня репозитория (`.../DiplomProject`).

### Основная команда (Windows PowerShell)

```powershell
./backend/scripts/start-local-backend.ps1
```

### Основная команда (Linux/macOS/Git Bash)

```bash
./backend/scripts/start-local-backend.sh
```

Эти скрипты делают **полный reset локальной Postgres-среды** (для dev), чтобы убрать ошибку `SQL State 28P01`:
1. Останавливают compose-стек.
2. Удаляют volume (сбрасывают старые креды/данные).
3. Поднимают новый Postgres с `postgres/postgres`.
4. Запускают backend с явными `DB_URL/DB_USERNAME/DB_PASSWORD`.

> Важно: reset удаляет локальные данные в Postgres volume.

## Why it failed before

Ошибка `SQL State 28P01` означает, что пароль пользователя `postgres` в уже существующей БД не совпадал с тем, что backend пытается использовать. У Postgres пароль задаётся при первой инициализации volume, потом простая смена env уже не применится.

## Manual commands (if needed)

### 1) Hard reset Postgres (compose + volume)

```bash
docker compose -f backend/docker-compose.yml down -v --remove-orphans
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

```bash
docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Ports}}"
```

Если есть другой контейнер на `5432`, останови его.

## Swagger / OpenAPI (without PostgreSQL)

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=swagger'
```

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`
