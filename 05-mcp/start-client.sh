#!/bin/bash
# Start script for 05-mcp MCP Client (Tic-Tac-Toe Web UI)
# Connects to the MCP server to discover and invoke game tools.
# Also loads Foundry credentials so the agent-chat path (LLM-orchestrated MCP)
# can call Microsoft Foundry from the client.

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

# Verify required variables (only needed by the agent-chat path; the direct-call
# path works without them, but spring.ai.openai.* placeholders must resolve at startup).
if [ -z "$AZURE_OPENAI_ENDPOINT" ] || [ -z "$AZURE_OPENAI_API_KEY" ] || [ -z "$AZURE_OPENAI_FAST_DEPLOYMENT" ]; then
    echo "Error: Missing required environment variables (AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY, AZURE_OPENAI_FAST_DEPLOYMENT)"
    exit 1
fi

echo ""
echo "Make sure the MCP Server is running on http://localhost:8085 (run start-server.sh first)"
echo ""
echo "Starting Tic-Tac-Toe Web UI on http://localhost:8082 ..."
echo "Press Ctrl+C to stop."
echo ""

mvn spring-boot:run -q
