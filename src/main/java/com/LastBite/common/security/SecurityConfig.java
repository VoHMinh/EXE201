package com.LastBite.common.security;

import com.LastBite.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.Instant;

/**
 * Central security configuration — <b>single filter chain</b>.
 * <p>
 * Improvement over DiHouse (which had 3 chains with duplicated endpoints):
 * <ul>
 *   <li>One chain with clear public/protected separation</li>
 *   <li>Stateless sessions (no JSESSIONID cookie)</li>
 *   <li>Custom JSON responses for 401 and 403</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthConverter jwtAuthConverter;

    /** Public endpoints that do NOT require authentication. */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/register-partner",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password/**",
            "/api/v1/auth/reset-password/**",
            "/error",
    };

    /** Swagger / OpenAPI endpoints. */
    private static final String[] SWAGGER_ENDPOINTS = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CORS — uses the CorsConfig bean
                .cors(Customizer.withDefaults())

                // CSRF disabled (stateless JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless sessions
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI — always public
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll()

                        // Auth endpoints — always public
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // Public store browsing (GET only)
                        .requestMatchers(HttpMethod.GET, "/api/v1/stores/**").permitAll()

                        // Health check
                        .requestMatchers("/actuator/health").permitAll()

                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Custom 401 / 403 JSON responses
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler())
                )

                // JWT resource server
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .jwt(jwt -> jwt
                                .decoder(jwtTokenProvider)
                                .jwtAuthenticationConverter(jwtAuthConverter)));

        return http.build();
    }

    // ── Error handlers (pure String, NO ObjectMapper) ──

    private static AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (req, res, authEx) -> {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setContentType("application/json;charset=UTF-8");
            writeJsonError(res.getOutputStream(),
                    ErrorCode.UNAUTHENTICATED.getCode(),
                    ErrorCode.UNAUTHENTICATED.getDefaultMessage(),
                    req.getRequestURI());
        };
    }

    private static AccessDeniedHandler forbiddenHandler() {
        return (req, res, denied) -> {
            res.setStatus(HttpStatus.FORBIDDEN.value());
            res.setContentType("application/json;charset=UTF-8");
            writeJsonError(res.getOutputStream(),
                    ErrorCode.FORBIDDEN.getCode(),
                    ErrorCode.FORBIDDEN.getDefaultMessage(),
                    req.getRequestURI());
        };
    }

    /**
     * Writes a JSON error response using plain string formatting.
     * No ObjectMapper, no Jackson, no bean injection — zero risk of startup failure.
     */
    private static void writeJsonError(java.io.OutputStream out, int code, String message, String path)
            throws IOException {
        String json = String.format(
                "{\"code\":%d,\"message\":\"%s\",\"timestamp\":%s,\"path\":\"%s\"}",
                code,
                escapeJson(message),
                Instant.now().getEpochSecond() + "." + Instant.now().getNano(),
                escapeJson(path)
        );
        out.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        out.flush();
    }

    /** Escape special JSON characters in a string value. */
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
