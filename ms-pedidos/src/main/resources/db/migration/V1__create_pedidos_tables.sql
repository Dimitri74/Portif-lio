-- V1__create_pedidos_tables.sql
-- Florinda Eats — ms-pedidos (MySQL)

-- -----------------------------------------------------------
-- Pedidos
-- -----------------------------------------------------------
CREATE TABLE pedidos (
    id                  CHAR(36)        PRIMARY KEY,
    cliente_id          CHAR(36)        NOT NULL,
    restaurante_id      CHAR(36)        NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDENTE',
    valor_total         DECIMAL(10, 2)  NOT NULL,
    observacao          VARCHAR(500),
    endereco_entrega    VARCHAR(500),
    criado_em           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    atualizado_em       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                            ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_status CHECK (
        status IN ('PENDENTE','CONFIRMADO','PREPARANDO','SAIU_PARA_ENTREGA','ENTREGUE','CANCELADO')
    ),
    CONSTRAINT chk_valor_total CHECK (valor_total >= 15.00)
);

CREATE INDEX idx_pedidos_cliente       ON pedidos (cliente_id);
CREATE INDEX idx_pedidos_restaurante   ON pedidos (restaurante_id);
CREATE INDEX idx_pedidos_status        ON pedidos (status);
CREATE INDEX idx_pedidos_criado_em     ON pedidos (criado_em);

-- -----------------------------------------------------------
-- Itens do pedido (snapshot do catálogo no momento do pedido)
-- -----------------------------------------------------------
CREATE TABLE itens_pedido (
    id              CHAR(36)        PRIMARY KEY,
    pedido_id       CHAR(36)        NOT NULL,
    item_id         CHAR(36)        NOT NULL,
    nome_item       VARCHAR(150)    NOT NULL,
    preco_unitario  DECIMAL(8, 2)   NOT NULL,
    quantidade      INT             NOT NULL,
    subtotal        DECIMAL(10, 2)  NOT NULL,
    CONSTRAINT fk_itens_pedido      FOREIGN KEY (pedido_id)
        REFERENCES pedidos(id) ON DELETE CASCADE,
    CONSTRAINT chk_quantidade       CHECK (quantidade >= 1),
    CONSTRAINT chk_preco_unitario   CHECK (preco_unitario > 0)
);

CREATE INDEX idx_itens_pedido_pedido ON itens_pedido (pedido_id);

-- -----------------------------------------------------------
-- Histórico de status (auditoria de cada transição)
-- -----------------------------------------------------------
CREATE TABLE historico_status_pedido (
    id          CHAR(36)        PRIMARY KEY,
    pedido_id   CHAR(36)        NOT NULL,
    status_de   VARCHAR(20),
    status_para VARCHAR(20)     NOT NULL,
    motivo      VARCHAR(300),
    criado_em   DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_historico_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos(id) ON DELETE CASCADE
);

CREATE INDEX idx_historico_pedido ON historico_status_pedido (pedido_id);
