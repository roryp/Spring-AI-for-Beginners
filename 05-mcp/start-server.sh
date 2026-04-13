#!/bin/bash
# Start script for 05-mcp MCP Server
# Starts the Spring Boot MCP server on port 8080

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Change to mcp-server directory
cd "$SCRIPT_DIR/mcp-server"

echo ""
echo "Starting MCP Server on http://localhost:8080 ..."
echo "Press Ctrl+C to stop."
echo ""

mvn spring-boot:run -q
