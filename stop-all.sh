#!/bin/bash

# Stop all Spring AI for Beginners applications

echo "Stopping all Spring AI for Beginners applications..."

stop_on_port() {
    local port=$1
    local module=$2
    local stopped=false

    if command -v lsof &> /dev/null; then
        local pids
        pids=$(lsof -ti:"$port" 2>/dev/null || true)
        if [ -n "$pids" ]; then
            echo "Stopping $module on port $port (PIDs: $pids)"
            # shellcheck disable=SC2086
            kill -15 $pids 2>/dev/null || true
            sleep 1
            # shellcheck disable=SC2086
            kill -9 $pids 2>/dev/null || true
            if ! lsof -ti:"$port" &>/dev/null; then
                echo "[OK] Successfully stopped $module"
                stopped=true
            else
                echo "Warning: $module may still be running on port $port"
            fi
        fi
    else
        local pids
        pids=$(netstat -ano 2>/dev/null | grep "LISTENING" | grep -w ":$port" | awk '{print $5}' | sort -u)
        if [ -n "$pids" ]; then
            echo "Stopping $module on port $port"
            for pid in $pids; do
                if [[ "$pid" =~ ^[0-9]+$ ]]; then
                    if taskkill //F //PID "$pid" >/dev/null 2>&1; then
                        stopped=true
                    fi
                fi
            done
            if [ "$stopped" = true ]; then
                echo "[OK] Successfully stopped $module"
            fi
        fi
    fi

    if [ "$stopped" = false ]; then
        echo "No process found for $module on port $port"
    fi

    return 0
}

stop_on_port 8080 "01-introduction"
stop_on_port 8083 "02-prompt-engineering"
stop_on_port 8081 "03-rag"
stop_on_port 8084 "04-tools"
stop_on_port 8082 "05-mcp-client"
stop_on_port 8085 "05-mcp-server"
stop_on_port 8086 "06-agents"

for jar in \
    "spring-ai-introduction-1.0.0.jar" \
    "spring-ai-prompt-engineering-1.0.0.jar" \
    "spring-ai-rag-1.0.0.jar" \
    "tools-1.0.0.jar" \
    "spring-ai-mcp-server-1.0.0.jar" \
    "spring-ai-mcp-client-1.0.0.jar" \
    "spring-ai-agents-1.0.0.jar"; do
    if pkill -f "$jar" 2>/dev/null; then
        echo "[OK] Stopped $jar process"
    fi
done

echo ""
echo "All applications stopped."