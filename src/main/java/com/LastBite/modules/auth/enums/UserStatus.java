package com.LastBite.modules.auth.enums;

/**
 * Trạng thái vòng đời tài khoản.
 */
public enum UserStatus {

    /** Tài khoản đang hoạt động và có thể dùng đầy đủ tính năng. */
    ACTIVE,

    /** Tài khoản tạm thời bị vô hiệu hóa bởi người dùng hoặc admin. */
    INACTIVE,

    /** Tài khoản bị admin cấm vĩnh viễn. */
    BANNED
}
