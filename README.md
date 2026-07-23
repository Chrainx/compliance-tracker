# Compliance Tracker

A compliance deadline tracker for Singapore SMEs. Tracks obligations like ACRA Annual Return,
GST filing, and work pass renewals, computes each business's actual due dates from its own
parameters (e.g. Financial Year End), and (eventually) sends automated reminder notifications
ahead of each deadline.

> **Disclaimer:** This is a reminder/tracking tool, not compliance advice. It is not a
> substitute for consulting a qualified accountant or company secretary. Deadline rules are
> sourced from public ACRA/IRAS/MOM pages but may become outdated — always verify against the
> official source before relying on a date.

This is a portfolio project built to demonstrate backend engineering fundamentals — a rules
engine, reliable job scheduling/dispatch, and cloud deployment — rather than a production
business.

## Tech stack

| Layer      | Choice                          |
|------------|----------------------------------|
| Language   | Java 21 (LTS)                    |
| Framework  | Spring Boot 4.1.0                |
| Build tool | Maven                            |
| Database   | PostgreSQL 16 (Docker locally)   |
| Testing    | JUnit 5 (via `spring-boot-starter-test`) |
| CI         | GitHub Actions                   |
| Planned    | AWS SQS (job queue), AWS ECS/Fargate + RDS (deployment) |

## Architecture (current)

```
Client (curl / browser)
        │
        ▼
BusinessController   (REST layer — @RestController)
        │
        ▼
BusinessRepository   (Spring Data JPA — auto-implemented interface)
        │
        ▼
PostgreSQL            (Business table)
```

- **`Business`** — entity representing an SME and the parameters its compliance deadlines are
  computed from (`name`, `financialYearEnd`, `gstRegistered`).
- **`BusinessRepository`** — Spring Data JPA repository interface. Extending `JpaRepository`
  gives `save`/`findAll`/`findById`/etc. for free, with no method bodies written — Spring
  generates the implementation at runtime.
- **`BusinessController`** — exposes `POST /api/businesses` (create) and
  `GET /api/businesses` (list) over HTTP.
- **`HelloController`** — `GET /hello`, a minimal smoke-test endpoint from initial setup.

### Planned (not built yet — see [open issues](https://github.com/Chrainx/compliance-tracker/issues))

- **Rule engine** — pure Java logic that takes a `Business` and computes its upcoming
  `Deadline`s, based on rules sourced from official ACRA/IRAS/MOM pages.
- **Scheduled dispatch** — a periodic job detects deadlines coming due, enqueues reminder
  jobs on AWS SQS; a worker consumes the queue and sends notifications, with idempotency
  (no duplicate sends on retry) and dead-letter handling (give up gracefully after N failures).
- **Cloud deployment** — AWS ECS/Fargate + RDS, replacing local Docker Postgres.
- **Load testing** — real throughput/latency numbers against the deployed system.

## Running locally

Requires Java 21, Maven, and Docker.

```bash
# 1. Start Postgres (custom port 5434 to avoid clashing with other local projects)
docker run --name compliance-postgres -e POSTGRES_PASSWORD=devpassword \
  -e POSTGRES_DB=compliance_tracker -p 5434:5432 -d postgres:16

# 2. Run the app (custom port 8081)
./mvnw spring-boot:run
```

The app will be available at `http://localhost:8081`.

## API

| Method | Path               | Description               |
|--------|--------------------|----------------------------|
| GET    | `/hello`           | Smoke-test endpoint        |
| POST   | `/api/businesses`  | Create a business          |
| GET    | `/api/businesses`  | List all businesses        |

Example:

```bash
curl -X POST http://localhost:8081/api/businesses \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Cafe Pte Ltd", "financialYearEnd": "2026-12-31", "gstRegistered": true}'
```

## Testing

```bash
./mvnw test
```

## Status

Actively in development. See [open issues](https://github.com/Chrainx/compliance-tracker/issues)
for the current roadmap.
