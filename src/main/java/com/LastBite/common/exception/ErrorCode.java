package com.LastBite.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi tập trung với <b>các dải mã duy nhất, không chồng lấn</b>.
 * <p>
 * Mỗi dải mã được dành cho một domain cụ thể để tránh trùng mã lỗi.
 *
 * <pre>
 *  1000        → Success
 *  4000–4009   → Validation / Input errors
 *  4010–4019   → Authentication errors
 *  4030–4039   → Authorization / Forbidden errors
 *  4040–4059   → Not Found errors
 *  4090–4099   → Conflict / Duplicate errors
 *  4150        → Unsupported Media Type
 *  4290        → Rate Limiting
 *  4999        → Generic client error
 *  5000+       → Server errors
 * </pre>
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── Success ──────────────────────────────────
    SUCCESS(1000, "Thành công", HttpStatus.OK),

    // ── 400: Validation / Input ──────────────────
    VALIDATION_ERROR(4000, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    MALFORMED_JSON(4001, "JSON request không đúng định dạng", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(4002, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(4003, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_ENUM_VALUE(4004, "Giá trị enum không hợp lệ", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD(4005, "Thiếu trường bắt buộc", HttpStatus.BAD_REQUEST),
    INVALID_DATE_FORMAT(4006, "Định dạng ngày không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_PAGINATION(4007, "Tham số phân trang không hợp lệ (page >= 0, size 1-100)", HttpStatus.BAD_REQUEST),
    INVALID_SORT_FIELD(4008, "Không thể sắp xếp theo trường này", HttpStatus.BAD_REQUEST),
    REQUEST_FAILED(4009, "Yêu cầu thất bại", HttpStatus.BAD_REQUEST),

    // ── 401: Authentication ──────────────────────
    UNAUTHENTICATED(4010, "Chưa xác thực — vui lòng đăng nhập", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(4011, "Sai thông tin đăng nhập", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(4012, "Token đã hết hạn", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(4013, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(4014, "Tài khoản đã bị khóa", HttpStatus.UNAUTHORIZED),
    TOKEN_REUSE_DETECTED(4015, "Phát hiện token tái sử dụng — tất cả phiên đã bị huỷ", HttpStatus.UNAUTHORIZED),
    OTP_EXPIRED(4016, "Mã OTP đã hết hạn — vui lòng yêu cầu mã mới", HttpStatus.UNAUTHORIZED),
    OTP_INVALID(4017, "Mã OTP không đúng", HttpStatus.UNAUTHORIZED),
    OTP_MAX_ATTEMPTS(4018, "Vượt quá số lần thử OTP — vui lòng yêu cầu mã mới", HttpStatus.UNAUTHORIZED),

    // ── 403: Authorization ───────────────────────
    FORBIDDEN(4030, "Không có quyền truy cập", HttpStatus.FORBIDDEN),
    ACCOUNT_NOT_ACTIVATED(4031, "Tài khoản chưa được kích hoạt", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED(4032, "Tài khoản đã bị vô hiệu hóa", HttpStatus.FORBIDDEN),
    EMAIL_ALREADY_VERIFIED(4033, "Email đã được xác minh", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED(4034, "Email chưa được xác minh — vui lòng xác minh email trước", HttpStatus.FORBIDDEN),

    // ── 404: Not Found ───────────────────────────
    RESOURCE_NOT_FOUND(4040, "Không tìm thấy tài nguyên", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(4041, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    STORE_NOT_FOUND(4042, "Không tìm thấy cửa hàng", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(4043, "Không tìm thấy sản phẩm", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(4044, "Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND),

    // ── 409: Conflict ────────────────────────────
    DUPLICATE_RESOURCE(4090, "Dữ liệu đã tồn tại", HttpStatus.CONFLICT),
    EMAIL_EXISTS(4091, "Email đã được đăng ký", HttpStatus.CONFLICT),
    PHONE_EXISTS(4092, "Số điện thoại đã được đăng ký", HttpStatus.CONFLICT),
    USERNAME_EXISTS(4093, "Tên đăng nhập đã tồn tại", HttpStatus.CONFLICT),

    // ── 415 / 429 ────────────────────────────────
    UNSUPPORTED_MEDIA_TYPE(4150, "Loại media không được hỗ trợ", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    TOO_MANY_REQUESTS(4290, "Quá nhiều yêu cầu — vui lòng thử lại sau", HttpStatus.TOO_MANY_REQUESTS),

    // ── 500: Server ──────────────────────────────
    UNEXPECTED_ERROR(5000, "Lỗi hệ thống — vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(5003, "Dịch vụ tạm thời không khả dụng", HttpStatus.SERVICE_UNAVAILABLE);

    private final int code;
    private final String defaultMessage;
    private final HttpStatus status;
}
