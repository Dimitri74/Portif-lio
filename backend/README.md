# Florinda Eats 2.0

Plataforma de food delivery como projeto de portfolio da pos-graduacao **Java Elite (UNIPDS)**, baseada em microsservicos com Quarkus, Kafka e observabilidade.

---

## Arquitetura

```text
florinda-eats/
|- ms-catalogo          PostgreSQL/PgVector + Panache ORM + Redis + Kafka
|- ms-pedidos           MySQL (reactive + JDBC para Flyway) + Kafka
|- ms-pagamentos        MySQL JDBC + Panache ORM + Kafka + Fault Tolerance
|- ms-notificacoes      Kafka Consumer (eventos da saga)
|- ms-ia-suporte        LangChain4j + RAG + PgVector + Ollama  ← Fase 3
|- mcp-florinda-server  MCP SSE tools (getOrderStatus, cancelOrder, menu)  ← Fase 3
|- testes-integracao    cenarios de integracao com Testcontainers  ← Fase 4
```

## Stack principal

### Core & Linguagem

| Ícone | Tecnologia | Versão | O que é e o que faz |
|---|---|---|---|
| ☕ | **Java** | 21 LTS | Linguagem de programação principal. Java 21 oferece virtual threads, pattern matching avançado e melhor performance. Usado para toda lógica de negócio. |
| ⚡ | **Quarkus** | 3.17.5 | Framework cloud-native otimizado para microsserviços. Oferece startup ultra-rápido (1-2s), baixo consumo de memória, dev mode com live reload e compilação nativa com GraalVM. Base de todos os 6 módulos. |

### Bancos de Dados & Cache

| Ícone | Tecnologia | Versão | O que é e o que faz |
|---|---|---|---|
| 🐘 | **PostgreSQL + PgVector** | 16 | Banco de dados relacional com suporte a vetores (embeddings). Armazena catálogo, restaurantes e dados vetorizados para RAG (Retrieval Augmented Generation). Usado por ms-catalogo e ms-ia-suporte. |
| 🐬 | **MySQL** | 8.0 | Banco relacional separado por domínio. Instâncias dedicadas: ms-pedidos (port 3307) e ms-pagamentos (port 3308). Suporta JDBC com pool nativo Quarkus e Hibernate ORM. Versionamento automático via Flyway. |
| 🔴 | **Redis** | 7 | Cache distribuído em memória. Armazena histórico de conversas do agente IA, cache de catálogo, e suporta pub/sub para notificações real-time. Melhora performance e reduz carga no banco. |
| 🔍 | **Elasticsearch** | (opcional) | Motor de busca distribuído para indexação full-text. Usado em testes e pode ser integrado para busca avançada de restaurantes/itens. |

### Mensageria & Event Streaming

| Ícone | Tecnologia | Versão | O que é e o que faz |
|---|---|---|---|
| 🚀 | **Apache Kafka** | 3.9.0 | Message broker distribuído. Implementa SAGA padrão para orquestração de eventos entre microsserviços. Tópicos: order.created, payment.approved/failed, order.status.updated, catalog.item.updated. Garante entrega ordenada e confiável. |
| 📡 | **SmallRye Reactive Messaging** | (Quarkus BOM) | Abstração sobre Kafka integrada ao Quarkus. Permite consumir/produzir mensagens via @Incoming/@Outgoing annotations. Suporta backpressure, retry automático e integração nativa com reactive streams. |

### IA & Machine Learning

| Ícone | Tecnologia | Versão | O que é e o que faz |
|---|---|---|---|
| 🦙 | **Ollama** | latest | Servidor LLM local. Roda llama3.2 (7B) para respostas conversacionais e nomic-embed-text para geração de embeddings. Alternativa offline a APIs cloud. Acesso via REST na porta 11434. |
| 🔗 | **LangChain4j** | 0.26.1 | Framework de orquestração para IA. Abstração sobre LLMs, gerenciamento de memory (histórico em Redis), Chain of Thought para raciocínio estruturado, e pipeline RAG completo. Core do ms-ia-suporte. |
| 📚 | **PgVector (PostgreSQL)** | 0.1.0 | Extensão do PostgreSQL para armazenar e buscar embeddings vetoriais. Implementa busca semântica via similaridade. Recupera contexto relevante para aumentar respostas do agente IA (RAG). |

### Protocolos & APIs

| Ícone | Tecnologia | Versão | O que é e o que faz |
|---|---|---|---|
| 🌐 | **Jakarta REST (RESTful APIs)** | (Jakarta EE) | Padrão de comunicação REST entre microsserviços e clientes. Endpoints `/v1/*` para cada domínio. Content negotiation automático, validação via Jakarta Validation, resposta em JSON. |
| 📖 | **OpenAPI 3.0 / Swagger UI** | (via SmallRye) | Documentação automática de APIs REST. Cada módulo expõe Swagger UI em `/swagger-ui`. Permite testes interativos de endpoints direto do navegador. |
| 🔄 | **Server-Sent Events (SSE)** | (HTTP standard) | Protocolo unidirecional para push real-time. Usado pelo MCP server para comunicação bidirecional com agentes IA. Alternativa leve ao WebSocket. |
| 🔌 | **MCP Protocol** | 1.0.0.Alpha6 | Model Context Protocol via Quarkiverse. Permite ao agente IA chamar tools do servidor (getOrderStatus, cancelOrder, getMenu). SSE como transport. |

### Resiliência & Observabilidade

| Ícone | Tecnologia | Versão | O que é e o que faz |
|---|---|---|---|
| 🔁 | **SmallRye Fault Tolerance** | (Quarkus BOM) | Implementa padrões de resiliência: @Retry (retry automático), @Timeout (timeout em operações), @CircuitBreaker (circuit breaker para degradação elegante), @Fallback (fallback de emergência). Protege ms-pedidos, ms-ia-suporte. |
| 📊 | **OpenTelemetry (OTEL)** | 1.35.0 (via Quarkus) | Observabilidade distribuída agnóstica. Instrumentação automática de requisições HTTP, Kafka, BD. Exporta traces para Jaeger, métricas para Prometheus. Endpoint OTEL Collector em localhost:4317. |
| 📈 | **Prometheus** | latest (Docker) | Sistema de monitoramento e alertas. Coleta métricas em formato time-series. Cada módulo expõe `/q/metrics`. Inclui JVM metrics (heap, GC, threads), CPU, latência. Alertas configuráveis via alertas.yml. |
| 📊 | **Grafana** | latest (Docker) | Plataforma de visualização. 2 dashboards provisionados: "Visão Geral" (latência P95, taxa erros, throughput, Circuit Breaker), "JVM vs Native" (comparação startup, heap, CPU). |
| 🔍 | **Jaeger** | latest (Docker) | Tracing distribuído. Recebe traces do OTEL. Permite debugar fluxo entre módulos (pedido → pagamento → notificação). UI em localhost:16686. |
| 📝 | **Micrometer** | (Quarkus BOM) | Biblioteca de métricas abstrata. Integração automática com Prometheus via Quarkus. Coleta métricas de aplicação (requisições, latência, erros) além de JVM. |

### Testes & Qualidade

| Ícone | Tecnologia | Versão | O que é e o que faz |
|---|---|---|---|
| 🧪 | **JUnit 5 (Jupiter)** | 5.x (via Quarkus) | Framework de testes padrão. Annotations @Test, @ParameterizedTest, @Nested. Integração nativa com Quarkus via @QuarkusTest. Suporta hot reload em dev mode. |
| 🎯 | **Mockito** | 5.x | Framework de mocking. Mock de dependências (CatalogoClient, PagamentoService). Verify de chamadas e argumentos. Usado em testes unitários de Services e Handlers. |
| 🔗 | **REST Assured** | 5.x | Biblioteca fluente para testes de API HTTP. Assertions para status, body, headers. Suporte a JSON Path para validação de payloads complexos. Testes de integração de endpoints. |
| 🐳 | **Testcontainers** | 1.19.8 | Testes com infraestrutura real em Docker. PostgreSQL, MySQL, Kafka rodam em containers durante testes. Sem mock de banco — testes "integração realista". Suite em testes-integracao/. |
| 🔐 | **Jakarta Validation** | (Jakarta EE) | Validação declarativa via annotations @NotNull, @NotBlank, @Email, @Size, @Pattern. Validação automática em DTOs nos endpoints (400 Bad Request se falhar). |

### Build, Containerização & Deployment

| Ícone | Tecnologia | Versão | O que é e o que faz |
|---|---|---|---|
| 🐳 | **Docker & Docker Desktop** | latest | Containerização de microsserviços e dependências (Postgres, MySQL, Kafka, Ollama, Prometheus, Grafana, Jaeger). Permite ambiente local idêntico ao produção. |
| 📦 | **Docker Compose** | 2.x | Orquestração local de múltiplos containers. Arquivo `observabilidade/docker-compose.yml` sobe stack completa (Prometheus, Grafana, Jaeger, OTEL Collector). |
| 🔨 | **Maven** | 3.9.9 | Ferramenta de build Java. Gerencia dependências via pom.xml, compila código, roda testes, empacota JARs. Monorepo configurado com módulos. |
| 🏗️ | **Jib** | (via Quarkus) | Plugin de build de container sem Docker CLI. Constrói imagens OCI otimizadas. Usado em CI/CD para build JVM rápido. |
| 🌶️ | **GraalVM Native Image** | 21-graalce (opcional) | Compilador ahead-of-time (AOT) para Java. Gera binários nativos com startup ultra-rápido (32ms) e footprint mínimo. Usado no ms-catalogo para Kubernetes. Build via `build-native.sh`. |
| ☸️ | **Kubernetes (Minikube)** | 1.28+ (local) | Orquestração de containers em produção simulada. Manifests em `kubernetes/` para todos os 6 serviços. ConfigMap/Secret centralizados, Ingress nginx, HPA para ms-catalogo. |
| 🔄 | **GitHub Actions** | (CI/CD nativa) | Pipeline CI/CD. Workflows: ci.yml (build → testes → qualidade), cd.yml (build Jib + Native → deploy Minikube). Automático em push/PR. |

### ORM & Persistência

| Ícone | Tecnologia | Versão | O que é e o que faz |
|---|---|---|---|
| 🗃️ | **Hibernate Panache** | (Quarkus BOM) | Abstração ORM simplificada. Queries type-safe, operações CRUD automáticas. Integração transparente com Quarkus datasource. Usado em ms-catalogo (PostgreSQL) e ms-pedidos/pagamentos (MySQL). |
| 📝 | **Flyway** | (via Quarkus) | Ferramenta de versionamento de schema. Scripts SQL incrementais (V001__, V002__) aplicados automaticamente no startup. Garante consistência de schema entre ambientes. |

### Utilidades & Libraries

| Ícone | Tecnologia | Versão | O que é e o que faz |
|---|---|---|---|
| 🏷️ | **Lombok** | 1.18.32 | Gerador de código Java via annotations. @Data (getters/setters), @Builder, @Slf4j. Reduz boilerplate sem refletir em runtime. |
| 🗺️ | **MapStruct** | 1.5.5.Final | Gerador de mappers entre DTOs e entities em compile-time. Type-safe, sem refletir em runtime. Usado para transformar CriarRestauranteRequest → RestauranteEntity. |
| 📋 | **SmallRye Config** | (Quarkus BOM) | Gerenciamento centralizado de configurações. Suporta application.properties, environment variables, secrets. Injeta via @ConfigProperty ou ConfigProvider. |
| 🔒 | **Quarkus Security** | (Quarkus BOM) | Framework de segurança. Suporta OIDC, JWT, RBAC. Pronto para integração com Auth0, Keycloak. Endpoints sensíveis podem exigir autenticação. |

---

## Fase 4 — Observabilidade Docker + Kubernetes + CI/CD

### O que foi adicionado

#### Observabilidade
- **Prometheus** com scrape de todos os 6 serviços via `/q/metrics`
- **Grafana** com 2 dashboards provisionados automaticamente:
  - `Florinda Eats — Visão Geral`: latência P95, taxa de erros, throughput, Circuit Breaker
  - `ms-catalogo — JVM vs Native`: comparação de startup time, heap e CPU
- **Jaeger** integrado ao Grafana como datasource para tracing distribuído
- **Alertas Prometheus**: serviço DOWN, latência alta, taxa de erros 5xx, Circuit Breaker aberto
- **Stack oficial local via Docker** em `observabilidade/docker-compose.yml`

#### Kubernetes (Minikube local)
- Manifests para todos os 6 serviços em `namespace: florinda-app`
- `ms-catalogo` com imagem **native GraalVM** + HPA (1→10 pods, CPU 70%)
- ConfigMap e Secret centralizados
- Ingress nginx em `florinda.local`

#### CI/CD — GitHub Actions
- `ci.yml`: build → testes unitários → testes integração → qualidade
- `cd.yml`: build Jib (JVM) + build Native (GraalVM) → deploy Minikube com smoke test

---

### Como aplicar a Fase 4 no projeto

#### 1. Observabilidade local via Docker

Arquivos usados pela stack local:
```
florinda-eats/
├── observabilidade/
│   ├── docker-compose.yml
│   ├── prometheus/
│   │   ├── prometheus.yml
│   │   └── florinda-alerts.yml
│   └── grafana/
│       └── provisioning/
│           ├── datasources/
│           │   └── datasources.yml
│           └── dashboards/
│               ├── dashboards.yml
│               ├── florinda-overview.json
│               └── florinda-native.json
```

Suba a stack:
```powershell
docker compose -f .\observabilidade\docker-compose.yml up -d
docker compose -f .\observabilidade\docker-compose.yml ps
```

Acesse:

- Grafana: http://localhost:3000 (`admin` / `admin`)
- Prometheus: http://localhost:9090
- Jaeger: http://localhost:16686

> Se preferir, o `dev-up.ps1` sobe essa stack automaticamente. Use `-SkipObservability` para desativar.

---

#### 2. Kubernetes — Deploy local com Minikube

```bash
# Inicia Minikube com recursos suficientes
minikube start --cpus=4 --memory=6g

# Habilita Ingress
minikube addons enable ingress

# Adiciona entrada no /etc/hosts
echo "$(minikube ip) florinda.local" | sudo tee -a /etc/hosts

# Aplica todos os manifests
kubectl apply -f kubernetes/infra/00-namespaces.yml
kubectl apply -f kubernetes/infra/dependencies.yml
kubectl apply -f kubernetes/infra/outros-servicos.yml
kubectl apply -f kubernetes/infra/ingress.yml

# Deploy do ms-catalogo nativo (veja seção abaixo)
./build-native.sh

# Verifica pods
kubectl get pods -n florinda-app
```

---

#### 3. Build Native do ms-catalogo

```bash
# Opção A: build local (requer GraalVM CE 21)
sdk install java 21-graalce
sdk use java 21-graalce
./build-native.sh

# Opção B: build via container (sem GraalVM local)
cd ms-catalogo
mvn package -Pnative -DskipTests \
  -Dquarkus.native.container-build=true \
  -Dquarkus.container-image.build=true
```

Após o build, compare startup time nos logs:
```bash
kubectl logs -l app=ms-catalogo -n florinda-app | grep "started in"
# Native: Quarkus x.x.x started in 0.032s
# JVM:    Quarkus x.x.x started in 1.847s
```

---

#### 4. CI/CD — GitHub Actions

Os workflows já ficam em `.github/workflows/` na raiz do monorepo.

Configure os secrets no GitHub:
- `GITHUB_TOKEN` — automático
- Nenhum secret adicional necessário para o deploy local

Para ativar o pipeline:
```bash
git add .
git commit -m "feat: fase 4 — observabilidade + k8s native + cicd"
git push origin main
```

---

### Endpoints de observabilidade por serviço

| Serviço | Health | Métricas | Swagger |
|---|---|---|---|
| ms-catalogo | :8082/q/health | :8082/q/metrics | :8082/swagger-ui |
| ms-pedidos | :8080/q/health | :8080/q/metrics | :8080/swagger-ui |
| ms-pagamentos | :8081/q/health | :8081/q/metrics | :8081/swagger-ui |
| ms-notificacoes | :8084/q/health | :8084/q/metrics | — |
| ms-ia-suporte | :8083/q/health | :8083/q/metrics | :8083/swagger-ui |
| mcp-server | :8085/q/health | :8085/q/metrics | :8085/swagger-ui |

### Dashboards Grafana

| Dashboard | URL |
|---|---|
| Visão geral | http://localhost:3000/d/florinda-overview |
| JVM vs Native | http://localhost:3000/d/florinda-native-comparison |
| Jaeger tracing | http://localhost:16686 |
| Prometheus | http://localhost:9090 |

---

## Como subir localmente

Use o guia completo em `QUICKSTART.md`.

Ele cobre:

- configuracao de Java e Maven no PowerShell
- subida da infraestrutura minima via Docker (Postgres/PgVector, Redis, MySQL pedidos, MySQL pagamentos, Kafka, Ollama)
- execucao dos 6 modulos: `ms-catalogo`, `ms-pedidos`, `ms-pagamentos`, `ms-notificacoes`, `ms-ia-suporte`, `mcp-florinda-server`
- roteiro de testes no Swagger/OpenAPI
- observabilidade local via Docker (`Prometheus`, `Grafana`, `Jaeger`)
- colecao Postman para o agente IA

Para documentacao especifica do agente IA e MCP: `AgenteFlorindaIA.md`.

---

## Endpoints de desenvolvimento

| Modulo | Swagger | Health |
|---|---|---|
| ms-catalogo (8082) | http://localhost:8082/swagger-ui | http://localhost:8082/q/health |
| ms-pedidos (8080) | http://localhost:8080/swagger-ui | http://localhost:8080/q/health |
| ms-pagamentos (8081) | http://localhost:8081/swagger-ui | http://localhost:8081/q/health |
| ms-notificacoes (8084) | http://localhost:8084/swagger-ui | http://localhost:8084/q/health |
| ms-ia-suporte (8083) | http://localhost:8083/swagger-ui | http://localhost:8083/q/health |
| mcp-florinda-server (8085) | http://localhost:8085/swagger-ui | http://localhost:8085/q/health |

Endpoints especificos notaveis:

- `http://localhost:8084/v1/notificacoes/status` — status util de notificacoes
- `http://localhost:8083/v1/ia/chat` — chat com o agente IA (POST)
- `http://localhost:8083/v1/ia/admin/ingerir` — ingestao RAG (POST, admin)
- `http://localhost:8083/v1/ia/health` — health check estendido do agente
- `http://localhost:8085/mcp/sse` — SSE stream MCP

Observabilidade local (Fase 4):

- `http://localhost:3000` — Grafana (`admin` / `admin`)
- `http://localhost:9090` — Prometheus
- `http://localhost:16686` — Jaeger UI

---

## Topicos Kafka principais

| Topico | Producer | Consumer(s) |
|---|---|---|
| `order.created` | ms-pedidos | ms-pagamentos, ms-notificacoes |
| `order.status.updated` | ms-pedidos | ms-ia-suporte, ms-notificacoes |
| `payment.approved` | ms-pagamentos | ms-pedidos, ms-notificacoes |
| `payment.failed` | ms-pagamentos | ms-pedidos, ms-notificacoes |
| `catalog.item.updated` | ms-catalogo | ms-ia-suporte |

Detalhes do fluxo: `SAGA-KAFKA.md`.

---

## Testes de integracao

Os cenarios da fase atual estao em `testes-integracao/` para:

- `ms-catalogo`
- `ms-pedidos`
- `ms-pagamentos`

Na fase 4, a meta e executar essa suite antes de commit (ou pre-push) e como gate no pipeline CI/CD.

---

## Roadmap

- [x] Fase 1 — Monorepo + ms-catalogo + ms-pedidos + infraestrutura local
- [x] Fase 2 — ms-pagamentos + ms-notificacoes + saga Kafka ponta a ponta
- [x] Fase 3 — ms-ia-suporte (RAG + LangChain4j) + mcp-florinda-server (MCP SSE)
- [ ] Fase 4 — observabilidade completa + Kubernetes + CI/CD com testes de integracao

---

## Autor
Marcus Dimitri - Projeto de portfolio da pos-graduacao Java Elite - UNIPDS.
