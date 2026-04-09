# Florinda Eats 2.0

Plataforma de food delivery como projeto de portfolio da pos-graduacao **Java Elite (UNIPDS)**, baseada em microsservicos com Quarkus, Kafka e observabilidade.

---

## Arquitetura

```text
florinda-eats/
|- ms-catalogo          PostgreSQL + Panache ORM + Redis
|- ms-pedidos           MySQL (reactive + JDBC para Flyway) + Kafka
|- ms-pagamentos        MySQL JDBC + Panache ORM + Kafka + Fault Tolerance
|- ms-notificacoes      Kafka Consumer (eventos da saga)
|- ms-ia-suporte        LangChain4j + RAG + PgVector + Ollama (fase 3)
|- testes-integracao    cenarios de integracao com Testcontainers
`- mcp-florinda-server  MCP SSE tools
```

## Stack principal

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Quarkus 3.17.5 |
| Mensageria | Apache Kafka + SmallRye Reactive Messaging |
| Bancos | PostgreSQL, MySQL, Redis, PgVector |
| IA | LangChain4j, Ollama |
| Resiliencia | SmallRye Fault Tolerance |
| Observabilidade | OpenTelemetry, Prometheus, Grafana, Jaeger |
| Testes | JUnit 5, Mockito, REST Assured, Testcontainers |

---

## Estado atual (fase 2)

- `ms-catalogo`
- `ms-pedidos`
- `ms-pagamentos`
- `ms-notificacoes`
- `testes-integracao` (suite dedicada para consolidacao na fase 4)

A saga principal ja esta modelada: `order.created` -> `payment.approved|payment.failed` -> `order.status.updated`, com consumo paralelo em `ms-notificacoes`.

---

## Como subir localmente

Use o guia completo em `QUICKSTART.md`.

Ele cobre:

- configuracao de Java e Maven no PowerShell
- subida da infraestrutura minima via Docker (Postgres, Redis, MySQL pedidos, MySQL pagamentos e Kafka)
- execucao dos modulos `ms-catalogo`, `ms-pedidos`, `ms-pagamentos` e `ms-notificacoes`
- roteiro de testes no Swagger/OpenAPI
- referencia para testes de integracao com Testcontainers

---

## Endpoints de desenvolvimento

- Catalogo: `http://localhost:8082/swagger-ui`
- Pedidos: `http://localhost:8080/swagger-ui`
- Pagamentos: `http://localhost:8081/swagger-ui`
- Notificacoes: `http://localhost:8084/swagger-ui`

Health checks:

- `http://localhost:8082/q/health`
- `http://localhost:8080/q/health`
- `http://localhost:8081/q/health`
- `http://localhost:8084/q/health`

Status util de notificacoes:

- `http://localhost:8084/v1/notificacoes/status`

---

## Topicos Kafka principais

| Topico | Producer | Consumer(s) |
|---|---|---|
| `order.created` | ms-pedidos | ms-pagamentos, ms-notificacoes |
| `order.status.updated` | ms-pedidos | ms-ia-suporte (fase 3), ms-notificacoes |
| `payment.approved` | ms-pagamentos | ms-pedidos, ms-notificacoes |
| `payment.failed` | ms-pagamentos | ms-pedidos, ms-notificacoes |
| `catalog.item.updated` | ms-catalogo | ms-ia-suporte (fase 3) |

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

- [x] Fase 1 - Monorepo + ms-catalogo + ms-pedidos + infraestrutura local
- [x] Fase 2 - ms-pagamentos + ms-notificacoes + saga Kafka ponta a ponta
- [ ] Fase 3 - ms-ia-suporte (RAG + LangChain4j) + MCP server
- [ ] Fase 4 - observabilidade completa + Kubernetes + CI/CD com testes de integracao

---

## Autor
Marcus Dimitri - Projeto de portfolio da pos-graduacao Java Elite - UNIPDS.
