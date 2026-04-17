<div align="center">

<img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Quarkus-3.17.5-4695EB?style=for-the-badge&logo=quarkus&logoColor=white"/>
<img src="https://img.shields.io/badge/Next.js-15-000000?style=for-the-badge&logo=nextdotjs&logoColor=white"/>
<img src="https://img.shields.io/badge/Apache_Kafka-3.9-231F20?style=for-the-badge&logo=apachekafka&logoColor=white"/>
<img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
<img src="https://img.shields.io/badge/Kubernetes-Minikube-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white"/>

# 🍽️ Florinda Eats 2.0

### Plataforma completa de Food Delivery — Portfólio de Pós-Graduação Java Elite · UNIPDS

> **Arquitetura de microsserviços** com Quarkus, Kafka, IA generativa com RAG, frontend Next.js 15,  
> observabilidade completa e deploy nativo via GraalVM + Kubernetes.

[![GitHub last commit](https://img.shields.io/github/last-commit/Dimitri74/Portifolio-UNIPDS?style=flat-square&color=4695EB)](https://github.com/Dimitri74/Portifolio-UNIPDS)
[![License](https://img.shields.io/badge/license-MIT-green?style=flat-square)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=flat-square&logo=openjdk)](https://adoptium.net/)
[![Status](https://img.shields.io/badge/status-Em%20Produção-success?style=flat-square)]()

</div>

---

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Estrutura do Repositório](#-estrutura-do-repositório)
- [Backend — Microsserviços](#-backend--microsserviços-quarkus)
- [Frontend — Dashboard & App](#-frontend--dashboard--app-nextjs)
- [Stack Tecnológica Completa](#-stack-tecnológica-completa)
- [Fluxo SAGA com Kafka](#-fluxo-saga-com-kafka)
- [Observabilidade](#-observabilidade)
- [Como Executar Localmente](#-como-executar-localmente)
- [Kubernetes & CI/CD](#-kubernetes--cicd)
- [Autor](#-autor)

---

## 🎯 Visão Geral

O **Florinda Eats 2.0** é uma plataforma completa de food delivery construída como projeto de portfólio da pós-graduação **Java Elite (UNIPDS)**. O projeto demonstra domínio de arquitetura moderna de software em produção:

| Característica | Detalhe |
|---|---|
| 🏗️ Padrão arquitetural | Microsserviços com Clean Architecture + DDD |
| 📡 Comunicação assíncrona | SAGA Pattern via Apache Kafka |
| 🤖 Inteligência Artificial | Agente conversacional RAG com LangChain4j + Ollama |
| 🔌 Protocolo MCP | Model Context Protocol para tools de IA |
| 📊 Observabilidade | OpenTelemetry + Prometheus + Grafana + Jaeger |
| ☸️ Containerização | Docker Compose + Kubernetes (Minikube) + GraalVM Native |
| 🖥️ Frontend | Dashboard administrativo + App de pedidos em Next.js 15 |
| 🔁 Resiliência | Circuit Breaker, Retry, Timeout, Fallback |
| 🧪 Testes | Unitários + Integração com Testcontainers |
| 🚀 CI/CD | GitHub Actions com pipeline completo |

---

## 🏛️ Arquitetura

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        FLORINDA EATS 2.0                                │
│                                                                         │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐              │
│  │  ms-catalogo │    │  ms-pedidos  │    │ms-pagamentos │              │
│  │  :8082       │    │  :8080       │    │  :8081       │              │
│  │  PostgreSQL  │    │    MySQL     │    │    MySQL     │              │
│  │  PgVector    │    │  Flyway      │    │  Flyway      │              │
│  │  Redis Cache │    │  Reactive    │    │  Fault Tol.  │              │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘              │
│         │                   │                    │                      │
│         └───────────────────┴────────────────────┘                     │
│                             │                                           │
│                    ┌────────▼────────┐                                  │
│                    │  Apache Kafka   │  SAGA Pattern                    │
│                    │  :9092          │  order.created                   │
│                    │                 │  payment.approved                │
│                    └────────┬────────┘  payment.failed                 │
│                             │           order.status.updated            │
│              ┌──────────────┼──────────────┐                           │
│              │              │              │                            │
│   ┌──────────▼──────┐ ┌─────▼──────┐ ┌────▼─────────────┐            │
│   │ms-notificacoes  │ │ms-ia-suporte│ │mcp-florinda-server│            │
│   │  :8084          │ │  :8083      │ │  :8085            │            │
│   │  Kafka Consumer │ │  LangChain4j│ │  MCP Protocol SSE │            │
│   │                 │ │  RAG+PgVec  │ │  Tools: status,   │            │
│   │                 │ │  Ollama     │ │  cancel, menu     │            │
│   └─────────────────┘ └─────────────┘ └───────────────────┘           │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────┐           │
│  │               OBSERVABILIDADE (Docker)                   │           │
│  │   Prometheus :9090  │  Grafana :3000  │  Jaeger :16686  │           │
│  │   OTEL Collector :4317  │  Alertas automáticos          │           │
│  └─────────────────────────────────────────────────────────┘           │
│                                                                         │
│  ┌───────────────────────────────────────┐                             │
│  │        FRONTEND (Next.js 15)          │                             │
│  │   Dashboard Admin  │  App de Pedidos  │   :3001                     │
│  │   Recharts         │  Tailwind CSS 4  │                             │
│  └───────────────────────────────────────┘                             │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Estrutura do Repositório

```
Portifolio-UNIPDS/
│
├── backend/                          # 🔵 Microsserviços Java/Quarkus
│   ├── ms-catalogo/                  #    Catálogo de restaurantes e cardápios
│   ├── ms-pedidos/                   #    Gestão de pedidos
│   ├── ms-pagamentos/                #    Processamento de pagamentos
│   ├── ms-notificacoes/              #    Notificações via Kafka
│   ├── ms-ia-suporte/                #    Agente IA com RAG
│   ├── mcp-florinda-server/          #    MCP Server (tools para IA)
│   ├── kubernetes/                   #    Manifests K8s
│   ├── observabilidade/              #    Docker Compose + Grafana + Prometheus
│   ├── testes-integracao/            #    Suite Testcontainers
│   ├── .github/workflows/            #    CI/CD GitHub Actions
│   └── pom.xml                       #    Maven monorepo root
│
└── frontend/                         # 🟢 Dashboard & App Next.js 15
    ├── src/app/                      #    Pages (App Router)
    ├── src/components/               #    Componentes React
    │   ├── dashboard/                #    Charts, Stats, Tables
    │   ├── pedidos/                  #    Cardápio, Carrinho, Checkout
    │   ├── admin/                    #    Formulários de gestão
    │   ├── chat/                     #    Widget IA integrado
    │   ├── layout/                   #    Header, Sidebar, MainLayout
    │   └── ui/                       #    Design System próprio
    ├── src/hooks/                    #    Custom hooks (useCart)
    ├── src/lib/                      #    API client, utilitários
    └── src/types/                    #    TypeScript types
```

---

## 🔵 Backend — Microsserviços (Quarkus)

### Módulos e Responsabilidades

| Módulo | Porta | Banco | Responsabilidade |
|---|---|---|---|
| **ms-catalogo** | 8082 | PostgreSQL + PgVector | CRUD de restaurantes, cardápios e itens. Cache Redis. Publica eventos `catalog.item.updated`. Suporte a embeddings vetoriais para RAG. |
| **ms-pedidos** | 8080 | MySQL | Criação e gestão de pedidos. Reactive datasource + JDBC para Flyway. Publica `order.created` e `order.status.updated`. Consome `payment.*`. |
| **ms-pagamentos** | 8081 | MySQL | Processamento de pagamentos com gateway simulado. Fault Tolerance (Circuit Breaker + Retry + Fallback). Publica `payment.approved/failed`. |
| **ms-notificacoes** | 8084 | — | Consumer Kafka puro. Registra e exibe histórico de todas as notificações da saga em tempo real. |
| **ms-ia-suporte** | 8083 | PostgreSQL + PgVector | Agente IA conversacional. Pipeline RAG completo com LangChain4j, embeddings nomic-embed-text e LLM llama3.2 via Ollama. Histórico de conversa em Redis. |
| **mcp-florinda-server** | 8085 | — | Servidor MCP (Model Context Protocol) com SSE. Expõe tools `getOrderStatus`, `cancelOrder`, `getMenu` para agentes IA externos. |

### Endpoints Principais

```
# Catálogo
GET  /v1/restaurantes
POST /v1/restaurantes
GET  /v1/restaurantes/{id}/cardapios

# Pedidos
POST /v1/pedidos
GET  /v1/pedidos/{id}

# Pagamentos
POST /v1/pagamentos
POST /v1/pagamentos/{id}/estorno

# Agente IA
POST /v1/ia/chat
POST /v1/ia/admin/ingerir      (RAG ingestion)
GET  /v1/ia/health

# MCP
GET  /mcp/sse                  (SSE stream)

# Observabilidade (todos os módulos)
GET  /q/health
GET  /q/metrics
GET  /swagger-ui
```

---

## 🟢 Frontend — Dashboard & App (Next.js)

Interface moderna construída com **Next.js 15 App Router**, **TypeScript**, **Tailwind CSS 4** e **Recharts**.

### Páginas e Funcionalidades

| Página | Rota | Funcionalidade |
|---|---|---|
| **Home** | `/` | Landing page com visão geral da plataforma |
| **Dashboard** | `/dashboard` | KPIs em tempo real: receita, pedidos, gráficos de pizza e linha |
| **Pedidos** | `/pedidos` | Cardápio interativo, carrinho, checkout e rastreamento |
| **Admin** | `/admin` | Gestão de restaurantes, cardápios e itens |

### Componentes Destacados

- **`ChatWidget`** — Widget flutuante integrado ao agente IA (`POST /v1/ia/chat`)
- **`RevenueChart`** — Gráfico de receita com Recharts (LineChart responsivo)
- **`OrdersPieChart`** — Distribuição de pedidos por status
- **`CartDrawer`** — Carrinho lateral com gestão de estado via `useCart` hook
- **`OrderTracking`** — Rastreamento de pedido em tempo real
- **`StatsCard`** — Cards de métricas com ícones Lucide React

---

## 🛠️ Stack Tecnológica Completa

### ☕ Backend — Linguagem & Runtime

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **Java** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="32"/> | 21 LTS | Linguagem principal de todos os microsserviços. Java 21 traz Virtual Threads (Project Loom) para alta concorrência sem custo de threads do SO, pattern matching avançado e performance otimizada. Toda a lógica de negócio, domínio, serviços e consumers Kafka são escritos em Java. |
| **Quarkus** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/quarkus/quarkus-original.svg" width="32"/> | 3.17.5 | Framework cloud-native otimizado para microsserviços. Startup em 1-2s em modo JVM (vs 10-15s do Spring Boot) e 32ms em modo nativo. Dev Mode com live reload automático. BOM unificado para gerenciamento de dependências. Base de todos os 6 módulos. |
| **Maven** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/maven/maven-original.svg" width="32"/> | 3.9.9 | Ferramenta de build e gerenciamento de dependências. Monorepo com módulos hierárquicos — um único `mvn install` compila todos os 6 microsserviços. Gerencia plugins Quarkus, Jib, Native Image e execução de testes. |
| **GraalVM Native Image** | <img src="https://www.graalvm.org/resources/img/logo-colored.svg" width="32"/> | 21 CE | Compilador AOT (Ahead-of-Time) que transforma bytecode Java em binário nativo executável. Resulta em startup de 32ms e footprint de memória ~70% menor. Usado no `ms-catalogo` para deploy Kubernetes com imagem Docker < 100MB. |

### 🗄️ Bancos de Dados & Cache

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **PostgreSQL** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg" width="32"/> | 16 | Banco relacional robusto usado pelo `ms-catalogo` e `ms-ia-suporte`. Armazena restaurantes, cardápios, itens e dados de conhecimento para RAG. Suporta a extensão PgVector para operações de similaridade vetorial. |
| **PgVector** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg" width="32"/> | 0.1.0 | Extensão do PostgreSQL para armazenamento e busca de vetores (embeddings). Implementa busca por similaridade coseno/L2. Armazena os embeddings gerados pelo modelo `nomic-embed-text` para o pipeline RAG do agente IA. Consultas `<->` de distância vetorial. |
| **MySQL** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg" width="32"/> | 8.0 | Banco relacional para domínios transacionais. `ms-pedidos` usa instância na porta 3307 com driver reactive para máxima throughput. `ms-pagamentos` usa instância na porta 3308 com JDBC síncrono para transações financeiras. Schemas versionados via Flyway. |
| **Redis** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redis/redis-original.svg" width="32"/> | 7 | Cache distribuído em memória. Armazena o histórico de conversas do agente IA por sessão (`chat:{sessionId}`), cache de catálogo para reduzir carga no PostgreSQL e suporta TTL automático de sessões. Acesso via Quarkus Redis Client. |
| **Flyway** | <img src="https://www.red-gate.com/wp-content/uploads/2018/05/Flyway-logo.png" width="32"/> | (Quarkus BOM) | Versionamento e migração automática de schema de banco de dados. Scripts SQL incrementais (`V001__`, `V002__`) aplicados na inicialização de cada serviço. Garante consistência de schema entre ambientes dev/staging/prod. Usado em `ms-pedidos`, `ms-pagamentos`, `ms-ia-suporte`. |

### 📡 Mensageria & Streaming

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **Apache Kafka** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/apachekafka/apachekafka-original.svg" width="32"/> | 3.9.0 | Message broker distribuído para comunicação assíncrona entre microsserviços. Implementa o padrão SAGA coreografado: cada serviço publica eventos e reage a eventos de outros. Garante entrega ordenada, durabilidade e escalabilidade. Tópicos: `order.created`, `payment.approved`, `payment.failed`, `order.status.updated`, `catalog.item.updated`. |
| **SmallRye Reactive Messaging** | <img src="https://smallrye.io/images/logo.png" width="32"/> | (Quarkus BOM) | Abstração sobre Kafka integrada ao Quarkus. Permite consumir/produzir mensagens com simples annotations `@Incoming` e `@Outgoing` nos métodos. Suporta backpressure reativo, retry automático, Dead Letter Queue e integração transparente com CDI/Quarkus. |

### 🤖 Inteligência Artificial & ML

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **LangChain4j** | <img src="https://docs.langchain4j.dev/img/logo.png" width="32"/> | 0.26.1 | Framework Java de orquestração para aplicações de IA. Gerencia o pipeline RAG completo: chunking de documentos → geração de embeddings → armazenamento vetorial → recuperação semântica → augmentação do prompt → resposta do LLM. Gerencia histórico de conversas (Memory) em Redis e integra com Ollama. Core do `ms-ia-suporte`. |
| **Ollama** | <img src="https://ollama.com/public/ollama.png" width="32"/> | latest | Servidor LLM local para execução de modelos de linguagem sem dependência de APIs cloud. Roda dois modelos: `llama3.2` (7B params) para geração de respostas conversacionais e `nomic-embed-text` para geração de embeddings vetoriais. Expõe REST API na porta 11434. |
| **MCP Protocol** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/quarkus/quarkus-original.svg" width="32"/> | 1.0.0.Alpha6 | Model Context Protocol via Quarkiverse. Protocolo padronizado para expor "ferramentas" que agentes IA podem chamar. O `mcp-florinda-server` expõe: `getOrderStatus` (consultar pedido), `cancelOrder` (cancelar pedido) e `getMenu` (listar cardápio). Usa SSE como transport. |
| **RAG (Retrieval Augmented Generation)** | 🔍 | — | Padrão arquitetural de IA que enriquece prompts com contexto recuperado de base de conhecimento. Fluxo: pergunta do usuário → embedding → busca vetorial no PgVector → top-K chunks relevantes → prompt aumentado → LLM gera resposta fundamentada nos dados do projeto. Elimina alucinações e mantém respostas precisas sobre o negócio. |

### 🔁 Resiliência & Fault Tolerance

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **SmallRye Fault Tolerance** | <img src="https://smallrye.io/images/logo.png" width="32"/> | (Quarkus BOM) | Implementação de padrões de resiliência via annotations. `@Retry`: tenta a operação N vezes antes de falhar. `@Timeout`: falha a operação se demorar além do limite. `@CircuitBreaker`: abre o circuito após X falhas consecutivas, evitando cascata de erros. `@Fallback`: retorna resposta degradada de emergência. Aplicado em `ms-pagamentos` e `ms-ia-suporte`. |

### 📊 Observabilidade & Monitoramento

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **OpenTelemetry** | <img src="https://opentelemetry.io/img/logos/opentelemetry-horizontal-color.svg" width="48"/> | 1.35.0 | Padrão aberto para observabilidade distribuída. Instrumentação automática de requisições HTTP, operações Kafka, queries de banco e chamadas entre serviços. Gera traces distribuídos que mostram o caminho completo de uma requisição pelos microsserviços. Exporta para Jaeger (traces) e Prometheus (métricas). |
| **Prometheus** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/prometheus/prometheus-original.svg" width="32"/> | latest | Sistema de monitoramento e alertas time-series. Faz scrape das métricas de todos os 6 serviços via `/q/metrics` a cada 15s. Coleta: JVM heap/GC/threads, latência por endpoint, taxa de erros, status do Circuit Breaker, throughput Kafka. Alertas configurados em `florinda-alerts.yml`: serviço DOWN, latência > 2s, erro 5xx > 1%. |
| **Grafana** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/grafana/grafana-original.svg" width="32"/> | latest | Plataforma de visualização de métricas e dashboards. 2 dashboards provisionados automaticamente via GitOps: **"Florinda Eats — Visão Geral"** (latência P95, taxa de erros, throughput, Circuit Breaker) e **"JVM vs Native"** (comparação de startup time, heap e CPU). Integrado ao Jaeger como datasource para correlação trace-métrica. |
| **Jaeger** | <img src="https://www.jaegertracing.io/img/jaeger-logo.png" width="32"/> | latest | Plataforma de rastreamento distribuído (distributed tracing). Recebe spans do OTEL Collector e monta o trace completo de cada requisição. Permite visualizar: tempo gasto em cada microsserviço, queries de banco, chamadas Kafka, identificação de gargalos. UI acessível em `localhost:16686`. |
| **Micrometer** | <img src="https://micrometer.io/images/micrometer-logo-dark.svg" width="32"/> | (Quarkus BOM) | Facade de métricas de aplicação. Integração automática com Prometheus via Quarkus. Coleta métricas de negócio (pedidos criados, pagamentos processados, erros de gateway) além das métricas JVM padrão. |

### 🌐 APIs & Protocolos

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **Jakarta REST (JAX-RS)** | <img src="https://jakarta.ee/images/jakarta/jakarta-ee-logo-color.svg" width="48"/> | (Jakarta EE) | Padrão para criação de APIs RESTful. Todos os microsserviços expõem endpoints `/v1/*` com suporte a content negotiation JSON automático, validação via Bean Validation e tratamento global de exceções via `@Provider`. |
| **OpenAPI 3.0 / Swagger UI** | <img src="https://upload.wikimedia.org/wikipedia/commons/a/ab/Swagger-logo.png" width="32"/> | (SmallRye) | Documentação automática de APIs gerada a partir das annotations JAX-RS. Cada módulo expõe Swagger UI em `/swagger-ui` com todos os endpoints, schemas de request/response e exemplos. Permite testes interativos de toda a API diretamente no browser. |
| **Server-Sent Events (SSE)** | 📡 | HTTP std | Protocolo de push unidirecional (servidor → cliente) baseado em HTTP. Usado pelo `mcp-florinda-server` como transport para o Model Context Protocol. Permite que agentes IA se conectem e recebam respostas em streaming das tools. |
| **REST Client (MicroProfile)** | <img src="https://microprofile.io/wp-content/uploads/sites/3/2016/09/microprofile-logo.png" width="32"/> | (Quarkus BOM) | Cliente HTTP declarativo para comunicação síncrona entre microsserviços. O `mcp-florinda-server` usa para chamar `ms-pedidos` e `ms-catalogo`. Configuração via `@RegisterRestClient` e injeção CDI. |

### 🗃️ ORM & Persistência

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **Hibernate Panache** | <img src="https://hibernate.org/images/hibernate-logo.svg" width="48"/> | (Quarkus BOM) | Abstração ORM simplificada sobre Hibernate. Pattern Active Record: entidades herdam de `PanacheEntity` e ganham `persist()`, `findById()`, `listAll()` automaticamente. Suporte a queries JPQL type-safe e paginação. Usado em `ms-catalogo` (PostgreSQL) e `ms-pagamentos` (MySQL). |
| **Hibernate Reactive** | <img src="https://hibernate.org/images/hibernate-logo.svg" width="48"/> | (Quarkus BOM) | Versão não-bloqueante do Hibernate para I/O assíncrono com banco de dados. Usado no `ms-pedidos` para maximizar throughput sem bloquear threads. Integração com Mutiny (`Uni<>`, `Multi<>`) para pipeline reativo end-to-end. |

### 🔐 Validação & Segurança

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **Jakarta Bean Validation** | <img src="https://jakarta.ee/images/jakarta/jakarta-ee-logo-color.svg" width="48"/> | (Jakarta EE) | Validação declarativa de DTOs via annotations: `@NotNull`, `@NotBlank`, `@Email`, `@Size`, `@Pattern`, `@Min`, `@Max`. Validação automática nos endpoints REST — resposta `400 Bad Request` com detalhes do erro quando a validação falha. |
| **Quarkus Security** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/quarkus/quarkus-original.svg" width="32"/> | (Quarkus BOM) | Framework de segurança preparado para OIDC, JWT e RBAC. Estrutura pronta para integração com Keycloak ou Auth0. Endpoints sensíveis (`/v1/ia/admin/*`) protegidos por roles. |

### 🧰 Utilitários & Produtividade

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **Lombok** | <img src="https://projectlombok.org/img/lombok.png" width="32"/> | 1.18.32 | Gerador de código boilerplate Java via annotations em compile-time: `@Data` (getters/setters/equals/hashCode), `@Builder` (builder pattern), `@Slf4j` (logger SLF4J), `@RequiredArgsConstructor`. Reduz drasticamente linhas de código sem overhead em runtime. |
| **MapStruct** | <img src="https://mapstruct.org/images/mapstruct.png" width="32"/> | 1.5.5.Final | Gerador de mappers entre objetos (DTO ↔ Entity) em compile-time via annotation processing. Type-safe, sem reflection em runtime. Gera código Java puro otimizado. Usado para transformar `CriarRestauranteRequest → RestauranteEntity` e entidades → DTOs de response. |
| **SmallRye Config** | <img src="https://smallrye.io/images/logo.png" width="32"/> | (Quarkus BOM) | Gerenciamento unificado de configurações. Suporta `application.properties`, variáveis de ambiente, secrets e profiles (`%dev`, `%prod`). Injeta valores via `@ConfigProperty`. Permite override completo de config via env vars no Docker/K8s. |

### 🧪 Testes

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **JUnit 5 (Jupiter)** | <img src="https://junit.org/junit5/assets/img/junit5-logo.png" width="32"/> | 5.x | Framework de testes padrão Java. `@Test`, `@ParameterizedTest`, `@Nested`, `@BeforeEach`. Integração nativa com Quarkus via `@QuarkusTest` que sobe o contexto completo do microsserviço para testes de integração. |
| **Mockito** | <img src="https://site.mockito.org/javadoc/current/org/mockito/logo.png" width="32"/> | 5.x | Framework de mocking para testes unitários. `@Mock`, `@InjectMocks`, `when(...).thenReturn(...)`, `verify(...)`. Usado para isolar Services e testar regras de negócio sem dependências externas (banco, Kafka, HTTP). |
| **REST Assured** | <img src="https://rest-assured.io/img/logo-transparent.png" width="32"/> | 5.x | DSL fluente para testes de APIs HTTP. `given().when().get("/v1/restaurantes").then().statusCode(200).body("nome", equalTo("..."))`. Testes de contrato dos endpoints REST com JSON Path assertions. |
| **Testcontainers** | <img src="https://testcontainers.com/logo.png" width="32"/> | 1.19.8 | Biblioteca para testes de integração com infraestrutura real em Docker. Sobe PostgreSQL, MySQL e Kafka como containers efêmeros durante os testes. Suite em `testes-integracao/` para `ms-catalogo`, `ms-pedidos` e `ms-pagamentos`. Sem mocks de banco — testes "realistas". |

### 🐳 Containerização & Deploy

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **Docker** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg" width="32"/> | latest | Containerização de todos os componentes. Cada microsserviço tem Dockerfile gerado pelo Quarkus. Infraestrutura local (PostgreSQL, MySQL, Kafka, Redis, Ollama) roda em containers Docker garantindo ambiente idêntico entre dev e produção. |
| **Docker Compose** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg" width="32"/> | 2.x | Orquestração local de múltiplos containers. `observabilidade/docker-compose.yml` sobe toda a stack de monitoramento (Prometheus, Grafana, Jaeger, OTEL Collector) com um único comando. Health checks e dependências entre serviços configurados. |
| **Kubernetes (Minikube)** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kubernetes/kubernetes-plain.svg" width="32"/> | 1.28+ | Orquestração de containers em escala. Manifests em `kubernetes/` para todos os 6 microsserviços no namespace `florinda-app`. `ms-catalogo` com imagem **GraalVM Native** e **HPA** (auto-scale 1→10 pods, trigger CPU 70%). ConfigMap e Secrets centralizados. Ingress nginx em `florinda.local`. |
| **Jib** | <img src="https://cloud.google.com/images/products/jib/jib-logo.png" width="32"/> | (Quarkus) | Plugin de build de imagens OCI sem Docker CLI. Constrói imagens layer-optimizadas diretamente do Maven, separando dependências de código da aplicação para máximo aproveitamento de cache. Usado no pipeline CI/CD para build JVM rápido. |

### 🔄 CI/CD

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **GitHub Actions** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/github/github-original.svg" width="32"/> | — | Pipeline CI/CD completo. **`ci.yml`**: build → testes unitários → testes de integração com Testcontainers → análise de qualidade. **`cd.yml`**: build Jib (JVM) + build Native (GraalVM) → push para registry → deploy no Minikube com smoke test automático. Ativado em push/PR na branch `main`. |

---

## 🟢 Frontend — Stack Tecnológica

| Tecnologia | Ícone | Versão | O que é e o que faz no projeto |
|---|---|---|---|
| **Next.js** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nextjs/nextjs-original.svg" width="32"/> | 15.3.0 | Framework React com renderização híbrida (SSR/SSG/CSR). Usa **App Router** (diretório `app/`) com React Server Components. Route Handlers (`/api/*`) funcionam como proxy para os microsserviços backend, evitando CORS em produção. Hot reload instantâneo em dev. |
| **React** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg" width="32"/> | 19.0.0 | Biblioteca para construção de interfaces. React 19 traz melhorias de performance com o novo compilador React, Actions para formulários e hooks aprimorados. Componentes funcionais com hooks são o padrão em todo o projeto. |
| **TypeScript** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/typescript/typescript-original.svg" width="32"/> | 5.8.0 | Superset tipado do JavaScript. Tipos definidos em `src/types/index.ts` para todas as entidades do domínio (Restaurante, Pedido, Pagamento, ItemCardapio). Erros de tipo detectados em compile-time, autocompletar e refactoring seguros no IDE. |
| **Tailwind CSS** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/tailwindcss/tailwindcss-original.svg" width="32"/> | 4.1.0 | Framework CSS utility-first. Tailwind v4 é ~10x mais rápido que v3 (novo engine em Rust/Oxide). Design system baseado em classes utilitárias diretamente no JSX. Sistema de cores, espaçamento e tipografia consistentes em toda a aplicação sem CSS customizado. |
| **Recharts** | <img src="https://recharts.org/favicon.ico" width="32"/> | 2.15.0 | Biblioteca de gráficos para React baseada em D3. Usada nos componentes do Dashboard: `LineChart` para receita ao longo do tempo, `PieChart` para distribuição de pedidos por status, `BarChart` para comparativos. Responsivo e animado nativamente. |
| **Lucide React** | <img src="https://lucide.dev/logo.light.svg" width="32"/> | 0.525.0 | Biblioteca de ícones SVG para React. +1000 ícones como componentes React tree-shakeable (bundle size mínimo). Usado em todo o Design System: ícones de navegação no Sidebar, ícones em StatsCards, botões e badges da interface. |
| **ESLint** | <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/eslint/eslint-original.svg" width="32"/> | 9.x | Linter estático para JavaScript/TypeScript. Configuração `eslint-config-next` com regras específicas para Next.js. Detecta erros comuns, imports não utilizados, problemas de acessibilidade (jsx-a11y) e padrões incorretos de React hooks. Integrado ao CI/CD. |

---

## 🔄 Fluxo SAGA com Kafka

```
Cliente → POST /v1/pedidos
              │
              ▼
        ms-pedidos ──── publica ──→ [order.created]
              │                            │
              │                  ┌─────────┘
              │                  ▼
              │           ms-pagamentos
              │           (gateway simulado)
              │           Circuit Breaker ativo
              │                  │
              │         ┌────────┴────────┐
              │         ▼                 ▼
              │   [payment.approved]  [payment.failed]
              │         │                 │
              │    ┌────┘                 └────┐
              │    ▼                           ▼
              │  ms-pedidos              ms-pedidos
              │  status: APROVADO        status: CANCELADO
              │         │                      │
              └─────────┴──── publica ──→ [order.status.updated]
                                               │
                              ┌────────────────┴────────────────┐
                              ▼                                  ▼
                       ms-notificacoes                    ms-ia-suporte
                       (registra evento)                  (atualiza contexto)
```

**Tópicos Kafka:**

| Tópico | Producer | Consumer(s) |
|---|---|---|
| `order.created` | ms-pedidos | ms-pagamentos, ms-notificacoes |
| `order.status.updated` | ms-pedidos | ms-ia-suporte, ms-notificacoes |
| `payment.approved` | ms-pagamentos | ms-pedidos, ms-notificacoes |
| `payment.failed` | ms-pagamentos | ms-pedidos, ms-notificacoes |
| `catalog.item.updated` | ms-catalogo | ms-ia-suporte |

---

## 📊 Observabilidade

Stack completa de monitoramento provisionada via Docker Compose:

| Serviço | URL | Função |
|---|---|---|
| **Grafana** | http://localhost:3000 (`admin`/`admin`) | Dashboards de métricas e alertas |
| **Prometheus** | http://localhost:9090 | Coleta e consulta de métricas |
| **Jaeger UI** | http://localhost:16686 | Rastreamento distribuído de requests |
| **OTEL Collector** | localhost:4317 (gRPC) | Recebe e roteia telemetria |

**Dashboards provisionados automaticamente:**
- 📈 `Florinda Eats — Visão Geral`: latência P95, taxa de erros 5xx, throughput RPS, estado Circuit Breaker
- 🔥 `JVM vs Native`: comparativo de startup time, heap usage, CPU entre modo JVM e GraalVM Native

---

## 🚀 Como Executar Localmente

### Pré-requisitos

- Java 21 (Eclipse Adoptium recomendado)
- Maven 3.9+
- Docker Desktop
- Node.js 22+
- Ollama instalado (`ollama pull llama3.2 && ollama pull nomic-embed-text`)

### 1. Infraestrutura (Docker)

```powershell
# Sobe PostgreSQL, MySQL x2, Kafka, Redis, Ollama
.\backend\dev-up.ps1

# Sobe observabilidade (Prometheus, Grafana, Jaeger)
docker compose -f .\backend\observabilidade\docker-compose.yml up -d
```

### 2. Backend — Microsserviços

```powershell
# Cada serviço em um terminal separado (ou usar dev-up.ps1)
cd backend

mvn -pl ms-catalogo quarkus:dev       # :8082
mvn -pl ms-pedidos quarkus:dev        # :8080
mvn -pl ms-pagamentos quarkus:dev     # :8081
mvn -pl ms-notificacoes quarkus:dev   # :8084
mvn -pl ms-ia-suporte quarkus:dev     # :8083
mvn -pl mcp-florinda-server quarkus:dev # :8085
```

### 3. Frontend

```powershell
cd frontend
npm install
npm run dev   # :3001
```

### 4. Acesse

| Interface | URL |
|---|---|
| 🖥️ Frontend App | http://localhost:3001 |
| 📚 Swagger Catálogo | http://localhost:8082/swagger-ui |
| 📚 Swagger Pedidos | http://localhost:8080/swagger-ui |
| 📚 Swagger Pagamentos | http://localhost:8081/swagger-ui |
| 🤖 Chat IA | http://localhost:8083/swagger-ui |
| 📊 Grafana | http://localhost:3000 |
| 🔍 Jaeger Tracing | http://localhost:16686 |
| 📈 Prometheus | http://localhost:9090 |

---

## ☸️ Kubernetes & CI/CD

### Deploy Kubernetes (Minikube)

```bash
minikube start --cpus=4 --memory=6g
minikube addons enable ingress

kubectl apply -f backend/kubernetes/infra/00-namespaces.yml
kubectl apply -f backend/kubernetes/infra/dependencies.yml
kubectl apply -f backend/kubernetes/infra/outros-servicos.yml
kubectl apply -f backend/kubernetes/infra/ingress.yml

# Build Native do ms-catalogo
./backend/build-native.sh

kubectl get pods -n florinda-app
```

### Pipelines CI/CD (GitHub Actions)

| Workflow | Trigger | Etapas |
|---|---|---|
| `ci.yml` | Push / PR → main | Build → Testes Unitários → Testes Integração → Qualidade |
| `cd.yml` | Push → main | Build Jib → Build Native GraalVM → Deploy Minikube → Smoke Test |

---

## 📐 Princípios Arquiteturais Aplicados

| Princípio | Como foi aplicado |
|---|---|
| **Clean Architecture** | Separação em camadas: `domain/`, `service/`, `resource/`, `infra/`. Domain objects sem dependências de framework. |
| **DDD (Domain-Driven Design)** | Cada microsserviço é um Bounded Context. Linguagem ubíqua por domínio (Pedido, Pagamento, Catálogo). |
| **SAGA Pattern** | Transações distribuídas via coreografia Kafka. Sem coordenador central — cada serviço reage a eventos. |
| **CQRS** | Separação de leitura e escrita no `ms-catalogo` (reactive reads + sync writes). |
| **Strangler Fig** | Arquitetura preparada para substituição incremental de módulos sem impacto nos demais. |
| **12-Factor App** | Configuração via env vars, logs para stdout, stateless, backing services intercambiáveis. |

---

## 👤 Autor

<div align="center">

**Marcus Dimitri**  
Pós-Graduação Java Elite — UNIPDS  

[![GitHub](https://img.shields.io/badge/GitHub-Dimitri74-181717?style=for-the-badge&logo=github)](https://github.com/Dimitri74)

> *"Este projeto foi desenvolvido com o objetivo de demonstrar domínio real de arquitetura de software moderna,  
> indo além do CRUD — implementando padrões de resiliência, observabilidade, IA generativa e deploy nativo."*

</div>

---

<div align="center">

**⭐ Se este projeto foi útil, deixe uma estrela no repositório! ⭐**

<sub>Florinda Eats 2.0 — Portfólio de Pós-Graduação Java Elite · UNIPDS · 2026</sub>

</div>

