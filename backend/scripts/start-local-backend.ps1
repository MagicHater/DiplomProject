$ErrorActionPreference = "Stop"

$rootDir = Resolve-Path (Join-Path $PSScriptRoot "../..")
Set-Location $rootDir

# Clean up legacy container name from previous instructions (if exists)
try {
  docker rm -f adaptive-testing-postgres | Out-Null
} catch {
}

$dbUrl = "jdbc:postgresql://localhost:5432/adaptive_testing"
$dbUser = "postgres"
$dbPass = "postgres"

$env:DB_URL = $dbUrl
$env:DB_USERNAME = $dbUser
$env:DB_PASSWORD = $dbPass

docker compose -f backend/docker-compose.yml up -d postgres
./gradlew :backend:bootRun
