#!/bin/bash
# Start script for 05-mcp MCP Client (Tic-Tac-Toe Web UI)
# Connects to the MCP server to discover and invoke game tools

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Change to mcp-client directory
cd "$SCRIPT_DIR/mcp-client"

echo ""
echo "Make sure the MCP Server is running on http://localhost:8085 (run start-server.sh first)"
echo ""
echo "Starting Tic-Tac-Toe Web UI on http://localhost:8082 ..."
echo "Press Ctrl+C to stop."
echo ""

mvn spring-boot:run -q
