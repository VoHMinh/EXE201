package com.LastBite.common.config;

import com.LastBite.modules.auth.entity.User;
import com.LastBite.modules.auth.enums.AuthProvider;
import com.LastBite.modules.auth.enums.UserRole;
import com.LastBite.modules.auth.enums.UserStatus;
import com.LastBite.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * Seeds a default ADMIN account on application startup if it does not exist.
 * <p>
 * Credentials are read from environment variables / application config so they
 * are never hard-coded in source. Falls back to sensible defaults for local dev.
 * <p>
 * <b>IMPORTANT:</b> Change the default password immediately in production by
 * setting {@code ADMIN_PASSWORD} env var.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${app.admin.email:admin@lastbite.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123456}")
    private String adminPassword;

    @Value("${app.admin.full-name:LastBite Admin}")
    private String adminFullName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (isProdProfile() && "Admin@123456".equals(adminPassword)) {
            throw new IllegalStateException("ADMIN_PASSWORD must be set in production");
        }

        try {
            if (userRepository.existsByEmail(adminEmail)) {
                log.info("Admin account already exists: {}", adminEmail);
                return;
            }

            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .fullName(adminFullName)
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .authProvider(AuthProvider.LOCAL)
                    .emailVerified(true)
                    .phoneVerified(false)
                    .build();

            userRepository.save(admin);
            log.info("Default admin account created: {} (role=ADMIN)", adminEmail);
        } catch (Exception e) {
            log.warn("Admin seeder skipped due to error (app continues normally): {}", e.getMessage());
        }
    }

    private boolean isProdProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}
