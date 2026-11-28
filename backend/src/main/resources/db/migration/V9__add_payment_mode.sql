-- Add payment_mode to direct_payments.
-- Required for DIRECT_PAYMENT type, optional for FEE_DEDUCTION and PERSONAL_EXPENSE.
ALTER TABLE direct_payments ADD COLUMN payment_mode VARCHAR(40);
