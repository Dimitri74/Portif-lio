param(
    [switch]$StartModules,
    [switch]$KillPorts,
    [switch]$SkipInfra,
    [switch]$DryRun,
    [switch]$RecreateContainers,
    [switch]$WaitForHealth,
    [int]$HealthTimeoutSec = 180
)

$ErrorActionPreference = 'Stop'
$RepoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$LogRoot = Join-Path $RepoRoot ".dev-logs"

function Write-Step {
    param([string]$Message)
    Write-Host "[dev-up] $Message" -ForegroundColor Cyan
}

function Invoke-External {
    param(
        [string]$Command,
        [string[]]$Arguments
    )

    $display = "$Command $($Arguments -join ' ')"
    if ($DryRun) {
        Write-Host "[dry-run] $display" -ForegroundColor Yellow
        return 0
    }

    $output = & $Command @Arguments 2>&1
    $exitCode = $LASTEXITCODE

    if ($output) {
        $output | ForEach-Object { Write-Host $_ }
    }

    return [int]$exitCode
}

function Get-ContainerDiagnostics {
    param([string]$Name)

    if ($DryRun) {
        return
    }

    Write-Host "[dev-up] Diagnostics for '$Name'" -ForegroundColor DarkYellow
    docker inspect --format "status={{.State.Status}} error={{.State.Error}} oom={{.State.OOMKilled}} exit={{.State.ExitCode}}" $Name 2>$null
    docker logs --tail 40 $Name 2>$null
}

function Recreate-Container {
    param(
        [string]$Name,
        [string[]]$RunArgs
    )

    Write-Step "Recreating container '$Name'"

    $rmCode = Invoke-External -Command "docker" -Arguments @("rm", "-f", $Name)
    if ($rmCode -ne 0) {
        throw "Failed to remove container '$Name' before recreation."
    }

    $runCode = Invoke-External -Command "docker" -Arguments $RunArgs
    if ($runCode -ne 0) {
        throw "Failed to recreate container '$Name'."
    }
}

function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Command '$Name' not found in PATH. Open a terminal with Java/Maven/Docker configured and try again."
    }
}

function Ensure-Container {
    param(
        [string]$Name,
        [string[]]$RunArgs
    )

    Write-Step "Ensuring container '$Name'"

    if ($DryRun) {
        Write-Host "[dry-run] docker ps -a --filter name=^/$Name$ --format {{.Names}}" -ForegroundColor Yellow
        Write-Host "[dry-run] docker start $Name (if exists) OR docker $($RunArgs -join ' ')" -ForegroundColor Yellow
        return
    }

    $existing = docker ps -a --filter "name=^/$Name$" --format "{{.Names}}"
    if ($existing -eq $Name) {
        if ($RecreateContainers) {
            Recreate-Container -Name $Name -RunArgs $RunArgs
            return
        }

        $code = Invoke-External -Command "docker" -Arguments @("start", $Name)
        if ($code -ne 0) {
            Get-ContainerDiagnostics -Name $Name

            throw "Failed to start container '$Name'. Run '.\\dev-up.ps1 -RecreateContainers' to force recreation."
        }
    }
    else {
        $code = Invoke-External -Command "docker" -Arguments $RunArgs
        if ($code -ne 0) {
            throw "Failed to create container '$Name'."
        }
    }
}

function Clear-ModulePorts {
    $ports = @(8080, 8081, 8082, 8084)
    Write-Step "Checking module ports: $($ports -join ', ')"

    foreach ($port in $ports) {
        $listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if (-not $listeners) {
            continue
        }

        $pids = $listeners | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($portPid in $pids) {
            if ($DryRun) {
                Write-Host "[dry-run] Stop-Process -Id $portPid -Force  # port $port" -ForegroundColor Yellow
                continue
            }

            try {
                Stop-Process -Id $portPid -Force -ErrorAction Stop
                Write-Host "Stopped PID $portPid on port $port"
            }
            catch {
                Write-Warning "Could not stop PID $portPid on port ${port}: $($_.Exception.Message)"
            }
        }
    }
}

function Stop-OldQuarkusShells {
    Write-Step "Stopping old quarkus:dev shells"

    $modulePattern = 'mvn\s+-pl\s+(ms-catalogo|ms-pedidos|ms-pagamentos|ms-notificacoes)\s+quarkus:dev'
    $currentPid = $PID

    $procs = Get-CimInstance Win32_Process -Filter "name = 'powershell.exe'"
    foreach ($proc in $procs) {
        if ($proc.ProcessId -eq $currentPid) {
            continue
        }

        if ($proc.CommandLine -and $proc.CommandLine -match $modulePattern) {
            if ($DryRun) {
                Write-Host "[dry-run] Stop-Process -Id $($proc.ProcessId) -Force  # old quarkus shell" -ForegroundColor Yellow
                continue
            }

            try {
                Stop-Process -Id $proc.ProcessId -Force -ErrorAction Stop
                Write-Host "Stopped old quarkus shell PID $($proc.ProcessId)"
            }
            catch {
                Write-Warning "Could not stop old quarkus shell PID $($proc.ProcessId): $($_.Exception.Message)"
            }
        }
    }
}

function Start-Modules {
    $modules = @(
        @{ Name = 'ms-catalogo'; Port = 8082 },
        @{ Name = 'ms-pedidos'; Port = 8080 },
        @{ Name = 'ms-pagamentos'; Port = 8081 },
        @{ Name = 'ms-notificacoes'; Port = 8084 }
    )

    Write-Step "Opening PowerShell windows for Quarkus modules"

    if (-not $DryRun -and -not (Test-Path $LogRoot)) {
        New-Item -ItemType Directory -Path $LogRoot | Out-Null
    }

    foreach ($module in $modules) {
        $logPath = Join-Path $LogRoot ("{0}.log" -f $module.Name)
        $cmd = "Set-Location '$RepoRoot'; mvn -pl $($module.Name) quarkus:dev *>&1 | Tee-Object -FilePath '$logPath'"
        if ($DryRun) {
            Write-Host "[dry-run] powershell -NoExit -Command \"$cmd\"" -ForegroundColor Yellow
            continue
        }

        Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", $cmd | Out-Null
        Write-Host "Started $($module.Name) (expected port $($module.Port))"
    }
}

function Show-ModuleLogHints {
    $modules = @('ms-catalogo', 'ms-pedidos', 'ms-pagamentos', 'ms-notificacoes')

    Write-Host "[dev-up] Log hints (.dev-logs):" -ForegroundColor DarkYellow
    foreach ($name in $modules) {
        $logPath = Join-Path $LogRoot ("{0}.log" -f $name)
        if (Test-Path $logPath) {
            Write-Host ("--- {0} ({1}) ---" -f $name, $logPath)
            Get-Content $logPath -Tail 20
        }
        else {
            Write-Host ("--- {0}: log not created yet ---" -f $name)
        }
    }
}

function Test-ModuleHealth {
    param([int]$Port)

    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri ("http://localhost:{0}/q/health" -f $Port) -TimeoutSec 4
        return ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300)
    }
    catch {
        return $false
    }
}

function Wait-ModulesHealthy {
    $modules = @(
        @{ Name = 'ms-catalogo'; Port = 8082 },
        @{ Name = 'ms-pedidos'; Port = 8080 },
        @{ Name = 'ms-pagamentos'; Port = 8081 },
        @{ Name = 'ms-notificacoes'; Port = 8084 }
    )

    Write-Step "Waiting modules health for up to ${HealthTimeoutSec}s"
    $deadline = (Get-Date).AddSeconds($HealthTimeoutSec)
    $healthy = @{}

    foreach ($m in $modules) {
        $healthy[$m.Name] = $false
    }

    while ((Get-Date) -lt $deadline) {
        foreach ($m in $modules) {
            if (-not $healthy[$m.Name]) {
                $healthy[$m.Name] = Test-ModuleHealth -Port $m.Port
            }
        }

        $pending = $healthy.Keys | Where-Object { -not $healthy[$_] }
        if ($pending.Count -eq 0) {
            Write-Host "All modules are ONLINE (/q/health)." -ForegroundColor Green
            return
        }

        Start-Sleep -Seconds 5
    }

    Write-Warning "Some modules did not become healthy in ${HealthTimeoutSec}s:"
    foreach ($m in $modules) {
        if ($healthy[$m.Name]) {
            Write-Host (" - {0} (port {1}): ONLINE" -f $m.Name, $m.Port) -ForegroundColor Green
        }
        else {
            Write-Host (" - {0} (port {1}): OFFLINE" -f $m.Name, $m.Port) -ForegroundColor Red
        }
    }

    Show-ModuleLogHints

    throw "One or more modules are offline. Check the PowerShell windows opened by dev-up for startup errors."
}

Write-Step "Validating prerequisites"
Require-Command -Name "java"
Require-Command -Name "mvn"
Require-Command -Name "docker"

if (-not $DryRun) {
    java -version
    mvn -v
    docker --version
}

if ($KillPorts) {
    Stop-OldQuarkusShells
    Start-Sleep -Seconds 2
    Clear-ModulePorts
}

if (-not $SkipInfra) {
    Write-Step "Ensuring Docker infrastructure"

    Ensure-Container -Name "florinda-postgres" -RunArgs @(
        "run", "--name", "florinda-postgres",
        "-e", "POSTGRES_DB=catalogo_db",
        "-e", "POSTGRES_USER=florinda",
        "-e", "POSTGRES_PASSWORD=florinda123",
        "-p", "5433:5432",
        "-d", "postgres:16"
    )

    Ensure-Container -Name "florinda-redis" -RunArgs @(
        "run", "--name", "florinda-redis",
        "-p", "6379:6379",
        "-d", "redis:7"
    )

    Ensure-Container -Name "florinda-mysql-pedidos" -RunArgs @(
        "run", "--name", "florinda-mysql-pedidos",
        "-e", "MYSQL_ROOT_PASSWORD=root",
        "-e", "MYSQL_DATABASE=pedidos_db",
        "-e", "MYSQL_USER=florinda",
        "-e", "MYSQL_PASSWORD=florinda123",
        "-p", "3307:3306",
        "-d", "mysql:8.0"
    )

    Ensure-Container -Name "florinda-mysql-pagamentos" -RunArgs @(
        "run", "--name", "florinda-mysql-pagamentos",
        "-e", "MYSQL_ROOT_PASSWORD=root",
        "-e", "MYSQL_DATABASE=pagamentos_db",
        "-e", "MYSQL_USER=florinda",
        "-e", "MYSQL_PASSWORD=florinda123",
        "-p", "3308:3306",
        "-d", "mysql:8.0"
    )

    Ensure-Container -Name "florinda-kafka" -RunArgs @(
        "run", "--name", "florinda-kafka",
        "-p", "9092:9092",
        "-e", "KAFKA_NODE_ID=1",
        "-e", "KAFKA_PROCESS_ROLES=broker,controller",
        "-e", "KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093",
        "-e", "KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092",
        "-e", "KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER",
        "-e", "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT",
        "-e", "KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093",
        "-e", "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1",
        "-e", "KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1",
        "-e", "KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1",
        "-e", "KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS=0",
        "-d", "apache/kafka:3.9.0"
    )

    if (-not $DryRun) {
        docker ps --format "table {{.Names}}`t{{.Status}}`t{{.Ports}}"
    }
}

if ($StartModules) {
    Start-Modules

    if ($WaitForHealth -and -not $DryRun) {
        Wait-ModulesHealthy
    }
}

Write-Step "Done."
Write-Host "Tip: run '.\\dev-up.ps1 -StartModules -KillPorts' to start infra + modules."
Write-Host "Tip: if a container is corrupted/stuck, run '.\\dev-up.ps1 -RecreateContainers'."
Write-Host "Tip: add -WaitForHealth to fail fast when a module does not come online."

