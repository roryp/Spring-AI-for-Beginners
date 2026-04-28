#!/bin/bash

# Azure Developer CLI (azd) environment integration
# This script loads environment variables from azd and creates/updates .env file

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

echo "Loading Azure Developer CLI (azd) environment..."

# Check if azd is installed
if ! command -v azd &> /dev/null; then
    echo "Error: Azure Developer CLI (azd) is not installed"
    echo "Install it from: https://learn.microsoft.com/azure/developer/azure-developer-cli/install-azd"
    exit 1
fi

# Get environment variables from azd, with fallback to existing .env values
if [ -f "$ENV_FILE" ]; then
    # Source existing .env for fallback values
    set +e
    source "$ENV_FILE" 2>/dev/null
    set -e
fi

# Change to 01-introduction directory to access the azd environment
cd "$SCRIPT_DIR/01-introduction"

# Try to get from azd, fallback to existing values, then to defaults
AZD_ENDPOINT=$(azd env get-value AZURE_OPENAI_ENDPOINT 2>/dev/null | head -n 1 || true)
AZD_API_KEY=$(azd env get-value AZURE_OPENAI_KEY 2>/dev/null | head -n 1 || true)
AZD_DEPLOYMENT=$(azd env get-value AZURE_OPENAI_DEPLOYMENT 2>/dev/null | head -n 1 || true)
AZD_FAST_DEPLOYMENT=$(azd env get-value AZURE_OPENAI_FAST_DEPLOYMENT 2>/dev/null | head -n 1 || true)
AZD_EMBEDDING=$(azd env get-value AZURE_OPENAI_EMBEDDING_DEPLOYMENT 2>/dev/null | head -n 1 || true)

# Return to script directory
cd "$SCRIPT_DIR"

# Use azd values if available, otherwise use existing or defaults
AZURE_OPENAI_ENDPOINT="${AZD_ENDPOINT:-${AZURE_OPENAI_ENDPOINT}}"
AZURE_OPENAI_API_KEY="${AZD_API_KEY:-${AZURE_OPENAI_API_KEY}}"
AZURE_OPENAI_DEPLOYMENT="${AZD_DEPLOYMENT:-${AZURE_OPENAI_DEPLOYMENT:-gpt-5.2}}"
AZURE_OPENAI_FAST_DEPLOYMENT="${AZD_FAST_DEPLOYMENT:-${AZURE_OPENAI_FAST_DEPLOYMENT:-gpt-4o-mini}}"
AZURE_OPENAI_EMBEDDING_DEPLOYMENT="${AZD_EMBEDDING:-${AZURE_OPENAI_EMBEDDING_DEPLOYMENT:-text-embedding-3-small}}"

# Validate required variables
if [ -z "$AZURE_OPENAI_ENDPOINT" ]; then
    echo "Error: AZURE_OPENAI_ENDPOINT not found in azd environment or existing .env"
    echo "Set it with: azd env set AZURE_OPENAI_ENDPOINT <value>"
    exit 1
fi

if [ -z "$AZURE_OPENAI_API_KEY" ]; then
    echo "Error: AZURE_OPENAI_API_KEY not found in azd environment or existing .env"
    echo "Set it with: azd env set AZURE_OPENAI_KEY <value>"
    exit 1
fi

# Create/update .env file
echo "Creating/updating .env file from azd environment..."

cat > "$ENV_FILE" << EOF
AZURE_OPENAI_ENDPOINT=$AZURE_OPENAI_ENDPOINT
AZURE_OPENAI_API_KEY=$AZURE_OPENAI_API_KEY
AZURE_OPENAI_DEPLOYMENT=$AZURE_OPENAI_DEPLOYMENT
AZURE_OPENAI_FAST_DEPLOYMENT=$AZURE_OPENAI_FAST_DEPLOYMENT
AZURE_OPENAI_EMBEDDING_DEPLOYMENT=$AZURE_OPENAI_EMBEDDING_DEPLOYMENT
EOF

# Create .env files in module directories
for module_dir in 01-introduction 02-prompt-engineering 03-rag 04-tools 05-mcp/mcp-server 05-mcp/mcp-client 06-agents; do
    module_env="$SCRIPT_DIR/$module_dir/.env"
    if [ -d "$SCRIPT_DIR/$module_dir" ]; then
        echo "Creating .env in $module_dir..."
        cp "$ENV_FILE" "$module_env"
        
        # Add module-specific variables
        if [ "$module_dir" == "04-tools" ]; then
            echo "TOOLS_BASE_URL=http://localhost:8084" >> "$module_env"
        fi
    fi
done

echo "✓ Environment variables successfully loaded from azd"
echo "✓ .env file created/updated at: $ENV_FILE"
echo "✓ .env files created in all module directories"
echo ""
echo "Configuration:"
echo "  AZURE_OPENAI_ENDPOINT: $AZURE_OPENAI_ENDPOINT"
echo "  AZURE_OPENAI_API_KEY: [HIDDEN]"
echo "  AZURE_OPENAI_DEPLOYMENT: $AZURE_OPENAI_DEPLOYMENT"
echo "  AZURE_OPENAI_FAST_DEPLOYMENT: $AZURE_OPENAI_FAST_DEPLOYMENT"
echo "  AZURE_OPENAI_EMBEDDING_DEPLOYMENT: $AZURE_OPENAI_EMBEDDING_DEPLOYMENT"
echo ""
echo "You can now run: ./start-all.sh"
