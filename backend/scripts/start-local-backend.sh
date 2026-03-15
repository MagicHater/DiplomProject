#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

DB_URL="jdbc:postgresql://127.0.0.1:5433/adaptive_testing"
DB_USERNAME="postgres"
DB_PASSWORD="postgres"
RESET_DB="${RESET_DB:-0}"

echo "[meta] start-local-backend.sh v3 (DB+FLYWAY hard-sync)"
echo "[meta] git branch: $(git rev-parse --abbrev-ref HEAD), commit: $(git rev-parse --short HEAD), RESET_DB=$RESET_DB"

echo "[1/4] Stopping compose stack (if exists)..."
docker compose -f backend/docker-compose.yml down --remove-orphans >/dev/null 2>&1 || true

if [[ "$RESET_DB" == "1" ]]; then
  echo "[2/4] RESET_DB=1 -> removing compose volumes (this deletes local data)..."
  docker compose -f backend/docker-compose.yml down -v --remove-orphans >/dev/null 2>&1 || true
else
  echo "[2/4] Keeping existing Postgres volume (set RESET_DB=1 for full reset)."
fi

echo "[3/4] Starting Postgres on host port 5433..."
docker compose -f backend/docker-compose.yml up -d postgres

container_id="$(docker compose -f backend/docker-compose.yml ps -q postgres)"
if [[ -z "$container_id" ]]; then
  echo "Postgres container was not created."
  exit 1
fi

echo "[4/4] Waiting for Postgres health..."
for i in {1..30}; do
  status="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container_id")"
  if [[ "$status" == "healthy" || "$status" == "running" ]]; then
    break
  fi
  sleep 1
done

final_status="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container_id")"
if [[ "$final_status" != "healthy" && "$final_status" != "running" ]]; then
  echo "Postgres is not ready. Current status: $final_status"
  docker compose -f backend/docker-compose.yml ps
  docker compose -f backend/docker-compose.yml logs --tail=100 postgres
  exit 1
fi

echo "[fix] Enforcing postgres user password inside container..."
docker compose -f backend/docker-compose.yml exec -T postgres \
  psql -U postgres -d postgres -c "ALTER USER postgres WITH PASSWORD '$DB_PASSWORD';" >/dev/null

echo "[check] Verifying TCP login inside postgres container (127.0.0.1:5432)..."
docker compose -f backend/docker-compose.yml exec -T -e PGPASSWORD="$DB_PASSWORD" postgres \
  psql -h 127.0.0.1 -U "$DB_USERNAME" -d adaptive_testing -c "select 1" >/dev/null

echo "[check] Optional host-path probe (host.docker.internal:5433)..."
if ! docker run --rm -e PGPASSWORD="$DB_PASSWORD" postgres:16 \
  psql -h host.docker.internal -p 5433 -U "$DB_USERNAME" -d adaptive_testing -c "select 1" >/dev/null; then
  echo "[warn] Host-path probe failed on this machine; continuing (backend still uses localhost:5433)."
fi

echo "[run] Starting backend with explicit DB credentials..."
# Neutralize conflicting Spring env vars from shell/session.
unset SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD
unset SPRING_FLYWAY_URL SPRING_FLYWAY_USER SPRING_FLYWAY_PASSWORD
unset SPRING_APPLICATION_JSON

DB_URL="$DB_URL" DB_USERNAME="$DB_USERNAME" DB_PASSWORD="$DB_PASSWORD" \
FLYWAY_URL="$DB_URL" FLYWAY_USER="$DB_USERNAME" FLYWAY_PASSWORD="$DB_PASSWORD" \
  ./gradlew :backend:bootRun --no-daemon --args="--spring.datasource.url=$DB_URL --spring.datasource.username=$DB_USERNAME --spring.datasource.password=$DB_PASSWORD --spring.flyway.url=$DB_URL --spring.flyway.user=$DB_USERNAME --spring.flyway.password=$DB_PASSWORD"
