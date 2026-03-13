#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

# Clean up legacy container name from previous instructions (if exists)
docker rm -f adaptive-testing-postgres >/dev/null 2>&1 || true

# Start postgres in a known state
DB_URL="jdbc:postgresql://localhost:5432/adaptive_testing"
DB_USERNAME="postgres"
DB_PASSWORD="postgres"

DB_URL="$DB_URL" DB_USERNAME="$DB_USERNAME" DB_PASSWORD="$DB_PASSWORD" \
  docker compose -f backend/docker-compose.yml up -d postgres

# Run backend with explicit DB credentials
DB_URL="$DB_URL" DB_USERNAME="$DB_USERNAME" DB_PASSWORD="$DB_PASSWORD" \
  ./gradlew :backend:bootRun
