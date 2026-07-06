package com.nutzycraft.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    @Value("${app.admin.email}")
    private String adminEmail;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            // Grant ADMIN only to the configured admin email. The email claim comes
            // from a Clerk-signature-verified JWT, so it cannot be forged by the client.
            String email = jwt.getClaimAsString("email");
            if (email != null && email.equalsIgnoreCase(adminEmail)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            return authorities;
        });

        http
                // CORS — use the centralized CorsConfigurationSource from CorsConfig
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // CSRF disabled — stateless API
                .csrf(csrf -> csrf.disable())

                // Stateless sessions — no server-side session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — no token required
                        .requestMatchers("/actuator/health").permitAll()
                        // Static frontend resources
                        .requestMatchers(
                                "/", "/*.html", "/*.css", "/*.js", "/*.jpg", "/*.png",
                                "/*.ico", "/*.svg", "/*.json", "/*.woff", "/*.woff2"
                        ).permitAll()
                        // WebSocket upgrade — auth deferred to STOMP ChannelInterceptor
                        .requestMatchers("/ws/**").permitAll()
                        // Public API endpoints that should remain accessible
                        .requestMatchers("/api/jobs/search/**").permitAll()
                        .requestMatchers("/api/freelancers/search/**").permitAll()
                        .requestMatchers("/api/contact/**").permitAll()
                        .requestMatchers("/api/portfolio/**").permitAll()
                        // Clerk webhooks are authenticated via Svix signature verification
                        // inside the controller, not a bearer JWT
                        .requestMatchers("/api/webhooks/**").permitAll()
                        // Admin endpoints require the ADMIN role, not just any logged-in user
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // All other API endpoints require authentication
                        .requestMatchers("/api/**").authenticated()
                        // Everything else (static files served by Spring) is public
                        .anyRequest().permitAll()
                )

                // Verify Clerk-issued JWTs via JWKS (configured in application.properties)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

        return http.build();
    }
}
