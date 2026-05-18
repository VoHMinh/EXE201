package com.LastBite.modules.auth.controller;

import com.LastBite.common.response.ApiResponse;
import com.LastBite.modules.auth.dto.request.LoginRequest;
import com.LastBite.modules.auth.dto.request.RefreshTokenRequest;
import com.LastBite.modules.auth.dto.request.RegisterRequest;
import com.LastBite.modules.auth.dto.response.AuthResponse;
import com.LastBite.modules.auth.dto.response.UserResponse;
import com.LastBite.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng ký, đăng nhập, quản lý token")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản Customer")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.register(request), "Đăng ký thành công"));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng email + password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request), "Đăng nhập thành công"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token bằng refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(authService.refresh(request.getRefreshToken()), "Làm mới token thành công"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất (thu hồi refresh token)")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Đăng xuất tất cả thiết bị")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("user_id"));
        authService.logoutAll(userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin user hiện tại")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("user_id"));
        return ResponseEntity.ok(ApiResponse.ok(authService.getCurrentUser(userId)));
    }
}
