package com.LastBite.modules.auth.enums;

/**
 * Các role người dùng trong toàn hệ thống.
 * <p>
 * Lưu dưới dạng VARCHAR trong database để dễ đọc.
 */
public enum UserRole {

    /** Khách hàng xem và mua Surprise Bag. */
    CUSTOMER,

    /** Chủ cửa hàng đăng bán Surprise Bag. */
    STORE_OWNER,

    /** Đội vận hành LastBite — có toàn quyền hệ thống. */
    ADMIN
}
