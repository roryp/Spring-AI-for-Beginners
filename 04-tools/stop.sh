#!/bin/bash

# Stop script for 04-tools module

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PORT=8084
MODULE_NAME="04-tools"
JAR_NAME="tools-1.0.0.jar"

echo "Stopping $MODULE_NAME..."

# Function to stop application on specific port
stop_on_port() {
    local port=$1
    local stopped=false
    
    if command -v lsof &> /dev/null; then
        # Use lsof if available (Mac/Linux)
        local pids=$(lsof -ti:$port 2>/dev/null)
        if [ ! -z "$pids" ]; then
            echo "Stopping processes on port $port (PIDs: $pids)"
            kill -15 $pids 2>/dev/null || kill -9 $pids 2>/dev/null
            sleep 1
            # Verify stopped
            if ! lsof -ti:$port &>/dev/null; then
                echo "✓ Successfully stopped processes on port $port"
                stopped=true
            else
                echo "Warning: Some processes may still be running on port $port"
            fi
        fi
    else
        # Use netstat for Windows Git Bash
        local pids=$(netstat -ano | grep "LISTENING" | grep -w ":$port" | awk '{print $5}' | sort -u)
        if [ ! -z "$pids" ]; then
            echo "Stopping processes on port $port"
            for pid in $pids; do
                if [[ "$pid" =~ ^[0-9]+$ ]]; then
                    taskkill //F //PID $pid 2>/dev/null && stopped=true
                fi
            done
            if [ "$stopped" = true ]; then
                echo "✓ Successfully stopped processes on port $port"
            fi
        fi
    fi
    
    if [ "$stopped" = false ]; then
        echo "No process found running on port $port"
        return 1
    fi
    return 0
}

# Stop by port
stopped_by_port=false
if stop_on_port $PORT; then
    stopped_by_port=true
fi

# Also try to kill by JAR name if not already stopped
if [ "$stopped_by_port" = false ]; then
    if pkill -f "$JAR_NAME" 2>/dev/null; then
        echo "✓ Stopped $JAR_NAME process"
        stopped_by_port=true
    fi
fi

if [ "$stopped_by_port" = false ]; then
    echo "No running instance of $MODULE_NAME found"
    exit 1
else
    echo "$MODULE_NAME stopped successfully"
    exit 0
fi

