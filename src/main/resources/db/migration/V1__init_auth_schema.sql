-- ═══════════════════════════════════════════════════════════
--  V1 — LastBite Core Auth Schema
--  Tables: users, refresh_tokens, store_registrations
-- ═══════════════════════════════════════════════════════════

-- ── Users ──────────────────────────────────────────────────
-- Core identity table. password_hash is nullable to support
-- future OAuth providers (Google, Zalo) where users don't
-- set a local password.
CREATE TABLE users (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255),
    full_name       VARCHAR(255) NOT NULL,
    phone           VARCHAR(20)  UNIQUE,
    avatar_url      VARCHAR(500),
    role            VARCHAR(50)  NOT NULL DEFAULT 'CUSTOMER',
    status          VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    auth_provider   VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    phone_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_users_role CHECK (role IN ('CUSTOMER', 'STORE_OWNER', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED')),
    CONSTRAINT chk_users_auth_provider CHECK (auth_provider IN ('LOCAL', 'GOOGLE', 'ZALO'))
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- ── Refresh Tokens ────────────────────────────────────────
-- Stores SHA-256 hashed refresh tokens for Token Rotation.
-- Raw tokens are NEVER persisted.
CREATE TABLE refresh_tokens (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) UNIQUE NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- ── Store Registrations ───────────────────────────────────
-- Store owner applications requiring admin approval.
-- Flow: PENDING → APPROVED (user.role → STORE_OWNER) / REJECTED
CREATE TABLE store_registrations (
    id                          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_name               VARCHAR(255) NOT NULL,
    business_license_number     VARCHAR(100),
    business_license_image_url  VARCHAR(500),
    address                     VARCHAR(500) NOT NULL,
    status                      VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    reviewed_by                 UUID,
    reviewed_at                 TIMESTAMPTZ,
    rejection_reason            VARCHAR(1000),
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_store_reg_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_store_reg_user_id ON store_registrations(user_id);
CREATE INDEX idx_store_reg_status ON store_registrations(status);
