package com.LastBite.modules.store.controller;

import com.LastBite.common.response.ApiResponse;
import com.LastBite.modules.store.dto.response.StoreDetailResponse;
import com.LastBite.modules.store.dto.response.StoreResponse;
import com.LastBite.modules.store.enums.StoreCategory;
import com.LastBite.modules.store.service.StoreQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Tag(name = "Store (Public)", description = "Tìm kiếm cửa hàng — không cần đăng nhập")
public class StorePublicController {

    private final StoreQueryService storeQueryService;

    @GetMapping
    @Operation(summary = "Tìm kiếm cửa hàng (keyword, category, phân trang)")
    public ResponseEntity<ApiResponse<Page<StoreResponse>>> searchStores(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) StoreCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(ApiResponse.ok(storeQueryService.searchStores(keyword, category, pageable)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Xem chi tiết cửa hàng theo slug")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStoreBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(storeQueryService.getStoreBySlug(slug)));
    }
}
