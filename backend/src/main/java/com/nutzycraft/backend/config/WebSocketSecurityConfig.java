package com.nutzycraft.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JdbcTemplate jdbcTemplate;

    public WebSocketSecurityConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
                    return message;
                }

                List<String> authHeaders = accessor.getNativeHeader("Authorization");
                if (authHeaders == null || authHeaders.isEmpty()) {
                    throw new org.springframework.messaging.MessagingException("Unauthorized: missing Authorization header");
                }

                String authHeader = authHeaders.get(0);
                if (!authHeader.startsWith("Bearer ")) {
                    throw new org.springframework.messaging.MessagingException("Unauthorized: invalid Authorization header");
                }

                String token = authHeader.substring(7);

                try {
                    // Same SQL as NeonAuthFilter — column quoting is required for camelCase Postgres columns
                    String sql = "SELECT u.id, u.email, u.name " +
                                 "FROM neon_auth.session s " +
                                 "JOIN neon_auth.\"user\" u ON s.\"userId\" = u.id " +
                                 "WHERE s.token = ? AND s.\"expiresAt\" > NOW()";

                    Map<String, Object> userDetails = jdbcTemplate.queryForMap(sql, token);

                    if (userDetails == null || userDetails.isEmpty() || userDetails.get("email") == null) {
                        throw new org.springframework.messaging.MessagingException("Unauthorized: invalid token");
                    }

                    String email = (String) userDetails.get("email");
                    // Principal name must be a plain string so convertAndSendToUser(email, ...) resolves correctly
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

                    accessor.setUser(auth);
                } catch (EmptyResultDataAccessException e) {
                    throw new org.springframework.messaging.MessagingException("Unauthorized: token not found or expired");
                }

                return message;
            }
        });
    }
}
