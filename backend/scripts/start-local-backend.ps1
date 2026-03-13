$ErrorActionPreference = "Stop"

$rootDir = Resolve-Path (Join-Path $PSScriptRoot "../..")
Set-Location $rootDir

$dbUrl = "jdbc:postgresql://localhost:5433/adaptive_testing"
$dbUser = "postgres"
$dbPass = "postgres"

Write-Host "[1/5] Stopping compose stack (if exists)..."
docker compose -f backend/docker-compose.yml down --remove-orphans | Out-Null

Write-Host "[2/5] Removing compose volumes to reset Postgres credentials/data..."
docker compose -f backend/docker-compose.yml down -v --remove-orphans | Out-Null

Write-Host "[3/5] Starting fresh Postgres on host port 5433..."
docker compose -f backend/docker-compose.yml up -d postgres | Out-Null

Write-Host "[4/5] Waiting for Postgres health..."
$healthy = $false
for ($i = 0; $i -lt 30; $i++) {
  $json = docker compose -f backend/docker-compose.yml ps --format json postgres
  if ($json -match '"Health":"healthy"') {
    $healthy = $true
    break
  }
  Start-Sleep -Seconds 1
}
if (-not $healthy) {
  Write-Error "Postgres did not become healthy in time."
  docker compose -f backend/docker-compose.yml ps
  docker compose -f backend/docker-compose.yml logs --tail=100 postgres
  exit 1
}

Write-Host "[5/5] Starting backend with explicit DB env..."
$env:DB_URL = $dbUrl
$env:DB_USERNAME = $dbUser
$env:DB_PASSWORD = $dbPass
./gradlew :backend:bootRun
