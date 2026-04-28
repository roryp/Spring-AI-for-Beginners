#!/bin/bash

# Start all Spring AI for Beginners applications
# Sources the centralized .env file and starts all modules 01-06
# (including 05-mcp server and client, in that order).

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [ ! -f "$ENV_FILE" ]; then
    echo "Error: .env file not found at $ENV_FILE"
    echo "Please copy .env.example to .env and fill in your credentials"
    exit 1
fi

echo "Loading environment variables from $ENV_FILE"
set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

if [ -z "$AZURE_OPENAI_ENDPOINT" ] || [ -z "$AZURE_OPENAI_API_KEY" ] || [ -z "$AZURE_OPENAI_DEPLOYMENT" ] || [ -z "$AZURE_OPENAI_FAST_DEPLOYMENT" ]; then
    echo "Error: Missing required environment variables"
    echo "Please ensure AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY, AZURE_OPENAI_DEPLOYMENT, and AZURE_OPENAI_FAST_DEPLOYMENT are set in .env"
    exit 1
fi

echo "Environment variables loaded successfully"
echo "AZURE_OPENAI_ENDPOINT: $AZURE_OPENAI_ENDPOINT"
echo "AZURE_OPENAI_DEPLOYMENT: $AZURE_OPENAI_DEPLOYMENT"
echo "AZURE_OPENAI_FAST_DEPLOYMENT: $AZURE_OPENAI_FAST_DEPLOYMENT"

is_port_in_use() {
    local port=$1
    if command -v lsof &> /dev/null; then
        lsof -Pi :"$port" -sTCP:LISTEN -t >/dev/null 2>&1
    elif command -v ss &> /dev/null; then
        ss -lnt "sport = :$port" 2>/dev/null | grep -q LISTEN
    else
        netstat -ano 2>/dev/null | grep "LISTENING" | grep -w ":$port" >/dev/null 2>&1
    fi
}

wait_for_port() {
    local port=$1
    local max_wait=60
    local count=0
    while [ $count -lt $max_wait ]; do
        if is_port_in_use "$port"; then
            return 0
        fi
        sleep 1
        count=$((count + 1))
    done
    return 1
}

start_app() {
    local module=$1
    local port=$2
    local jar_name=$3
    local module_dir="$SCRIPT_DIR/$module"
    local jar_file="$module_dir/target/$jar_name"
    # Use only the leaf segment for the log file name so sub-module paths
    # (e.g. "05-mcp/mcp-server") don't produce invalid file names.
    local log_base
    log_base="$(basename "$module")"
    local log_file="$module_dir/$log_base.log"

    echo ""
    echo "Starting $module on port $port..."

    if is_port_in_use "$port"; then
        echo "Warning: Port $port is already in use. Skipping $module"
        return 0
    fi

    if [ ! -f "$jar_file" ]; then
        echo "JAR file not found. Building $module..."
        if ! ( cd "$module_dir" && mvn clean package -DskipTests ); then
            echo "Error: Build failed for $module"
            return 1
        fi
        if [ ! -f "$jar_file" ]; then
            echo "Error: Build succeeded but JAR file not created at $jar_file"
            return 1
        fi
        echo "Build completed successfully"
    fi

    ( cd "$module_dir" && nohup java -jar "$jar_file" > "$log_file" 2>&1 & )

    echo "Started $module (log: $log_file), waiting for port $port..."

    if wait_for_port "$port"; then
        echo "[OK] $module is running on port $port"
        return 0
    else
        echo "[FAIL] $module failed to start. Check $log_file for details"
        return 1
    fi
}

failed_modules=()

# 05-mcp: server MUST start before client (client connects to server on startup)
modules=(
    "01-introduction 8080 spring-ai-introduction-1.0.0.jar"
    "02-prompt-engineering 8083 spring-ai-prompt-engineering-1.0.0.jar"
    "03-rag 8081 spring-ai-rag-1.0.0.jar"
    "04-tools 8084 tools-1.0.0.jar"
    "05-mcp/mcp-server 8085 spring-ai-mcp-server-1.0.0.jar"
    "05-mcp/mcp-client 8082 spring-ai-mcp-client-1.0.0.jar"
    "06-agents 8086 spring-ai-agents-1.0.0.jar"
)

for entry in "${modules[@]}"; do
    # shellcheck disable=SC2086
    set -- $entry
    if ! start_app "$1" "$2" "$3"; then
        failed_modules+=("$1")
    fi
done

echo ""
echo "============================================"
if [ ${#failed_modules[@]} -eq 0 ]; then
    echo "All applications started successfully!"
else
    echo "Some applications failed to start:"
    for module in "${failed_modules[@]}"; do
        echo "  - $module"
    done
fi
echo ""
echo "Running applications:"
echo "01-introduction:       http://localhost:8080"
echo "02-prompt-engineering: http://localhost:8083"
echo "03-rag:                http://localhost:8081"
echo "04-tools:              http://localhost:8084"
echo "05-mcp server:         http://localhost:8085"
echo "05-mcp client:         http://localhost:8082"
echo "06-agents:             http://localhost:8086"
echo "============================================"
echo ""
echo "To stop all applications, run: ./stop-all.sh"