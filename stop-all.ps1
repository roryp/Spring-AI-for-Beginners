# Stop all Spring AI for Beginners applications

$ErrorActionPreference = "Continue"

Write-Host "Stopping all Spring AI for Beginners applications..."

function Stop-OnPort {
    param([int]$Port, [string]$Module)

    $stopped = $false
    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if ($connections) {
        $procIds = $connections | Select-Object -ExpandProperty OwningProcess -Unique
        Write-Host "Stopping $Module on port $Port (PIDs: $($procIds -join ', '))"

        foreach ($procId in $procIds) {
            try {
                Stop-Process -Id $procId -Force -ErrorAction Stop
                $stopped = $true
            } catch {
                Write-Host "Warning: Could not stop process $procId"
            }
        }

        if ($stopped) {
            Write-Host "[OK] Successfully stopped $Module" -ForegroundColor Green
        }
    } else {
        Write-Host "No process found for $Module on port $Port"
    }
}

Stop-OnPort -Port 8080 -Module "01-introduction"
Stop-OnPort -Port 8083 -Module "02-prompt-engineering"
Stop-OnPort -Port 8081 -Module "03-rag"
Stop-OnPort -Port 8084 -Module "04-tools"
Stop-OnPort -Port 8082 -Module "05-mcp-client"
Stop-OnPort -Port 8085 -Module "05-mcp-server"
Stop-OnPort -Port 8086 -Module "06-agents"

$jarPattern = "spring-ai-introduction-1\.0\.0\.jar|spring-ai-prompt-engineering-1\.0\.0\.jar|spring-ai-rag-1\.0\.0\.jar|tools-1\.0\.0\.jar|spring-ai-mcp-server-1\.0\.0\.jar|spring-ai-mcp-client-1\.0\.0\.jar|spring-ai-agents-1\.0\.0\.jar"

$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue
foreach ($proc in $javaProcesses) {
    $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($proc.Id)" -ErrorAction SilentlyContinue).CommandLine
    if ($cmdLine -and ($cmdLine -match $jarPattern)) {
        try {
            Stop-Process -Id $proc.Id -Force -ErrorAction Stop
            Write-Host "[OK] Stopped Java process $($proc.Id)" -ForegroundColor Green
        } catch {
            Write-Host "Warning: Could not stop Java process $($proc.Id)"
        }
    }
}

# Give the OS a moment to release file handles, then clean up empty .log.err
# files created by Start-Process -RedirectStandardError (Spring Boot writes
# everything to stdout, so these are almost always empty).
Start-Sleep -Milliseconds 500
Get-ChildItem -Path $PSScriptRoot -Recurse -Filter *.log.err -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Length -eq 0 } |
    ForEach-Object { Remove-Item $_.FullName -Force -ErrorAction SilentlyContinue }

Write-Host ""
Write-Host "All applications stopped." -ForegroundColor Green