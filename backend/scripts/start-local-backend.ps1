$ErrorActionPreference = "Stop"

$rootDir = Resolve-Path (Join-Path $PSScriptRoot "../..")
Set-Location $rootDir

$dbUrl = "jdbc:postgresql://localhost:5432/adaptive_testing"
$dbUser = "postgres"
$dbPass = "postgres"

Write-Host "[1/4] Stopping compose stack (if exists)..."
docker compose -f backend/docker-compose.yml down --remove-orphans | Out-Null

Write-Host "[2/4] Removing compose volumes to reset Postgres credentials/data..."
docker compose -f backend/docker-compose.yml down -v --remove-orphans | Out-Null

Write-Host "[3/4] Starting fresh Postgres..."
$env:DB_URL = $dbUrl
$env:DB_USERNAME = $dbUser
$env:DB_PASSWORD = $dbPass

docker compose -f backend/docker-compose.yml up -d postgres

Write-Host "[4/4] Starting backend with explicit DB env..."
./gradlew :backend:bootRun
