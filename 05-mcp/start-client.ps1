# Start script for 05-mcp MCP Client (Tic-Tac-Toe Web UI)
# Connects to the MCP server to discover and invoke game tools

$ErrorActionPreference = "Stop"

$ScriptDir = $PSScriptRoot

# Change to mcp-client directory
Set-Location (Join-Path $ScriptDir "mcp-client")

Write-Host ""
Write-Host "Make sure the MCP Server is running on http://localhost:8080 (run start-server.ps1 first)"
Write-Host ""
Write-Host "Starting Tic-Tac-Toe Web UI on http://localhost:8081 ..."
Write-Host "Press Ctrl+C to stop."
Write-Host ""

mvn spring-boot:run -q
