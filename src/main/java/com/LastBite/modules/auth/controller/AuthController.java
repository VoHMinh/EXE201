package com.LastBite.modules.auth.controller;

import com.LastBite.common.response.ApiResponse;
import com.LastBite.modules.auth.dto.request.*;
import com.LastBite.modules.auth.dto.response.AuthResponse;
import com.LastBite.modules.auth.dto.response.UserResponse;
import com.LastBite.modules.auth.service.AuthService;
import com.LastBite.modules.auth.service.GoogleAuthService;
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
@Tag(name = "Authentication", description = "Đăng ký, đăng nhập, xác minh email, Google OAuth, quản lý token")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    // ── Registration ─────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản Customer — gửi OTP qua email, chưa trả token")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Đăng ký thành công — vui lòng kiểm tra email để nhận mã OTP"));
    }

    @PostMapping("/register-partner")
    @Operation(summary = "Đăng ký tài khoản Đối tác — tạo tài khoản + cửa hàng, gửi OTP qua email")
    public ResponseEntity<ApiResponse<Void>> registerPartner(
            @Valid @RequestBody RegisterPartnerRequest request) {
        authService.registerPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null,
                        "Đăng ký đối tác thành công — vui lòng kiểm tra email để nhận mã OTP"));
    }

    // ── Email Verification (OTP) ─────────────────

    @PostMapping("/verify-email")
    @Operation(summary = "Xác minh email bằng mã OTP — thành công sẽ trả access + refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(authService.verifyEmail(request), "Xác minh email thành công"));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Gửi lại mã OTP xác minh email")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đã gửi lại mã OTP — vui lòng kiểm tra email"));
    }

    // ── Login ────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng email + password (yêu cầu đã xác minh email)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request), "Đăng nhập thành công"));
    }

    @PostMapping("/google")
    @Operation(summary = "Đăng nhập/Đăng ký bằng Google — gửi id_token từ Google Sign-In SDK")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(
            @Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(googleAuthService.authenticateWithGoogle(request.getIdToken()),
                        "Đăng nhập Google thành công"));
    }

    // ── Token Management ─────────────────────────

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token bằng refresh token (Token Rotation + Reuse Detection)")
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

    // ── Profile ──────────────────────────────────

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin user hiện tại")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("user_id"));
        return ResponseEntity.ok(ApiResponse.ok(authService.getCurrentUser(userId)));
    }
}
