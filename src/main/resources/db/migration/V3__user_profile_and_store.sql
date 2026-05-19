-- ═══════════════════════════════════════════════════════════
--  V3 — User Profile & Store Module
--  Tables: user_addresses, stores, store_schedules
--  Alters: users (add last_login_at)
-- ═══════════════════════════════════════════════════════════

-- ── ALTER users ───────────────────────────────────────────
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMPTZ;

-- ── User Addresses ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_addresses (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label        VARCHAR(50)  NOT NULL DEFAULT 'HOME',
    full_address TEXT         NOT NULL,
    lat          FLOAT,
    lng          FLOAT,
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_addresses_user_id ON user_addresses(user_id);

-- ── Stores ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS stores (
    id                          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id                    UUID         NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    name                        VARCHAR(255) NOT NULL,
    slug                        VARCHAR(300) UNIQUE NOT NULL,
    description                 TEXT,
    category                    VARCHAR(50)  NOT NULL,
    phone                       VARCHAR(20),
    email                       VARCHAR(255),
    address                     TEXT         NOT NULL,
    lat                         FLOAT,
    lng                         FLOAT,
    cover_image_url             VARCHAR(500),
    logo_url                    VARCHAR(500),
    business_license_number     VARCHAR(100),
    business_license_image_url  VARCHAR(500),
    status                      VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    verification_status         VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    rejection_reason            VARCHAR(1000),
    avg_rating                  FLOAT        NOT NULL DEFAULT 0,
    total_ratings               INTEGER      NOT NULL DEFAULT 0,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_store_status CHECK (status IN ('ACTIVE', 'PAUSED', 'CLOSED', 'SUSPENDED')),
    CONSTRAINT chk_store_verification CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    CONSTRAINT chk_store_category CHECK (category IN ('BAKERY', 'RESTAURANT', 'CAFE', 'GROCERY', 'CONVENIENCE'))
);

CREATE INDEX IF NOT EXISTS idx_stores_slug ON stores(slug);
CREATE INDEX IF NOT EXISTS idx_stores_owner_id ON stores(owner_id);
CREATE INDEX IF NOT EXISTS idx_stores_status ON stores(status);
CREATE INDEX IF NOT EXISTS idx_stores_category ON stores(category);
CREATE INDEX IF NOT EXISTS idx_stores_verification ON stores(verification_status);
CREATE INDEX IF NOT EXISTS idx_stores_lat_lng ON stores(lat, lng);

-- ── Store Schedules ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS store_schedules (
    id           UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id     UUID     NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    day_of_week  INTEGER  NOT NULL,
    open_time    TIME     NOT NULL,
    close_time   TIME     NOT NULL,
    is_open      BOOLEAN  NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_day_of_week CHECK (day_of_week BETWEEN 0 AND 6),
    CONSTRAINT uq_store_day UNIQUE (store_id, day_of_week)
);

CREATE INDEX IF NOT EXISTS idx_store_schedules_store_id ON store_schedules(store_id);
