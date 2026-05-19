package com.LastBite.modules.auth.service;

import com.LastBite.common.exception.ApiException;
import com.LastBite.common.exception.ErrorCode;
import com.LastBite.modules.auth.dto.request.LoginRequest;
import com.LastBite.modules.auth.dto.request.RegisterPartnerRequest;
import com.LastBite.modules.auth.dto.request.RegisterRequest;
import com.LastBite.modules.auth.dto.response.AuthResponse;
import com.LastBite.modules.auth.dto.response.UserResponse;
import com.LastBite.modules.auth.entity.User;
import com.LastBite.modules.auth.enums.AuthProvider;
import com.LastBite.modules.auth.enums.UserRole;
import com.LastBite.modules.auth.enums.UserStatus;
import com.LastBite.modules.auth.repository.UserRepository;
import com.LastBite.modules.store.dto.request.CreateStoreRequest;
import com.LastBite.modules.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final StoreService storeService;

    /**
     * Register a new CUSTOMER account with email + password.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check duplicates
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.EMAIL_EXISTS);
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new ApiException(ErrorCode.PHONE_EXISTS);
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} ({})", user.getEmail(), user.getId());

        return buildAuthResponse(user);
    }

    /**
     * Register a new STORE_OWNER account + create the Store in one step.
     * <p>
     * Flow: Đăng ký tài khoản đối tác → tạo user (role = STORE_OWNER) + tạo Store (PENDING).
     * Store chỉ hiện lên app khi admin duyệt (verification = VERIFIED).
     */
    @Transactional
    public AuthResponse registerPartner(RegisterPartnerRequest request) {
        // Check duplicates
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.EMAIL_EXISTS);
        }
        if (request.getOwnerPhone() != null && userRepository.existsByPhone(request.getOwnerPhone())) {
            throw new ApiException(ErrorCode.PHONE_EXISTS);
        }

        // 1. Create user with STORE_OWNER role
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getOwnerPhone())
                .role(UserRole.STORE_OWNER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        user = userRepository.save(user);

        // 2. Create store for this owner
        CreateStoreRequest storeReq = new CreateStoreRequest();
        storeReq.setName(request.getStoreName());
        storeReq.setDescription(request.getStoreDescription());
        storeReq.setCategory(request.getStoreCategory());
        storeReq.setPhone(request.getStorePhone());
        storeReq.setEmail(request.getStoreEmail());
        storeReq.setAddress(request.getStoreAddress());
        storeReq.setLat(request.getLat());
        storeReq.setLng(request.getLng());
        storeReq.setCoverImageUrl(request.getCoverImageUrl());
        storeReq.setLogoUrl(request.getLogoUrl());
        storeReq.setBusinessLicenseNumber(request.getBusinessLicenseNumber());
        storeReq.setBusinessLicenseImageUrl(request.getBusinessLicenseImageUrl());

        storeService.createStoreInternal(user, storeReq);

        log.info("New partner registered: {} ({}) with store: {}",
                user.getEmail(), user.getId(), request.getStoreName());

        return buildAuthResponse(user);
    }

    /**
     * Login with email + password.
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));

        // Check password (only for LOCAL auth)
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Check account status
        if (user.getStatus() == UserStatus.BANNED) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new ApiException(ErrorCode.ACCOUNT_DISABLED);
        }

        log.info("User logged in: {}", user.getEmail());
        user.setLastLoginAt(java.time.Instant.now());
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    /**
     * Refresh access token using a valid refresh token.
     * Implements Token Rotation: old token revoked, new token issued.
     */
    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        // 1. Validate old refresh token (checks Redis first, then DB)
        UUID userId = refreshTokenService.validateAndGetUserId(rawRefreshToken);

        // 2. Revoke old token (Token Rotation)
        refreshTokenService.revokeToken(rawRefreshToken);

        // 3. Load user and issue new tokens
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.ACCOUNT_DISABLED);
        }

        log.debug("Token refreshed for user: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    /**
     * Logout — revoke the specific refresh token.
     */
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeToken(rawRefreshToken);
    }

    /**
     * Logout from ALL devices — revoke all refresh tokens.
     */
    @Transactional
    public void logoutAll(UUID userId) {
        refreshTokenService.revokeAllByUserId(userId);
    }

    /**
     * Get current user profile from JWT claims.
     */
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return toUserResponse(user);
    }

    // ── Private helpers ──────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenDuration())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .authProvider(user.getAuthProvider())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
