package com.LastBite.modules.auth.enums;

/**
 * Account lifecycle status.
 */
public enum UserStatus {

    /** Account is fully active and can use all features. */
    ACTIVE,

    /** Account has been temporarily deactivated (by user or admin). */
    INACTIVE,

    /** Account has been permanently banned by admin. */
    BANNED
}
