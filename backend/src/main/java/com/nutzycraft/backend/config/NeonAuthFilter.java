package com.nutzycraft.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class NeonAuthFilter extends OncePerRequestFilter {

    private final JdbcTemplate jdbcTemplate;

    public NeonAuthFilter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Directly query the Neon Auth tables in the database to validate the opaque token!
            String sql = "SELECT u.id, u.email, u.name " +
                         "FROM neon_auth.session s " +
                         "JOIN neon_auth.\"user\" u ON s.\"userId\" = u.id " +
                         "WHERE s.token = ? AND s.\"expiresAt\" > NOW()";

            Map<String, Object> userDetails = jdbcTemplate.queryForMap(sql, token);

            if (userDetails != null && !userDetails.isEmpty()) {
                Map<String, String> principal = new HashMap<>();
                principal.put("providerId", userDetails.get("id") != null ? userDetails.get("id").toString() : null);
                principal.put("email", (String) userDetails.get("email"));
                principal.put("name", (String) userDetails.get("name"));

                if (principal.get("email") != null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            principal, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // Token not found or expired
            System.err.println("NeonAuthFilter: Token not found or expired.");
        } catch (Exception e) {
            System.err.println("NeonAuthFilter: Failed to validate token - " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
