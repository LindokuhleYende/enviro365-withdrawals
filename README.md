# Enviro365 Investments — Withdrawal Notice Automation System

A full-stack system that lets investors view their portfolios, submit withdrawal notices, have
them validated against business rules, review their withdrawal history, and export CSV
statements.

Built as a junior developer assessment. Backend: **Spring Boot 3 (Java 21)**. Frontend: plain
**HTML / CSS / JavaScript**, served directly from the Spring Boot app so the whole system runs
as a single deployable unit.

Package root: `com.enviro.assessment.junior.lindokuhleyende`

---

## 1. Features

- **Portfolio dashboard** — view an investor's details and all held products, with a computed
  total balance.
- **Withdrawal notices** — submit a withdrawal request against a specific product; the amount
  is validated before anything is persisted.
- **Withdrawal history** — list past withdrawal notices, filterable by investor, product,
  status, and date range.
- **CSV export** — download a withdrawal statement (same filters as history) as a `.csv` file.
- **Swagger / OpenAPI docs** — interactive API documentation and a built-in "try it out" console.
- **H2 in-memory database** — zero setup, seeded with sample investors/products on startup.

### Business rules enforced

1. **Retirement age rule** — withdrawals from a `RETIREMENT` product are only allowed if the
   investor's age is **strictly greater than 65**.
2. **Balance rule** — a withdrawal amount may **not exceed the product's current balance**.
3. **90% cap rule** — a withdrawal amount may **not exceed 90% of the product's current
   balance**, even if the balance itself would technically cover it.

All three rules are enforced server-side in `WithdrawalServiceImpl`, independent of the UI, and
are covered by unit and integration tests.

---

## 2. Tech stack

| Layer            | Technology                                                 |
|-------------------|------------------------------------------------------------|
| Language          | Java 21                                                    |
| Framework         | Spring Boot 3.3.4 (Web, Data JPA, Validation)              |
| Database          | H2 (in-memory)                                             |
| API docs          | springdoc-openapi (Swagger UI)                             |
| Boilerplate       | Lombok                                                     |
| Testing           | JUnit 5, Mockito, AssertJ, Spring MockMvc                  |
| Frontend          | HTML5, CSS3, vanilla JavaScript (fetch API, no build step) |
| Build tool        | Maven                                                      |

---

## 3. Project structure

```
enviro365-withdrawals/
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/com/enviro/assessment/junior/lindokuhleyende/
    │   │   ├── Enviro365WithdrawalsApplication.java   # entry point
    │   │   ├── config/            # OpenApiConfig, WebConfig (CORS)
    │   │   ├── controller/        # PortfolioController, WithdrawalController, ReportController
    │   │   ├── dto/                # request/response DTOs (never expose entities directly)
    │   │   ├── entity/             # JPA entities (Investor, Product, WithdrawalNotice) + enums
    │   │   ├── exception/          # custom exceptions + GlobalExceptionHandler
    │   │   ├── repository/         # Spring Data JPA repositories + Specifications
    │   │   └── service/            # interfaces + impl/ (business logic, incl. validation rules)
    │   └── resources/
    │       ├── application.yml     # H2, JPA, Swagger config
    │       ├── data.sql            # seed data (3 investors, 6 products)
    │       └── static/             # frontend (index.html, css/, js/)
    └── test
        └── java/.../
            ├── service/            # unit tests (Mockito) for business rules
            └── controller/         # integration tests (MockMvc + real H2 context)
```

**Design choices:**
- **DTO layer** — controllers never accept or return JPA entities directly; `WithdrawalRequestDto`,
  `WithdrawalResponseDto`, `PortfolioResponseDto`, etc. decouple the API contract from the
  persistence model.
- **Global exception handling** — `GlobalExceptionHandler` (`@RestControllerAdvice`) converts
  `ResourceNotFoundException` → 404, `InvalidWithdrawalException` / bean-validation failures →
  400, and anything unexpected → 500, all in a consistent JSON shape (`ErrorResponseDto`).
- **Input validation** — `WithdrawalRequestDto` uses Jakarta Bean Validation (`@NotNull`,
  `@DecimalMin`) in addition to the service-layer business rules.
- **REST best practices** — resource-oriented URLs, correct HTTP verbs/status codes
  (`201 Created` on withdrawal submission, `404` for missing resources, `400` for invalid
  input), and query-parameter based filtering rather than overloaded endpoints.

---

## 4. Running the project

### Prerequisites
- JDK 21+
- Maven 3.8+ (or use your IDE's bundled Maven)

### Build and run

```bash
cd enviro365-withdrawals
mvn spring-boot:run
```

or build a runnable jar:

```bash
mvn clean package
java -jar target/enviro365-withdrawals.jar
```

The application starts on **http://localhost:8080**.

### Access points

| Resource            | URL                                              |
|----------------------|---------------------------------------------------|
| Frontend UI           | http://localhost:8080/                            |
| Swagger UI            | http://localhost:8080/swagger-ui.html              |
| OpenAPI JSON spec      | http://localhost:8080/v3/api-docs                  |
| H2 console             | http://localhost:8080/h2-console                   |

**H2 console credentials:**
- JDBC URL: `jdbc:h2:mem:enviro365db`
- Username: `sa`
- Password: *(leave blank)*

### Seed data

On every startup, `data.sql` seeds:

| Investor       | Age (approx.) | Products                                                        |
|-----------------|----------------|--------------------------------------------------------------------|
| John Smith      | ~40s (under 65) | Discretionary Investment Plan (R250,000), Retirement Annuity Fund (R500,000) |
| Mary Johnson    | 65+             | Retirement Annuity Fund (R800,000), Tax-Free Savings Account (R120,000) |
| Peter Ndlovu    | ~30s (under 65) | Unit Trust Growth Fund (R90,000), Discretionary Investment Plan (R45,000) |

This is intentionally set up so you can immediately demonstrate the retirement age rule: try
withdrawing from John Smith's Retirement Annuity Fund (id 2) and it will be rejected, while the
same withdrawal from Mary Johnson's Retirement Annuity Fund (id 3) succeeds.

---

## 5. API documentation

Full interactive documentation is available via **Swagger UI** once the app is running:
`http://localhost:8080/swagger-ui.html`

### Summary of endpoints

| Method | Endpoint                                   | Description                                       |
|--------|----------------------------------------------|-----------------------------------------------------|
| GET    | `/api/investors`                              | List all investors (id, name, email, age)            |
| GET    | `/api/investors/{investorId}/portfolio`       | Get an investor's portfolio (products + total balance) |
| POST   | `/api/withdrawals`                             | Submit a withdrawal notice                            |
| GET    | `/api/withdrawals`                             | Get withdrawal history (filterable)                    |
| GET    | `/api/reports/withdrawals/csv`                | Download withdrawal statement as CSV (filterable)      |

#### `GET /api/withdrawals` and `/api/reports/withdrawals/csv` query parameters (all optional)

| Param        | Type              | Example                    |
|---------------|--------------------|------------------------------|
| `investorId`  | Long                | `1`                            |
| `productId`   | Long                | `2`                            |
| `status`      | `PENDING\|APPROVED\|REJECTED` | `PENDING`        |
| `from`        | ISO date-time       | `2026-01-01T00:00:00`          |
| `to`          | ISO date-time       | `2026-12-31T23:59:59`          |

### Example requests

**Submit a withdrawal:**

```bash
curl -X POST http://localhost:8080/api/withdrawals \
  -H "Content-Type: application/json" \
  -d '{
        "investorId": 2,
        "productId": 3,
        "amount": 5000.00,
        "notes": "Monthly drawdown"
      }'
```

Successful response (`201 Created`):

```json
{
  "id": 1,
  "investorId": 2,
  "investorName": "Mary Johnson",
  "productId": 3,
  "productName": "Retirement Annuity Fund",
  "amountRequested": 5000.00,
  "balanceBeforeWithdrawal": 800000.00,
  "balanceAfterWithdrawal": 795000.00,
  "status": "PENDING",
  "requestDate": "2026-07-19T10:15:30",
  "notes": "Monthly drawdown"
}
```

**Business rule violation** (`400 Bad Request`):

```json
{
  "timestamp": "2026-07-19T10:16:02",
  "status": 400,
  "error": "Bad Request",
  "message": "Retirement withdrawals are only permitted for investors over the age of 65. Investor is currently 40.",
  "path": "/api/withdrawals"
}
```

**Get portfolio:**

```bash
curl http://localhost:8080/api/investors/1/portfolio
```

**Download CSV statement (filtered):**

```bash
curl -OJ "http://localhost:8080/api/reports/withdrawals/csv?investorId=1&status=PENDING"
```

---

## 6. Running the tests

```bash
mvn test
```

Test coverage includes:

- **`WithdrawalServiceImplTest`** (unit, Mockito) — every business rule in isolation:
  retirement age rule (rejected/allowed/bypassed for non-retirement products), balance
  exceeded, 90% cap exceeded, exact-boundary case (withdrawal == 90% of balance allowed),
  investor/product not found, and product-doesn't-belong-to-investor mismatch.
- **`PortfolioServiceImplTest`** (unit, Mockito) — total balance calculation and
  investor-not-found handling.
- **`WithdrawalControllerIntegrationTest`** (integration, MockMvc + real H2 + seed data) —
  end-to-end request/response behaviour for withdrawals, portfolio retrieval, bean validation
  errors, and CSV export content type.

---

## 7. Frontend

The UI (`src/main/resources/static/`) is a single page with no build step, served automatically
by Spring Boot's static resource handler:

- **Portfolio dashboard** — investor selector, total balance, and a products table.
- **Withdrawal form** — product dropdown, amount, optional notes; surfaces backend validation
  errors inline (e.g. "Withdrawal amount exceeds the maximum allowed withdrawal of 90% of
  balance").
- **Withdrawal history table** — auto-refreshes after each submission; status shown as a
  colour-coded pill.
- **CSV download** — filter by status/date range, then downloads directly from
  `/api/reports/withdrawals/csv`.

It talks to the backend purely via `fetch()` calls to the `/api/**` endpoints described above.

---

## 8. Notes / assumptions

- A withdrawal notice is created with status `PENDING` once it passes validation — this
  represents "accepted, awaiting back-office processing," not an automatic payout. The
  `balanceAfterWithdrawal` figure is a **calculated snapshot** for the notice; it does not
  mutate the product's actual stored balance (that would typically happen in a separate
  back-office approval/settlement process, out of scope for this assessment).
- The 90% cap is evaluated against the product's *current* balance at the time of the request,
  rounded to 2 decimal places (`HALF_UP`).
- CORS is enabled permissively on `/api/**` purely to make local frontend development against a
  separately-served UI easier; it has no effect when the bundled frontend is used, since it's
  served same-origin.

## 9. AI Usage Disclosure

AI tools were used to assist with specific parts of this assessment. All generated content was reviewed, modified where necessary, 
and integrated by me.

### AI Tools Used
- Claude 4 Sonnet (Anthropic)

### How AI Was Used
- Assisted with drafting and improving the project README.
- Assisted with generating and refining controller unit tests.
- Provided guidance on exception handling implementation and best practices.
- Helped explain framework concepts and troubleshoot implementation issues during development.

### My Contribution
The application architecture, business logic, API implementation, database design, debugging, and final integration were 
