#!/bin/bash
# Start script for 05-mcp MCP Server (Tic-Tac-Toe Game Engine + AI via Azure OpenAI)
# Loads environment variables and starts the server on port 8085

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
ENV_FILE="$ROOT_DIR/.env"

# Change to mcp-server directory
cd "$SCRIPT_DIR/mcp-server"

# Check if .env exists
if [ ! -f "$ENV_FILE" ]; then
    echo "Error: .env file not found at $ENV_FILE. Please copy .env.example to .env in the root directory"
    exit 1
fi

# Load environment variables
echo "Loading environment variables from $ENV_FILE"
set -a
source "$ENV_FILE"
set +a

# Verify required variables
if [ -z "$AZURE_OPENAI_ENDPOINT" ] || [ -z "$AZURE_OPENAI_API_KEY" ] || [ -z "$AZURE_OPENAI_DEPLOYMENT" ]; then
    echo "Error: Missing required environment variables (AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY, AZURE_OPENAI_DEPLOYMENT)"
    exit 1
fi

echo ""
echo "Starting Tic-Tac-Toe MCP Server on http://localhost:8085 ..."
echo "Press Ctrl+C to stop."
echo ""

mvn spring-boot:run -q
