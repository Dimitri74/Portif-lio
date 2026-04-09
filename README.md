# Florinda Eats 2.0

Plataforma de food delivery construida como projeto de portfolio da pos-graduacao **Java Elite (UNIPDS)**, com arquitetura de microsservicos, eventos Kafka e observabilidade.

---

## Arquitetura

```text
florinda-eats/
|- ms-catalogo          PostgreSQL + Panache ORM + Redis
|- ms-pedidos           MySQL Reactive + Hibernate Reactive + Kafka
|- ms-pagamentos        MySQL JDBC + Panache ORM + Kafka + Fault Tolerance
|- ms-notificacoes      Kafka Consumer
|- ms-ia-suporte        LangChain4j + RAG + PgVector + Ollama
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

## Modulos implementados ate o momento

- `ms-catalogo`
- `ms-pedidos`
- `ms-pagamentos`

Esses tres modulos ja podem ser executados em dev local com Swagger habilitado.

---

## Como subir localmente

Use o guia completo em `QUICKSTART.md`.

Ele cobre:

- configuracao de Java e Maven no PowerShell
- subida da infraestrutura minima via Docker (Postgres, Redis, MySQL pedidos, MySQL pagamentos e Kafka)
- execucao dos modulos `ms-catalogo`, `ms-pedidos` e `ms-pagamentos`
- roteiro de testes no Swagger/OpenAPI

---

## Endpoints de desenvolvimento

- Catalogo: `http://localhost:8082/swagger-ui`
- Pedidos: `http://localhost:8080/swagger-ui`
- Pagamentos: `http://localhost:8081/swagger-ui`

Health checks:

- `http://localhost:8082/q/health`
- `http://localhost:8080/q/health`
- `http://localhost:8081/q/health`

---

## Topicos Kafka principais

| Topico | Producer | Consumer(s) |
|---|---|---|
| `order.created` | ms-pedidos | ms-pagamentos, ms-notificacoes |
| `order.status.updated` | ms-pedidos | ms-ia-suporte, ms-notificacoes |
| `payment.approved` | ms-pagamentos | ms-pedidos, ms-notificacoes |
| `payment.failed` | ms-pagamentos | ms-pedidos, ms-notificacoes |
| `catalog.item.updated` | ms-catalogo | ms-ia-suporte |

---

## Roadmap

- [x] Fase 1 - Monorepo + ms-catalogo + ms-pedidos + infraestrutura local
- [x] Fase 2 (parcial) - ms-pagamentos + eventos de pagamento
- [ ] Fase 2 (restante) - ms-notificacoes + testes de integracao completos
- [ ] Fase 3 - ms-ia-suporte (RAG + LangChain4j) + MCP server
- [ ] Fase 4 - observabilidade completa + Kubernetes + CI/CD

---

## Autor

Projeto de portfolio da pos-graduacao Java Elite - UNIPDS.
