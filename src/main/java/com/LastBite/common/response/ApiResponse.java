package com.LastBite.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Bao response API thống nhất.
 * <p>
 * Mọi endpoint trả về wrapper này để front-end luôn biết nơi lấy payload
 * ({@code result}), trạng thái ({@code code / message}) và lỗi validation
 * ({@code errors}).
 *
 * @param <T> the type of the response payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** Mã trạng thái cấp ứng dụng (1000 = thành công). */
    @Builder.Default
    private int code = 1000;

    /** Thông báo trạng thái dễ đọc. */
    private String message;

    /** Payload thật của response. */
    private T result;

    /** Lỗi validation theo từng field (field → message). */
    private Map<String, String> errors;

    /** Thời điểm server tạo response. */
    private Instant timestamp;

    /** Path request tạo ra response này. */
    private String path;

    // ── Convenience factory methods ────────────────

    public static <T> ApiResponse<T> ok(T result) {
        return ApiResponse.<T>builder()
                .code(1000)
                .message("Success")
                .result(result)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> ok(T result, String message) {
        return ApiResponse.<T>builder()
                .code(1000)
                .message(message)
                .result(result)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiResponse<Void> ok() {
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Success")
                .timestamp(Instant.now())
                .build();
    }
}
