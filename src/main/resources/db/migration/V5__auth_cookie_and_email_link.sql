-- V5 - Gia cố auth
-- - email_verification_tokens hỗ trợ OTP hoặc token link một lần
-- - thời hạn access token do config quản lý; refresh token chuyển sang httpOnly cookie ở tầng API

ALTER TABLE email_verification_tokens
    ALTER COLUMN otp_code DROP NOT NULL;

ALTER TABLE email_verification_tokens
    ADD COLUMN IF NOT EXISTS token_hash VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uq_email_verify_token_hash
    ON email_verification_tokens(token_hash)
    WHERE token_hash IS NOT NULL;
