package com.LastBite.modules.auth.enums;

/**
 * Trạng thái hồ sơ đăng ký cửa hàng.
 * <p>
 * Luồng: PENDING → APPROVED / REJECTED
 */
public enum StoreRegistrationStatus {

    /** Hồ sơ đã gửi, đang chờ admin duyệt. */
    PENDING,

    /** Admin đã duyệt — người dùng được cấp ROLE_STORE_OWNER. */
    APPROVED,

    /** Admin từ chối — người dùng giữ ROLE_CUSTOMER. */
    REJECTED
}
