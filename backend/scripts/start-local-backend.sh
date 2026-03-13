#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

DB_URL="jdbc:postgresql://localhost:5432/adaptive_testing"
DB_USERNAME="postgres"
DB_PASSWORD="postgres"

echo "[1/4] Stopping compose stack (if exists)..."
docker compose -f backend/docker-compose.yml down --remove-orphans >/dev/null 2>&1 || true

echo "[2/4] Removing compose volumes to reset Postgres credentials/data..."
docker compose -f backend/docker-compose.yml down -v --remove-orphans >/dev/null 2>&1 || true

echo "[3/4] Starting fresh Postgres..."
DB_URL="$DB_URL" DB_USERNAME="$DB_USERNAME" DB_PASSWORD="$DB_PASSWORD" \
  docker compose -f backend/docker-compose.yml up -d postgres

echo "[4/4] Starting backend with explicit DB credentials..."
DB_URL="$DB_URL" DB_USERNAME="$DB_USERNAME" DB_PASSWORD="$DB_PASSWORD" \
  ./gradlew :backend:bootRun
