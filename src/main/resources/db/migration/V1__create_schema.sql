-- Esquema inicial do banco digital.

CREATE TABLE accounts (
    id          UUID            NOT NULL,
    name        VARCHAR(150)    NOT NULL,
    balance     NUMERIC(19, 2)  NOT NULL,
    version     BIGINT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT ck_accounts_balance_non_negative CHECK (balance >= 0)
);

CREATE TABLE transfers (
    id                      UUID            NOT NULL,
    source_account_id       UUID            NOT NULL,
    destination_account_id  UUID            NOT NULL,
    amount                  NUMERIC(19, 2)  NOT NULL,
    idempotency_key         VARCHAR(100),
    created_at              TIMESTAMP       NOT NULL,
    CONSTRAINT pk_transfers PRIMARY KEY (id),
    CONSTRAINT fk_transfers_source FOREIGN KEY (source_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_transfers_destination FOREIGN KEY (destination_account_id) REFERENCES accounts (id),
    CONSTRAINT ck_transfers_amount_positive CHECK (amount > 0),
    CONSTRAINT uk_transfers_idempotency_key UNIQUE (idempotency_key)
);

-- Acelera a consulta de movimentacoes (extrato) por conta.
CREATE INDEX idx_transfers_source ON transfers (source_account_id, created_at DESC);
CREATE INDEX idx_transfers_destination ON transfers (destination_account_id, created_at DESC);

CREATE TABLE notifications (
    id          UUID            NOT NULL,
    account_id  UUID            NOT NULL,
    transfer_id UUID            NOT NULL,
    type        VARCHAR(30)     NOT NULL,
    message     VARCHAR(500)    NOT NULL,
    created_at  TIMESTAMP       NOT NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id)
);

CREATE INDEX idx_notifications_account ON notifications (account_id);
