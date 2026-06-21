-- Contas pre-carregadas para facilitar a demonstracao no Swagger.
-- (Requisito A: "base pre-carregada de clientes OU permitir cadastro").

INSERT INTO accounts (id, name, balance, version, created_at, updated_at) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Maria Silva',  1000.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'Joao Souza',     500.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'Ana Pereira',   2500.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
