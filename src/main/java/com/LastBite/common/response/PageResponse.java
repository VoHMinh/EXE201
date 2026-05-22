package com.LastBite.common.response;

import java.util.List;

/**
 * Wrapper response phân trang.
 *
 * @param content       items on the current page
 * @param page          zero-based page index
 * @param size          page size
 * @param totalElements total number of matching items
 * @param totalPages    total number of pages
 * @param <T>           element type
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
