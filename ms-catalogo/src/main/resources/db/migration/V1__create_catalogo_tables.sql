-- V1__create_catalogo_tables.sql
-- Florinda Eats — ms-catalogo
-- Cria as tabelas: restaurantes, cardapios, itens_cardapio

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- -----------------------------------------------------------
-- Restaurantes
-- -----------------------------------------------------------
CREATE TABLE restaurantes (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome                VARCHAR(150)    NOT NULL,
    descricao           VARCHAR(500),
    categoria           VARCHAR(50)     NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'FECHADO',
    telefone            VARCHAR(20),
    email               VARCHAR(100),
    -- Endereço embutido (Value Object)
    endereco_logradouro VARCHAR(200)    NOT NULL,
    endereco_numero     VARCHAR(10)     NOT NULL,
    endereco_bairro     VARCHAR(100)    NOT NULL,
    endereco_cidade     VARCHAR(100)    NOT NULL,
    endereco_uf         CHAR(2)         NOT NULL,
    endereco_cep        VARCHAR(9)      NOT NULL,
    -- Horário de funcionamento
    horario_abertura    TIME,
    horario_fechamento  TIME,
    -- Auditoria
    criado_em           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    atualizado_em       TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_restaurantes_status   ON restaurantes (status);
CREATE INDEX idx_restaurantes_categoria ON restaurantes (categoria);
CREATE INDEX idx_restaurantes_cidade   ON restaurantes (endereco_cidade);

-- -----------------------------------------------------------
-- Cardápios (um restaurante pode ter mais de um cardápio)
-- ex: "Almoço", "Jantar", "Delivery"
-- -----------------------------------------------------------
CREATE TABLE cardapios (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    restaurante_id  UUID            NOT NULL REFERENCES restaurantes(id) ON DELETE CASCADE,
    nome            VARCHAR(100)    NOT NULL,
    descricao       VARCHAR(300),
    ativo           BOOLEAN         NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cardapios_restaurante ON cardapios (restaurante_id);
CREATE INDEX idx_cardapios_ativo       ON cardapios (ativo);

-- -----------------------------------------------------------
-- Itens do cardápio
-- -----------------------------------------------------------
CREATE TABLE itens_cardapio (
    id              UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    cardapio_id     UUID            NOT NULL REFERENCES cardapios(id) ON DELETE CASCADE,
    nome            VARCHAR(150)    NOT NULL,
    descricao       VARCHAR(500),
    preco           NUMERIC(8, 2)   NOT NULL CHECK (preco >= 0),
    disponivel      BOOLEAN         NOT NULL DEFAULT TRUE,
    foto_url        VARCHAR(500),
    -- Informações nutricionais opcionais
    calorias        INTEGER,
    vegetariano     BOOLEAN         NOT NULL DEFAULT FALSE,
    vegano          BOOLEAN         NOT NULL DEFAULT FALSE,
    sem_gluten      BOOLEAN         NOT NULL DEFAULT FALSE,
    -- Auditoria
    criado_em       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_itens_cardapio_cardapio    ON itens_cardapio (cardapio_id);
CREATE INDEX idx_itens_cardapio_disponivel  ON itens_cardapio (disponivel);
CREATE INDEX idx_itens_cardapio_preco       ON itens_cardapio (preco);

-- -----------------------------------------------------------
-- Trigger: atualiza automaticamente o campo atualizado_em
-- -----------------------------------------------------------
CREATE OR REPLACE FUNCTION set_atualizado_em()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_restaurantes_atualizado_em
    BEFORE UPDATE ON restaurantes
    FOR EACH ROW EXECUTE FUNCTION set_atualizado_em();

CREATE TRIGGER trg_cardapios_atualizado_em
    BEFORE UPDATE ON cardapios
    FOR EACH ROW EXECUTE FUNCTION set_atualizado_em();

CREATE TRIGGER trg_itens_cardapio_atualizado_em
    BEFORE UPDATE ON itens_cardapio
    FOR EACH ROW EXECUTE FUNCTION set_atualizado_em();
