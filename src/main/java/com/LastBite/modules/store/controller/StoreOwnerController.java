package com.LastBite.modules.store.controller;

import com.LastBite.common.response.ApiResponse;
import com.LastBite.modules.store.dto.request.CreateStoreRequest;
import com.LastBite.modules.store.dto.request.ScheduleRequest;
import com.LastBite.modules.store.dto.request.UpdateStoreRequest;
import com.LastBite.modules.store.dto.response.StoreDetailResponse;
import com.LastBite.modules.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/store-owner/store")
@RequiredArgsConstructor
@Tag(name = "Store Owner", description = "Quản lý cửa hàng (dành cho chủ cửa hàng)")
public class StoreOwnerController {

    private final StoreService storeService;

    @PostMapping
    @Operation(summary = "Tạo cửa hàng mới (tự động chuyển role sang STORE_OWNER)")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> createStore(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateStoreRequest request) {
        UUID ownerId = extractUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(storeService.createStore(ownerId, request), "Tạo cửa hàng thành công — chờ duyệt"));
    }

    @GetMapping
    @Operation(summary = "Xem cửa hàng của tôi")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getMyStore(@AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = extractUserId(jwt);
        return ResponseEntity.ok(ApiResponse.ok(storeService.getMyStore(ownerId)));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('STORE_OWNER')")
    @Operation(summary = "Cập nhật thông tin cửa hàng")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> updateStore(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateStoreRequest request) {
        UUID ownerId = extractUserId(jwt);
        return ResponseEntity.ok(ApiResponse.ok(storeService.updateStore(ownerId, request), "Cập nhật thành công"));
    }

    @PutMapping("/schedules")
    @PreAuthorize("hasAuthority('STORE_OWNER')")
    @Operation(summary = "Cập nhật lịch mở/đóng cửa")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> updateSchedules(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody List<ScheduleRequest> requests) {
        UUID ownerId = extractUserId(jwt);
        return ResponseEntity.ok(ApiResponse.ok(storeService.updateSchedules(ownerId, requests)));
    }

    @PatchMapping("/pause")
    @PreAuthorize("hasAuthority('STORE_OWNER')")
    @Operation(summary = "Tạm ngưng cửa hàng")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> pauseStore(@AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = extractUserId(jwt);
        return ResponseEntity.ok(ApiResponse.ok(storeService.pauseStore(ownerId), "Cửa hàng đã tạm ngưng"));
    }

    @PatchMapping("/activate")
    @PreAuthorize("hasAuthority('STORE_OWNER')")
    @Operation(summary = "Mở lại cửa hàng")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> activateStore(@AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = extractUserId(jwt);
        return ResponseEntity.ok(ApiResponse.ok(storeService.activateStore(ownerId), "Cửa hàng đã hoạt động lại"));
    }

    private UUID extractUserId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("user_id"));
    }
}
