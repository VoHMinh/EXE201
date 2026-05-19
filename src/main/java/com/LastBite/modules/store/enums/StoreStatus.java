package com.LastBite.modules.store.enums;

/**
 * Operational status of a store.
 */
public enum StoreStatus {
    /** Store is open and operational. */
    ACTIVE,
    /** Temporarily paused by the owner. */
    PAUSED,
    /** Permanently closed by the owner. */
    CLOSED,
    /** Suspended by admin (policy violation). */
    SUSPENDED
}
