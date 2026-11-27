-- Remove direct_employer_payment column from monthly_entries.
-- Direct payments are now tracked independently in the direct_payments table.
ALTER TABLE monthly_entries DROP COLUMN IF EXISTS direct_employer_payment;
