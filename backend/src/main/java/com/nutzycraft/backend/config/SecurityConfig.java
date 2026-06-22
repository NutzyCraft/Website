package com.nutzycraft.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final NeonAuthFilter neonAuthFilter;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource, NeonAuthFilter neonAuthFilter) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.neonAuthFilter = neonAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
                        // Public API endpoints that should remain accessible
                        .requestMatchers("/api/jobs/search/**").permitAll()
                        .requestMatchers("/api/freelancers/search/**").permitAll()
                        .requestMatchers("/api/contact/**").permitAll()
                        .requestMatchers("/api/portfolio/**").permitAll()
                        .requestMatchers("/api/auth/latest-session").permitAll()
                        // All other API endpoints require authentication
                        .requestMatchers("/api/**").authenticated()
                        // Everything else (static files served by Spring) is public
                        .anyRequest().permitAll()
                )

                // Add Custom Neon Auth Introspection Filter
                .addFilterBefore(neonAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
