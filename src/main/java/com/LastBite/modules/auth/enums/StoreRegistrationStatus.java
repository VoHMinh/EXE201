package com.LastBite.modules.auth.enums;

/**
 * Store registration application status.
 * <p>
 * Flow: PENDING → APPROVED / REJECTED
 */
public enum StoreRegistrationStatus {

    /** Application submitted, waiting for admin review. */
    PENDING,

    /** Admin approved — user granted ROLE_STORE_OWNER. */
    APPROVED,

    /** Admin rejected — user remains ROLE_CUSTOMER. */
    REJECTED
}
