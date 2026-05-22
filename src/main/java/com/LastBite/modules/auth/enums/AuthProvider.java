package com.LastBite.modules.auth.enums;

/**
 * Nhà cung cấp xác thực — ghi nhận người dùng đăng ký/đăng nhập bằng cách nào.
 * <p>
 * Thiết kế để dễ mở rộng: bắt đầu với LOCAL (email + mật khẩu),
 * sau đó thêm GOOGLE, ZALO mà không cần đổi schema.
 */
public enum AuthProvider {

    /** Xác thực bằng email + mật khẩu. */
    LOCAL,

    /** Google OAuth2. */
    GOOGLE,

    /** Zalo OAuth / SMS OTP. */
    ZALO
}
