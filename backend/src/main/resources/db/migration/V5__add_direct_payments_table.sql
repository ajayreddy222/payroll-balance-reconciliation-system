-- Standalone direct payments table.
-- Stores cash/check payments received directly from the employer,
-- outside of the regular payroll/paystub system.
-- The gross amount (amount_received / 0.80) is calculated and stored
-- at insert time by the application.

CREATE TABLE direct_payments (
  id               BIGSERIAL PRIMARY KEY,
  payment_date     DATE           NOT NULL,
  amount_received  NUMERIC(14,2)  NOT NULL,
  gross_amount     NUMERIC(14,2)  NOT NULL,
  notes            TEXT,
  created_at       TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_direct_payments_date ON direct_payments(payment_date);
