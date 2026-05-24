-- ═══════════════════════════════════════════════════════════
--  V6 — Phase 2: Surprise Bags & Daily Inventory
--  Tables: surprise_bags, bag_daily_stocks, stock_audit_logs
-- ═══════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS surprise_bags (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id           UUID         NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    bag_type           VARCHAR(30)  NOT NULL DEFAULT 'STANDARD',
    photos             TEXT[]       NOT NULL DEFAULT ARRAY[]::TEXT[],
    estimated_value    NUMERIC(10,0) NOT NULL,
    sale_price         NUMERIC(10,0) NOT NULL,
    platform_fee       NUMERIC(10,0) NOT NULL DEFAULT 4000,
    max_per_order      INTEGER      NOT NULL DEFAULT 1,
    pickup_start_time  TIME         NOT NULL,
    pickup_end_time    TIME         NOT NULL,
    available_days     INTEGER[]    NOT NULL DEFAULT ARRAY[0,1,2,3,4,5,6],
    status             VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    version            INTEGER      NOT NULL DEFAULT 1,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_surprise_bags_type CHECK (bag_type IN ('STANDARD', 'BREAD', 'MEAL', 'GROCERY', 'MIXED')),
    CONSTRAINT chk_surprise_bags_status CHECK (status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'ARCHIVED')),
    CONSTRAINT chk_surprise_bags_estimated_value CHECK (estimated_value > 0),
    CONSTRAINT chk_surprise_bags_sale_price_floor CHECK (sale_price >= 15000),
    CONSTRAINT chk_surprise_bags_sale_price_min_ratio CHECK (sale_price >= estimated_value * 0.10),
    CONSTRAINT chk_surprise_bags_sale_price_max_ratio CHECK (sale_price <= estimated_value * 0.50),
    CONSTRAINT chk_surprise_bags_max_per_order CHECK (max_per_order BETWEEN 1 AND 3),
    CONSTRAINT chk_surprise_bags_pickup_window CHECK (
        EXTRACT(EPOCH FROM (pickup_end_time - pickup_start_time)) BETWEEN 1800 AND 14400
    ),
    CONSTRAINT chk_surprise_bags_available_days_not_empty CHECK (array_length(available_days, 1) >= 1),
    CONSTRAINT chk_surprise_bags_available_days_range CHECK (
        available_days <@ ARRAY[0,1,2,3,4,5,6]
    )
);

CREATE INDEX IF NOT EXISTS idx_surprise_bags_store_id ON surprise_bags(store_id);
CREATE INDEX IF NOT EXISTS idx_surprise_bags_store_status ON surprise_bags(store_id, status);
CREATE INDEX IF NOT EXISTS idx_surprise_bags_pickup_window ON surprise_bags(pickup_start_time, pickup_end_time);

CREATE TABLE IF NOT EXISTS bag_daily_stocks (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    bag_id              UUID         NOT NULL REFERENCES surprise_bags(id) ON DELETE CASCADE,
    store_id            UUID         NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    date                DATE         NOT NULL,
    quantity            INTEGER      NOT NULL DEFAULT 0,
    reserved            INTEGER      NOT NULL DEFAULT 0,
    sold                INTEGER      NOT NULL DEFAULT 0,
    sale_price_override NUMERIC(10,0),
    status              VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    version             INTEGER      NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_bag_daily_stocks_bag_date UNIQUE (bag_id, date),
    CONSTRAINT chk_bag_daily_stocks_status CHECK (status IN ('ACTIVE', 'SOLD_OUT', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT chk_bag_daily_stocks_quantity CHECK (quantity >= 0 AND quantity <= 50),
    CONSTRAINT chk_bag_daily_stocks_reserved CHECK (reserved >= 0),
    CONSTRAINT chk_bag_daily_stocks_sold CHECK (sold >= 0),
    CONSTRAINT chk_bag_daily_stocks_available CHECK (reserved + sold <= quantity)
);

CREATE INDEX IF NOT EXISTS idx_bag_daily_stocks_store_date_status ON bag_daily_stocks(store_id, date, status);
CREATE INDEX IF NOT EXISTS idx_bag_daily_stocks_date ON bag_daily_stocks(date);
CREATE INDEX IF NOT EXISTS idx_bag_daily_stocks_date_status ON bag_daily_stocks(date, status);
CREATE INDEX IF NOT EXISTS idx_bag_daily_stocks_store_date ON bag_daily_stocks(store_id, date);

CREATE TABLE IF NOT EXISTS stock_audit_logs (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    bag_id            UUID        NOT NULL REFERENCES surprise_bags(id) ON DELETE CASCADE,
    daily_stock_id    UUID        REFERENCES bag_daily_stocks(id) ON DELETE SET NULL,
    actor_id          UUID        REFERENCES users(id) ON DELETE SET NULL,
    action            VARCHAR(40) NOT NULL,
    delta             INTEGER     NOT NULL,
    quantity_before   INTEGER     NOT NULL,
    quantity_after    INTEGER     NOT NULL,
    reason            TEXT,
    order_id          UUID,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_stock_audit_logs_action CHECK (
        action IN ('STOCK_ADD', 'STOCK_REDUCE', 'STOCK_SET', 'RESERVE', 'RESERVE_CANCEL', 'SELL', 'EXPIRE_UNSOLD')
    )
);

CREATE INDEX IF NOT EXISTS idx_stock_audit_logs_bag_id ON stock_audit_logs(bag_id);
CREATE INDEX IF NOT EXISTS idx_stock_audit_logs_daily_stock_id ON stock_audit_logs(daily_stock_id);
CREATE INDEX IF NOT EXISTS idx_stock_audit_logs_order_id ON stock_audit_logs(order_id);
CREATE INDEX IF NOT EXISTS idx_stock_audit_logs_created_at ON stock_audit_logs(created_at);
