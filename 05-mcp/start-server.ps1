# Start script for 05-mcp MCP Server
# Starts the Spring Boot MCP server on port 8080

$ErrorActionPreference = "Stop"

$ScriptDir = $PSScriptRoot
$RootDir = Split-Path $ScriptDir -Parent
$EnvFile = Join-Path $RootDir ".env"

# Change to mcp-server directory
Set-Location (Join-Path $ScriptDir "mcp-server")

Write-Host ""
Write-Host "Starting MCP Server on http://localhost:8080 ..."
Write-Host "Press Ctrl+C to stop."
Write-Host ""

mvn spring-boot:run -q
