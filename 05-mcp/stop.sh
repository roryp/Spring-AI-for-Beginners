#!/bin/bash

# Stop script for 05-mcp module (stops both MCP Server and MCP Client)

SERVER_PORT=8085
CLIENT_PORT=8082
MODULE_NAME="05-mcp"

echo "Stopping $MODULE_NAME..."

stopped=false

# Function to stop application on specific port
stop_on_port() {
    local port=$1
    local label=$2

    if command -v lsof &> /dev/null; then
        local pids=$(lsof -ti:$port 2>/dev/null)
        if [ ! -z "$pids" ]; then
            echo "Stopping $label on port $port (PIDs: $pids)"
            echo "$pids" | xargs kill -9 2>/dev/null
            stopped=true
        fi
    elif command -v fuser &> /dev/null; then
        if fuser $port/tcp &> /dev/null; then
            echo "Stopping $label on port $port"
            fuser -k $port/tcp 2>/dev/null
            stopped=true
        fi
    fi
}

stop_on_port $SERVER_PORT "MCP Server"
stop_on_port $CLIENT_PORT "MCP Client"

# Also stop by jar name
for jar_name in "spring-ai-mcp-server-1.0.0.jar" "spring-ai-mcp-client-1.0.0.jar"; do
    pids=$(pgrep -f "$jar_name" 2>/dev/null)
    if [ ! -z "$pids" ]; then
        echo "Stopping $jar_name (PIDs: $pids)"
        echo "$pids" | xargs kill -9 2>/dev/null
        stopped=true
    fi
done

if [ "$stopped" = true ]; then
    echo "$MODULE_NAME stopped."
else
    echo "$MODULE_NAME is not running."
fi
