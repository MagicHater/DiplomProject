$ErrorActionPreference = "Stop"

$rootDir = Resolve-Path (Join-Path $PSScriptRoot "../..")
Set-Location $rootDir

$dbUrl = "jdbc:postgresql://localhost:5433/adaptive_testing"
$dbUser = "postgres"
$dbPass = "postgres"
$resetDb = $env:RESET_DB
if ([string]::IsNullOrWhiteSpace($resetDb)) { $resetDb = "0" }

Write-Host "[meta] start-local-backend.ps1 v2 (DB+FLYWAY hard-sync)"

Write-Host "[1/4] Stopping compose stack (if exists)..."
docker compose -f backend/docker-compose.yml down --remove-orphans | Out-Null

if ($resetDb -eq "1") {
  Write-Host "[2/4] RESET_DB=1 -> removing compose volumes (this deletes local data)..."
  docker compose -f backend/docker-compose.yml down -v --remove-orphans | Out-Null
} else {
  Write-Host "[2/4] Keeping existing Postgres volume (set RESET_DB=1 for full reset)."
}

Write-Host "[3/4] Starting Postgres on host port 5433..."
docker compose -f backend/docker-compose.yml up -d postgres | Out-Null

Write-Host "[4/4] Waiting for Postgres health..."
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

Write-Host "[fix] Enforcing postgres user password inside container..."
docker compose -f backend/docker-compose.yml exec -T postgres `
  psql -U postgres -d postgres -c "ALTER USER postgres WITH PASSWORD '$dbPass';" | Out-Null

Write-Host "[run] Starting backend with explicit DB env..."
$env:DB_URL = $dbUrl
$env:DB_USERNAME = $dbUser
$env:DB_PASSWORD = $dbPass
$env:FLYWAY_URL = $dbUrl
$env:FLYWAY_USER = $dbUser
$env:FLYWAY_PASSWORD = $dbPass

# Neutralize conflicting Spring env vars from user/system scope.
Remove-Item Env:SPRING_DATASOURCE_URL -ErrorAction SilentlyContinue
Remove-Item Env:SPRING_DATASOURCE_USERNAME -ErrorAction SilentlyContinue
Remove-Item Env:SPRING_DATASOURCE_PASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:SPRING_FLYWAY_URL -ErrorAction SilentlyContinue
Remove-Item Env:SPRING_FLYWAY_USER -ErrorAction SilentlyContinue
Remove-Item Env:SPRING_FLYWAY_PASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:SPRING_APPLICATION_JSON -ErrorAction SilentlyContinue

./gradlew :backend:bootRun --no-daemon
