$ErrorActionPreference = "Stop"

$rootDir = Resolve-Path (Join-Path $PSScriptRoot "../..")
Set-Location $rootDir

$composeFile = "backend/docker-compose.yml"
$dbUrl = "jdbc:postgresql://localhost:5433/adaptive_testing"
$dbUser = "postgres"
$dbPass = "postgres"
$resetDb = if ([string]::IsNullOrWhiteSpace($env:RESET_DB)) { "0" } else { $env:RESET_DB }

function Invoke-Compose {
  param(
    [string[]]$Args,
    [switch]$IgnoreError
  )

  try {
    & docker compose -f $composeFile @Args | Out-Null
  }
  catch {
    if (-not $IgnoreError) {
      throw
    }
  }
}

Write-Host "[meta] start-local-backend.ps1 v4 (local Postgres reset + Flyway resync)"
Write-Host "[meta] git branch: $(git rev-parse --abbrev-ref HEAD), commit: $(git rev-parse --short HEAD), RESET_DB=$resetDb"

Write-Host "[1/5] Stopping compose stack (if exists)..."
Invoke-Compose -Args @("down", "--remove-orphans") -IgnoreError

if ($resetDb -eq "1") {
  $volumeNames = @()
  try {
    $volumeNames = (& docker compose -f $composeFile config --volumes 2>$null) |
      ForEach-Object { $_.Trim() } |
      Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
  }
  catch {
    $volumeNames = @()
  }

  Write-Host "[2/5] RESET_DB=1 detected. FULL local DB reset will be performed."
  if ($volumeNames.Count -gt 0) {
    Write-Host "      Compose volumes to remove: $($volumeNames -join ', ')"
  }
  else {
    Write-Host "      Compose volumes will be removed via 'docker compose down -v'."
  }
  Write-Host "      WARNING: this destroys local PostgreSQL test/development data."
  Write-Host "      After reset Flyway will apply all migrations from scratch."

  Invoke-Compose -Args @("down", "-v", "--remove-orphans") -IgnoreError
}
else {
  Write-Host "[2/5] Keeping existing Postgres volume (set RESET_DB=1 for full DB reset)."
}

Write-Host "[3/5] Starting Postgres on host port 5433..."
& docker compose -f $composeFile up -d postgres | Out-Null

$containerId = (& docker compose -f $composeFile ps -q postgres 2>$null | Select-Object -First 1).Trim()
if ([string]::IsNullOrWhiteSpace($containerId)) {
  Write-Error "Postgres container was not created."
  exit 1
}

Write-Host "[4/5] Waiting for Postgres healthcheck..."
$healthy = $false
for ($i = 0; $i -lt 60; $i++) {
  $status = (& docker inspect -f "{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" $containerId 2>$null | Select-Object -First 1).Trim()
  if ($status -eq "healthy" -or $status -eq "running") {
    $healthy = $true
    break
  }
  Start-Sleep -Seconds 1
}

if (-not $healthy) {
  Write-Error "Postgres did not become healthy in time."
  & docker compose -f $composeFile ps
  & docker compose -f $composeFile logs --tail=100 postgres
  exit 1
}

Write-Host "[5/5] Starting backend with explicit DB env vars..."
$env:DB_URL = $dbUrl
$env:DB_USERNAME = $dbUser
$env:DB_PASSWORD = $dbPass
./gradlew :backend:bootRun
