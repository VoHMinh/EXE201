package com.LastBite.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a decoded {@link Jwt} into a Spring Security
 * {@link JwtAuthenticationToken} with extracted authorities.
 * <p>
 * <b>Simpler than DiHouse:</b> no Redis/session dependency.
 * Roles are read directly from the JWT {@code roles} claim.
 * When you later need session management, extend this class.
 * <p>
 * Expected JWT claims:
 * <ul>
 *   <li>{@code sub} — user identifier (email / phone)</li>
 *   <li>{@code user_id} — UUID of the user</li>
 *   <li>{@code roles} — list of role strings (e.g. ["CUSTOMER", "STORE_OWNER"])</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String userId = jwt.getClaimAsString("user_id");

        if (userId == null) {
            throw new JwtException("Missing required JWT claim: user_id");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract roles from JWT claims
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            }
        } else {
            // Default role if none specified in token
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // Extract permissions (optional, for fine-grained access control)
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions != null) {
            for (String perm : permissions) {
                authorities.add(new SimpleGrantedAuthority("PERM_" + perm));
            }
        }

        String principal = jwt.getSubject() != null ? jwt.getSubject() : userId;

        return new JwtAuthenticationToken(jwt, authorities, principal);
    }
}
