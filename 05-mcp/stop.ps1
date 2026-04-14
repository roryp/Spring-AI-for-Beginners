# Stop script for 05-mcp module (stops both MCP Server and MCP Client)

$ErrorActionPreference = "Continue"

$ServerPort = 8080
$ClientPort = 8081
$ModuleName = "05-mcp"

Write-Host "Stopping $ModuleName..."

$stopped = $false

foreach ($port in @($ServerPort, $ClientPort)) {
    $label = if ($port -eq $ServerPort) { "MCP Server" } else { "MCP Client" }
    try {
        $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($connections) {
            $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
            Write-Host "Stopping $label on port $port (PIDs: $($pids -join ', '))"
            foreach ($pid in $pids) {
                Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            }
            $stopped = $true
        }
    }
    catch {
        # Port not in use
    }
}

# Also stop by jar name
foreach ($jarName in @("spring-ai-mcp-server-1.0.0.jar", "spring-ai-mcp-client-1.0.0.jar")) {
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue |
        Where-Object { $_.CommandLine -like "*$jarName*" }
    foreach ($proc in $javaProcesses) {
        Write-Host "Stopping process $($proc.Id) ($jarName)"
        Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        $stopped = $true
    }
}

if ($stopped) {
    Write-Host "$ModuleName stopped."
} else {
    Write-Host "$ModuleName is not running."
}
