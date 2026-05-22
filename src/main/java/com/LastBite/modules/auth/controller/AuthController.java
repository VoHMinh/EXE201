package com.LastBite.modules.auth.controller;

import com.LastBite.common.exception.ApiException;
import com.LastBite.common.exception.ErrorCode;
import com.LastBite.common.response.ApiResponse;
import com.LastBite.modules.auth.dto.request.GoogleAuthRequest;
import com.LastBite.modules.auth.dto.request.LoginRequest;
import com.LastBite.modules.auth.dto.request.RefreshTokenRequest;
import com.LastBite.modules.auth.dto.request.RegisterPartnerRequest;
import com.LastBite.modules.auth.dto.request.RegisterRequest;
import com.LastBite.modules.auth.dto.request.ResendOtpRequest;
import com.LastBite.modules.auth.dto.request.VerifyEmailRequest;
import com.LastBite.modules.auth.dto.response.AuthResponse;
import com.LastBite.modules.auth.dto.response.UserResponse;
import com.LastBite.modules.auth.service.AuthService;
import com.LastBite.modules.auth.service.GoogleAuthService;
import com.LastBite.modules.auth.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, email verification, Google OAuth, token management")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;

    @Value("${app.auth.refresh-cookie-secure:false}")
    private boolean refreshCookieSecure;

    @Value("${app.auth.refresh-cookie-same-site:Lax}")
    private String refreshCookieSameSite;

    @PostMapping("/register")
    @Operation(summary = "Register a customer account and send an email verification link")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Dang ky thanh cong - vui long kiem tra email de xac minh tai khoan"));
    }

    @PostMapping("/register-partner")
    @Operation(summary = "Register a partner account, create store, and send an email verification link")
    public ResponseEntity<ApiResponse<Void>> registerPartner(@Valid @RequestBody RegisterPartnerRequest request) {
        authService.registerPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Dang ky doi tac thanh cong - vui long kiem tra email de xac minh tai khoan"));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email by OTP; reserved for high-risk flows")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return authResponse(authService.verifyEmail(request), "Xac minh email thanh cong");
    }

    @GetMapping("/verify-email-link")
    @Operation(summary = "Verify email by one-time link; used by registration flows")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmailLink(@RequestParam String token) {
        return authResponse(authService.verifyEmailLink(token), "Xac minh email thanh cong");
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Send a fresh OTP verification code")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Da gui lai ma OTP - vui long kiem tra email"));
    }

    @PostMapping("/resend-verification-link")
    @Operation(summary = "Send a fresh email verification link")
    public ResponseEntity<ApiResponse<Void>> resendVerificationLink(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendVerificationLink(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Da gui lai link xac minh - vui long kiem tra email"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authResponse(authService.login(request), "Dang nhap thanh cong");
    }

    @PostMapping("/google")
    @Operation(summary = "Login or register with Google id_token")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        return authResponse(googleAuthService.authenticateWithGoogle(request.getIdToken()),
                "Dang nhap Google thanh cong");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token with httpOnly refresh cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshTokenRequest request) {
        return authResponse(authService.refresh(resolveRefreshToken(cookieRefreshToken, request)),
                "Lam moi token thanh cong");
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke current refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshTokenRequest request) {
        authService.logout(resolveRefreshToken(cookieRefreshToken, request));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.ok());
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("user_id"));
        authService.logoutAll(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.ok());
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("user_id"));
        return ResponseEntity.ok(ApiResponse.ok(authService.getCurrentUser(userId)));
    }

    private ResponseEntity<ApiResponse<AuthResponse>> authResponse(AuthResponse auth, String message) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(auth.getRefreshToken()).toString())
                .body(ApiResponse.ok(auth.withoutRefreshToken(), message));
    }

    private String resolveRefreshToken(String cookieRefreshToken, RefreshTokenRequest request) {
        if (cookieRefreshToken != null && !cookieRefreshToken.isBlank()) {
            return cookieRefreshToken;
        }
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            return request.getRefreshToken();
        }
        throw new ApiException(ErrorCode.TOKEN_INVALID, "Thieu refresh token");
    }

    private ResponseCookie refreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/v1/auth")
                .maxAge(Duration.ofSeconds(jwtService.getRefreshTokenDuration()))
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
    }
}
