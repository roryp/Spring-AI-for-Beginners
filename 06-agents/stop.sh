#!/bin/bash

# Stop script for 06-agents module

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PORT=8086
MODULE_NAME="06-agents"
JAR_NAME="spring-ai-agents-1.0.0.jar"

echo "Stopping $MODULE_NAME..."

# Function to stop application on specific port
stop_on_port() {
    local port=$1
    local stopped=false

    if command -v lsof &> /dev/null; then
        local pids=$(lsof -ti:$port 2>/dev/null)
        if [ ! -z "$pids" ]; then
            echo "Stopping processes on port $port (PIDs: $pids)"
            kill -15 $pids 2>/dev/null || kill -9 $pids 2>/dev/null
            sleep 1
            if ! lsof -ti:$port &>/dev/null; then
                echo "Successfully stopped processes on port $port"
                stopped=true
            else
                echo "Warning: Some processes may still be running on port $port"
            fi
        fi
    elif command -v ss &> /dev/null; then
        local pids=$(ss -tlnp "sport = :$port" 2>/dev/null | grep -oP 'pid=\K[0-9]+')
        if [ ! -z "$pids" ]; then
            echo "Stopping processes on port $port (PIDs: $pids)"
            kill -15 $pids 2>/dev/null || kill -9 $pids 2>/dev/null
            stopped=true
        fi
    else
        # Windows Git Bash fallback via netstat + taskkill
        local pids=$(netstat -ano | grep "LISTENING" | grep -w ":$port" | awk '{print $5}' | sort -u)
        if [ ! -z "$pids" ]; then
            echo "Stopping processes on port $port (PIDs: $pids)"
            for pid in $pids; do
                if [[ "$pid" =~ ^[0-9]+$ ]]; then
                    taskkill //F //PID $pid 2>/dev/null && stopped=true
                fi
            done
        fi
    fi

    if [ "$stopped" = false ]; then
        echo "No process found on port $port"
    fi
}

stop_on_port $PORT
