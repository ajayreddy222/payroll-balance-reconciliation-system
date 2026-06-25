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
│       ├── application.yml        # Base app configuration
│       ├── application-local.yml  # Local credentials (gitignored)
│       └── db/migration/          # Flyway SQL migrations
├── frontend/                   # React SPA
│   └── src/
│       ├── api/               # Axios client
│       ├── pages/             # Dashboard, Entries, Projects, Reports
│       ├── components/        # Shared layout components
│       └── types/             # TypeScript type definitions
├── docker-compose.yml          # Full-stack Docker deployment
├── start-app.bat               # Start both services
├── stop-app.bat                # Stop both services
└── README.md
```

## Features

- **Dashboard** — Current balance, balance by year/project, totals
- **Projects** — Manage projects with vendor fee %, 80-20 rate, LCA amount
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

### 1. Database (Remote - Neon)

The application uses a remote PostgreSQL database hosted on [Neon](https://neon.tech). No local database setup is required.

Database credentials are stored in `backend/src/main/resources/application-local.yml` (gitignored). Create this file with your Neon connection details:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<your-neon-host>/payroll_reconciliation?sslmode=require
    username: <your-neon-username>
    password: <your-neon-password>

app:
  jwt-secret: <your-jwt-secret>
```

### 2. Quick Start (Single Click)

```bash
# Start both backend and frontend
start-app.bat

# Stop both services
stop-app.bat
```

### 3. Backend (Manual)

```bash
cd backend
mvn clean spring-boot:run
```

Runs on http://localhost:8080

### 4. Frontend (Manual)

```bash
cd frontend
npm install
npm run dev
```

Runs on http://localhost:5173

### 5. Docker Compose (full stack)

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
