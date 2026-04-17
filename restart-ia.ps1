param(
    [switch]$KillPort
)

$ErrorActionPreference = 'Stop'
$RepoRoot  = Split-Path -Parent $MyInvocation.MyCommand.Path
$LogPath   = Join-Path $RepoRoot ".dev-logs\ms-ia-suporte-solo.log"

# ── 1. Mata processo que estiver escutando na porta 8083 ─────────────────────
$conns = Get-NetTCPConnection -LocalPort 8083 -State Listen -ErrorAction SilentlyContinue
if ($conns) {
    $pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($p in $pids) {
        try {
            Stop-Process -Id $p -Force -ErrorAction Stop
            Write-Host "[restart-ia] Encerrado PID $p (porta 8083)" -ForegroundColor Yellow
        } catch {
            Write-Warning "Nao foi possivel encerrar PID ${p}: $($_.Exception.Message)"
        }
    }
    Start-Sleep -Seconds 2
} else {
    Write-Host "[restart-ia] Porta 8083 livre." -ForegroundColor DarkGray
}

# ── 2. Monta o comando evitando o problema de quoting com -Djvm.args ─────────
# Usar JAVA_TOOL_OPTIONS em vez de -Djvm.args resolve o split no espaco.
$script = @"
Set-Location '$RepoRoot'
`$env:MAVEN_OPTS      = '-Xmx256m -Xms64m'
`$env:JAVA_TOOL_OPTIONS = '-Xmx384m -Xms64m'
`$env:OTEL_SDK_DISABLED = 'true'
mvn -pl ms-ia-suporte quarkus:dev *>&1 | Tee-Object -FilePath '$LogPath'
"@

# ── 3. Abre nova janela PowerShell com título identificável ──────────────────
Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", $script
Write-Host "[restart-ia] ms-ia-suporte iniciando... acompanhe em:" -ForegroundColor Cyan
Write-Host "  $LogPath" -ForegroundColor Cyan
Write-Host ""
Write-Host "Para aguardar o health check:" -ForegroundColor DarkGray
Write-Host '  do { Start-Sleep 8; $r = try { Invoke-WebRequest http://localhost:8083/q/health -TimeoutSec 4 -UseBasicParsing -EA Stop } catch {} } while (-not $r); Write-Host "ONLINE!"' -ForegroundColor DarkGray
