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

## Estado atual (fase 3 — em andamento)

- [x] `ms-catalogo` — catalogo de restaurantes e cardapios
- [x] `ms-pedidos` — criacao e ciclo de vida de pedidos
- [x] `ms-pagamentos` — processamento e estorno de pagamentos
- [x] `ms-notificacoes` — consumidor de eventos da saga
- [x] `ms-ia-suporte` — agente RAG conversacional (LangChain4j + Ollama + PgVector)
- [x] `mcp-florinda-server` — servidor MCP com tools SSE para o agente IA
- [ ] `testes-integracao` — suite dedicada para consolidacao na fase 4

A saga principal esta modelada: `order.created` → `payment.approved|payment.failed` → `order.status.updated`, com consumo paralelo em `ms-notificacoes` e reindexacao automatica em `ms-ia-suporte`.

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
