# Start script for 05-mcp MCP Server (Tic-Tac-Toe Game Engine + AI via Azure OpenAI)
# Loads environment variables and starts the server on port 8085

$ErrorActionPreference = "Stop"

$ScriptDir = $PSScriptRoot
$RootDir = Split-Path $ScriptDir -Parent
$EnvFile = Join-Path $RootDir ".env"

# Change to mcp-server directory
Set-Location (Join-Path $ScriptDir "mcp-server")

# Check if .env exists
if (-not (Test-Path $EnvFile)) {
    Write-Error ".env file not found at $EnvFile. Please copy .env.example to .env in the root directory"
    exit 1
}

# Load environment variables
Write-Host "Loading environment variables from $EnvFile"
Get-Content $EnvFile | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}

# Verify required variables
$endpoint = [Environment]::GetEnvironmentVariable('AZURE_OPENAI_ENDPOINT', 'Process')
$apiKey = [Environment]::GetEnvironmentVariable('AZURE_OPENAI_API_KEY', 'Process')
$deployment = [Environment]::GetEnvironmentVariable('AZURE_OPENAI_DEPLOYMENT', 'Process')

if (-not $endpoint -or -not $apiKey -or -not $deployment) {
    Write-Error "Missing required environment variables (AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY, AZURE_OPENAI_DEPLOYMENT)"
    exit 1
}

Write-Host ""
Write-Host "Starting Tic-Tac-Toe MCP Server on http://localhost:8085 ..."
Write-Host "Press Ctrl+C to stop."
Write-Host ""

mvn spring-boot:run -q
