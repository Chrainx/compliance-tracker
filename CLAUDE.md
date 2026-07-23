# Compliance Tracker — Project Context

## How I want you to work with me (READ THIS FIRST)

I am learning Java/Spring Boot as I build this. **Do not just write full solutions and hand them to me.** Instead:

- Explain the concept or the next small step first, in plain language.
- Ask me clarifying questions before jumping to code if something is ambiguous.
- When code is needed, prefer showing me a small snippet and explaining *why*, over generating whole files silently.
- After any change, tell me exactly what to run and what output to expect, so I can verify it worked myself — don't just say "done."
- If I ask you to "just do it," still briefly explain what you did afterward — don't skip the explanation.
- Push back on my ideas if they're technically weak, redundant with something I already have, or contradict something I said earlier in this file or in our conversation. Don't just validate whatever I ask for.
- Assume I know basic programming concepts (I know Go, Python, TypeScript, C++) but am new to Java specifically and to Spring Boot's conventions/annotations — explain Java/Spring-specific "magic" (annotations, dependency injection, Hibernate auto-config) rather than assuming I already get it.

## What this project is

A **compliance deadline tracker** for Singapore SMEs — tracks obligations like ACRA Annual Return, GST filing, and work pass renewals, computes each business's actual due dates from their own parameters (e.g. Financial Year End), and sends automated reminder notifications ahead of each deadline.

This is a **portfolio/interview project**, not a live business — goal is to demonstrate real backend engineering (a rules engine, job scheduling/dispatch with retries and idempotency, cloud deployment), framed around a genuinely defensible business idea, not a generic CRUD app or tutorial clone.

Important: I am not a lawyer/accountant. The app must frame itself as a reminder/tracking tool, not compliance advice, with a visible disclaimer. Any deadline rule must be sourced from an actual public gov.sg/ACRA/IRAS page — never invent a rule.

## Tech stack (decided, don't change without discussing)

- **Language:** Java 21 (LTS — deliberately not 25/26, which are newer but less battle-tested with Spring Boot right now)
- **Framework:** Spring Boot 4.1.0
- **Build tool:** Maven
- **Database:** PostgreSQL 16, running in Docker locally
- **Planned later:** AWS SQS (job queue for reminder dispatch), AWS ECS/Fargate + RDS (deployment), basic load testing

## Current environment (macOS)

- Java 21 installed via Homebrew (Temurin), `JAVA_HOME` explicitly pinned in `~/.zshrc` to Java 21 (a newer Java 26 is also installed as a Homebrew dependency of something else — ignore it, don't let tools pick it up)
- Maven and AWS CLI installed via Homebrew
- Docker Desktop installed, must be manually opened (`open -a Docker`) before `docker` commands work
- Project lives at `~/Documents/Projects/compliance-tracker` (was previously double-nested due to an unzip mistake — already fixed/flattened)
- VS Code is the editor (not IntelliJ)

## Postgres container

Running locally in Docker, **not on the default port** — port 5432 was already taken by an old project's container (`artium-postgres`), so this project uses **5434** instead:

```bash
docker run --name compliance-postgres -e POSTGRES_PASSWORD=devpassword -e POSTGRES_DB=compliance_tracker -p 5434:5432 -d postgres:16
```

Other old containers (from unrelated past projects — Artium, DocuQuery) were stopped to free up ports/resources; they're not part of this project and can be ignored.

## Spring Boot app config

The app runs on **port 8081** (not the default 8080 — that was taken by an old project's container). Current `application.properties`:

```properties
spring.application.name=compliance-tracker

server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5434/compliance_tracker
spring.datasource.username=postgres
spring.datasource.password=devpassword

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

`ddl-auto=update` is fine for local dev (lets Hibernate auto-create/update tables from entity classes) — **must not be used in a real production deployment later**, flag this if it comes up during AWS deployment planning.

## Known gotchas already hit once — watch for these recurring

- **`pom.xml` dependencies have gone missing/gotten reset more than once** during editing — always double check the full `<dependencies>` block actually contains what's expected (spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, postgresql driver, spring-boot-starter-test, spring-boot-devtools) after any edit, rather than assuming a prior edit persisted.
- **`application.properties` similarly got wiped back to near-empty once** — same caution applies, verify contents after edits rather than assuming.
- **New Java files must be placed inside the correct package folder** (`src/main/java/com/chrainx/compliance_tracker/`), matching the `package` declaration at the top of the file — a file placed one level too high will fail to compile with confusing "cannot find symbol" errors.
- **DevTools auto-restart doesn't always reliably pick up changes to `application.properties`** — if a config change doesn't seem to take effect, do a full manual stop (Ctrl+C) and restart (`./mvnw spring-boot:run`) rather than assuming DevTools caught it.
- When verifying anything, prefer checking the actual state directly (e.g. `docker exec ... psql ... "\dt"` to check real tables) over trusting terminal output alone — logs can look clean while the real behavior is still broken.

## Project status so far (what's already working, verified)

- Full toolchain confirmed working: Java 21 → Maven → Spring Boot → Tomcat → Postgres, end to end
- `HelloController` — a working `GET /hello` endpoint, returns a plain string
- `Business` entity (id, name, financialYearEnd, gstRegistered) — table confirmed created in Postgres via `\dt`
- `BusinessRepository` — Spring Data JPA repository interface (save/findAll/findById for free)
- `BusinessController` — `POST /api/businesses` and `GET /api/businesses` endpoints, just written, not yet verified with curl

## Immediate next step (in progress)

Verify the `POST`/`GET /api/businesses` endpoints work via curl:

```bash
curl -X POST http://localhost:8081/api/businesses -H "Content-Type: application/json" -d '{"name": "Test Cafe Pte Ltd", "financialYearEnd": "2026-12-31", "gstRegistered": true}'
curl http://localhost:8081/api/businesses
```

## Roadmap after that (not started yet, don't jump ahead without discussing)

1. Design the **compliance rules** on paper first (3 concrete obligations: ACRA Annual Return, GST F5 filing, one work-pass renewal type) — plain-English rule + source link for each, before writing any rule-engine code
2. Build the rule engine as pure, unit-tested Java logic (given a `Business`, compute upcoming `Deadline`s) — no API/queue yet
3. Wrap it in a REST API
4. Add scheduled reminder dispatch (AWS SQS + worker pattern), with real idempotency handling (don't double-send if a worker crashes and retries) and dead-letter handling (give up gracefully after N failures, don't retry forever)
5. Deploy to real AWS (ECS/Fargate + RDS), replacing local Docker Postgres
6. Add basic load testing and get real throughput/latency numbers — this becomes the actual resume-worthy evidence, not just "it works on my laptop"

## My background (for context on how to explain things)

NUS CS + Math graduate, ASEAN Scholar, Codeforces Expert (competitive programming in C++), comfortable in Go/Python/TypeScript/PHP, learning Java/Spring specifically for this project. Applying for SWE roles in Singapore — this project's purpose is to be a genuine, defensible, non-generic portfolio piece for that job search, so prioritize correctness and my actual understanding over speed.
