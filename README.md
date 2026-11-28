# Payroll Balance Reconciliation System

A full-stack application that replaces manual Excel-based payroll balance tracking with an automated system for calculating employer/employee payroll balances, deductions, and carry-forward totals.

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA |
| Frontend | React 18, TypeScript, Material UI 5, Vite |
| Database | PostgreSQL 16+ |
| Authentication | JWT (JSON Web Tokens) |
| API Docs | Swagger UI (springdoc-openapi) |
| Build | Maven (backend), npm (frontend) |
| Deployment | Docker, Docker Compose |

---

## Business Rules

### Monthly Entry Calculation

1. **Actual Earnings** = Hours Worked × Employee Hourly Rate (80-20 rate)
2. **Monthly Balance** = Actual Earnings − Paystub (LCA) − Insurance
3. **If Actual < LCA** — negative balance, full deficit flows into the cumulative balance unchanged
4. **Insurance** is auto-applied from the Insurance Rates table — only applies to projects with LCA > 0 (paystub-running projects)
5. **Standalone Direct Payments** are recorded separately from monthly entries — enter the net amount received and the system grosses it up as `Amount Received ÷ 0.80` and deducts that gross amount from the dashboard's running balance
6. **The 80-20 gross-up is not applied to monthly entries** — it applies only to standalone Direct Payments
7. **Yearly Carry-Forward** — Previous year's ending balance becomes next year's opening balance

### Payments Ledger

Payments, fees, and personal expenses are recorded independently of monthly entries:

| Payment Type | Gross-up | Affects Balance | Payment Mode |
|-------------|----------|----------------|-------------|
| `DIRECT_PAYMENT` | Gross = Net ÷ 0.80 | ✅ Yes | Required (Zelle/Cash/Check/Online/Paystub) |
| `FEE_DEDUCTION` | No gross-up, as-is | ✅ Yes | Optional |
| `PERSONAL_EXPENSE` | No gross-up, as-is | ❌ No (tracked only) | Required (Zelle/Cash/Check/Online/Paystub) |

```
Current Balance = Sum of all monthly balances
                − Sum of DIRECT_PAYMENT gross amounts
                − Sum of FEE_DEDUCTION amounts
                (PERSONAL_EXPENSE excluded from balance)
```

### Insurance Rates

Insurance runs on a **Sep–Aug cycle** (not January–December). One rate per period is entered; the system auto-applies it to every monthly entry for that period on save.

- Only applied to projects with `lcaAmount > 0` (paystub-running projects)
- When a new rate is added, the previous open rate is automatically closed
- Use **Recalculate All** to re-apply rates to existing entries

### Vendor Fee & Employer Margin Analytics

The system calculates how much each intermediary takes from the client billing rate:

```
Client pays (per hour)  = employeeHourlyRate (billing rate)
Vendor Fee              = Client pays × (vendorFeePercentage / 100)
Employer Gets           = Client pays − Vendor Fee
You Get (per hour)      = eightyTwentyRate
Employer Margin         = Employer Gets − You Get
```

**Example (Client/Vendor — 3% vendor fee):**
| | Per Hour | 160 hrs/month |
|--|---------|---------------|
| Client pays | $62.00 | $9,920.00 |
| Vendor takes (3%) | $1.86 | $297.60 |
| Employer gets | $60.14 | $9,622.40 |
| You get (80-20 rate) | $48.11 | $7,697.60 |
| Employer margin | $12.03 | $1,924.80 |

---

## Features

| Feature | Description |
|---------|-------------|
| **Dashboard** | Current balance, total paid out (gross), balance by year/project, vendor fee & employer margin analytics |
| **Vendor Fee Analytics** | Total vendor fee (all time), vendor fee by year, per-project breakdown |
| **Employer Margin Analytics** | Total employer margin, margin by year, margin per hour per project, monthly breakdown |
| **Projects** | Manage projects with billing rate, vendor fee %, 80-20 rate, LCA amount; view per-project margin details |
| **Monthly Entries** | Simplified 5-field multi-row form; auto-fill from project defaults; edit/delete; filter by year/project; insurance auto-applied on save |
| **Payments Ledger** | Multi-row add for Direct Payments (80-20 gross-up), Fee Deductions, and Personal Expenses; payment mode per entry (Zelle/Cash/Check/Online/Paystub) |
| **Insurance Rates** | Manage Sep–Aug insurance rates; auto-applied to paystub-running projects on save; Recalculate All button |
| **Reports** | Monthly, quarterly, yearly reports with PDF and Excel export |
| **Yearly Carry-Forward** | Automatic carry-forward from previous year's ending balance |
| **Swagger UI** | Interactive API documentation at `/swagger-ui/index.html` |
| **Authentication** | JWT-based login with role-based access control |

---

## Database Migrations

| Version | Description |
|---------|-------------|
| V1 | Initial schema — all tables |
| V2 | Seed default admin user |
| V3 | Add vendor fee, 80-20 rate, LCA columns to projects |
| V4 | Rename discount_percentage to vendor_fee_percentage |
| V5 | Add standalone direct_payments table |
| V6 | Remove direct_employer_payment from monthly_entries |
| V7 | Add payment_type column to direct_payments |
| V8 | Drop employer_payments, paystubs, adjustments tables; remove other_adjustment from monthly_entries |
| V9 | Add payment_mode column to direct_payments |
| V10 | Create insurance_rates table |

---

## Project Structure

```
payroll-balance-reconciliation-system/
├── backend/
│   ├── src/main/java/com/payroll/reconciliation/
│   │   ├── config/            # Security, CORS, OpenAPI, data initialization
│   │   ├── controller/        # REST endpoints
│   │   ├── dto/               # Request/Response DTOs
│   │   ├── entity/            # JPA entities
│   │   ├── repository/        # Spring Data repositories
│   │   ├── security/          # JWT filter, service, UserDetails
│   │   └── service/           # Business logic
│   └── src/main/resources/
│       ├── application.yml            # Base configuration
│       ├── application-local.yml      # Local credentials (gitignored)
│       └── db/migration/              # Flyway SQL migrations (V1–V10)
├── frontend/                   # React SPA
│   └── src/
│       ├── api/               # Axios client with JWT interceptor
│       ├── pages/             # Dashboard, Projects, Entries, Payments, Insurance, Reports
│       ├── components/        # Layout, navigation
│       └── types/             # TypeScript type definitions
├── docker-compose.yml          # Full-stack Docker deployment
├── start-app.bat               # Start both services
├── stop-app.bat                # Stop both services
└── README.md
```

---

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 16+

---

## Local Development Setup

### 1. Database

**Option A — Local PostgreSQL**

```sql
CREATE DATABASE payroll_reconciliation;
CREATE USER payroll WITH PASSWORD 'payroll';
GRANT ALL PRIVILEGES ON DATABASE payroll_reconciliation TO payroll;
```

Flyway creates all tables automatically on first startup.

**Option B — Neon (Remote, Free)**

Sign up at [neon.tech](https://neon.tech), create a project, and use the connection string in `application-local.yml`.

### 2. Backend Configuration

Create `backend/src/main/resources/application-local.yml` (gitignored):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payroll_reconciliation
    username: <username>
    password: <password>

app:
  jwt-secret: <your-jwt-secret>
```

For Neon:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://<neon-host>/payroll_reconciliation?sslmode=require
    username: <username>
    password: <password>

app:
  jwt-secret: <your-jwt-secret>
```

### 3. Quick Start (Windows)

```bash
start-app.bat    # Start both backend and frontend
stop-app.bat     # Stop both services
```

### 4. Manual Start

```bash
# Backend
cd backend
mvn clean spring-boot:run
```

Runs on http://localhost:8080

# Frontend (separate terminal)
```bash
cd frontend
npm install
npm run dev
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |

### 5. Docker Compose

```bash
docker-compose up --build
```

---

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Authenticate and receive JWT |
| POST | `/api/auth/register` | Register new user |

### Projects
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/projects` | List all projects |
| POST | `/api/projects` | Create project |
| PUT | `/api/projects/{id}` | Update project |

### Monthly Entries
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/monthly-entries` | List all entries |
| POST | `/api/monthly-entries` | Create/upsert entry (insurance auto-applied) |
| PUT | `/api/monthly-entries/{id}` | Update entry |
| DELETE | `/api/monthly-entries/{id}` | Delete entry |

### Payments Ledger
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/direct-payments` | List all payment entries |
| POST | `/api/direct-payments` | Create a single entry |
| POST | `/api/direct-payments/batch` | Create multiple entries at once |
| PUT | `/api/direct-payments/{id}` | Update an entry |
| DELETE | `/api/direct-payments/{id}` | Delete an entry |

### Insurance Rates
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/insurance-rates` | List all rates |
| POST | `/api/insurance-rates` | Create rate (auto-closes previous open rate) |
| PUT | `/api/insurance-rates/{id}` | Update rate |
| DELETE | `/api/insurance-rates/{id}` | Delete rate |
| POST | `/api/insurance-rates/recalculate` | Re-apply rates to all monthly entries |

### Dashboard & Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/summary` | Full dashboard data |
| POST | `/api/yearly-balances/recalculate` | Recalculate yearly carry-forward |
| GET | `/api/reports/monthly` | Monthly report (xlsx/pdf) |
| GET | `/api/reports/quarterly` | Quarterly report (xlsx/pdf) |
| GET | `/api/reports/yearly` | Yearly report (xlsx/pdf) |

---

## Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

**To test authenticated endpoints:**
1. Call `POST /api/auth/login` with your credentials
2. Copy the `token` from the response
3. Click the **Authorize 🔓** button at the top right
4. Enter `Bearer <your-token>` and click Authorize

---

## Default Credentials

| Email | Password | Role |
|-------|----------|------|
| admin@payroll.com | admin123 | ADMIN |

## License

Private — All rights reserved.
