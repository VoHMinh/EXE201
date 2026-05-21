-- ═══════════════════════════════════════════════════════════
--  V4 — Phase 1 Schema Updates
--  • stores: add district, city columns + indexes
--  • refresh_tokens: add device_info column
--  • NEW: store_reliability_stats table
--  • NEW: email_verification_tokens table (OTP flow)
-- ═══════════════════════════════════════════════════════════

-- ── Stores: add district + city ───────────────────────────
ALTER TABLE stores ADD COLUMN IF NOT EXISTS district VARCHAR(100);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS city VARCHAR(100) NOT NULL DEFAULT 'ho-chi-minh';

CREATE INDEX IF NOT EXISTS idx_stores_district_status ON stores(district, status);
CREATE INDEX IF NOT EXISTS idx_stores_city_district_status ON stores(city, district, status);

-- ── Refresh Tokens: add device_info ───────────────────────
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS device_info VARCHAR(500);

-- ── Store Reliability Stats (1:1 with stores) ─────────────
CREATE TABLE IF NOT EXISTS store_reliability_stats (
    store_id             UUID        PRIMARY KEY REFERENCES stores(id) ON DELETE CASCADE,
    total_bags_listed    INTEGER     NOT NULL DEFAULT 0,
    total_bags_sold      INTEGER     NOT NULL DEFAULT 0,
    total_bags_fulfilled INTEGER     NOT NULL DEFAULT 0,
    total_bags_no_show   INTEGER     NOT NULL DEFAULT 0,
    fulfillment_rate     FLOAT       NOT NULL DEFAULT 1.0,
    warning_count        INTEGER     NOT NULL DEFAULT 0,
    is_under_review      BOOLEAN     NOT NULL DEFAULT FALSE,
    suspended_until      TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_fulfillment_rate CHECK (fulfillment_rate >= 0 AND fulfillment_rate <= 1)
);

-- ── Email Verification Tokens (OTP) ──────────────────────
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    otp_code    VARCHAR(6)   NOT NULL,
    attempts    INTEGER      NOT NULL DEFAULT 0,
    verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_otp_attempts CHECK (attempts >= 0 AND attempts <= 5)
);

CREATE INDEX IF NOT EXISTS idx_email_verify_user_id ON email_verification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_email_verify_expires ON email_verification_tokens(expires_at);
