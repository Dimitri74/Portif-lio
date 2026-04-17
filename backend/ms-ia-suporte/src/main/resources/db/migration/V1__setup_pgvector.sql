-- V1__setup_pgvector.sql
-- Florinda Eats — ms-ia-suporte
-- Cria extensão vector, tabela de embeddings e índice IVFFlat

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- -----------------------------------------------------------
-- Base de conhecimento RAG
-- dimensão 768 = nomic-embed-text
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS knowledge_embeddings (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    conteudo    TEXT        NOT NULL,
    embedding   vector(768),
    fonte       VARCHAR(50) NOT NULL,   -- 'faq', 'cardapio', 'pedido', 'politica'
    fonte_id    VARCHAR(100),           -- UUID do item de origem (para invalidação)
    metadata    JSONB,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índice IVFFlat para busca semântica eficiente
-- lists=100 é adequado para até ~1M de vetores
CREATE INDEX IF NOT EXISTS idx_embedding_cosine
    ON knowledge_embeddings
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX IF NOT EXISTS idx_fonte          ON knowledge_embeddings (fonte);
CREATE INDEX IF NOT EXISTS idx_fonte_id       ON knowledge_embeddings (fonte_id);
CREATE INDEX IF NOT EXISTS idx_criado_em      ON knowledge_embeddings (criado_em);

-- -----------------------------------------------------------
-- Histórico de conversas (memória de longo prazo — Fase futura)
-- Fase 3: memória de curto prazo via Redis
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS conversas (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    sessao_id       VARCHAR(100) NOT NULL,
    cliente_id      UUID,
    role            VARCHAR(20) NOT NULL,   -- 'user' | 'assistant'
    conteudo        TEXT        NOT NULL,
    tokens          INT,
    criado_em       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_conversas_sessao ON conversas (sessao_id);
CREATE INDEX IF NOT EXISTS idx_conversas_cliente ON conversas (cliente_id);
