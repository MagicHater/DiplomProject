#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

DB_URL="jdbc:postgresql://localhost:5433/adaptive_testing"
DB_USERNAME="postgres"
DB_PASSWORD="postgres"
RESET_DB="${RESET_DB:-0}"

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

echo "[run] Starting backend with explicit DB credentials..."
DB_URL="$DB_URL" DB_USERNAME="$DB_USERNAME" DB_PASSWORD="$DB_PASSWORD" \
  ./gradlew :backend:bootRun
