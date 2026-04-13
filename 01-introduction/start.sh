#!/bin/bash

# Start script for 01-introduction module
# Sources environment variables from parent .env file

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
ENV_FILE="$ROOT_DIR/.env"

# Change to module directory
cd "$SCRIPT_DIR"

# Check if .env exists
if [ ! -f "$ENV_FILE" ]; then
    echo "Error: .env file not found at $ENV_FILE"
    echo "Please copy .env.example to .env in the root directory"
    exit 1
fi

# Source environment variables
echo "Loading environment variables from $ENV_FILE"
set -a
source "$ENV_FILE"
set +a

# Verify required variables
if [ -z "$AZURE_OPENAI_ENDPOINT" ] || [ -z "$AZURE_OPENAI_API_KEY" ] || [ -z "$AZURE_OPENAI_DEPLOYMENT" ]; then
    echo "Error: Missing required environment variables (AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY, AZURE_OPENAI_DEPLOYMENT)"
    exit 1
fi

echo "Starting 01-introduction on port 8080..."

JAR_FILE="$SCRIPT_DIR/target/introduction-1.0.0.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found. Building..."
    if ! mvn clean package -DskipTests; then
        echo "Error: Build failed"
        exit 1
    fi
    
    if [ ! -f "$JAR_FILE" ]; then
        echo "Error: Build succeeded but JAR file not created at $JAR_FILE"
        exit 1
    fi
    echo "Build completed successfully"
fi

java -jar "$JAR_FILE"

