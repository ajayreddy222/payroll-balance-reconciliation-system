-- Add payment_type to direct_payments to distinguish between:
--   DIRECT_PAYMENT  - cash/check from employer (80-20 gross-up applies)
--   FEE_DEDUCTION   - perm fee, legal fee etc. (deducted as-is, no gross-up)
--   INSURANCE       - health/dental insurance deduction (deducted as-is)

ALTER TABLE direct_payments ADD COLUMN payment_type VARCHAR(40) NOT NULL DEFAULT 'DIRECT_PAYMENT';

-- Recalculate gross_amount for existing rows:
-- DIRECT_PAYMENT rows already have correct gross (amount / 0.80) — no change needed.
