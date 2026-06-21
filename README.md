# Banco Digital API

API REST para um banco digital fictício. Permite o **CRUD de contas**, a
**transferência de fundos entre contas** e a **consulta de movimentações
financeiras (extrato)**, com foco em **consistência sob alta concorrência**,
**resiliência** e **performance**.

Projeto desenvolvido como teste técnico.

---

## Sumário

- [Stack e versões](#stack-e-versões)
- [Como rodar](#como-rodar)
  - [Opção 1 — Docker Compose (recomendada)](#opção-1--docker-compose-recomendada)
  - [Opção 2 — Local com Maven](#opção-2--local-com-maven)
- [Documentação Swagger](#documentação-swagger)
- [Endpoints](#endpoints)
- [Exemplos de uso (cURL)](#exemplos-de-uso-curl)
- [Testes](#testes)
- [Arquitetura e decisões de design](#arquitetura-e-decisões-de-design)
- [Estrutura do projeto](#estrutura-do-projeto)

---

## Stack e versões

| Camada                 | Tecnologia                          | Versão   |
|------------------------|-------------------------------------|----------|
| Linguagem              | Java                                | 21 (LTS) |
| Framework              | Spring Boot                         | 3.5.13   |
| Build / dependências   | Maven                               | 3.9+     |
| Banco de dados         | PostgreSQL                          | 16       |
| Migrations             | Flyway                              | (gerida pelo Spring Boot) |
| Documentação           | springdoc-openapi (Swagger UI)      | 2.8.17   |
| Banco de testes        | H2 (em memória)                     | (test)   |
| Testes                 | JUnit 5, Mockito, AssertJ, MockMvc  | (test)   |

**Por que estas versões?** O requisito pedia "as versões mais recentes, porém
estáveis e sem bugs". Optei por **Java 21 (LTS)** + **Spring Boot 3.5.x** em vez
do recém-lançado Spring Boot 4.x: a linha 3.5 é madura, amplamente adotada e tem
total compatibilidade com o ecossistema (springdoc, Flyway, drivers), reduzindo
o risco de incompatibilidades. Java 21 é a versão LTS mais recente, garantindo
suporte de longo prazo e ampla portabilidade entre máquinas.

---

## Como rodar

### Opção 1 — Docker Compose (recomendada)

Sobe o PostgreSQL **e** a aplicação já configurados, sem precisar de Java/Maven
instalados na máquina.

Pré-requisito: Docker e Docker Compose.

```bash
docker compose up --build
```

A API ficará disponível em `http://localhost:8080`. Para parar:

```bash
docker compose down          # mantém os dados
docker compose down -v       # remove também o volume do banco
```

### Opção 2 — Local com Maven

Pré-requisitos: JDK 21 e Maven 3.9+. É preciso um PostgreSQL acessível.

1. Suba apenas o banco (via Docker) ou use um PostgreSQL próprio:

   ```bash
   docker run --name bank-db -e POSTGRES_DB=bankdb -e POSTGRES_USER=bank \
     -e POSTGRES_PASSWORD=bank -p 5432:5432 -d postgres:16-alpine
   ```

2. (Opcional) ajuste as variáveis de ambiente — veja `.env.example`. Os valores
   padrão já apontam para o banco acima.

3. Rode a aplicação:

   ```bash
   mvn spring-boot:run
   ```

O Flyway cria o schema e carrega 3 contas de exemplo automaticamente no primeiro
start.

---

## Documentação Swagger

Com a aplicação no ar:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Health check:** http://localhost:8080/actuator/health

---

## Endpoints

Base: `/api/v1`

### Contas

| Método | Caminho                  | Descrição                         |
|--------|--------------------------|-----------------------------------|
| POST   | `/accounts`              | Cria uma conta                    |
| GET    | `/accounts`              | Lista contas (paginado)           |
| GET    | `/accounts/{id}`         | Busca conta por ID                |
| PUT    | `/accounts/{id}`         | Atualiza dados cadastrais         |
| DELETE | `/accounts/{id}`         | Remove conta                      |

### Transferências e movimentações

| Método | Caminho                          | Descrição                             |
|--------|----------------------------------|---------------------------------------|
| POST   | `/transfers`                     | Transfere valores entre duas contas   |
| GET    | `/accounts/{id}/movements`       | Extrato (movimentações) da conta      |

`POST /transfers` aceita o header opcional **`Idempotency-Key`**.

Contas pré-carregadas para teste:

- `11111111-1111-1111-1111-111111111111` — Maria Silva (R$ 1000,00)
- `22222222-2222-2222-2222-222222222222` — João Souza (R$ 500,00)
- `33333333-3333-3333-3333-333333333333` — Ana Pereira (R$ 2500,00)

---

## Exemplos de uso (cURL)

Criar conta:

```bash
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"name": "Carlos Lima", "initialBalance": 300.00}'
```

Transferir (com idempotência):

```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 7c1f-abc-001" \
  -d '{
        "sourceAccountId": "11111111-1111-1111-1111-111111111111",
        "destinationAccountId": "22222222-2222-2222-2222-222222222222",
        "amount": 150.00
      }'
```

Consultar extrato:

```bash
curl http://localhost:8080/api/v1/accounts/11111111-1111-1111-1111-111111111111/movements
```

---

## Testes

```bash
mvn test
```

Cobertura dos testes (foco em regras de negócio):

- **AccountServiceTest** — CRUD e cenário de conta inexistente.
- **TransferServiceTest** — transferência bem-sucedida (débito/crédito + evento),
  saldo insuficiente, origem igual ao destino, conta inexistente e
  **idempotência** (chave repetida não reprocessa).
- **NotificationServiceTest** — gera notificação para remetente e destinatário.
- **AccountControllerTest** (`@WebMvcTest`) — camada web: 201 na criação,
  validação (400) e mapeamento de erro 404.
- **AccountRepositoryTest** (`@DataJpaTest`) — consulta com lock pessimista.
- **BankApiApplicationTests** — smoke test de subida do contexto.

Os testes usam **H2 em memória** (schema gerado pelo Hibernate, Flyway desligado
no perfil de teste), portanto **não exigem PostgreSQL** para rodar.

---

## Arquitetura e decisões de design

### Organização por feature (package-by-feature)

O código é agrupado por domínio (`account`, `transfer`, `notification`) em vez de
por camada técnica. Isso mantém coesão alta, facilita navegação e deixa claro o
limite de cada contexto.

Em cada feature há a divisão clássica em camadas: **Controller** (HTTP/REST) →
**Service** (regras de negócio e transações) → **Repository** (persistência) →
**Entity** (domínio). DTOs (`record` do Java 21) isolam o contrato da API das
entidades JPA, evitando vazamento do modelo interno.

### Consistência sob alta concorrência — lock pessimista

O ponto central do teste é transferir dinheiro com segurança quando várias
requisições competem pelas mesmas contas. A estratégia adotada foi
**lock pessimista de escrita** (`@Lock(PESSIMISTIC_WRITE)`, traduzido para
`SELECT ... FOR UPDATE` no PostgreSQL):

- Ao iniciar a transferência, ambas as contas são carregadas com lock. Nenhuma
  outra transação consegue ler/alterar aqueles saldos até o commit, eliminando a
  condição de corrida clássica de *lost update*.
- **Prevenção de deadlock:** quando duas transferências envolvem o mesmo par de
  contas em sentidos opostos (A→B e B→A), travar em ordem arbitrária causaria
  deadlock. Por isso os locks são sempre adquiridos em **ordem determinística**,
  ordenando pelos UUIDs das contas (`min`/`max`). Toda transação trava primeiro a
  mesma conta, evitando o ciclo de espera.
- A transação `@Transactional` garante atomicidade: débito, crédito e registro da
  transferência são confirmados juntos, ou nada é.

Como segunda barreira, a entidade `Account` possui `@Version` (lock otimista),
protegendo contra escritas concorrentes vindas por outros caminhos.

> **Por que pessimista e não otimista?** Em transferências o conflito sobre o
> mesmo saldo é esperado e frequente sob alta concorrência. O lock pessimista
> evita o desperdício de reprocessamento/retries que o otimista exigiria nesse
> cenário. A regra de saldo (`balance >= 0`) ainda é reforçada por um
> `CHECK` constraint no banco.

### Idempotência

Transferências aceitam um header `Idempotency-Key`. A chave é gravada com índice
**único** na tabela `transfers`. Se a mesma chave chegar novamente (ex.: retry de
rede), a API devolve a transferência original **sem reprocessar**, evitando
débito duplicado. Uma corrida real entre duas requisições idênticas é barrada
pelo índice único e tratada como `409 Conflict`.

### Notificações — evento assíncrono e desacoplado

Após o commit da transferência, um `TransferCompletedEvent` é publicado. O
`NotificationService` o consome com:

- `@TransactionalEventListener(AFTER_COMMIT)` — a notificação só dispara **depois**
  que a transferência foi de fato persistida; um rollback não gera notificação
  "fantasma".
- `@Async` (pool dedicado) — o envio roda fora da thread da transferência, sem
  impactar a latência do caminho crítico.

O envio em si é **simulado por log** e a notificação é persistida (trilha de
auditoria). Em produção, bastaria trocar a implementação por um gateway real de
e-mail/SMS/push — o ponto de extensão já está isolado.

### Tratamento de erros padronizado

Um `@RestControllerAdvice` traduz exceções de domínio em respostas HTTP
consistentes (`ApiError` com timestamp, status, mensagem, path e erros de
validação por campo):

| Situação                         | HTTP |
|----------------------------------|------|
| Validação de payload             | 400  |
| Transferência inválida           | 400  |
| Conta não encontrada             | 404  |
| Requisição duplicada (idempotência) | 409  |
| Saldo insuficiente               | 422  |

### Banco e migrations

O schema é versionado com **Flyway** (`db/migration`), garantindo evolução
controlada e reprodutível. O Hibernate roda em modo `validate` em produção (não
altera o schema), e há `CHECK` constraints (`balance >= 0`, `amount > 0`) e
índices nas colunas usadas pelo extrato.

### Valores monetários

Todo dinheiro usa `BigDecimal` com escala 2 (`NUMERIC(19,2)`), evitando os erros
de arredondamento típicos de `double`/`float`.

---

## Estrutura do projeto

```
banco-digital-api
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── src
    ├── main
    │   ├── java/com/compass/bank
    │   │   ├── BankApiApplication.java
    │   │   ├── config/            # OpenAPI (Swagger) e Async
    │   │   ├── account/           # entidade, repo, service, controller, DTOs
    │   │   ├── transfer/          # entidade, repo, service, controller, DTOs
    │   │   ├── notification/      # evento, entidade, listener assíncrono
    │   │   └── common/exception/  # exceções e handler global
    │   └── resources
    │       ├── application.yml
    │       └── db/migration/      # V1 schema, V2 seed
    └── test
        ├── java/com/compass/bank/ # testes unitários e de fatia
        └── resources/application.properties  # perfil de teste (H2)
```

---

## Observações finais

- `open-in-view` está desabilitado (boa prática: evita queries lazy fora da
  camada de serviço).
- Actuator exposto para `health`, `info` e `metrics`, úteis para
  observabilidade/resiliência.
- A paginação dos endpoints de listagem e extrato usa o padrão `Pageable` do
  Spring Data (`?page=0&size=20&sort=...`).
