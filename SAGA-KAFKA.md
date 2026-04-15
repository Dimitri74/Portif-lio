# Saga Kafka — Florinda Eats 2.0

## Fluxo end-to-end: criação de pedido até notificação

```
Cliente
  │
  ▼
POST /v1/pedidos                         (ms-pedidos :8080)
  │
  ├─► Valida restaurante ABERTO          (REST → ms-catalogo :8082)
  ├─► Persiste Pedido [PENDENTE]         (MySQL: pedidos_db)
  └─► Publica: order.created ────────────────────────────────────────────┐
                                                                          │
                                         ┌────────────────────────────────┘
                                         ▼
                                    Apache Kafka
                                    topic: order.created
                                         │
                              ┌──────────┴──────────┐
                              ▼                     ▼
                       ms-pagamentos         ms-notificacoes
                       (consumer)            (consumer)
                              │                     │
                              │                     └─► LOG: "Pedido recebido!"
                              ▼
                    Processa pagamento
                    via GatewaySimulado
                    (Circuit Breaker +
                     Retry + Fallback)
                              │
              ┌───────────────┴───────────────┐
              ▼ APROVADO                      ▼ REJEITADO
    Publica: payment.approved       Publica: payment.failed
              │                              │
    ┌─────────┴──────────┐        ┌──────────┴──────────┐
    ▼                    ▼        ▼                      ▼
ms-pedidos        ms-notif.   ms-pedidos           ms-notif.
(consumer)        (consumer)  (consumer)           (consumer)
    │                  │          │                     │
    ▼                  ▼          ▼                     ▼
Pedido           LOG: "Pag.    Pedido              LOG: "Falha
[CONFIRMADO]     aprovado!"  [CANCELADO]           pagamento!"
    │
    └─► Publica: order.status.updated [CONFIRMADO]
                              │
                    ┌─────────┴──────────┐
                    ▼                    ▼
             ms-notificacoes       ms-ia-suporte
             (consumer)            (consumer - Fase 3)
                    │
                    └─► LOG: "Pedido confirmado!"
```

## Tópicos e contratos

| Tópico                 | Producer       | Consumers                          |
|------------------------|----------------|------------------------------------|
| `order.created`        | ms-pedidos     | ms-pagamentos, ms-notificacoes     |
| `order.status.updated` | ms-pedidos     | ms-ia-suporte, ms-notificacoes     |
| `payment.approved`     | ms-pagamentos  | ms-pedidos, ms-notificacoes        |
| `payment.failed`       | ms-pagamentos  | ms-pedidos, ms-notificacoes        |
| `catalog.item.updated` | ms-catalogo    | ms-ia-suporte (Fase 3)             |

## Como rodar o fluxo completo localmente

```powershell
# 1. Sobe infra
pwsh -File .\dev-up.ps1 -SkipObservability

# 2. Sobe ms-catalogo (porta 8082) — precisa estar UP para ms-pedidos validar restaurante
cd ms-catalogo && mvn quarkus:dev &

# 3. Sobe ms-pagamentos (porta 8081) — consumer de order.created
cd ms-pagamentos && mvn quarkus:dev &

# 4. Sobe ms-notificacoes (porta 8084) — consumer de todos os eventos
cd ms-notificacoes && mvn quarkus:dev &

# 5. Sobe ms-pedidos (porta 8080) — dispara a saga
cd ms-pedidos && mvn quarkus:dev

# 6. Cria um pedido e observa a saga no Kafka UI (http://localhost:8090)
curl -X POST http://localhost:8080/v1/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId":     "11111111-0000-0000-0000-000000000001",
    "restauranteId": "a1b2c3d4-0000-0000-0000-000000000001",
    "itens": [
      {
        "itemId":        "c1000000-0000-0000-0000-000000000001",
        "nomeItem":      "Picanha na brasa",
        "precoUnitario": 89.90,
        "quantidade":    1
      }
    ],
    "enderecoEntrega": "Rua das Flores, 10, Juazeiro do Norte CE"
  }'
```

## Observando a saga no Kafka UI

Acesse http://localhost:8090 e observe os tópicos:
- `order.created` — 1 mensagem publicada pelo ms-pedidos
- `payment.approved` — 1 mensagem publicada pelo ms-pagamentos (se PIX)
- `order.status.updated` — 1 mensagem [CONFIRMADO] publicada pelo ms-pedidos

Nos logs dos serviços você verá:
```
ms-pedidos:    INFO  Pedido criado: <uuid> | total: R$ 89.90
ms-pagamentos: INFO  Evento order.created recebido: pedido <uuid> valor=R$89.90
ms-pagamentos: INFO  Pagamento APROVADO: <uuid> | gateway: GW-XXXXXXXX
ms-pedidos:    INFO  Pedido <uuid> confirmado via pagamento <uuid>
ms-notific.:   INFO  [NOTIFICACAO] tipo=PEDIDO_CRIADO    mensagem='Seu pedido foi recebido!'
ms-notific.:   INFO  [NOTIFICACAO] tipo=PAGAMENTO_APROVADO mensagem='Pagamento de R$ 89.90 aprovado'
ms-notific.:   INFO  [NOTIFICACAO] tipo=PEDIDO_CONFIRMADO mensagem='Seu pedido foi confirmado!'
```

## Rodando os testes de integração com Testcontainers

```bash
# Testa ms-catalogo com PostgreSQL real
cd ms-catalogo
mvn test -Dtest=RestauranteIntegrationTest

# Testa ms-pedidos com MySQL real
cd ms-pedidos
mvn test -Dtest=PedidoIntegrationTest

# Testa ms-pagamentos com MySQL real (valida RN06 e RN08)
cd ms-pagamentos
mvn test -Dtest=PagamentoIntegrationTest
```

> Os Testcontainers sobem os bancos e o Kafka automaticamente —
> não precisa do docker-compose rodando para os testes de integração.
