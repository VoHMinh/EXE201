-- V2 - Ensure auth schema exists for databases that were baselined at V1.
--
-- If a production database already contained objects before Flyway was enabled,
-- baseline-on-migrate may have recorded V1 without executing V1__init_auth_schema.sql.
-- Keep this migration idempotent so those environments can create the missing
-- auth tables while fresh databases continue to use V1 normally.

CREATE TABLE IF NOT EXISTS users (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
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

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) UNIQUE NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS store_registrations (
    id                          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_name               VARCHAR(255)  NOT NULL,
    business_license_number     VARCHAR(100),
    business_license_image_url  VARCHAR(500),
    address                     VARCHAR(500)  NOT NULL,
    status                      VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    reviewed_by                 UUID,
    reviewed_at                 TIMESTAMPTZ,
    rejection_reason            VARCHAR(1000),
    created_at                  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_store_reg_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

CREATE INDEX IF NOT EXISTS idx_store_reg_user_id ON store_registrations(user_id);
CREATE INDEX IF NOT EXISTS idx_store_reg_status ON store_registrations(status);
