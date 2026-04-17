-- V3__fix_langchain4j_pgvector_schema.sql
-- Adiciona colunas requeridas pelo LangChain4j PgVectorEmbeddingStore.
--
-- LangChain4j espera:
--   embedding_id  UUID  (identificador do vetor)
--   text          TEXT  (conteúdo do segmento — usado no findRelevant())
--   metadata      JSON  (metadados opcionais)
--
-- A tabela foi criada em V1 com colunas "id" e "conteudo".
-- Esta migração adiciona os aliases que o LangChain4j precisa.

ALTER TABLE knowledge_embeddings
    ADD COLUMN IF NOT EXISTS embedding_id UUID;

UPDATE knowledge_embeddings
    SET embedding_id = id
    WHERE embedding_id IS NULL;

ALTER TABLE knowledge_embeddings
    ALTER COLUMN embedding_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_ke_embedding_id
    ON knowledge_embeddings (embedding_id);

ALTER TABLE knowledge_embeddings
    ADD COLUMN IF NOT EXISTS text TEXT;

UPDATE knowledge_embeddings
    SET text = conteudo
    WHERE text IS NULL;
