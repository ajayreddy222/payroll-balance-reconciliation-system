-- Drop redundant audit/detail tables that are no longer needed.
-- employer_payments and paystubs are replaced by the direct_payments table.
-- adjustments is replaced by insurance_rates and direct_payments.

-- Remove INSURANCE type entries from direct_payments before dropping INSURANCE support
DELETE FROM direct_payments WHERE payment_type = 'INSURANCE';

-- Drop tables (CASCADE handles any remaining FK references)
DROP TABLE IF EXISTS employer_payments CASCADE;
DROP TABLE IF EXISTS paystubs CASCADE;
DROP TABLE IF EXISTS adjustments CASCADE;

-- Remove other_adjustment column from monthly_entries (fees now in direct_payments)
ALTER TABLE monthly_entries DROP COLUMN IF EXISTS other_adjustment;
