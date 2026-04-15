# Florinda Eats Quickstart (Fase 1 · Fase 2 · Fase 3 IA · Fase 4 Observabilidade)

Guia rapido para subir a infraestrutura local, iniciar os modulos, habilitar a observabilidade da Fase 4 e validar o fluxo no Swagger.

> **Fase 3 (IA):** para documentacao completa do agente, veja `AgenteFlorindaIA.md`.

## 1) Pre-requisitos

- Windows + PowerShell
- Docker Desktop iniciado
- Maven 3.9+ (`mvn -v`)
- JDK 21 ativo no terminal

## 2) Ajustar Java e Maven no terminal atual

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + (($env:Path -split ';' | Where-Object { $_ -and ($_ -notmatch 'Oracle\\Java\\javapath') -and ($_ -notmatch 'Java\\jdk-25') } | Select-Object -Unique) -join ';')

# fallback para o Maven neste projeto
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
  $env:Path += ";C:\Users\marcu\tools\apache-maven-3.9.9\bin"
}

java -version
mvn -v
```

## 3) Verificar Docker

```powershell
docker --version
docker info --format '{{.ServerVersion}}'
docker ps
```

## 4) Subir infraestrutura minima

No primeiro uso, rode `docker run`. Se o container ja existir, use `docker start`.

### 4.1 PostgreSQL (ms-catalogo) - porta 5433

```powershell
docker run --name florinda-postgres -e POSTGRES_DB=catalogo_db -e POSTGRES_USER=florinda -e POSTGRES_PASSWORD=florinda123 -p 5433:5432 -d pgvector/pgvector:pg16
docker start florinda-postgres
```

### 4.2 Redis (ms-catalogo) - porta 6379

```powershell
docker run --name florinda-redis -p 6379:6379 -d redis:7
docker start florinda-redis
```

### 4.3 MySQL (ms-pedidos) - porta 3307

```powershell
docker run --name florinda-mysql-pedidos -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=pedidos_db -e MYSQL_USER=florinda -e MYSQL_PASSWORD=florinda123 -p 3307:3306 -d mysql:8.0
docker start florinda-mysql-pedidos
```

### 4.4 MySQL (ms-pagamentos) - porta 3308

```powershell
docker run --name florinda-mysql-pagamentos -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=pagamentos_db -e MYSQL_USER=florinda -e MYSQL_PASSWORD=florinda123 -p 3308:3306 -d mysql:8.0
docker start florinda-mysql-pagamentos
```

### 4.5 Kafka (eventos entre pedidos e pagamentos) - porta 9092

```powershell
docker run --name florinda-kafka -p 9092:9092 -d `
  -e KAFKA_NODE_ID=1 `
  -e KAFKA_PROCESS_ROLES=broker,controller `
  -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 `
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 `
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER `
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT `
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 `
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 `
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 `
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 `
  -e KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS=0 `
  apache/kafka:3.9.0

docker start florinda-kafka
```

### 4.6 Ollama (LLM local — Fase 3) - porta 11434

**Opção A — Ollama instalado nativamente (recomendado)**

Se o Ollama já esta instalado na maquina (https://ollama.com/download), o servico sobe automaticamente no Windows e responde em `http://localhost:11434`. Nenhum container Docker e necessario.

Verificar:
```powershell
Invoke-RestMethod http://localhost:11434/api/tags
```

**Opção B — Ollama via Docker** (somente sem instalacao nativa)

```powershell
docker run --name florinda-ollama -p 11434:11434 -v florinda-ollama-data:/root/.ollama -d ollama/ollama
docker start florinda-ollama
```

> O `dev-up.ps1` detecta automaticamente o Ollama nativo e pula o container Docker.

### 4.7 Baixar modelos LLM (apenas uma vez — ~2.2 GB no total)

```powershell
# Com Ollama nativo:
ollama pull llama3.2
ollama pull nomic-embed-text

# Com Ollama via Docker:
docker exec florinda-ollama ollama pull llama3.2
docker exec florinda-ollama ollama pull nomic-embed-text
```

Ou use o script (detecta automaticamente qual modo esta ativo):

```powershell
.\dev-up.ps1 -PullModels
```

### 4.8 Criar banco ia_suporte_db (apenas uma vez)

```powershell
$dbExists = docker exec florinda-postgres psql -U florinda -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'ia_suporte_db'"
if ($dbExists -ne '1') {
  docker exec florinda-postgres psql -U florinda -d postgres -c "CREATE DATABASE ia_suporte_db"
}
```

> O `dev-up.ps1` ja faz isso automaticamente.

### 4.9 Observabilidade local (Fase 4) — Prometheus + Grafana + Jaeger

```powershell
docker compose -f .\observabilidade\docker-compose.yml up -d
docker compose -f .\observabilidade\docker-compose.yml ps
```

URLs:

- Grafana: `http://localhost:3000` (`admin` / `admin`)
- Prometheus: `http://localhost:9090`
- Jaeger: `http://localhost:16686`

## 5) Subir os modulos (terminais separados)

```powershell
Set-Location "C:\Users\marcu\workspace\Projeto\florinda-eats"
```

### ⚠️ AVISO CRÍTICO: Usar `dev-up.ps1` com `-StartModules` vs Fluxo Manual

> **RESUMO:** Subir os 6 módulos **simultaneamente** via `.\dev-up.ps1 -StartModules` tem **alta taxa de falha** em Windows. Recomendamos:
> 
> - ✅ **Para primeira vez / validação rápida:** use `.\dev-up.ps1` **SEM** `-StartModules` para subir apenas containers/observabilidade, depois suba os módulos manualmente em terminais separados (seção 5 abaixo).
> - ✅ **Para máquinas com 16+ GB RAM e pagefile configurado:** pode tentar `.\dev-up.ps1 -StartModules -KillPorts -SkipObservability -WaitForHealth`, mas monitore os logs.
> - ❌ **NÃO recomendado:** `.\dev-up.ps1 -StartModules -KillPorts` em Windows com pagefile < 4 GB → vai dar OOM/malloc failed.

**Por quê?** 6 JVMs inicializando simultaneamente = pico de 3+ GB memória + contaminação OTEL. Fluxo manual = 1 terminal por vez, tempo total ~5-10 minutos, 100% confiável.

**Sintomas de falha do script:**
- `malloc failed` ou `net.dll: pagefile too small`
- Health check timeout após 360 segundos
- Módulos aparecem como "Started" mas porta não responde
- Logs fragmentados em `.dev-logs/*.log` em vez de visível no terminal

**Problemas técnicos específicos (análise shell experiente):**

| Problema | Causa | Por que subir junto falha |
|----------|-------|--------------------------|
| **Race Condition Docker** | PostgreSQL/MySQL precisam 10-15s para estar totalmente ready | Script aguarda 5s fixo, depois tenta usar DB que ainda está inicializando |
| **Memory Pressure** | Cada módulo = Maven (256 MB) + JVM Quarkus (280-384 MB) | 6 módulos × 600 MB = 3.6 GB de pico simultâneo → OOM se pagefile < 4 GB |
| **Subshells Async** | Script abre 6 PowerShells via `Start-Process \| Out-Null` | Nenhuma sincronização real; health check começa antes de Maven compilar |
| **Environment Isolation** | Variáveis `$env:JAVA_HOME` e PATH definidas em subshells | Subshell novo não herda PATH do terminal principal → java/mvn não encontrado |
| **OTEL Collector** | Observabilidade ativada por padrão envia telemetria para localhost:4317 | 6 JVMs disparando métricas simultâneas → Collector pode estar lento → timeout em startup |
| **Logs Invisíveis** | Output redirecionado para `.dev-logs/*.log` | Você não vê qual módulo travou e aonde; script mata tudo após 360s |

**Comparação: fluxo manual vs script**

```
SCRIPT dev-up.ps1 -StartModules          | FLUXO MANUAL (RECOMENDADO)
================================================|================================================
6 JVMs em 120 segundos                   | 1 JVM por vez, 40-60s cada
Pico 3+ GB memória simultâneo             | Pico 600 MB, sequencial
Sem visibilidade de logs                  | Logs visíveis em tempo real
Falha silenciosa após 360s                | Você vê exatamente onde travou
Requer pagefile 8 GB ou mais              | Funciona com pagefile padrão 4 GB
Taxa de sucesso: ~40%                     | Taxa de sucesso: 100%
Tempo total (se falhar): 6-10 minutos      | Tempo total (sempre OK): 5-10 minutos
```

---

Alternativa rapida (infra + opcionalmente modulos) com script:

```powershell
# ✅ RECOMENDADO (primeira vez): apenas containers + observabilidade
.\dev-up.ps1

# DEPOIS: suba os modulos MANUALMENTE em terminais separados (veja seção 5 abaixo)
# Isso evita picos de memoria e garante visibilidade total dos logs

# garantir containers + baixar modelos Ollama (primeira vez, ~3 GB)
.\dev-up.ps1 -PullModels

# ⚠️ CRÍTICO — NÃO RECOMENDADO em Windows comum: tenta subir 6 JVMs simultaneamente
# Causa: OOM, malloc failed, picos de memoria de 3+ GB
# Solução: use o fluxo MANUAL (terminais separados) abaixo em vez disso
# .\dev-up.ps1 -StartModules -KillPorts          # ❌ NÃO USE
# .\dev-up.ps1 -StartModules -KillPorts -PullModels  # ❌ NÃO USE

# ⚠️ ALTERNATIVA SEGURA: se tiver pagefile >= 8 GB, pode tentar com menos módulos
# (apenas core 4 módulos, sem IA pesada, sem observabilidade):
.\dev-up.ps1 -StartModules -KillPorts -SkipObservability -SkipAI -WaitForHealth

# subir apenas a stack de observabilidade local (Prometheus/Grafana/Jaeger)
.\dev-up.ps1 -StartObservability

# se algum container estiver corrompido/travado, recrie tudo da infra
.\dev-up.ps1 -RecreateContainers
```

**🔥 FLUXO RECOMENDADO (100% confiável):**

```powershell
# 1️⃣ Subir apenas infraestrutura Docker (containers + observabilidade)
.\dev-up.ps1

# 2️⃣ Aguarde ~30 segundos, confirme containers
docker ps  # todos devem estar "Up"

# 3️⃣ DEPOIS, abra 6 terminais PowerShell SEPARADOS e rode os comandos abaixo
# Terminal A, Terminal B, Terminal C, etc (seção 5 abaixo)
# Isso evita picos de memória e garante 100% visibilidade dos logs
```

Se algum modulo falhar com porta em uso, rode antes:

```powershell
netstat -ano | findstr :8080
netstat -ano | findstr :8081
netstat -ano | findstr :8082
netstat -ano | findstr :8083
netstat -ano | findstr :8084
netstat -ano | findstr :8085

# mate o PID em LISTENING da porta que estiver ocupada
taskkill /PID <pid> /F
```

### 📋 Abrir 6 terminais PowerShell e rodar os módulos SEQUENCIALMENTE

> **Por quê sequencial?** Cada JVM (Maven + Quarkus forked) consome ~280-380 MB. 6 JVMs simultâneas = 3+ GB de pico.
> - **Windows pagefile < 4 GB:** OOM, malloc failed, travado
> - **Sequencial (1 por vez):** estável, tempo total ~5-10 minutos
> 
> **Aguarde cada módulo inicializar completamente ANTES de abrir o próximo terminal:**
> Procure pela mensagem: `[io.quarkus] Quarkus X.X.X started in XX.XXs`

---

### Terminal A - ms-catalogo (8082)

```powershell
mvn -pl ms-catalogo quarkus:dev
```

### Terminal B - ms-pedidos (8080)

```powershell
mvn -pl ms-pedidos quarkus:dev
```

### Terminal C - ms-pagamentos (8081)

```powershell
mvn -pl ms-pagamentos quarkus:dev
```

### Terminal D - ms-notificacoes (8084)

```powershell
mvn -pl ms-notificacoes quarkus:dev
```

### Terminal E - ms-ia-suporte (8083) — Fase 3

```powershell
mvn -pl ms-ia-suporte quarkus:dev
```

### Terminal F - mcp-florinda-server (8085) — Fase 3

```powershell
mvn -pl mcp-florinda-server quarkus:dev
```

## 6) Swagger / Dev UI

- Catalogo Swagger: `http://localhost:8082/swagger-ui`
- Catalogo Dev UI: `http://localhost:8082/q/dev-ui`
- Pedidos Swagger: `http://localhost:8080/swagger-ui`
- Pedidos Dev UI: `http://localhost:8080/q/dev-ui`
- Pagamentos Swagger: `http://localhost:8081/swagger-ui`
- Pagamentos Dev UI: `http://localhost:8081/q/dev-ui`
- Notificacoes Swagger: `http://localhost:8084/swagger-ui`
- Notificacoes Dev UI: `http://localhost:8084/q/dev-ui`
- **IA Suporte Swagger: `http://localhost:8083/swagger-ui`** ← Fase 3
- **IA Suporte Dev UI: `http://localhost:8083/q/dev-ui`** ← Fase 3
- **MCP Server Swagger: `http://localhost:8085/swagger-ui`** ← Fase 3
- **MCP Server Dev UI: `http://localhost:8085/q/dev-ui`** ← Fase 3

## 6.1) Observabilidade / Dashboards

- Grafana: `http://localhost:3000`
- Prometheus: `http://localhost:9090`
- Jaeger: `http://localhost:16686`
- Datasource OTLP dos módulos em dev: `http://localhost:4317`

## 7) Fluxo de teste sugerido no Swagger

### 7.1 Catalogo - criar restaurante

No `ms-catalogo` (`POST /v1/restaurantes`):

```json
{
  "nome": "Churrascaria Florinda",
  "descricao": "Carnes na brasa",
  "categoria": "Churrasco",
  "telefone": "88999999999",
  "email": "contato@florinda.com",
  "endereco": {
    "logradouro": "Rua das Flores",
    "numero": "10",
    "bairro": "Centro",
    "cidade": "Juazeiro do Norte",
    "uf": "CE",
    "cep": "63000-000"
  },
  "horarioAbertura": "11:00:00",
  "horarioFechamento": "23:00:00"
}
```

Copie o `id` retornado e abra o restaurante com `PUT /v1/restaurantes/{id}/abrir`.

### 7.2 Pedidos - criar pedido

No `ms-pedidos` (`POST /v1/pedidos`):

```json
{
  "clienteId": "11111111-0000-0000-0000-000000000001",
  "restauranteId": "COLE_AQUI_O_ID_DO_RESTAURANTE",
  "itens": [
    {
      "itemId": "c1000000-0000-0000-0000-000000000001",
      "nomeItem": "Picanha na brasa",
      "precoUnitario": 89.90,
      "quantidade": 1
    }
  ],
  "observacao": "Sem cebola",
  "enderecoEntrega": "Rua das Flores, 10"
}
```

Copie o `id` do pedido.

### 7.3 Pagamentos - validar processamento por evento

No `ms-pagamentos`, consulte `GET /v1/pagamentos/pedido/{pedidoId}` usando o id do pedido criado.

### 7.4 Pagamentos - fluxo manual (admin/teste)

No `ms-pagamentos` (`POST /v1/pagamentos`):

```json
{
  "pedidoId": "11111111-1111-1111-1111-111111111111",
  "clienteId": "22222222-2222-2222-2222-222222222222",
  "valor": 49.90,
  "metodo": "PIX"
}
```

Depois valide:

- `GET /v1/pagamentos/{id}`
- `POST /v1/pagamentos/{id}/estorno`

Body do estorno:

```json
{
  "motivo": "Cancelamento solicitado pelo cliente"
}
```

## 8) Validacao rapida da saga

Depois de criar pedido em `ms-pedidos`, acompanhe:

- logs de `ms-pagamentos` para `order.created`
- logs de `ms-pedidos` para atualizacao por `payment.approved|payment.failed`
- logs de `ms-notificacoes` para notificacoes dos topicos consumidos
- logs de `ms-ia-suporte` para reindexacao via `catalog.item.updated`

Referencia de fluxo completo: `SAGA-KAFKA.md`.

## 9) Agente IA — Fase 3 (ms-ia-suporte + mcp-florinda-server)

Apos subir os dois novos modulos, siga o roteiro completo em `AgenteFlorindaIA.md`.

Fluxo resumido de teste:

**Passo 1 — Verificar modelos no Ollama**

```powershell
curl http://localhost:11434/api/tags
```

**Passo 2 — Chat simples com o agente**

```powershell
curl -X POST http://localhost:8083/v1/ia/chat `
  -H "Content-Type: application/json" `
  -d '{"pergunta":"Qual o prazo de entrega?","sessaoId":"sessao-01"}'
```

**Passo 3 — Ingerir FAQ no RAG**

O seed inicial (11 FAQs) e aplicado automaticamente pelo Flyway na primeira subida do `ms-ia-suporte`.

Para ingerir manualmente via Swagger (`POST /v1/ia/admin/ingerir`):

```json
{
  "conteudo": "Como cancelo um pedido? O cancelamento e permitido enquanto o pedido estiver Pendente ou Confirmado.",
  "fonte": "faq",
  "fonteId": "faq-cancelamento-01"
}
```

**Passo 4 — Usar Postman**

Importe o arquivo `Florinda-Eats-Complete.postman_collection.json` da raiz do projeto no Postman.

Essa collection tem TUDO das Fases 1, 2 e 3.

**Passo 5 — Health check de todos os modulos**

```powershell
curl http://localhost:8082/q/health   # ms-catalogo
curl http://localhost:8080/q/health   # ms-pedidos
curl http://localhost:8081/q/health   # ms-pagamentos
curl http://localhost:8084/q/health   # ms-notificacoes
curl http://localhost:8083/q/health   # ms-ia-suporte
curl http://localhost:8085/q/health   # mcp-florinda-server
```

## 10) Problemas comuns

- `mvn : O termo 'mvn' nao e reconhecido`:
  - adicione no terminal atual: `$env:Path += ";C:\Users\marcu\tools\apache-maven-3.9.9\bin"`.
  - alternativa: `& "C:\Users\marcu\tools\apache-maven-3.9.9\bin\mvn.cmd" -pl ms-catalogo quarkus:dev`.
- `Connection refused` no startup:
  - confira se os containers corretos estao ativos (`docker ps`).
  - valide portas: `5433` (Postgres), `6379` (Redis), `3307` (MySQL pedidos), `3308` (MySQL pagamentos), `9092` (Kafka), `11434` (Ollama), `3000/9090/16686/4317` (observabilidade).
- `Connection refused: localhost:11434` (ms-ia-suporte):
  - Ollama nao esta rodando. Execute `docker start florinda-ollama`.
- `model llama3.2 not found` ou `model nomic-embed-text not found`:
  - modelos LLM nao foram baixados. Execute `.\dev-up.ps1 -PullModels`.
- `ia_suporte_db does not exist`:
  - rode o bootstrap idempotente da seção `4.8` ou `./dev-up.ps1` sem `-SkipInfra`.
- `pgvector extension not found`:
  - use `pgvector/pgvector:pg16` como imagem do Postgres (nao `postgres:16`).
- Grafana/Prometheus/Jaeger nao sobem:
  - execute `docker compose -f .\observabilidade\docker-compose.yml up -d`.
  - valide com `docker compose -f .\observabilidade\docker-compose.yml ps`.
- `[ERROR] Failed to execute goal ... quarkus-maven-plugin:...:dev`:
  - geralmente e erro secundario: o app caiu antes (porta ocupada, banco indisponivel, credencial incorreta) ou o processo foi interrompido.
  - verifique conflito de porta: `netstat -ano | findstr :8080` (troque para `8081`, `8082`, `8083`, `8084`, `8085` conforme o modulo).
  - finalize o PID conflitante: `taskkill /PID <pid> /F` e suba novamente.
- `dev-up` terminou, mas modulo ficou offline:
  - o script apenas abre os terminais; se o Quarkus falhar no startup, a porta nao sobe.
  - rode com `-WaitForHealth` para validar automaticamente e ver quais portas ficaram offline.
  - evite rodar varias vezes seguidas sem fechar os terminais antigos; isso gera conflito de porta no bind final.
- `FATAL: password authentication failed` / `Access denied`:
  - usuario/senha do app nao batem com o container atual.
  - remova e recrie o container com as credenciais do quickstart.
- `Failed to start container '...'` no `dev-up.ps1`:
  - agora o script mostra diagnostico do Docker (`inspect` e `logs`).
  - para recuperar rapidamente, rode `.\dev-up.ps1 -RecreateContainers`.
- `Unable to find image '... locally'` ao subir container:
  - isso sozinho nao e erro; o Docker ainda esta baixando a imagem.
  - analise a proxima linha para ver se houve falha real de tag.

## 11) Validacao rapida final

```powershell
curl http://localhost:8082/q/health   # ms-catalogo
curl http://localhost:8080/q/health   # ms-pedidos
curl http://localhost:8081/q/health   # ms-pagamentos
curl http://localhost:8084/q/health   # ms-notificacoes
curl http://localhost:8083/q/health   # ms-ia-suporte (Fase 3)
curl http://localhost:8085/q/health   # mcp-florinda-server (Fase 3)
```

Se os seis retornarem `UP`, ambiente completo pronto para testes funcionais no Swagger.

## 12) Testes de integracao (fase 4)

Atualmente existem cenarios em `testes-integracao/` para:

- `ms-catalogo`
- `ms-pedidos`
- `ms-pagamentos`

Objetivo da fase 4:

- executar os testes de integracao antes de commit/pre-push
- executar os testes no pipeline CI/CD como gate de merge

Enquanto a integracao completa da suite no build raiz nao e finalizada, use os comandos do modulo para rodar os testes que ja estao mapeados no fluxo atual.
