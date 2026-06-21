-- Contas pre-carregadas para facilitar a demonstracao e os testes.
-- (Requisito A: "base pre-carregada de clientes OU permitir cadastro").
--
-- 10 contas de clientes + 1 conta de administrador (ADM) com saldo alto
-- para testes de transferencia.
--
-- IMPORTANTE: se o banco ja tiver sido criado com a versao anterior desta
-- migration, o Flyway acusara "checksum mismatch". Para recarregar do zero:
--   docker compose down -v   (apaga o volume)  e depois  docker compose up
-- ou, sem Docker, recrie o schema/banco.

INSERT INTO accounts (id, name, balance, version, created_at, updated_at) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Maria Silva',      1000.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'Joao Souza',        500.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'Ana Pereira',      2500.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('44444444-4444-4444-4444-444444444444', 'Carlos Lima',       750.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555555', 'Beatriz Costa',    3200.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('66666666-6666-6666-6666-666666666666', 'Pedro Santos',     1500.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('77777777-7777-7777-7777-777777777777', 'Juliana Almeida',  4800.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('88888888-8888-8888-8888-888888888888', 'Rafael Oliveira',   980.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('99999999-9999-9999-9999-999999999999', 'Fernanda Rocha',   6100.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Lucas Martins',     250.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'ADM',           1000000.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
