package com.LastBite.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Unified API response envelope.
 * <p>
 * Every endpoint returns this wrapper so the front-end always knows where to
 * find the payload ({@code result}), status ({@code code / message}), and
 * validation errors ({@code errors}).
 *
 * @param <T> the type of the response payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** Application-level status code (1000 = success). */
    @Builder.Default
    private int code = 1000;

    /** Human-readable status message. */
    private String message;

    /** The actual response payload. */
    private T result;

    /** Field-level validation errors (field → message). */
    private Map<String, String> errors;

    /** Server timestamp of the response. */
    private Instant timestamp;

    /** Request path that produced this response. */
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
