package com.LastBite.modules.store.enums;

/**
 * Admin verification status for a store.
 * <p>
 * Flow: PENDING → VERIFIED (appears on app) / REJECTED (with reason)
 */
public enum VerificationStatus {
    /** Awaiting admin review. */
    PENDING,
    /** Approved — store is visible to customers. */
    VERIFIED,
    /** Rejected — owner can edit and resubmit. */
    REJECTED
}
