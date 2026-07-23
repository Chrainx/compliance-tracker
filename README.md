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
        │              │
        ▼              ▼
BusinessRepository   RuleEngine   (pure logic — @Component, no DB/HTTP dependency)
        │
        ▼
PostgreSQL            (Business table)
```

- **`Business`** — entity representing an SME and the parameters its compliance deadlines are
  computed from (`name`, `financialYearEnd`, `gstRegistered`).
- **`BusinessRepository`** — Spring Data JPA repository interface. Extending `JpaRepository`
  gives `save`/`findAll`/`findById`/etc. for free, with no method bodies written — Spring
  generates the implementation at runtime.
- **`RuleEngine`** — pure, unit-tested Java logic (`rules` package). Given a `Business` and a
  reference date, computes the list of currently-applicable `Deadline`s (each an
  `ObligationType` + due `LocalDate`). Has no dependency on the database or HTTP layer, and
  takes the reference date as a parameter rather than calling `LocalDate.now()` internally, so
  tests are fully deterministic. Currently implements ACRA Annual Return and GST F5 — Employment
  Pass renewal is blocked on a `WorkPass` entity not yet built (see [issue #2](https://github.com/Chrainx/compliance-tracker/issues/2)).
- **`BusinessController`** — exposes `POST /api/businesses` (create), `GET /api/businesses`
  (list), and `GET /api/businesses/{id}/deadlines` (compute and return that business's current
  deadlines via `RuleEngine`) over HTTP.
- **`HelloController`** — `GET /hello`, a minimal smoke-test endpoint from initial setup.

### Planned (not built yet — see [open issues](https://github.com/Chrainx/compliance-tracker/issues))

- **`WorkPass` entity** — to model employee work-pass expiry dates, needed to complete the
  Employment Pass rule.
- **Scheduled dispatch** — a periodic job detects deadlines coming due, enqueues reminder
  jobs on AWS SQS; a worker consumes the queue and sends notifications, with idempotency
  (no duplicate sends on retry) and dead-letter handling (give up gracefully after N failures).
- **Cloud deployment** — AWS ECS/Fargate + RDS, replacing local Docker Postgres.
- **Load testing** — real throughput/latency numbers against the deployed system.

### Compliance rules

| Obligation | Rule | Source | Status |
|---|---|---|---|
| ACRA Annual Return | `financialYearEnd + 7 months` | [ACRA — Deadline & requirements](https://www.acra.gov.sg/manage/companies/legal-requirements-common-offences/filing-annual-returns-companies/deadline-requirements/) | Implemented. Listed-company variant (5/6 months) not modeled — SME target audience is virtually always private/non-listed |
| GST F5 filing | `calendarQuarterEnd + 1 month` | [IRAS — Due dates and extensions](https://www.iras.gov.sg/taxes/goods-services-tax-(gst)/filing-gst/due-dates-and-requests-for-extension) | Implemented. Assumes standard calendar quarters; IRAS actually assigns a per-business cycle at GST registration which may not align to calendar quarters |
| Employment Pass renewal | `= passExpiryDate` (renewal window opens 6 months prior, no grace period after expiry) | [MOM — Renew a Pass (Employment Pass)](https://www.mom.gov.sg/passes-and-permits/employment-pass/renew-a-pass) | Not implemented — needs a new `WorkPass` entity; `Business` has no concept of employees/work passes yet |

This is a reminder/tracking tool, not compliance advice — always verify against the official
source before relying on a date (see disclaimer above).

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

| Method | Path                          | Description                                |
|--------|-------------------------------|---------------------------------------------|
| GET    | `/hello`                      | Smoke-test endpoint                         |
| POST   | `/api/businesses`             | Create a business                           |
| GET    | `/api/businesses`             | List all businesses                         |
| GET    | `/api/businesses/{id}/deadlines` | Compute and return that business's current compliance deadlines |

Example:

```bash
curl -X POST http://localhost:8081/api/businesses \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Cafe Pte Ltd", "financialYearEnd": "2026-12-31", "gstRegistered": true}'

curl http://localhost:8081/api/businesses/1/deadlines
# [{"obligationType":"ACRA_ANNUAL_RETURN","dueDate":"2027-07-31"},{"obligationType":"GST_F5","dueDate":"2026-10-30"}]
```

## Testing

```bash
./mvnw test
```

## Status

Actively in development. See [open issues](https://github.com/Chrainx/compliance-tracker/issues)
for the current roadmap.
