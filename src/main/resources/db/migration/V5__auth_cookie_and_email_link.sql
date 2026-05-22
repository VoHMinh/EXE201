-- V5 - Auth hardening
-- - email_verification_tokens supports either OTP or one-time link token
-- - access token duration is controlled by config; refresh token is moved to httpOnly cookie at API layer

ALTER TABLE email_verification_tokens
    ALTER COLUMN otp_code DROP NOT NULL;

ALTER TABLE email_verification_tokens
    ADD COLUMN IF NOT EXISTS token_hash VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uq_email_verify_token_hash
    ON email_verification_tokens(token_hash)
    WHERE token_hash IS NOT NULL;

