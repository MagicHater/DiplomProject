# Перенос проекта на другой компьютер

## 1) Подготовить исходный компьютер
1. Убедитесь, что все изменения закоммичены и отправлены в удалённый репозиторий:
   - `git status`
   - `git add . && git commit -m "..."`
   - `git push`
2. Сохраните локальные `.env`/секреты отдельно (их обычно нет в Git).
3. Зафиксируйте версии инструментов:
   - JDK (для Android и backend)
   - Android Studio + Android SDK
   - Gradle (используется wrapper, но JDK важен)
   - Docker/Docker Compose (если поднимаете PostgreSQL)

## 2) Подготовить новый компьютер
1. Установите:
   - Git
   - JDK 17 (рекомендуется для текущего Spring Boot/Gradle стека)
   - Android Studio (с Android SDK и эмулятором)
   - Docker Desktop (или Docker Engine + Compose)
2. Настройте доступ к GitHub/GitLab (SSH-ключ или token).

## 3) Клонировать проект
```bash
git clone <URL_репозитория>
cd DiplomProject
```

## 4) Настроить backend
1. Перейдите в `backend/`.
2. Создайте/проверьте локальную конфигурацию (`application-local.yml`, переменные среды, секреты JWT и доступ к БД).
3. Поднимите базу (если используете docker-compose):
   - `docker compose up -d`
   - при первом запуске на новой машине рекомендуется один раз выполнить с очисткой volume (`RESET_DB=1`), чтобы применились локальные правила auth из compose.
4. Запустите backend:
   - Linux/macOS: `./gradlew bootRun`
   - Windows: `gradlew.bat bootRun`

## 5) Настроить Android-приложение
1. Откройте корневую папку проекта в Android Studio.
2. Дождитесь синхронизации Gradle.
3. Проверьте endpoint API в клиенте (IP/host backend). Если backend работает локально, адрес должен быть доступен с эмулятора/устройства.
4. Запустите приложение на эмуляторе или устройстве.

## 6) Проверка после переноса
1. Backend health-check:
   - `GET /api/health` (или endpoint из вашего контроллера здоровья)
2. Проверка авторизации и основных сценариев:
   - регистрация/логин
   - прохождение теста
   - просмотр результатов
3. Прогон тестов:
   - backend: `cd backend && ../gradlew test` (или `./gradlew test` внутри backend)
   - app unit tests: `./gradlew test`


## Если backend не стартует с `SQL State: 28P01`
Это почти всегда проблема с паролем PostgreSQL (несовпадение пароля в volume и в переменных `DB_PASSWORD`).

Симптомы в логах:
- `Unable to obtain connection from database`
- `SQL State  : 28P01`
- ошибка аутентификации для пользователя `postgres`

Что сделать:
1. Полностью пересоздать контейнер **и volume** БД:
   - PowerShell: `$env:RESET_DB=1; ./backend/scripts/start-local-backend.ps1`
   - bash: `RESET_DB=1 ./backend/scripts/start-local-backend.sh`
2. Либо вручную удалить volume:
   - `docker compose -f backend/docker-compose.yml down -v`
   - затем снова: `./backend/scripts/start-local-backend.ps1` (или `.sh`)
3. Убедиться, что нет конфликтующих переменных среды:
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

Почему так происходит:
- Postgres инициализирует пароль только при первом создании data-volume.
- Если volume уже существовал с другим паролем, новый `POSTGRES_PASSWORD` в `docker-compose.yml` не применится к старым данным.

## Если ошибка 28P01 остаётся даже после `RESET_DB=1`
Проблема почти всегда в том, что backend подключается **не к тому** PostgreSQL (или к старому Docker volume в другом compose-проекте).

Проверьте по шагам:
1. Убедитесь, что именно docker-контейнер слушает порт `5433`:
   - `docker compose -f backend/docker-compose.yml ps`
   - `docker ps --format "table {{.Names}}	{{.Ports}}"`
2. Важно: проверка `docker compose ... exec postgres psql -U postgres ...` может проходить даже с неверным паролем,
   потому что это локальный socket внутри контейнера.
3. Проверьте именно парольную аутентификацию по TCP:
   - `docker compose -f backend/docker-compose.yml exec -e PGPASSWORD=postgres postgres psql -h 127.0.0.1 -U postgres -d adaptive_testing -c "select 1"`
   - Что считать успехом: в ответе есть строка `1 row`.
4. Если в п.3 получен успех (`1 row`), значит логин/пароль в контейнере корректны.
   Тогда сразу запускайте backend с теми же параметрами:
   - PowerShell:
     - `$env:DB_URL="jdbc:postgresql://localhost:5433/adaptive_testing"`
     - `$env:DB_USERNAME="postgres"`
     - `$env:DB_PASSWORD="postgres"`
     - `./gradlew :backend:bootRun --no-daemon`
5. Если команда из п.3 не проходит — удалите все связанные volume и поднимите заново:
   - `docker compose -f backend/docker-compose.yml down -v --remove-orphans`
   - `docker volume ls | findstr /I "pgdata adaptive"` (Windows)
   - `docker volume rm <имя_volume>` (если остались старые)
6. Если в п.3 всё ок, но backend всё равно падает с 28P01 — backend подключается к другому серверу:
   - проверьте системные переменные `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
   - временно задайте другой host-порт для контейнера (например `55433:5432`) и используйте
     `DB_URL=jdbc:postgresql://localhost:55433/adaptive_testing`.

Подсказка для Windows:
- иногда на машине уже установлен локальный PostgreSQL service, из-за чего легко перепутать, к какому экземпляру идёт подключение.


7. Если всё ещё `28P01`, исключите влияние переменных окружения и запустите backend с параметрами Spring напрямую:
   - PowerShell:
     - `./gradlew :backend:bootRun --no-daemon --args="--spring.datasource.url=jdbc:postgresql://localhost:5433/adaptive_testing --spring.datasource.username=postgres --spring.datasource.password=postgres"`

8. На Windows проверьте и очистите глобальные переменные среды (System/User), если они заданы:
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
   - `FLYWAY_URL`, `FLYWAY_USER`, `FLYWAY_PASSWORD`
   - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
   - `SPRING_APPLICATION_JSON`
   - `SPRING_FLYWAY_URL`, `SPRING_FLYWAY_USER`, `SPRING_FLYWAY_PASSWORD`

9. Перезапустите Docker Desktop после очистки env и повторите запуск из нового PowerShell-окна.
   - В новом скрипте при старте должна быть строка: `[meta] start-local-backend.ps1 v2 (DB+FLYWAY hard-sync)`.
   - Новый скрипт делает обязательный TCP-check внутри контейнера (`127.0.0.1:5432`).
   - host-path probe (`host.docker.internal:5433`) теперь warning-only: на некоторых Windows-конфигах он может падать, даже если backend затем стартует.

10. Если проблема остаётся, принудительно переустановите пароль `postgres` внутри контейнера и проверьте доступ с хоста:
   - `docker compose -f backend/docker-compose.yml exec postgres psql -U postgres -d postgres -c "ALTER USER postgres WITH PASSWORD 'postgres';"`
   - `docker run --rm -e PGPASSWORD=postgres postgres:16 psql -h host.docker.internal -p 5433 -U postgres -d adaptive_testing -c "select 1"`

11. Если шаг 10 не проходит, удалите ВСЕ старые volume проекта (в т.ч. с другим compose project name), затем поднимите БД заново:
   - `docker volume ls`
   - удалить связанные volume (`...pgdata...`, `...adaptive...`)
   - `docker compose -f backend/docker-compose.yml up -d postgres`

### Что означает ваш вывод
- `psql: ... password authentication failed for user "postgres"` (SQL State `28P01`) = вы подключились к Postgres, но пароль не совпадает.
- `service "postgres" is not running` = в момент части команд compose-сервис был не запущен; сначала надо поднять его.

### Рабочая последовательность (Windows, copy-paste)
```powershell
# 0) Гарантированно поднять postgres
 docker compose -f backend/docker-compose.yml up -d postgres
 docker compose -f backend/docker-compose.yml ps

# 1) Сбросить пароль postgres ВНУТРИ работающего контейнера
 docker compose -f backend/docker-compose.yml exec postgres psql -U postgres -d postgres -c "ALTER USER postgres WITH PASSWORD 'postgres';"

# 2) Проверить вход по TCP с хоста
 docker run --rm -e PGPASSWORD=postgres postgres:16 psql -h host.docker.internal -p 5433 -U postgres -d adaptive_testing -c "select 1"

# 3) Запустить backend с явным datasource
 ./gradlew :backend:bootRun --no-daemon --args="--spring.datasource.url=jdbc:postgresql://localhost:5433/adaptive_testing --spring.datasource.username=postgres --spring.datasource.password=postgres"
```

Если на шаге 1 снова `service "postgres" is not running`, значит Docker compose запущен не из того репозитория/пути или контейнер упал — проверьте `docker compose -f backend/docker-compose.yml logs postgres`.

### Жёсткий тест «это точно тот Postgres?»
Если после `ALTER ROLE` и явного `--spring.datasource.*` всё равно `28P01`, сделайте проверку с временным паролем:

```powershell
# 1) Задать ВРЕМЕННЫЙ пароль в контейнере
$env:TEMP_DB_PASS = "pg_temp_2026"
docker compose -f backend/docker-compose.yml exec postgres psql -U postgres -d postgres -c "ALTER USER postgres WITH PASSWORD '$env:TEMP_DB_PASS';"

# 2) Проверить TCP-логин этим паролем
docker compose -f backend/docker-compose.yml exec -e PGPASSWORD=$env:TEMP_DB_PASS postgres psql -h 127.0.0.1 -U postgres -d adaptive_testing -c "select 1"

# 3) Запустить backend с ТЕМ ЖЕ временным паролем
./gradlew :backend:bootRun --no-daemon --args="--spring.datasource.url=jdbc:postgresql://localhost:5433/adaptive_testing --spring.datasource.username=postgres --spring.datasource.password=$env:TEMP_DB_PASS"
```

Интерпретация:
- если шаг 2 OK, а backend всё равно пишет `28P01`, то backend подключается не к этому контейнеру;
- если шаг 2 не OK — проблема внутри самого контейнера/роли Postgres.


### Важно: почему падает именно `flywayInitializer`
В стектрейсе у вас ошибка создаётся в `flywayInitializer` (а не в основном DataSource).
Это значит, что Flyway может читать отдельные креды из env (`SPRING_FLYWAY_*`) и они могут отличаться от `spring.datasource.*`.

Если заданы `SPRING_FLYWAY_USER/PASSWORD`, backend будет падать с `28P01` даже при правильных `spring.datasource.*`.

Проверка/фикс (PowerShell):
```powershell
# посмотреть конфликтующие Flyway env
Get-ChildItem Env:SPRING_FLYWAY*

# очистить в текущей сессии
Remove-Item Env:SPRING_FLYWAY_URL -ErrorAction SilentlyContinue
Remove-Item Env:SPRING_FLYWAY_USER -ErrorAction SilentlyContinue
Remove-Item Env:SPRING_FLYWAY_PASSWORD -ErrorAction SilentlyContinue

# принудительно передать flyway+datasource одинаковыми
./gradlew :backend:bootRun --no-daemon --args="--spring.datasource.url=jdbc:postgresql://localhost:5433/adaptive_testing --spring.datasource.username=postgres --spring.datasource.password=$env:TEMP_DB_PASS --spring.flyway.url=jdbc:postgresql://localhost:5433/adaptive_testing --spring.flyway.user=postgres --spring.flyway.password=$env:TEMP_DB_PASS"
```

## Частые проблемы
- **JAVA_HOME не настроен** — Gradle не стартует.
- **Неправильный URL backend в Android-клиенте** — приложение не может подключиться.
- **Не поднята БД** — backend падает при старте.
- **Отсутствуют секреты/JWT ключи** — ошибки авторизации.
- **Разные версии SDK/JDK** — ошибки сборки.

## Мини-чеклист
- [ ] Репозиторий склонирован
- [ ] Секреты и локальные конфиги восстановлены
- [ ] БД запущена
- [ ] Backend успешно стартует
- [ ] Android-приложение собирается и запускается
- [ ] Базовые тесты/сценарии проходят
