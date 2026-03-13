#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

DB_URL="jdbc:postgresql://localhost:5433/adaptive_testing"
DB_USERNAME="postgres"
DB_PASSWORD="postgres"

echo "[1/5] Stopping compose stack (if exists)..."
docker compose -f backend/docker-compose.yml down --remove-orphans >/dev/null 2>&1 || true

echo "[2/5] Removing compose volumes to reset Postgres credentials/data..."
docker compose -f backend/docker-compose.yml down -v --remove-orphans >/dev/null 2>&1 || true

echo "[3/5] Starting fresh Postgres on host port 5433..."
docker compose -f backend/docker-compose.yml up -d postgres

container_id="$(docker compose -f backend/docker-compose.yml ps -q postgres)"
if [[ -z "$container_id" ]]; then
  echo "Postgres container was not created."
  exit 1
fi

echo "[4/5] Waiting for Postgres health..."
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

echo "[5/5] Starting backend with explicit DB credentials..."
DB_URL="$DB_URL" DB_USERNAME="$DB_USERNAME" DB_PASSWORD="$DB_PASSWORD" \
  ./gradlew :backend:bootRun
