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

По умолчанию скрипты **не удаляют Postgres volume**, поэтому зарегистрированные пользователи и другие данные сохраняются между перезапусками backend.

Что делают скрипты по умолчанию:
1. Останавливают compose-стек (без удаления volume).
2. Поднимают Postgres с текущим volume на порту `5433`.
3. Запускают backend с явными `DB_URL/DB_USERNAME/DB_PASSWORD`.

Если нужен полный reset (удалить данные), запускай скрипт с `RESET_DB=1`.

Linux/macOS/Git Bash:
```bash
RESET_DB=1 ./backend/scripts/start-local-backend.sh
```

PowerShell:
```powershell
$env:RESET_DB = "1"
./backend/scripts/start-local-backend.ps1
```

### Почему порт 5433

Мы специально используем `localhost:5433`, чтобы не попасть в локально установленный Postgres на `5432` и гарантированно ходить именно в контейнер Docker.

## Why it failed before

Ошибка `SQL State 28P01` (или подключение не к тому инстансу Postgres на 5432) означает, что пароль пользователя `postgres` в уже существующей БД не совпадал с тем, что backend пытается использовать. У Postgres пароль задаётся при первой инициализации volume, потом простая смена env уже не применится. Для этого случая можно сделать разовый запуск с `RESET_DB=1`.

## Manual commands (if needed)

### 1) Hard reset Postgres (compose + volume)

```bash
docker compose -f backend/docker-compose.yml down -v --remove-orphans
docker compose -f backend/docker-compose.yml up -d postgres
```

### 2) Start backend with explicit DB env

```bash
DB_URL=jdbc:postgresql://localhost:5433/adaptive_testing DB_USERNAME=postgres DB_PASSWORD=postgres ./gradlew :backend:bootRun
```

PowerShell equivalent:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5433/adaptive_testing"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
./gradlew :backend:bootRun
```

## If port 5433 is busy

```bash
docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Ports}}"
```

Если есть другой контейнер на `5433`, останови его.

## Swagger / OpenAPI (without PostgreSQL)

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=swagger'
```

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`

## API contracts

- Controller API contract: `backend/docs/controller-api-contract.md`


## Flyway checksum mismatch after edited migration (local dev)

If you changed an already-applied migration (for example `V2__adaptive_testing_model.sql`) and got:

```
Migration checksum mismatch for migration version 2
```

use a **full local DB reset**. This removes local Postgres data and lets Flyway apply migrations again from an empty database.

PowerShell (Windows):

```powershell
$env:RESET_DB = "1"
./backend/scripts/start-local-backend.ps1
```

After the reset run, you can return to normal launches (without `RESET_DB`) and data will persist as before.
