package com.LastBite.modules.auth.service;

import com.LastBite.common.exception.ApiException;
import com.LastBite.common.exception.ErrorCode;
import com.LastBite.modules.auth.dto.response.AuthResponse;
import com.LastBite.modules.auth.entity.User;
import com.LastBite.modules.auth.enums.AuthProvider;
import com.LastBite.modules.auth.enums.UserRole;
import com.LastBite.modules.auth.enums.UserStatus;
import com.LastBite.modules.auth.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;

/**
 * Google OAuth2 authentication service.
 * <p>
 * Flow:
 * <ol>
 *   <li>FE uses Google Sign-In SDK → gets an {@code id_token}</li>
 *   <li>FE sends {@code id_token} to {@code POST /api/v1/auth/google}</li>
 *   <li>BE verifies token with Google → creates/logs in user</li>
 *   <li>BE returns JWT access + refresh token</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.google.client-id:}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    void init() {
        if (googleClientId != null && !googleClientId.isBlank()) {
            verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            log.info("GoogleAuthService initialised with clientId: {}...{}",
                    googleClientId.substring(0, Math.min(8, googleClientId.length())),
                    googleClientId.substring(Math.max(0, googleClientId.length() - 4)));
        } else {
            log.warn("Google OAuth is DISABLED — app.google.client-id is not set");
        }
    }

    /**
     * Authenticate with a Google {@code id_token}.
     * <p>
     * If the user doesn't exist, creates a new CUSTOMER account with
     * {@code emailVerified = true} (Google already verified the email).
     */
    @Transactional
    public AuthResponse authenticateWithGoogle(String idTokenString) {
        if (verifier == null) {
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Google OAuth chưa được cấu hình");
        }

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            log.warn("Google token verification failed: {}", e.getMessage());
            throw new ApiException(ErrorCode.TOKEN_INVALID, "Google ID token không hợp lệ");
        }

        if (idToken == null) {
            throw new ApiException(ErrorCode.TOKEN_INVALID, "Google ID token không hợp lệ hoặc đã hết hạn");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String fullName = (String) payload.get("name");
        String avatarUrl = (String) payload.get("picture");

        if (email == null || email.isBlank()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Google token không chứa email");
        }

        // Find or create user
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .map(existingUser -> handleExistingUser(existingUser, avatarUrl))
                .orElseGet(() -> createGoogleUser(email, fullName, avatarUrl));

        return buildAuthResponse(user);
    }

    private User handleExistingUser(User user, String avatarUrl) {
        if (user.getStatus() == UserStatus.BANNED) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new ApiException(ErrorCode.ACCOUNT_DISABLED);
        }

        // Update avatar if user doesn't have one
        if (user.getAvatarUrl() == null && avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        // Mark email as verified (Google confirmed it)
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
        }
        user.setLastLoginAt(Instant.now());
        return userRepository.save(user);
    }

    private User createGoogleUser(String email, String fullName, String avatarUrl) {
        User user = User.builder()
                .email(email.toLowerCase().trim())
                .fullName(fullName != null ? fullName : email.split("@")[0])
                .avatarUrl(avatarUrl)
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.GOOGLE)
                .emailVerified(true)   // Google already verified
                .phoneVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("New Google user created: {} ({})", user.getEmail(), user.getId());
        return user;
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
}
