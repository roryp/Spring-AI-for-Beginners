# Start all Spring AI for Beginners applications
# Loads the centralized .env file and starts all modules 01-06
# (including 05-mcp server and client, in that order).

$ErrorActionPreference = "Stop"

$ScriptDir = $PSScriptRoot
$EnvFile = Join-Path $ScriptDir ".env"

if (-not (Test-Path $EnvFile)) {
    Write-Error ".env file not found at $EnvFile. Please copy .env.example to .env and fill in your credentials"
    exit 1
}

Write-Host "Loading environment variables from $EnvFile"
Get-Content $EnvFile | ForEach-Object {
    if ($_ -match '^\s*#') { return }
    if ($_ -match '^\s*$') { return }
    if ($_ -match '^\s*([^=\s]+)\s*=\s*(.*)\s*$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}

$endpoint = [Environment]::GetEnvironmentVariable('AZURE_OPENAI_ENDPOINT', 'Process')
$apiKey = [Environment]::GetEnvironmentVariable('AZURE_OPENAI_API_KEY', 'Process')
$deployment = [Environment]::GetEnvironmentVariable('AZURE_OPENAI_DEPLOYMENT', 'Process')

if (-not $endpoint -or -not $apiKey -or -not $deployment) {
    Write-Error "Missing required environment variables. Please ensure AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY, and AZURE_OPENAI_DEPLOYMENT are set in .env"
    exit 1
}

Write-Host "Environment variables loaded successfully" -ForegroundColor Green
Write-Host "AZURE_OPENAI_ENDPOINT: $endpoint"
Write-Host "AZURE_OPENAI_DEPLOYMENT: $deployment"
Write-Host "AZURE_OPENAI_FAST_DEPLOYMENT: $([Environment]::GetEnvironmentVariable('AZURE_OPENAI_FAST_DEPLOYMENT','Process'))"
Write-Host ""

function Test-PortInUse {
    param([int]$Port)
    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    return $null -ne $connections
}

function Wait-ForPort {
    param([int]$Port, [int]$MaxWaitSeconds = 60)
    $count = 0
    while ($count -lt $MaxWaitSeconds) {
        if (Test-PortInUse -Port $Port) { return $true }
        Start-Sleep -Seconds 1
        $count++
    }
    return $false
}

function Start-App {
    param([string]$Module, [int]$Port, [string]$JarName)

    $ModuleDir = Join-Path $ScriptDir $Module
    $JarFile = Join-Path $ModuleDir "target\$JarName"
    # Use only the leaf segment for the log file name so sub-module paths
    # (e.g. "05-mcp/mcp-server") don't produce invalid file names.
    $LogBase = (Split-Path $Module -Leaf)
    $LogFile = Join-Path $ModuleDir "$LogBase.log"
    $ErrLogFile = "$LogFile.err"

    Write-Host ""
    Write-Host "Starting $Module on port $Port..." -ForegroundColor Yellow

    if (Test-PortInUse -Port $Port) {
        Write-Host "Warning: Port $Port is already in use. Skipping $Module" -ForegroundColor Yellow
        return $true
    }

    if (-not (Test-Path $JarFile)) {
        Write-Host "JAR file not found. Building $Module..."
        Push-Location $ModuleDir
        try {
            mvn clean package -DskipTests
            if ($LASTEXITCODE -ne 0) {
                Write-Host "Error: Build failed for $Module" -ForegroundColor Red
                return $false
            }
            if (-not (Test-Path $JarFile)) {
                Write-Host "Error: Build succeeded but JAR file not created at $JarFile" -ForegroundColor Red
                return $false
            }
            Write-Host "Build completed successfully" -ForegroundColor Green
        } finally {
            Pop-Location
        }
    }

    # Remove any stale log files from previous runs
    if (Test-Path $LogFile)    { Remove-Item $LogFile    -Force -ErrorAction SilentlyContinue }
    if (Test-Path $ErrLogFile) { Remove-Item $ErrLogFile -Force -ErrorAction SilentlyContinue }

    $process = Start-Process -FilePath "java" `
        -ArgumentList "-jar", $JarFile `
        -WorkingDirectory $ModuleDir `
        -RedirectStandardOutput $LogFile `
        -RedirectStandardError $ErrLogFile `
        -WindowStyle Hidden `
        -PassThru

    Write-Host "Started $Module with PID $($process.Id), waiting for port $Port (log: $LogFile)..."

    if (Wait-ForPort -Port $Port -MaxWaitSeconds 60) {
        Write-Host "[OK] $Module is running on port $Port" -ForegroundColor Green
        return $true
    } else {
        Write-Host "[FAIL] $Module failed to start. Check $LogFile (and $ErrLogFile if present) for details" -ForegroundColor Red
        return $false
    }
}

$modules = @(
    @{ Name = "01-introduction";       Port = 8080; Jar = "spring-ai-introduction-1.0.0.jar" },
    @{ Name = "02-prompt-engineering"; Port = 8083; Jar = "spring-ai-prompt-engineering-1.0.0.jar" },
    @{ Name = "03-rag";                Port = 8081; Jar = "spring-ai-rag-1.0.0.jar" },
    @{ Name = "04-tools";              Port = 8084; Jar = "tools-1.0.0.jar" },
    # 05-mcp: server MUST start before client (client connects to server on startup)
    @{ Name = "05-mcp/mcp-server";     Port = 8085; Jar = "spring-ai-mcp-server-1.0.0.jar" },
    @{ Name = "05-mcp/mcp-client";     Port = 8082; Jar = "spring-ai-mcp-client-1.0.0.jar" },
    @{ Name = "06-agents";             Port = 8086; Jar = "spring-ai-agents-1.0.0.jar" }
)

$failedModules = @()
foreach ($m in $modules) {
    if (-not (Start-App -Module $m.Name -Port $m.Port -JarName $m.Jar)) {
        $failedModules += $m.Name
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
if ($failedModules.Count -eq 0) {
    Write-Host "All applications started successfully!" -ForegroundColor Green
} else {
    Write-Host "Some applications failed to start:" -ForegroundColor Red
    foreach ($module in $failedModules) {
        Write-Host "  - $module" -ForegroundColor Red
    }
}
Write-Host ""
Write-Host "Running applications:"
Write-Host "01-introduction:       http://localhost:8080"
Write-Host "02-prompt-engineering: http://localhost:8083"
Write-Host "03-rag:                http://localhost:8081"
Write-Host "04-tools:              http://localhost:8084"
Write-Host "05-mcp server:         http://localhost:8085"
Write-Host "05-mcp client:         http://localhost:8082"
Write-Host "06-agents:             http://localhost:8086"
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "To stop all applications, run: .\stop-all.ps1"