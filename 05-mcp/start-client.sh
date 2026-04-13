#!/bin/bash
# Start script for 05-mcp MCP Client
# Loads environment variables from parent .env file and connects to MCP server

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
ENV_FILE="$ROOT_DIR/.env"

# Change to mcp-client directory
cd "$SCRIPT_DIR/mcp-client"

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
echo "Make sure the MCP Server is running on http://localhost:8080 (run start-server.sh first)"
echo ""
echo "Running MCP Client..."
echo ""

mvn spring-boot:run -q
