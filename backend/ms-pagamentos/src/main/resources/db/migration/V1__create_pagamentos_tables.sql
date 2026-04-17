-- V1__create_pagamentos_tables.sql
-- Florinda Eats — ms-pagamentos (MySQL)

-- -----------------------------------------------------------
-- Pagamentos
-- RN06: pedido_id UNIQUE garante idempotência
-- -----------------------------------------------------------
CREATE TABLE pagamentos (
    id              CHAR(36)        PRIMARY KEY,
    pedido_id       CHAR(36)        NOT NULL UNIQUE,   -- RN06: idempotência
    cliente_id      CHAR(36)        NOT NULL,
    valor           DECIMAL(10, 2)  NOT NULL,
    metodo          VARCHAR(20)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDENTE',
    gateway_id      VARCHAR(100),                      -- ID externo do gateway
    gateway_payload TEXT,                              -- resposta raw do gateway
    tentativas      INT             NOT NULL DEFAULT 0,
    criado_em       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    atualizado_em   DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_metodo CHECK (
        metodo IN ('CARTAO_CREDITO','CARTAO_DEBITO','PIX','VALE_REFEICAO')
    ),
    CONSTRAINT chk_status CHECK (
        status IN ('PENDENTE','PROCESSANDO','APROVADO','REJEITADO','ESTORNADO')
    ),
    CONSTRAINT chk_valor CHECK (valor > 0)
);

CREATE INDEX idx_pagamentos_pedido    ON pagamentos (pedido_id);
CREATE INDEX idx_pagamentos_cliente   ON pagamentos (cliente_id);
CREATE INDEX idx_pagamentos_status    ON pagamentos (status);
CREATE INDEX idx_pagamentos_criado_em ON pagamentos (criado_em);

-- -----------------------------------------------------------
-- Estornos
-- RN08: só pagamentos APROVADO + pedido CANCELADO
-- -----------------------------------------------------------
CREATE TABLE estornos (
    id              CHAR(36)        PRIMARY KEY,
    pagamento_id    CHAR(36)        NOT NULL,
    valor           DECIMAL(10, 2)  NOT NULL,
    motivo          VARCHAR(300)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'SOLICITADO',
    gateway_id      VARCHAR(100),
    criado_em       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    atualizado_em   DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_estorno_pagamento FOREIGN KEY (pagamento_id)
        REFERENCES pagamentos(id),
    CONSTRAINT chk_estorno_status CHECK (
        status IN ('SOLICITADO','PROCESSADO','FALHOU')
    )
);

CREATE INDEX idx_estornos_pagamento ON estornos (pagamento_id);
