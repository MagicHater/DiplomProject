param(
    [switch]$ResetDb
)

$ErrorActionPreference = "Stop"

$rootDir = Resolve-Path (Join-Path $PSScriptRoot "../..")
Set-Location $rootDir

$dbUrl  = "jdbc:postgresql://localhost:5433/adaptive_testing"
$dbUser = "postgres"
$dbPass = "postgres"
$composeFile = "backend/docker-compose.yml"

function Get-ResetFlag {
    param(
        [bool]$SwitchReset
    )

    if ($SwitchReset) {
        return $true
    }

    $envValue = $env:RESET_DB
    if (-not [string]::IsNullOrWhiteSpace($envValue)) {
        return ($envValue -eq "1" -or $envValue.ToLower() -eq "true")
    }

    return $false
}

function Wait-PostgresHealthy {
    param(
        [int]$MaxAttempts = 60
    )

    for ($i = 1; $i -le $MaxAttempts; $i++) {
        try {
            $json = docker compose -f $composeFile ps --format json 2>$null
            if ($LASTEXITCODE -eq 0 -and $json) {
                if ($json -match '"Health":"healthy"') {
                    return $true
                }
            }
        }
        catch {
            # ignore transient compose output errors while container is starting
        }

        Start-Sleep -Seconds 1
    }

    return $false
}

$shouldResetDb = Get-ResetFlag -SwitchReset:$ResetDb

$gitBranch = "unknown"
$gitCommit = "unknown"

try { $gitBranch = (git rev-parse --abbrev-ref HEAD).Trim() } catch {}
try { $gitCommit = (git rev-parse --short HEAD).Trim() } catch {}

Write-Host "[meta] start-local-backend.ps1 v4"
Write-Host "[meta] git branch: $gitBranch, commit: $gitCommit, RESET_DB=$([int]$shouldResetDb)"
Write-Host "[meta] DB: $dbUrl"

Write-Host "[1/4] Stopping compose stack (if exists)..."
docker compose -f $composeFile down --remove-orphans | Out-Null

if ($shouldResetDb) {
    Write-Host "[2/4] RESET_DB enabled -> removing Postgres volume and local DB data..."
    Write-Host "[warn] This will delete ALL local PostgreSQL data for this project."
    docker compose -f $composeFile down -v --remove-orphans | Out-Null
}
else {
    Write-Host "[2/4] Keeping existing Postgres volume."
    Write-Host "[hint] If Flyway reports checksum mismatch, rerun with:"
    Write-Host "       ./backend/scripts/start-local-backend.ps1 -ResetDb"
    Write-Host "       or"
    Write-Host "       `$env:RESET_DB=1; ./backend/scripts/start-local-backend.ps1"
}

Write-Host "[3/4] Starting Postgres on host port 5433..."
docker compose -f $composeFile up -d postgres | Out-Null

Write-Host "[4/4] Waiting for Postgres health..."
$healthy = Wait-PostgresHealthy -MaxAttempts 60

if (-not $healthy) {
    Write-Error "Postgres did not become healthy in time."
    docker compose -f $composeFile ps
    docker compose -f $composeFile logs --tail=100 postgres
    exit 1
}

Write-Host "[run] Starting backend with explicit DB env..."
$env:DB_URL = $dbUrl
$env:DB_USERNAME = $dbUser
$env:DB_PASSWORD = $dbPass

try {
    ./gradlew :backend:bootRun
}
finally {
    Remove-Item Env:DB_URL -ErrorAction SilentlyContinue
    Remove-Item Env:DB_USERNAME -ErrorAction SilentlyContinue
    Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
}