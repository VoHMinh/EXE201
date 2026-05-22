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
 * Tạo tài khoản ADMIN mặc định khi ứng dụng khởi động nếu chưa tồn tại.
 * <p>
 * Thông tin đăng nhập được đọc từ biến môi trường / cấu hình ứng dụng để không
 * hard-code trong source. Có fallback tiện dụng cho môi trường local dev.
 * <p>
 * <b>QUAN TRỌNG:</b> Khi chạy production, bắt buộc đổi mật khẩu mặc định bằng
 * biến môi trường {@code ADMIN_PASSWORD}.
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
            throw new IllegalStateException("Bắt buộc cấu hình ADMIN_PASSWORD trong production");
        }

        try {
            if (userRepository.existsByEmail(adminEmail)) {
                log.info("Tài khoản admin đã tồn tại: {}", adminEmail);
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
            log.info("Đã tạo tài khoản admin mặc định: {} (role=ADMIN)", adminEmail);
        } catch (Exception e) {
            log.warn("Bỏ qua tạo admin mặc định do lỗi (ứng dụng vẫn tiếp tục chạy): {}", e.getMessage());
        }
    }

    private boolean isProdProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}
