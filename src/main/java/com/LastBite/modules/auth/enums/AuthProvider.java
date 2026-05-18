package com.LastBite.modules.auth.enums;

/**
 * Authentication provider — tracks HOW a user registered/logs in.
 * <p>
 * Designed for extensibility: start with LOCAL (email+password),
 * then add GOOGLE, ZALO without schema changes.
 */
public enum AuthProvider {

    /** Email + password authentication. */
    LOCAL,

    /** Google OAuth2 (future). */
    GOOGLE,

    /** Zalo OAuth / SMS OTP (future). */
    ZALO
}
