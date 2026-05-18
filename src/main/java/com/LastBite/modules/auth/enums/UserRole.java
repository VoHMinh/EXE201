package com.LastBite.modules.auth.enums;

/**
 * System-wide user roles.
 * <p>
 * Stored as VARCHAR in the database for readability.
 */
public enum UserRole {

    /** End-user who browses and purchases Surprise Bags. */
    CUSTOMER,

    /** Store owner (restaurant, bakery, supermarket) who lists Surprise Bags. */
    STORE_OWNER,

    /** LastBite operations team — full system access. */
    ADMIN
}
