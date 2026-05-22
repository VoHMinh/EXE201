package com.LastBite.modules.user.controller;

import com.LastBite.common.response.ApiResponse;
import com.LastBite.modules.auth.dto.response.UserResponse;
import com.LastBite.modules.user.dto.request.AddressRequest;
import com.LastBite.modules.user.dto.request.ChangePasswordRequest;
import com.LastBite.modules.user.dto.request.UpdateProfileRequest;
import com.LastBite.modules.user.dto.response.AddressResponse;
import com.LastBite.modules.user.service.AddressService;
import com.LastBite.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Hồ sơ người dùng", description = "Quản lý hồ sơ cá nhân và địa chỉ")
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    // ── Profile ──

    @GetMapping("/me")
    @Operation(summary = "Lấy hồ sơ cá nhân")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(userId)));
    }

    @PutMapping("/me")
    @Operation(summary = "Cập nhật hồ sơ cá nhân")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(userId, request), "Cập nhật thành công"));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Đổi mật khẩu")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = extractUserId(jwt);
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── Addresses ──

    @GetMapping("/me/addresses")
    @Operation(summary = "Danh sách địa chỉ giao hàng")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(ApiResponse.ok(addressService.getAddresses(userId)));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Thêm địa chỉ mới")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddressRequest request) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(addressService.addAddress(userId, request), "Thêm địa chỉ thành công"));
    }

    @PutMapping("/me/addresses/{addressId}")
    @Operation(summary = "Cập nhật địa chỉ")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest request) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(ApiResponse.ok(addressService.updateAddress(userId, addressId, request)));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @Operation(summary = "Xóa địa chỉ")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID addressId) {
        UUID userId = extractUserId(jwt);
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/me/addresses/{addressId}/default")
    @Operation(summary = "Đặt địa chỉ mặc định")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID addressId) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(ApiResponse.ok(addressService.setDefault(userId, addressId)));
    }

    private UUID extractUserId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("user_id"));
    }
}
