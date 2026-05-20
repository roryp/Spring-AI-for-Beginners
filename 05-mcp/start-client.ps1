# Start script for 05-mcp MCP Client (Tic-Tac-Toe Web UI)
# Connects to the MCP server to discover and invoke game tools.
# Also loads Foundry credentials so the agent-chat path (LLM-orchestrated MCP)
# can call Microsoft Foundry from the client.

$ErrorActionPreference = "Stop"

$ScriptDir = $PSScriptRoot
$RootDir = Split-Path $ScriptDir -Parent
$EnvFile = Join-Path $RootDir ".env"

# Change to mcp-client directory
Set-Location (Join-Path $ScriptDir "mcp-client")

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

# Verify required variables (only needed by the agent-chat path; the direct-call
# path works without them, but spring.ai.openai.* placeholders must resolve at startup).
$endpoint = [Environment]::GetEnvironmentVariable('AZURE_OPENAI_ENDPOINT', 'Process')
$apiKey = [Environment]::GetEnvironmentVariable('AZURE_OPENAI_API_KEY', 'Process')
$deployment = [Environment]::GetEnvironmentVariable('AZURE_OPENAI_FAST_DEPLOYMENT', 'Process')

if (-not $endpoint -or -not $apiKey -or -not $deployment) {
    Write-Error "Missing required environment variables (AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY, AZURE_OPENAI_FAST_DEPLOYMENT)"
    exit 1
}

Write-Host ""
Write-Host "Make sure the MCP Server is running on http://localhost:8085 (run start-server.ps1 first)"
Write-Host ""
Write-Host "Starting Tic-Tac-Toe Web UI on http://localhost:8082 ..."
Write-Host "Press Ctrl+C to stop."
Write-Host ""

mvn spring-boot:run -q
