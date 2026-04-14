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

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Quarkus 3.17.5 |
| Mensageria | Apache Kafka + SmallRye Reactive Messaging |
| Bancos | PostgreSQL/PgVector, MySQL, Redis |
| IA | LangChain4j 0.26.1, Ollama (llama3.2 + nomic-embed-text) |
| MCP | Quarkiverse MCP Server 1.0.0.CR1 |
| Resiliencia | SmallRye Fault Tolerance |
| Observabilidade | OpenTelemetry, Prometheus, Grafana, Jaeger |
| Testes | JUnit 5, Mockito, REST Assured, Testcontainers |

---

## Stack Detalhado com Tecnologias

### 🔧 Core & Framework

#### ☕ **Java 21 LTS** (v21.0.10)
Linguagem base do projeto. Java 21 traz **virtual threads**, **pattern matching avançado** e **record types** que melhoram a legibilidade e performance. Usamos para toda a lógica de negócio e APIs REST.

#### ⚡ **Quarkus** (v3.17.5)
Framework cloud-native otimizado para microsserviços. No Florinda Eats, Quarkus fornece:
- Startup ultra-rápido (~1-2s por módulo em dev mode)
- Baixíssimo consumo de memória (heap mínimo de 64MB via `-Xms64m`)
- Dev mode com live reload instantâneo
- Compilação nativa com GraalVM (pronto para Kubernetes)
- Integração automática com Kafka, REST, OpenAPI/Swagger

---

### 💾 Bancos de Dados

#### 🐘 **PostgreSQL 16 + PgVector** (via pgvector:pg16)
Banco de dados relacional principal. No projeto:
- Armazena **catálogo de restaurantes** e **cardápios** (`ms-catalogo`)
- Suporta **vetores** (embeddings) via extensão PgVector para RAG
- **Redis** com Panache ORM oferece queries type-safe em Java
- Migrações automáticas via **Flyway**

#### 🐬 **MySQL 8.0** (2 instâncias: pedidos + pagamentos)
Bancos separados por domínio (bounded context):
- `florinda-mysql-pedidos` (porta 3307) — tabelas de pedidos, itens, status
- `florinda-mysql-pagamentos` (porta 3308) — pagamentos, transações, estornos
- JDBC com pool nativo Quarkus + Hibernate ORM
- Flyway para versionamento de schema

#### 🔴 **Redis 7** (porta 6379)
Cache distribuído e session store:
- **Histórico de conversas** do agente IA (sessões Langchain4j)
- **Cache de catálogo** (restaurantes e itens)
- Suporta pub/sub para notificações em tempo real

---

### 📨 Mensageria & Eventos

#### 🚀 **Apache Kafka 3.9** (porta 9092)
Message broker central. Implementa o padrão **SAGA distribuída**:
- Tópicos: `order.created`, `payment.approved|failed`, `order.status.updated`, `catalog.item.updated`
- **SmallRye Reactive Messaging** integra Kafka aos módulos Quarkus
- Garante entrega ordenada de eventos entre microsserviços
- Suporta retry automático e dead-letter topics (configurável)

---

### 🤖 IA & RAG

#### 🦙 **Ollama** (porta 11434, nativo no host)
Servidor LLM local. No projeto:
- Roda **llama3.2** (7B) para geração de respostas conversacionais
- Roda **nomic-embed-text** para gerar embeddings de documentos
- Acesso via REST (`ms-ia-suporte` → `http://localhost:11434`)
- Alternativa a APIs cloud (OpenAI) — roda offline e localmente

#### 🔗 **LangChain4j** (v0.26.1)
Framework de orquestração para aplicações com IA:
- Abstração sobre LLMs (suporta Ollama, OpenAI, etc.)
- **Chain of Thought** para raciocínio estruturado
- **Memory Management** (histórico de conversas em Redis)
- **RAG Pipeline** (retrieval + augmented generation)

#### 📚 **PgVector** (extensão PostgreSQL)
Base vetorial integrada ao Postgres:
- Armazena embeddings de documentos do catálogo e FAQ
- Busca semântica via `pgvector` (`<->` operator)
- Recupera contexto relevante para aumentar respostas do LLM

---

### 🔌 Protocolos & APIs

#### 🌐 **REST/HTTP (Jakarta REST)**
Padrão de comunicação entre microsserviços e clientes:
- Endpoints `/v1/*` para cada domínio (restaurantes, pedidos, pagamentos, etc.)
- Content negotiation automático (JSON)
- OpenAPI 3.0 via SmallRye OpenAPI (Swagger UI em cada módulo)

#### 🔄 **SSE (Server-Sent Events) + MCP**
Protocolo bidirecional para o agente IA:
- `mcp-florinda-server` expõe **tools** via SSE (`/mcp/sse`)
- Agente IA conecta e chama tools como: `getOrderStatus`, `cancelOrder`, `getRestaurantMenu`
- Quarkiverse MCP Server v1.0.0.Alpha6 gerencia o protocolo

---

### 🛡️ Resiliência & Observabilidade

#### 🔁 **SmallRye Fault Tolerance**
Padrões de resiliência integrados:
- **@Retry** — retry automático em chamadas HTTP falhadas
- **@Timeout** — timeout em operações longas (evita travamento)
- **@CircuitBreaker** — circuit breaker para degradação elegante
- Usado em: `ms-pedidos` (chama `ms-catalogo`), `ms-ia-suporte` (chama Ollama)

#### 📊 **OpenTelemetry (OTEL)**
Observabilidade distribuída:
- Traces de requisições ponta-a-ponta entre microsserviços
- Metrics de performance (latência, throughput, erros)
- Exporta para **Jaeger** (traces) e **Prometheus** (métricas)
- Cada módulo tem `quarkus.otel.exporter.otlp.endpoint=http://localhost:4317`

#### 📈 **Prometheus + Micrometer**
Coleta de métricas:
- Endpoint `/q/metrics` em cada módulo (formato Prometheus)
- Inclui JVM metrics (heap, GC, threads) + métricas de negócio
- Pronto para scrape em Grafana

#### 🔍 **Jaeger (opcional)**
Visualização de traces distribuídos:
- Recebe traces do OTEL
- Permite debugar fluxo entre módulos (pedido → pagamento → notificação)
- UI em `http://localhost:16686` (se container rodar)

---

### ✅ Testes

#### 🧪 **JUnit 5** (Jupiter)
Framework de testes unitários e integração:
- Annotations `@Test`, `@ParameterizedTest`, `@Nested`
- Integração com Quarkus via `@QuarkusTest`
- Testes rodam em modo dev com hot reload

#### 🎯 **Mockito**
Mocking e espionagem em testes:
- Mock de dependências (CatalogoClient, PagamentoService, etc.)
- Verify de chamadas e argumentos
- Usado em testes unitários de Service

#### 🔗 **REST Assured**
Testes de API HTTP:
- Assertions fluentes para response status, body, headers
- Suporte a JSON Path para validação de payloads
- Usado em testes de integração de endpoints

#### 🐳 **Testcontainers**
Testes com infraestrutura real em containers:
- PostgreSQL, MySQL, Kafka em containers Docker durante testes
- Sem mock de banco — testes "integração realista"
- Suite em `testes-integracao/` para validação ponta-a-ponta

---

### 🔐 Validação & Segurança

#### ✔️ **Jakarta Validation (Bean Validation)**
Validação declarativa de DTOs:
- Annotations `@NotNull`, `@NotBlank`, `@Email`, `@Size`, `@Pattern`
- Validação automática em endpoints (400 Bad Request se falhar)
- Usado em: CriarPedidoRequest, ProcessarPagamentoRequest, etc.

#### 🔐 **Quarkus Security** (não ativo em dev, pronto para produção)
Integração com OIDC, JWT, RBAC
- Endpoints sensíveis (`/admin/*`) podem exigir autenticação
- Pronto para integração com auth0, Keycloak, etc.

---

## Como subir localmente

Use o guia completo em `QUICKSTART.md`.

Ele cobre:

- configuracao de Java e Maven no PowerShell
- subida da infraestrutura minima via Docker (Postgres/PgVector, Redis, MySQL pedidos, MySQL pagamentos, Kafka, Ollama)
- execucao dos 6 modulos: `ms-catalogo`, `ms-pedidos`, `ms-pagamentos`, `ms-notificacoes`, `ms-ia-suporte`, `mcp-florinda-server`
- roteiro de testes no Swagger/OpenAPI
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
