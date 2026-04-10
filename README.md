# 💰 FinTrack — Personal Finance Tracker

> A production-grade REST API built with Spring Boot 3, featuring JWT authentication, OTP-based login, real-time fund transfers, and financial analytics — backed by MySQL with Flyway migrations and containerised with Docker.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Domain Model](#domain-model)
- [API Reference](#api-reference)
- [Security Design](#security-design)
- [Database Migrations](#database-migrations)
- [Key Engineering Decisions](#key-engineering-decisions)
- [Frontend](#frontend)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Docker Deployment](#docker-deployment)
- [What's Next](#whats-next)

---

## Overview

FinTrack is a mini-banking proof-of-concept that lets users manage bank accounts, record income and expenses, transfer funds across accounts or to UPI/mobile numbers, and view financial reports. It is designed with a clean layered architecture — Controller → Service Interface → Service Implementation → Repository — and demonstrates real-world patterns such as optimistic locking, role-based access control, OTP verification, and centralised exception handling.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.3 |
| Security | Spring Security 6, JWT (jjwt 0.12.6), BCrypt |
| Persistence | Spring Data JPA (Hibernate), MySQL 8 |
| Migrations | Flyway |
| API Docs | SpringDoc OpenAPI / Swagger UI 2.8.3 |
| Build | Maven (Maven Wrapper included) |
| Containerisation | Docker, Docker Compose |
| Utilities | Lombok 1.18.36 |
| Mail | Mailgun HTTP API (pluggable, fail-open) |
| Test DB | H2 (in-memory) |

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   HTTP Clients                      │
│          (Browser Frontend / Swagger UI)            │
└────────────────────┬────────────────────────────────┘
                     │
         ┌───────────▼────────────┐
         │  JWT Auth Filter       │  (validates Bearer token on every request)
         └───────────┬────────────┘
                     │
         ┌───────────▼────────────┐
         │      Controllers       │  REST endpoints, input validation (@Valid)
         └───────────┬────────────┘
                     │
         ┌───────────▼────────────┐
         │   Service Interfaces   │  Clean contracts (Interface + Impl pattern)
         └───────────┬────────────┘
                     │
         ┌───────────▼────────────┐
         │      Repositories      │  Spring Data JPA
         └───────────┬────────────┘
                     │
         ┌───────────▼────────────┐
         │    MySQL 8 Database    │  Schema managed by Flyway
         └────────────────────────┘
```

Every service is split into an interface and an implementation class (e.g. `AuthService` / `AuthServiceImpl`), making components independently testable and swappable.

---

## Project Structure

```
personal-finance-tracker/
├── src/main/java/com/qburst/training/personalfinancetracker/
│   ├── PersonalFinanceTrackerApplication.java
│   ├── config/
│   │   ├── CorsConfig.java          # CORS rules
│   │   ├── OpenApiConfig.java       # Swagger / OpenAPI setup
│   │   └── SecurityConfig.java      # JWT filter chain, role guards
│   ├── controller/
│   │   ├── AuthController.java      # Register, login, OTP flows
│   │   ├── UserController.java      # User CRUD
│   │   ├── BankAccountController.java
│   │   ├── TransactionController.java
│   │   ├── TransferController.java
│   │   ├── ReportController.java
│   │   └── AdminController.java     # Admin-only endpoints
│   ├── service/
│   │   ├── auth/                    # AuthService + AuthServiceImpl
│   │   ├── user/                    # UserService + UserServiceImpl
│   │   ├── account/                 # BankAccountService + Impl
│   │   ├── transaction/             # TransactionService + Impl
│   │   ├── transfer/                # TransferService + Impl
│   │   ├── report/                  # ReportService + Impl
│   │   └── mail/                    # MailService + MailgunMailService
│   ├── repository/                  # Spring Data JPA repositories
│   ├── entity/                      # JPA entities
│   ├── dto/                         # Inner record DTOs (Request / Response)
│   ├── security/
│   │   ├── JwtService.java          # Token generation and validation
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── AuthContextService.java  # Current user resolver
│   │   └── AuthPrincipal.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── ResourceNotFoundException.java
│       ├── InsufficientBalanceException.java
│       └── DuplicateResourceException.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/                # Flyway SQL migrations V1–V16
├── frontend/                        # Static HTML/CSS/JS frontend
│   ├── index.html                   # Landing page
│   ├── dashboard.js                 # Main dashboard logic
│   ├── auth.js                      # Auth helper
│   ├── styles.css
│   └── *.html                       # Accounts, Transactions, Transfers, Reports, Users pages
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

## Domain Model

```
users
 ├── id, username, email, password_hash, full_name
 ├── role (USER | ADMIN)
 └── created_at, updated_at

banks                                    ← pre-seeded (HDFC, SBI, ICICI, Axis, Kotak)
 └── id, bank_name, bank_code, created_at

bank_accounts
 ├── id, user_id → users, bank_id → banks
 ├── account_number (unique), balance (DECIMAL 15,2)
 └── version                            ← optimistic locking

categories
 └── id, name, description

transactions                             ← unified ledger for all money movements
 ├── id, user_id, category_id
 ├── source_bank_id, dest_bank_id        ← nullable; populated depending on type
 ├── transaction_type  (INCOME | EXPENSE | ATM_WITHDRAWAL | TRANSFER)
 ├── transfer_type     (ACCOUNT | MOBILE | UPI)   ← only for TRANSFER rows
 ├── self_transfer (boolean)
 ├── destination_value (account no / mobile / UPI string)
 └── amount, description, created_at

login_otps
 └── id, user_id, otp_hash (BCrypt), purpose, expires_at, used_at, created_at
```

The `transactions` table acts as a **polymorphic ledger**: nullable foreign keys and the `transaction_type` / `transfer_type` discriminator columns keep all financial events in one place, simplifying reporting queries considerably.

---

## API Reference

### Authentication — `/api/auth`

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/api/auth/register` | Create a new user account | No |
| POST | `/api/auth/user/login` | Email + password login (USER) | No |
| POST | `/api/auth/admin/login` | Email + password login (ADMIN) | No |
| POST | `/api/auth/user/request-otp` | Send OTP to user's email | No |
| POST | `/api/auth/user/verify-otp` | Verify OTP and receive JWT | No |
| POST | `/api/auth/admin/request-otp` | Send OTP to admin's email | No |
| POST | `/api/auth/admin/verify-otp` | Verify admin OTP and receive JWT | No |
| GET | `/api/auth/me` | Return current session details | Yes |

### Users — `/api/users`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | Create a user |
| GET | `/api/users/{id}` | Get user profile |
| PUT | `/api/users/{id}` | Update user profile |

### Bank Accounts — `/api/bank-accounts`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/bank-accounts` | Open a new bank account |
| GET | `/api/bank-accounts/{id}` | Get account by ID |
| GET | `/api/bank-accounts/user/{userId}` | Get all accounts for a user |
| DELETE | `/api/bank-accounts/{id}` | Close a bank account |

### Transactions — `/api/transactions`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/transactions/income` | Record income into an account |
| POST | `/api/transactions/expense` | Record an account expense |
| POST | `/api/transactions/atm-withdrawal` | Record ATM cash withdrawal |
| POST | `/api/transactions/bank-expense` | Record a bank-initiated expense |
| GET | `/api/transactions/user/{userId}` | Full transaction history for a user |
| GET | `/api/transactions/{id}` | Get a single transaction |

### Transfers — `/api/transfers`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/transfers` | Transfer funds (ACCOUNT / MOBILE / UPI) |

### Reports — `/api/reports`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/reports/bank-balances` | Bank balance summary (filter by userId) |
| GET | `/api/reports/monthly-expenses` | Month-wise expense totals |
| GET | `/api/reports/expense-by-category` | Expense breakdown per category |
| GET | `/api/reports/income-expense-summary` | Total income vs total expense |

### Admin — `/api/admin` *(ADMIN role required)*

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/users` | List all registered users |
| GET | `/api/admin/activities` | Recent transactions across all users |

Interactive documentation is available at **`/swagger-ui.html`** when the server is running.

---

## Security Design

```
POST /api/auth/user/login
  → validate email + password
  → BCrypt.matches()
  → issue JWT (HS256, configurable expiry)

Every subsequent request:
  Authorization: Bearer <token>
  → JwtAuthenticationFilter extracts userId + role from claims
  → SecurityContext populated → @PreAuthorize evaluated

OTP flow (alternative to password):
  POST /api/auth/user/request-otp
    → generate 6-digit OTP
    → BCrypt hash stored in login_otps
    → plaintext sent via Mailgun

  POST /api/auth/user/verify-otp
    → look up latest unused OTP for user
    → BCrypt.matches() against stored hash
    → mark OTP as used (used_at = now)
    → issue JWT
```

Key points:
- Sessions are **stateless** — no HTTP session, no server-side state. Every request is authenticated via JWT.
- OTPs are stored as **BCrypt hashes**, never in plaintext.
- `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` guards admin routes at the method level.
- JWT secret must be at minimum 32 bytes; the application validates this at startup and throws `IllegalStateException` if the secret is too short.
- CORS is centrally configured via `CorsConfig`.

---

## Database Migrations

Schema evolution is managed by **Flyway** with versioned SQL scripts:

| Version | Script | Change |
|---|---|---|
| V1 | `create_users_table` | Initial users table |
| V2 | `create_banks_table` | Banks table + seed data (5 banks) |
| V3 | `create_bank_accounts_table` | Bank accounts with FK constraints |
| V4 | `create_wallets_table` | Wallet support (later deprecated) |
| V5 | `create_categories_table` | Expense categories |
| V6 | `create_transactions_table` | Unified transaction ledger |
| V7 | `add_version_to_bank_accounts` | Optimistic locking column |
| V8 | `add_version_to_wallets` | Optimistic locking for wallets |
| V9–V13 | Legacy placeholders | Preserve Flyway checksum history |
| V14 | `add_user_role_column` | Role-based access (USER / ADMIN) |
| V15 | `create_login_otps_table` | OTP table with indexed lookups |
| V16 | `remove_wallets_and_update_transfers` | Drop wallet columns; add transfer metadata (transfer_type, self_transfer, destination_value) |

---

## Key Engineering Decisions

**1. Unified Transaction Ledger**
All financial events — income, expense, ATM withdrawal, and transfers — are stored in a single `transactions` table. Nullable FK columns (`source_bank_id`, `dest_bank_id`) and a `transaction_type` enum discriminate between them. This makes reporting queries straightforward without complex multi-table unions.

**2. Optimistic Locking on Bank Accounts**
The `bank_accounts` table carries a `version` column, mapped via JPA's `@Version`. This prevents lost-update race conditions when concurrent requests modify the same account balance — instead of taking a database lock, a stale write results in an `OptimisticLockException`, protecting data integrity without sacrificing throughput.

**3. Interface + Impl Service Pattern**
Every service is declared as an interface (e.g. `AuthService`) with a concrete implementation (e.g. `AuthServiceImpl`). This enforces a clean API contract between layers, makes mocking in unit tests trivial, and allows alternative implementations (e.g. a no-op mail service for local dev) to be swapped via Spring's dependency injection without touching call sites.

**4. DTO Inner Records**
Request and Response types are defined as Java `record` classes nested inside a parent DTO class (e.g. `UserDto.Request`, `UserDto.Response`). Records are immutable, compact, and automatically generate constructors, `equals`, `hashCode`, and `toString`. Nesting them avoids class-file proliferation while keeping types strongly grouped by domain concept.

**5. OTP Hashed with BCrypt**
One-time passwords are never stored in plaintext. A BCrypt hash is persisted at issuance time and verified at login — the same approach used for passwords. This means the `login_otps` table is safe even in the event of a database leak.

**6. Fail-Open Mail Integration**
The Mailgun integration is fully optional. `MAILGUN_ENABLED=false` disables it entirely; `MAILGUN_FAIL_OPEN=true` ensures a mail delivery failure never crashes a login or registration flow. This design keeps the application functional in development environments with no mail credentials.

**7. Flyway Placeholder Migrations**
Versions V9–V13 are `SELECT 1` no-op placeholders. These exist to preserve Flyway's checksum history after a schema refactor, allowing the project to be deployed on top of databases created during earlier development iterations without requiring `baseline-on-migrate` workarounds.

**8. Global Exception Handling**
`@RestControllerAdvice` on `GlobalExceptionHandler` maps every exception type to a consistent JSON error envelope with `error`, `status`, and `timestamp` fields — covering validation errors (400), not found (404), conflicts (409), insufficient balance (422), and generic server errors (500).

---

## Frontend

A static single-page frontend is served directly from the Spring Boot application. It communicates exclusively via the REST API using `fetch` and stores the JWT in `sessionStorage`.

| Page | File | Purpose |
|---|---|---|
| Landing | `index.html` | App entry point and navigation |
| User Login | `user-login.html` | Email/password + OTP login for users |
| Admin Login | `admin-login.html` | Admin login flow |
| Register | `register.html` | New user registration |
| Dashboard | `dashboard.js` | Account overview, recent transactions |
| Accounts | `accounts.html` | View and manage bank accounts |
| Transactions | `transactions.html` | Record and browse transactions |
| Transfers | `transfers.html` | Initiate fund transfers |
| Reports | `reports.html` | Financial analytics and charts |
| Users | `users.html` | Admin user management |

All pages share a common `styles.css` and `auth.js` for token management and redirect guards.

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+ (or use the included `./mvnw`)
- MySQL 8.0+ (or skip to the Docker Compose option below)

### 1. Clone and configure

```bash
git clone <repo-url>
cd personal-finance-tracker
cp .env.example .env
# Edit .env with your database credentials and JWT secret
```

### 2. Run with Maven (local MySQL)

```bash
./mvnw spring-boot:run
```

### 3. Run with Docker Compose (recommended)

```bash
docker compose up --build
```

This starts both the MySQL container and the application, with a health-check dependency ensuring the app waits for the database to be ready before starting.

The API is available at **`http://localhost:8080`**

Swagger UI at **`http://localhost:8080/swagger-ui.html`**

---

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `DB_HOST` | MySQL host | `localhost` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Database name | — |
| `DB_USERNAME` | MySQL user | — |
| `DB_PASSWORD` | MySQL password | — |
| `SERVER_PORT` | HTTP port | `8080` |
| `APP_JWT_SECRET` | JWT signing secret (≥ 32 chars) | *(required)* |
| `APP_JWT_EXPIRATION_MINUTES` | Token TTL in minutes | `120` |
| `APP_OTP_EXPIRATION_MINUTES` | OTP TTL in minutes | `5` |
| `MAILGUN_ENABLED` | Enable Mailgun email delivery | `false` |
| `MAILGUN_FAIL_OPEN` | Continue on mail failure | `true` |
| `MAILGUN_BASE_URL` | Mailgun API base URL | `https://api.mailgun.net` |
| `MAILGUN_DOMAIN` | Mailgun sending domain | — |
| `MAILGUN_API_KEY` | Mailgun API key | — |
| `MAILGUN_FROM` | Sender email address | — |

---

## Docker Deployment

The `Dockerfile` uses a **multi-stage build** to keep the final image lean:

```
Stage 1 (builder):  eclipse-temurin:21-jdk-alpine
  → ./mvnw clean package (tests skipped)
  → produces target/*.jar

Stage 2 (runtime):  eclipse-temurin:21-jre-alpine
  → copies only the JAR (no JDK, no source)
  → EXPOSE 8080
```

`docker-compose.yml` defines two services:
- `mysql` — MySQL 8.0 with a persistent named volume and a health check (`mysqladmin ping`)
- `finance-api` — application container with `depends_on: mysql: condition: service_healthy`

MySQL is exposed on host port `3307` to avoid conflicts with a locally running MySQL instance.

---

## What's Next

- **Test coverage** — Unit tests for service layer (JUnit 5 + Mockito) and integration tests using the H2 in-memory database already on the classpath
- **Bulk transaction import** — CSV upload endpoint with per-row error reporting (`TransactionDto.BulkRowError` DTO already scaffolded in the codebase)
- **Email notifications** — Transaction confirmation and account activity alerts via the existing Mailgun integration
- **Pagination** — Add `Pageable` to transaction history and admin list endpoints
- **Refresh tokens** — Extend the auth flow with refresh token support for longer-lived sessions

---

*Built as a training project at QBurst — demonstrating Spring Boot 3, layered architecture, JWT security, and production-ready design patterns.*