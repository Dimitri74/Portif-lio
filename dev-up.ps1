param(
    [switch]$StartModules,
    [switch]$KillPorts,
    [switch]$SkipInfra,
    [switch]$DryRun,
    [switch]$RecreateContainers,
    [switch]$WaitForHealth,
    [switch]$PullModels,
    # Omite ms-ia-suporte e mcp-florinda-server (modulos pesados que precisam do Ollama)
    [switch]$SkipAI,
    [int]$HealthTimeoutSec = 360
)

$ErrorActionPreference = 'Stop'
$RepoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$LogRoot = Join-Path $RepoRoot ".dev-logs"

function Write-Step {
    param([string]$Message)
    Write-Host "[dev-up] $Message" -ForegroundColor Cyan
}

function Get-TrimmedFileContent {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return ""
    }

    return ((Get-Content $Path -Raw -ErrorAction SilentlyContinue) -as [string]).Trim()
}

function Invoke-ExternalDetailed {
    param(
        [string]$Command,
        [string[]]$Arguments,
        [switch]$Quiet
    )

    $display = "$Command $($Arguments -join ' ')"
    if ($DryRun) {
        Write-Host "[dry-run] $display" -ForegroundColor Yellow
        return [pscustomobject]@{
            ExitCode = 0
            StdOut   = ""
            StdErr   = ""
            Output   = @()
        }
    }

    $stdoutPath = [System.IO.Path]::GetTempFileName()
    $stderrPath = [System.IO.Path]::GetTempFileName()

    try {
        $process = Start-Process -FilePath $Command -ArgumentList $Arguments -NoNewWindow -Wait -PassThru -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath

        $stdout = Get-TrimmedFileContent -Path $stdoutPath
        $stderr = Get-TrimmedFileContent -Path $stderrPath
        $output = @()

        if ($stdout) {
            $output += ($stdout -split "`r?`n")
        }

        if ($stderr) {
            $output += ($stderr -split "`r?`n")
        }

        if (-not $Quiet) {
            $output | Where-Object { $_ -ne "" } | ForEach-Object { Write-Host $_ }
        }

        return [pscustomobject]@{
            ExitCode = [int]$process.ExitCode
            StdOut   = $stdout
            StdErr   = $stderr
            Output   = $output
        }
    }
    finally {
        Remove-Item $stdoutPath, $stderrPath -Force -ErrorAction SilentlyContinue
    }
}

function Invoke-External {
    param(
        [string]$Command,
        [string[]]$Arguments
    )

    return [int](Invoke-ExternalDetailed -Command $Command -Arguments $Arguments).ExitCode
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

function Get-DockerDesktopPath {
    $candidates = @(
        (Join-Path $env:ProgramFiles 'Docker\Docker\Docker Desktop.exe'),
        (Join-Path $env:LocalAppData 'Programs\Docker\Docker\Docker Desktop.exe')
    )

    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path $candidate)) {
            return $candidate
        }
    }

    return $null
}

function Ensure-DockerDaemon {
    if ($DryRun) {
        Write-Host "[dry-run] docker info --format {{.ServerVersion}}" -ForegroundColor Yellow
        return
    }

    $probeArgs = @("info", "--format", "{{.ServerVersion}}")
    $probe = Invoke-ExternalDetailed -Command "docker" -Arguments $probeArgs -Quiet
    if ($probe.ExitCode -eq 0) {
        Write-Host "[dev-up] Docker daemon: OK (Server $($probe.StdOut))" -ForegroundColor DarkGreen
        return
    }

    $originalError = ($probe.Output | Where-Object { $_ -and $_.Trim() } | Select-Object -First 2) -join ' '
    if (-not $originalError) {
        $originalError = 'docker info returned a non-zero exit code without details.'
    }

    $dockerDesktop = Get-DockerDesktopPath
    if ($dockerDesktop) {
        Write-Warning "Docker CLI encontrado, mas o daemon nao respondeu. Tentando iniciar o Docker Desktop..."

        if (-not (Get-Process -Name 'Docker Desktop' -ErrorAction SilentlyContinue)) {
            Start-Process -FilePath $dockerDesktop | Out-Null
            Start-Sleep -Seconds 3
        }

        $deadline = (Get-Date).AddSeconds(90)
        while ((Get-Date) -lt $deadline) {
            Start-Sleep -Seconds 5
            $probe = Invoke-ExternalDetailed -Command "docker" -Arguments $probeArgs -Quiet
            if ($probe.ExitCode -eq 0) {
                Write-Host "[dev-up] Docker daemon: OK (Server $($probe.StdOut))" -ForegroundColor DarkGreen
                return
            }
        }
    }

    throw @"
Docker CLI encontrado, mas o daemon do Docker nao esta acessivel.

Erro original:
  $originalError

Como resolver:
  1. Abra o Docker Desktop e aguarde o status 'Engine running'.
  2. Confirme com: docker info
  3. Rode novamente: .\dev-up.ps1 -StartModules -KillPorts -WaitForHealth -SkipAI

Se quiser apenas subir os modulos sem infraestrutura Docker ja neste momento,
use: .\dev-up.ps1 -SkipInfra -StartModules -KillPorts -WaitForHealth -SkipAI
"@
}

function Test-PageFile {
    # Verifica se o pagefile do Windows e suficiente para 6 JVMs simultaneos.
    # OOM/net.dll errors no startup quase sempre indicam pagefile pequeno.
    if ($DryRun) { return }
    try {
        $pagefiles = Get-CimInstance -ClassName Win32_PageFileUsage -ErrorAction SilentlyContinue
        if (-not $pagefiles) { return }
        $totalMB = ($pagefiles | Measure-Object -Property AllocatedBaseSize -Sum).Sum
        if ($totalMB -lt 4096) {
            Write-Warning @"
ATENCAO: Windows page file e pequeno (${totalMB} MB). Isso causa OOM/UnsatisfiedLinkError ao
subir 6 JVMs simultaneamente. Sintomas: 'malloc failed', 'net.dll: pagefile too small'.

SOLUCAO (uma vez, requer reinicializacao):
  1. Painel de Controle -> Sistema -> Configuracoes Avancadas do Sistema
  2. Aba 'Avancado' -> Desempenho -> Configuracoes -> Memoria Virtual
  3. Selecione 'Tamanho gerenciado pelo sistema' -> OK -> Reiniciar
  Ou: Tamanho inicial = 8192 MB, Tamanho maximo = 16384 MB
"@
        }
        else {
            Write-Host "[dev-up] Page file: ${totalMB} MB (OK)" -ForegroundColor DarkGreen
        }
    }
    catch {
        Write-Warning "Nao foi possivel verificar o page file: $($_.Exception.Message)"
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
    $ports = @(8080, 8081, 8082, 8083, 8084, 8085)
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

    $modulePattern = 'mvn\s+-pl\s+(ms-catalogo|ms-pedidos|ms-pagamentos|ms-notificacoes|ms-ia-suporte|mcp-florinda-server)\s+quarkus:dev'
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
    # JvmArgs limita o heap do processo Quarkus forked (via -Djvm.args).
    # IsAI = $true marca modulos que dependem do Ollama (podem ser pulados com -SkipAI).
    $modules = @(
        @{ Name = 'ms-catalogo';         Port = 8082; JvmArgs = '-Xmx280m -Xms64m'; IsAI = $false },
        @{ Name = 'ms-pedidos';          Port = 8080; JvmArgs = '-Xmx280m -Xms64m'; IsAI = $false },
        @{ Name = 'ms-pagamentos';       Port = 8081; JvmArgs = '-Xmx280m -Xms64m'; IsAI = $false },
        @{ Name = 'ms-notificacoes';     Port = 8084; JvmArgs = '-Xmx200m -Xms64m'; IsAI = $false },
        @{ Name = 'ms-ia-suporte';       Port = 8083; JvmArgs = '-Xmx384m -Xms64m'; IsAI = $true  },
        @{ Name = 'mcp-florinda-server'; Port = 8085; JvmArgs = '-Xmx200m -Xms64m'; IsAI = $true  }
    )

    Write-Step "Opening PowerShell windows for Quarkus modules"

    if (-not $DryRun -and -not (Test-Path $LogRoot)) {
        New-Item -ItemType Directory -Path $LogRoot | Out-Null
    }

    $isFirst = $true
    foreach ($module in $modules) {
        if ($SkipAI -and $module.IsAI) {
            Write-Host "  Skipping $($module.Name) (flag -SkipAI ativo)" -ForegroundColor DarkGray
            continue
        }

        # Escalonamento: aguarda 20s entre cada start para evitar pico de memoria simultaneo.
        # Sem isso, 6 JVMs inicializando ao mesmo tempo esgotam o pagefile do Windows.
        if (-not $isFirst -and -not $DryRun) {
            Write-Host "  Aguardando 20s antes de subir o proximo modulo (evita OOM por pico de memoria)..." -ForegroundColor DarkGray
            Start-Sleep -Seconds 20
        }
        $isFirst = $false

        $logPath = Join-Path $LogRoot ("{0}.log" -f $module.Name)
        $jvmArgs = $module.JvmArgs

        # MAVEN_OPTS limita o heap do proprio processo Maven (mvn.cmd).
        # Sem isso, o launcher Maven pode falhar com malloc/OutOfMemoryError antes de
        # sequer iniciar o Quarkus (comportamento observado em mcp-florinda-server).
        # -Djvm.args limita o heap do processo Quarkus forked separadamente.
        $cmd = "Set-Location '$RepoRoot'; `$env:MAVEN_OPTS='-Xmx256m -Xms64m'; mvn -pl $($module.Name) `"-Djvm.args=$jvmArgs`" quarkus:dev *>&1 | Tee-Object -FilePath '$logPath'"

        if ($DryRun) {
            Write-Host "[dry-run] powershell -NoExit -Command `"$cmd`"" -ForegroundColor Yellow
            continue
        }

        Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", $cmd | Out-Null
        Write-Host "Started $($module.Name) (expected port $($module.Port))"
    }
}

function Show-ModuleLogHints {
    $allModules  = @('ms-catalogo', 'ms-pedidos', 'ms-pagamentos', 'ms-notificacoes', 'ms-ia-suporte', 'mcp-florinda-server')
    $aiModules   = @('ms-ia-suporte', 'mcp-florinda-server')
    $modules     = if ($SkipAI) { $allModules | Where-Object { $_ -notin $aiModules } } else { $allModules }

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
    $allModules = @(
        @{ Name = 'ms-catalogo';         Port = 8082; IsAI = $false },
        @{ Name = 'ms-pedidos';          Port = 8080; IsAI = $false },
        @{ Name = 'ms-pagamentos';       Port = 8081; IsAI = $false },
        @{ Name = 'ms-notificacoes';     Port = 8084; IsAI = $false },
        @{ Name = 'ms-ia-suporte';       Port = 8083; IsAI = $true  },
        @{ Name = 'mcp-florinda-server'; Port = 8085; IsAI = $true  }
    )

    $modules = if ($SkipAI) { $allModules | Where-Object { -not $_.IsAI } } else { $allModules }

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
    Test-PageFile
}

if ($KillPorts) {
    Stop-OldQuarkusShells
    Start-Sleep -Seconds 2
    Clear-ModulePorts
}

if (-not $SkipInfra) {
    Write-Step "Ensuring Docker infrastructure"
    Ensure-DockerDaemon

    Ensure-Container -Name "florinda-postgres" -RunArgs @(
        "run", "--name", "florinda-postgres",
        "-e", "POSTGRES_DB=catalogo_db",
        "-e", "POSTGRES_USER=florinda",
        "-e", "POSTGRES_PASSWORD=florinda123",
        "-p", "5433:5432",
        "-d", "pgvector/pgvector:pg16"
    )

    if (-not $DryRun) {
        # Aguarda postgres inicializar para criar o segundo banco
        Start-Sleep -Seconds 5
        $dbCheck = docker exec florinda-postgres psql -U florinda -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'ia_suporte_db'" 2>$null
        if ($dbCheck -ne "1") {
            Write-Step "Creating database 'ia_suporte_db' for RAG..."
            docker exec florinda-postgres psql -U florinda -d postgres -c "CREATE DATABASE ia_suporte_db"
        }
    }

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

    # ------------------------------------------------------------------
    # Fase 3 - Ollama (LLM local)
    # Estrategia: se o Ollama ja estiver rodando nativamente na porta
    # 11434 (instalacao local), usa ele diretamente - sem container.
    # Apenas cria o container florinda-ollama se a porta estiver livre.
    # ------------------------------------------------------------------
    $ollamaLocal = $false
    if (-not $DryRun) {
        try {
            $null = Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -TimeoutSec 3
            $ollamaLocal = $true
            Write-Host "[dev-up] Ollama nativo detectado em localhost:11434 - container Docker nao e necessario." -ForegroundColor Green
        }
        catch {
            $ollamaLocal = $false
        }
    }

    if (-not $ollamaLocal) {
        Ensure-Container -Name "florinda-ollama" -RunArgs @(
            "run", "--name", "florinda-ollama",
            "-p", "11434:11434",
            "-v", "florinda-ollama-data:/root/.ollama",
            "-d", "ollama/ollama"
        )
    }

    if ($PullModels) {
        if ($DryRun) {
            Write-Host "[dry-run] ollama pull llama3.2" -ForegroundColor Yellow
            Write-Host "[dry-run] ollama pull nomic-embed-text" -ForegroundColor Yellow
        }
        elseif ($ollamaLocal) {
            Write-Step "Checking Ollama models (native)..."
            $ollamaCmd = Get-Command ollama -ErrorAction SilentlyContinue
            if ($ollamaCmd) {
                $installedModels = & ollama list 2>$null
                if ($installedModels -notmatch "llama3.2") {
                    Write-Step "Pulling llama3.2..."
                    & ollama pull llama3.2
                }
                else {
                    Write-Host "llama3.2 already present."
                }

                if ($installedModels -notmatch "nomic-embed-text") {
                    Write-Step "Pulling nomic-embed-text..."
                    & ollama pull nomic-embed-text
                }
                else {
                    Write-Host "nomic-embed-text already present."
                }
            }
            else {
                Write-Warning "ollama CLI not found in PATH. Verify models manually: http://localhost:11434/api/tags"
            }
        }
        else {
            Write-Step "Waiting for Ollama container to initialize (~8s)..."
            Start-Sleep -Seconds 8
            $ollamaModels = docker exec florinda-ollama ollama list 2>$null
            if ($ollamaModels -notmatch "llama3.2") {
                Write-Step "Pulling llama3.2 (this may take several minutes)..."
                docker exec florinda-ollama ollama pull llama3.2
            }
            else {
                Write-Host "llama3.2 already present."
            }

            if ($ollamaModels -notmatch "nomic-embed-text") {
                Write-Step "Pulling nomic-embed-text..."
                docker exec florinda-ollama ollama pull nomic-embed-text
            }
            else {
                Write-Host "nomic-embed-text already present."
            }
        }
    }

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
Write-Host "Tip: run '.\dev-up.ps1 -StartModules -KillPorts' to start infra + modules."
Write-Host "Tip: run '.\dev-up.ps1 -StartModules -KillPorts -SkipAI' to start apenas os 4 modulos core (sem Ollama/LangChain4j)."
Write-Host "Tip: run '.\dev-up.ps1 -PullModels' to download Ollama LLM models (first run, requires ~3 GB)."
Write-Host "Tip: combine flags: '.\dev-up.ps1 -StartModules -KillPorts -PullModels -WaitForHealth'."
Write-Host "Tip: se der OOM/net.dll error: aumente o pagefile do Windows (Sistema -> Memoria Virtual -> Gerenciado pelo sistema -> Reiniciar)."
Write-Host "Tip: if a container is corrupted/stuck, run '.\dev-up.ps1 -RecreateContainers'."
Write-Host "Tip: add -WaitForHealth to fail fast when a module does not come online."

