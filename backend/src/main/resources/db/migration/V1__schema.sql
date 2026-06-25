CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(160) NOT NULL,
  email VARCHAR(180) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(40) NOT NULL DEFAULT 'ROLE_USER',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE projects (
  id BIGSERIAL PRIMARY KEY,
  client_name VARCHAR(180) NOT NULL,
  vendor_name VARCHAR(180) NOT NULL,
  project_name VARCHAR(180) NOT NULL,
  employee_hourly_rate NUMERIC(12,2) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE,
  active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE monthly_entries (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  entry_month DATE NOT NULL,
  hours_worked NUMERIC(12,2) NOT NULL DEFAULT 0,
  employee_hourly_rate NUMERIC(12,2) NOT NULL DEFAULT 0,
  actual_earnings NUMERIC(14,2) NOT NULL DEFAULT 0,
  paystub_amount_received NUMERIC(14,2) NOT NULL DEFAULT 0,
  direct_employer_payment NUMERIC(14,2) NOT NULL DEFAULT 0,
  insurance_deduction NUMERIC(14,2) NOT NULL DEFAULT 0,
  other_adjustment NUMERIC(14,2) NOT NULL DEFAULT 0,
  monthly_balance NUMERIC(14,2) NOT NULL DEFAULT 0,
  notes TEXT,
  created_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_monthly_entry_project_month UNIQUE(project_id, entry_month)
);

CREATE TABLE paystubs (
  id BIGSERIAL PRIMARY KEY,
  monthly_entry_id BIGINT NOT NULL REFERENCES monthly_entries(id) ON DELETE CASCADE,
  pay_date DATE NOT NULL,
  amount NUMERIC(14,2) NOT NULL,
  document_reference VARCHAR(255),
  notes TEXT
);

CREATE TABLE employer_payments (
  id BIGSERIAL PRIMARY KEY,
  monthly_entry_id BIGINT NOT NULL REFERENCES monthly_entries(id) ON DELETE CASCADE,
  payment_date DATE NOT NULL,
  amount_received NUMERIC(14,2) NOT NULL,
  gross_amount NUMERIC(14,2) NOT NULL,
  notes TEXT
);

CREATE TABLE adjustments (
  id BIGSERIAL PRIMARY KEY,
  monthly_entry_id BIGINT REFERENCES monthly_entries(id) ON DELETE CASCADE,
  project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
  adjustment_date DATE NOT NULL,
  amount NUMERIC(14,2) NOT NULL,
  reason VARCHAR(500) NOT NULL,
  adjustment_type VARCHAR(40) NOT NULL,
  user_id BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE yearly_balances (
  id BIGSERIAL PRIMARY KEY,
  balance_year INTEGER NOT NULL UNIQUE,
  opening_balance NUMERIC(14,2) NOT NULL DEFAULT 0,
  earned_balance NUMERIC(14,2) NOT NULL DEFAULT 0,
  ending_balance NUMERIC(14,2) NOT NULL DEFAULT 0,
  calculated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_monthly_entries_month ON monthly_entries(entry_month);
CREATE INDEX idx_monthly_entries_project ON monthly_entries(project_id);
CREATE INDEX idx_adjustments_date ON adjustments(adjustment_date);
