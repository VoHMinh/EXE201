package com.LastBite.modules.auth.service;

import com.LastBite.common.exception.ApiException;
import com.LastBite.common.exception.ErrorCode;
import com.LastBite.common.service.EmailService;
import com.LastBite.modules.auth.dto.request.LoginRequest;
import com.LastBite.modules.auth.dto.request.RegisterPartnerRequest;
import com.LastBite.modules.auth.dto.request.RegisterRequest;
import com.LastBite.modules.auth.dto.request.ResendOtpRequest;
import com.LastBite.modules.auth.dto.request.VerifyEmailRequest;
import com.LastBite.modules.auth.dto.response.AuthResponse;
import com.LastBite.modules.auth.dto.response.UserResponse;
import com.LastBite.modules.auth.entity.EmailVerificationToken;
import com.LastBite.modules.auth.entity.User;
import com.LastBite.modules.auth.enums.AuthProvider;
import com.LastBite.modules.auth.enums.UserRole;
import com.LastBite.modules.auth.enums.UserStatus;
import com.LastBite.modules.auth.repository.EmailVerificationTokenRepository;
import com.LastBite.modules.auth.repository.UserRepository;
import com.LastBite.modules.store.dto.request.CreateStoreRequest;
import com.LastBite.modules.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
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
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final EmailService emailService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_EXPIRY_MINUTES = 10;

    /**
     * Register a new CUSTOMER account with email + password.
     * <p>
     * Does NOT return tokens — user must verify email first via OTP.
     * Sends an OTP code to the provided email address.
     */
    @Transactional
    public void register(RegisterRequest request) {
        // Check duplicates
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ApiException(ErrorCode.EMAIL_EXISTS);
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new ApiException(ErrorCode.PHONE_EXISTS);
        }

        // Create user (emailVerified = false)
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
        log.info("New user registered: {} ({}) — awaiting OTP verification", user.getEmail(), user.getId());

        // Generate and send OTP
        sendOtp(user);
    }

    /**
     * Register a new STORE_OWNER account + create the Store in one step.
     * <p>
     * Does NOT return tokens — user must verify email first via OTP.
     */
    @Transactional
    public void registerPartner(RegisterPartnerRequest request) {
        // Check duplicates
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
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
        storeReq.setDistrict(request.getStoreDistrict());
        storeReq.setCity(request.getStoreCity());
        storeReq.setLat(request.getLat());
        storeReq.setLng(request.getLng());
        storeReq.setCoverImageUrl(request.getCoverImageUrl());
        storeReq.setLogoUrl(request.getLogoUrl());
        storeReq.setBusinessLicenseNumber(request.getBusinessLicenseNumber());
        storeReq.setBusinessLicenseImageUrl(request.getBusinessLicenseImageUrl());

        storeService.createStoreInternal(user, storeReq);

        log.info("New partner registered: {} ({}) with store: {} — awaiting OTP verification",
                user.getEmail(), user.getId(), request.getStoreName());

        // Generate and send OTP
        sendOtp(user);
    }

    /**
     * Verify email with OTP code.
     * <p>
     * On success: marks user as emailVerified, returns access + refresh tokens.
     */
    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        EmailVerificationToken token = emailTokenRepository
                .findLatestValidByUserId(user.getId(), Instant.now())
                .orElseThrow(() -> new ApiException(ErrorCode.OTP_EXPIRED));

        if (token.hasMaxAttempts()) {
            throw new ApiException(ErrorCode.OTP_MAX_ATTEMPTS);
        }

        if (!token.getOtpCode().equals(request.getOtpCode())) {
            token.setAttempts(token.getAttempts() + 1);
            emailTokenRepository.save(token);
            throw new ApiException(ErrorCode.OTP_INVALID);
        }

        // OTP is correct — mark verified
        token.setVerified(true);
        emailTokenRepository.save(token);

        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    /**
     * Resend OTP to user's email. Deletes previous unverified tokens first.
     */
    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        sendOtp(user);
    }

    /**
     * Login with email + password.
     * <p>
     * Rejects login if email is not verified (LOCAL users only).
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

        // Check email verification (LOCAL users must verify email)
        if (user.getAuthProvider() == AuthProvider.LOCAL && !user.isEmailVerified()) {
            throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        log.info("User logged in: {}", user.getEmail());
        user.setLastLoginAt(Instant.now());
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
        // If token was already revoked (reuse), all sessions are killed automatically
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

    private void sendOtp(User user) {
        // Delete any previous unverified tokens
        emailTokenRepository.deleteUnverifiedByUserId(user.getId());

        // Generate 6-digit OTP
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .otpCode(otp)
                .expiresAt(Instant.now().plusSeconds(OTP_EXPIRY_MINUTES * 60L))
                .build();
        emailTokenRepository.save(token);

        // Send email asynchronously
        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
    }

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
