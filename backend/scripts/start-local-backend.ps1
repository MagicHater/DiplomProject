$ErrorActionPreference = "Stop"

function Assert-LastExitCode([string]$step) {
  if ($LASTEXITCODE -ne 0) {
    throw "$step failed with exit code $LASTEXITCODE"
  }
}

$rootDir = Resolve-Path (Join-Path $PSScriptRoot "../..")
Set-Location $rootDir

$dbUrl = "jdbc:postgresql://127.0.0.1:5433/adaptive_testing"
$dbUser = "postgres"
$dbPass = "postgres"
$resetDb = $env:RESET_DB
if ([string]::IsNullOrWhiteSpace($resetDb)) { $resetDb = "0" }
$retriedOnAuthFail = $env:RETRIED_ON_AUTH_FAIL
if ([string]::IsNullOrWhiteSpace($retriedOnAuthFail)) { $retriedOnAuthFail = "0" }

Write-Host "[meta] start-local-backend.ps1 v2 (DB+FLYWAY hard-sync)"

Write-Host "[meta] start-local-backend.ps1 v2 (DB+FLYWAY hard-sync)"

Write-Host "[meta] start-local-backend.ps1 v2 (DB+FLYWAY hard-sync)"

Write-Host "[meta] start-local-backend.ps1 v2 (DB+FLYWAY hard-sync)"

Write-Host "[meta] start-local-backend.ps1 v3 (DB+FLYWAY hard-sync)"
Write-Host "[meta] git branch: $(git rev-parse --abbrev-ref HEAD), commit: $(git rev-parse --short HEAD), RESET_DB=$resetDb"

Write-Host "[meta] start-local-backend.ps1 v3 (DB+FLYWAY hard-sync)"
Write-Host "[meta] git branch: $(git rev-parse --abbrev-ref HEAD), commit: $(git rev-parse --short HEAD), RESET_DB=$resetDb"

Write-Host "[1/4] Stopping compose stack (if exists)..."
docker compose -f backend/docker-compose.yml down --remove-orphans | Out-Null
Assert-LastExitCode "docker compose down"

if ($resetDb -eq "1") {
  Write-Host "[2/4] RESET_DB=1 -> removing compose volumes (this deletes local data)..."
  docker compose -f backend/docker-compose.yml down -v --remove-orphans | Out-Null
  Assert-LastExitCode "docker compose down -v"
} else {
  Write-Host "[2/4] Keeping existing Postgres volume (set RESET_DB=1 for full reset)."
}

Write-Host "[3/4] Starting Postgres on host port 5433..."
docker compose -f backend/docker-compose.yml up -d postgres | Out-Null
Assert-LastExitCode "docker compose up -d postgres"

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
Assert-LastExitCode "ALTER USER postgres"

Write-Host "[fix] Forcing pg_hba host auth method to trust..."
docker compose -f backend/docker-compose.yml exec -T postgres `
  sh -lc "sed -i 's/scram-sha-256/trust/g' \"\$PGDATA/pg_hba.conf\" && psql -U postgres -d postgres -c 'SELECT pg_reload_conf();'" | Out-Null
Assert-LastExitCode "force pg_hba trust + reload"

Write-Host "[check] Verifying TCP login inside postgres container (127.0.0.1:5432)..."
docker compose -f backend/docker-compose.yml exec -T -e PGPASSWORD=$dbPass postgres `
  psql -h 127.0.0.1 -U $dbUser -d adaptive_testing -c "select 1" | Out-Null
Assert-LastExitCode "in-container TCP check"

Write-Host "[check] Optional host-path probe (host.docker.internal:5433)..."
docker run --rm -e PGPASSWORD=$dbPass postgres:16 `
  psql -h host.docker.internal -p 5433 -U $dbUser -d adaptive_testing -c "select 1" | Out-Null
if ($LASTEXITCODE -ne 0) {
  Write-Warning "Host-path probe failed on this machine; continuing (backend still uses localhost:5433)."
}

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

./gradlew :backend:bootRun --no-daemon --args="--spring.datasource.url=$dbUrl --spring.datasource.username=$dbUser --spring.datasource.password=$dbPass --spring.flyway.url=$dbUrl --spring.flyway.user=$dbUser --spring.flyway.password=$dbPass"
