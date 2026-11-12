# Banco Digital API

REST API for a fictional digital bank. It supports **account CRUD**,
**fund transfers between accounts**, and **transaction history (statements)**,
with a focus on **consistency under high concurrency**, **resilience**, and
**performance**.

Originally built as a technical challenge.

> **Front end:** an Angular web app (in a **separate repository**) consumes this
> API — login, balance, transfers, statements, and account management.
> Repository: **<https://github.com/EwerthonSantana/banco-digital-frontend>**.

---

## Table of contents

- [Tech stack and versions](#tech-stack-and-versions)
- [How to run](#how-to-run)
  - [Option 1 — Docker Compose (recommended)](#option-1--docker-compose-recommended)
  - [Option 2 — Local with Maven](#option-2--local-with-maven)
- [Swagger documentation](#swagger-documentation)
- [Endpoints](#endpoints)
- [Seed data](#seed-data)
- [Usage examples (cURL)](#usage-examples-curl)
- [Tests](#tests)
- [Architecture and design decisions](#architecture-and-design-decisions)
- [Front end (Angular)](#front-end-angular)
- [Project structure](#project-structure)

---

## Tech stack and versions

| Layer                | Technology                         | Version                     |
| -------------------- | ---------------------------------- | --------------------------- |
| Language             | Java                               | 21 (LTS)                    |
| Framework            | Spring Boot                        | 3.5.13                      |
| Build / dependencies | Maven                              | 3.9+                        |
| Database             | PostgreSQL                         | 16                          |
| Migrations           | Flyway                             | (managed by Spring Boot)    |
| Documentation        | springdoc-openapi (Swagger UI)     | 2.8.17                      |
| Test database        | H2 (in-memory)                     | (test)                      |
| Testing              | JUnit 5, Mockito, AssertJ, MockMvc | (test)                      |

**Why these versions?** The requirement called for "the latest stable, bug-free
versions". I chose **Java 21 (LTS)** + **Spring Boot 3.5.x** over the newly
released Spring Boot 4.x: the 3.5 line is mature, widely adopted, and fully
compatible with the ecosystem (springdoc, Flyway, drivers), which reduces the
risk of incompatibilities. Java 21 is the latest LTS release, ensuring long-term
support and broad portability across machines.

---

## How to run

### Option 1 — Docker Compose (recommended)

Starts **PostgreSQL + the API**, fully configured, with no need to install
Java/Maven locally. (The front end is a separate project/repository — see the
[Front end](#front-end-angular) section.)

Prerequisites: Docker and Docker Compose.

```bash
docker compose up --build
```

Available services:

- **API:** http://localhost:8080 (Swagger at `/swagger-ui.html`)
- **PostgreSQL:** localhost:5432

To start only the database (running the API outside Docker):

```bash
docker compose up db            # PostgreSQL only
```

To stop:

```bash
docker compose down          # keeps the data
docker compose down -v       # also removes the database volume
```

### Option 2 — Local with Maven

Prerequisites: JDK 21 and Maven 3.9+. You need a reachable PostgreSQL instance.

1. Start just the database (via Docker) or use your own PostgreSQL:

   ```bash
   docker run --name bank-db -e POSTGRES_DB=bankdb -e POSTGRES_USER=bank \
     -e POSTGRES_PASSWORD=bank -p 5432:5432 -d postgres:16-alpine
   ```

2. (Optional) adjust the environment variables — see `.env.example`. The default
   values already point to the database above.

3. Run the application:

   ```bash
   mvn spring-boot:run
   ```

Flyway creates the schema and loads the sample accounts (10 customers + 1 admin)
automatically on first startup.

> ⚠️ **Reloading the seed:** the `V2__seed_accounts.sql` migration was expanded.
> If your database was created with an earlier version, Flyway will report a
> _checksum mismatch_ on startup. To reload from scratch, run
> `docker compose down -v` and start again (or recreate the database/schema).

---

## Swagger documentation

With the application running:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Health check:** http://localhost:8080/actuator/health

---

## Endpoints

Base path: `/api/v1`

### Accounts

| Method | Path             | Description               |
| ------ | ---------------- | ------------------------- |
| POST   | `/accounts`      | Create an account         |
| GET    | `/accounts`      | List accounts (paginated) |
| GET    | `/accounts/{id}` | Get an account by ID      |
| PUT    | `/accounts/{id}` | Update account details    |
| DELETE | `/accounts/{id}` | Delete an account         |

### Transfers and transactions

| Method | Path                       | Description                          |
| ------ | -------------------------- | ------------------------------------ |
| POST   | `/transfers`               | Transfer funds between two accounts  |
| GET    | `/accounts/{id}/movements` | Account statement (transactions)     |

`POST /transfers` accepts the optional **`Idempotency-Key`** header.

---

## Seed data

The `V2__seed_accounts.sql` migration creates **10 customer accounts + 1
administrator (ADM) account** with a high balance for testing:

| ID               | Name            | Initial balance  |
| ---------------- | --------------- | ---------------- |
| `11111111-…1111` | Maria Silva     | R$ 1,000.00      |
| `22222222-…2222` | Joao Souza      | R$ 500.00        |
| `33333333-…3333` | Ana Pereira     | R$ 2,500.00      |
| `44444444-…4444` | Carlos Lima     | R$ 750.00        |
| `55555555-…5555` | Beatriz Costa   | R$ 3,200.00      |
| `66666666-…6666` | Pedro Santos    | R$ 1,500.00      |
| `77777777-…7777` | Juliana Almeida | R$ 4,800.00      |
| `88888888-…8888` | Rafael Oliveira | R$ 980.00        |
| `99999999-…9999` | Fernanda Rocha  | R$ 6,100.00      |
| `aaaaaaaa-…aaaa` | Lucas Martins   | R$ 250.00        |
| `dddddddd-…dddd` | **ADM**         | R$ 1,000,000.00  |

> In the front end, login uses the account **name** as the username and the fixed
> password `123` for everyone. `ADM` has the administrator role.

---

## Usage examples (cURL)

Create an account:

```bash
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"name": "Carlos Lima", "initialBalance": 300.00}'
```

Transfer (with idempotency):

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

Get a statement:

```bash
curl http://localhost:8080/api/v1/accounts/11111111-1111-1111-1111-111111111111/movements
```

---

## Tests

```bash
mvn test
```

Test coverage (focused on business rules):

- **AccountServiceTest** — CRUD and the non-existent account scenario.
- **TransferServiceTest** — successful transfer (debit/credit + event),
  insufficient balance, same source and destination, non-existent account, and
  **idempotency** (a repeated key is not reprocessed).
- **NotificationServiceTest** — generates notifications for sender and recipient.
- **AccountControllerTest** (`@WebMvcTest`) — web layer: 201 on creation,
  validation (400), and 404 error mapping.
- **AccountRepositoryTest** (`@DataJpaTest`) — query with a pessimistic lock.
- **BankApiApplicationTests** — context startup smoke test.

Tests use **in-memory H2** (schema generated by Hibernate, Flyway disabled in the
test profile), so they **don't require PostgreSQL** to run.

---

## Architecture and design decisions

### Package-by-feature

Code is grouped by domain (`account`, `transfer`, `notification`) rather than
by technical layer. This keeps cohesion high, makes navigation easier, and makes
each context's boundary clear.

Each feature follows the classic layering: **Controller** (HTTP/REST) →
**Service** (business rules and transactions) → **Repository** (persistence) →
**Entity** (domain). DTOs (Java 21 `record`s) isolate the API contract from the
JPA entities, preventing the internal model from leaking.

### Consistency under high concurrency — pessimistic locking

The core challenge is moving money safely when multiple requests compete for the
same accounts. The chosen strategy is a **pessimistic write lock**
(`@Lock(PESSIMISTIC_WRITE)`, translated to `SELECT ... FOR UPDATE` in
PostgreSQL):

- When a transfer starts, both accounts are loaded with a lock. No other
  transaction can read/modify those balances until commit, eliminating the
  classic _lost update_ race condition.
- **Deadlock prevention:** when two transfers involve the same pair of accounts in
  opposite directions (A→B and B→A), locking in arbitrary order would cause a
  deadlock. That's why locks are always acquired in a **deterministic order**,
  sorted by account UUID (`min`/`max`). Every transaction locks the same account
  first, avoiding the wait cycle.
- The `@Transactional` boundary guarantees atomicity: debit, credit, and the
  transfer record are committed together, or not at all.

As a second safeguard, the `Account` entity has `@Version` (optimistic locking),
protecting against concurrent writes coming through other paths.

> **Why pessimistic rather than optimistic?** In transfers, conflicts on the same
> balance are expected and frequent under high concurrency. Pessimistic locking
> avoids the wasted reprocessing/retries that optimistic locking would require in
> this scenario. The balance rule (`balance >= 0`) is also enforced by a
> `CHECK` constraint in the database.

### Idempotency

Transfers accept an `Idempotency-Key` header. The key is stored with a **unique**
index in the `transfers` table. If the same key arrives again (e.g., a network
retry), the API returns the original transfer **without reprocessing it**,
preventing a duplicate debit. A real race between two identical requests is
blocked by the unique index and handled as `409 Conflict`.

### Notifications — asynchronous and decoupled events

After the transfer commits, a `TransferCompletedEvent` is published. The
`NotificationService` consumes it with:

- `@TransactionalEventListener(AFTER_COMMIT)` — the notification fires only
  **after** the transfer has actually been persisted; a rollback never produces a
  "ghost" notification.
- `@Async` (dedicated pool) — sending runs outside the transfer thread, without
  impacting critical-path latency.

Sending itself is **simulated via logging**, and the notification is persisted
(audit trail). In production, you would only need to swap the implementation for
a real email/SMS/push gateway — the extension point is already isolated.

### Standardized error handling

A `@RestControllerAdvice` translates domain exceptions into consistent HTTP
responses (`ApiError` with timestamp, status, message, path, and per-field
validation errors):

| Situation                           | HTTP |
| ----------------------------------- | ---- |
| Payload validation                  | 400  |
| Invalid transfer                    | 400  |
| Account not found                   | 404  |
| Duplicate request (idempotency)     | 409  |
| Insufficient balance                | 422  |

### Database and migrations

The schema is versioned with **Flyway** (`db/migration`), ensuring controlled and
reproducible evolution. Hibernate runs in `validate` mode in production (it never
alters the schema), and there are `CHECK` constraints (`balance >= 0`,
`amount > 0`) and indexes on the columns used by the statement queries.

### Monetary values

All money uses `BigDecimal` with scale 2 (`NUMERIC(19,2)`), avoiding the rounding
errors typical of `double`/`float`.

### CORS (front-end integration)

The `CorsConfig` class (`config/CorsConfig.java`) allows the Angular front end to
call the API from the browser. By default, the allowed origins are
`http://localhost:4200` **and** `http://127.0.0.1:4200` (on Windows the front end
is accessed via `127.0.0.1` to avoid `localhost`/IPv6 hangs), configurable via the
`app.cors.allowed-origins` property. Without this configuration, the browser would
block the front end's calls under the same-origin policy (CORS).

---

## Front end (Angular)

A front-end project consumes this API and lives in a **separate repository**:
**<https://github.com/EwerthonSantana/banco-digital-frontend>**.

- **Stack:** Angular 21 + Angular Material, with login (mocked, no JWT), balance
  view, transfers (with confirmation and idempotency), statements, and account
  management (CRUD).
- **How to run:** with this API running on `localhost:8080`, clone the front-end
  repository and run `npm install` + `npm start` (or `docker compose up --build`),
  then open `http://localhost:4200`.
- This API's CORS configuration already allows `http://localhost:4200` and
  `http://127.0.0.1:4200` (adjustable via `app.cors.allowed-origins`).
- Run instructions, screens, roles (ADMIN/USER), and architecture details are in
  the front end's README.

---

## Project structure

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
    │   │   ├── config/            # OpenAPI (Swagger), Async, and CORS
    │   │   ├── account/           # entity, repo, service, controller, DTOs
    │   │   ├── transfer/          # entity, repo, service, controller, DTOs
    │   │   ├── notification/      # event, entity, async listener
    │   │   └── common/exception/  # exceptions and global handler
    │   └── resources
    │       ├── application.yml
    │       └── db/migration/      # V1 schema, V2 seed
    └── test
        ├── java/com/compass/bank/ # unit and slice tests
        └── resources/application.properties  # test profile (H2)
```

---

## Final notes

- `open-in-view` is disabled (best practice: avoids lazy queries outside the
  service layer).
- Actuator is exposed for `health`, `info`, and `metrics`, useful for
  observability/resilience.
- Pagination on the list and statement endpoints uses Spring Data's `Pageable`
  convention (`?page=0&size=20&sort=...`).
