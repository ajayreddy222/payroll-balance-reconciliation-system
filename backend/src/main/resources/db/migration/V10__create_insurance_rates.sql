-- Insurance rates table.
-- One row per rate period (typically Sep-Aug each year).
-- The system auto-applies the matching rate to monthly entries on save.
-- effective_to is nullable — null means the rate is still active.

CREATE TABLE insurance_rates (
    id               BIGSERIAL    PRIMARY KEY,
    effective_from   DATE         NOT NULL,
    effective_to     DATE,
    monthly_amount   NUMERIC(14,2) NOT NULL,
    notes            TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_insurance_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX idx_insurance_rates_from ON insurance_rates(effective_from);
