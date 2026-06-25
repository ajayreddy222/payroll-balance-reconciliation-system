# Payroll Balance Reconciliation System

A full-stack application that replaces manual Excel-based payroll balance tracking with an automated system for calculating employer/employee payroll balances, deductions, and carry-forward totals.

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA |
| Frontend | React 18, TypeScript, Material UI 5, Vite |
| Database | PostgreSQL 16 |
| Authentication | JWT (JSON Web Tokens) |
| Build | Maven (backend), npm (frontend) |
| Deployment | Docker, Docker Compose |

## Business Rules

1. **Actual Earnings** = Hours Worked × Employee Hourly Rate (80-20 rate)
2. **Monthly Balance** = Actual Earnings - Paystub (LCA)
3. **Negative Adjustments** (fees, perm payments) are deducted BEFORE tax reduction
4. **80-20 After-Tax Rule** (×0.80) applies ONLY when employee receives direct payment from employer
5. **If Actual < LCA** (negative balance), no 80-20 reduction — full deficit taken from cumulative balance
6. **Direct Employer Payment** grossed up: Gross = Net ÷ 0.80
7. **Insurance Deductions** reduce balance directly
8. **Yearly Carry-Forward**: Previous year's ending balance becomes next year's opening balance

### Vendor Fee & Employer Margin Analytics

The system calculates how much each intermediary takes from the client billing rate:

```
Client pays (per hour)  = employeeHourlyRate (billing rate)
Vendor Fee              = Client pays × (vendorFeePercentage / 100)
Employer Gets           = Client pays - Vendor Fee
You Get (per hour)      = eightyTwentyRate
Employer Margin         = Employer Gets - You Get
```

**Example (Charter/Tellus):**
| | Per Hour | 160 hrs/month |
|--|---------|---------------|
| Client pays | $62.00 | $9,920.00 |
| Vendor takes (3%) | $1.86 | $297.60 |
| Employer gets | $60.14 | $9,622.40 |
| You get (80-20 rate) | $48.11 | $7,697.60 |
| Employer margin | $12.03 | $1,924.80 |

These analytics are computed on-the-fly from existing data (no additional database columns needed).

## Project Structure

```
payroll-balance-reconciliation-system/
├── backend/                    # Spring Boot REST API
│   ├── src/main/java/com/payroll/reconciliation/
│   │   ├── config/            # Security, CORS, data initialization
│   │   ├── controller/        # REST endpoints
│   │   ├── dto/               # Request/Response DTOs
│   │   ├── entity/            # JPA entities
│   │   ├── repository/        # Spring Data repositories
│   │   ├── security/          # JWT filter, service, UserDetails
│   │   └── service/           # Business logic
│   └── src/main/resources/
│       ├── application.yml    # App configuration
│       └── db/migration/      # Flyway SQL migrations
├── frontend/                   # React SPA
│   └── src/
│       ├── api/               # Axios client
│       ├── pages/             # Dashboard, Entries, Projects, Reports
│       ├── components/        # Shared layout components
│       └── types/             # TypeScript type definitions
├── docker-compose.yml          # Full-stack Docker deployment
└── README.md
```

## Features

- **Dashboard** — Current balance, balance by year/project, totals, vendor fee & employer margin analytics
- **Vendor Fee Analytics** — Total vendor fee (all time), vendor fee by year, per-project breakdown
- **Employer Margin Analytics** — Total employer margin, margin by year, margin per hour per project, monthly breakdown
- **Projects** — Manage projects with vendor fee %, 80-20 rate, LCA amount; view per-project margin details
- **Monthly Entries** — Multi-entry form, auto-fill from project defaults, edit/delete
- **Reports** — Monthly, quarterly, yearly reports with PDF/Excel export
- **Audit Trail** — Paystubs, employer payments, adjustments, yearly balances tracked automatically
- **Authentication** — JWT-based login with role-based access

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 16+

## Local Development Setup

### 1. Database

```bash
# Option A: Docker
docker run -d --name payroll-db \
  -e POSTGRES_DB=payroll_reconciliation \
  -e POSTGRES_USER=payroll \
  -e POSTGRES_PASSWORD=payroll \
  -p 5432:5432 postgres:16-alpine

# Option B: Local PostgreSQL
# Create database: payroll_reconciliation
# User: payroll / Password: payroll
```

### 2. Backend

```bash
cd backend
mvn clean spring-boot:run
```

Runs on http://localhost:8080

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on http://localhost:5173

### 4. Docker Compose (full stack)

```bash
docker-compose up --build
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Authenticate and get JWT |
| POST | `/api/auth/register` | Register new user |
| GET | `/api/projects` | List all projects |
| POST | `/api/projects` | Create project |
| PUT | `/api/projects/{id}` | Update project |
| GET | `/api/monthly-entries` | List all entries |
| POST | `/api/monthly-entries` | Create/upsert entry |
| PUT | `/api/monthly-entries/{id}` | Update entry |
| DELETE | `/api/monthly-entries/{id}` | Delete entry |
| GET | `/api/dashboard/summary` | Dashboard data |
| POST | `/api/yearly-balances/recalculate` | Recalculate yearly balances |
| POST | `/api/admin/backfill` | Backfill detail tables for existing data |

## Default Credentials

| Email | Password | Role |
|-------|----------|------|
| admin@payroll.com | admin123 | ADMIN |

## License

Private — All rights reserved.
