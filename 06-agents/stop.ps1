# Stop script for 06-agents module

$ErrorActionPreference = "Continue"

$ScriptDir = $PSScriptRoot
$Port = 8086
$ModuleName = "06-agents"
$JarName = "spring-ai-agents-1.0.0.jar"

Write-Host "Stopping $ModuleName..."

$stopped = $false

# Stop by port
try {
    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if ($connections) {
        $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
        Write-Host "Stopping processes on port $Port (PIDs: $($pids -join ', '))"

        foreach ($processId in $pids) {
            try {
                Stop-Process -Id $processId -Force -ErrorAction Stop
                $stopped = $true
            } catch {
                Write-Host "Warning: Could not stop process $processId"
            }
        }

        if ($stopped) {
            Write-Host "Successfully stopped processes on port $Port"
        }
    }
} catch {
    Write-Host "No process found on port $Port"
}

# Stop by process name
if (-not $stopped) {
    $javaProcs = Get-Process java -ErrorAction SilentlyContinue | Where-Object {
        $_.CommandLine -like "*$JarName*"
    }
    if ($javaProcs) {
        $javaProcs | Stop-Process -Force
        Write-Host "Stopped $ModuleName Java process"
    } else {
        Write-Host "$ModuleName does not appear to be running"
    }
}
