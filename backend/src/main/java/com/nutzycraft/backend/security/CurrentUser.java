package com.nutzycraft.backend.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

/**
 * Resolves the identity of the calling user from the verified Clerk JWT.
 *
 * Controllers must use this instead of trusting a client-supplied email
 * (query param or request body field) — those can be set to any value by
 * the caller and allow acting on behalf of other users.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    /**
     * @return the email claim of the authenticated Clerk session token.
     * @throws ResponseStatusException 401 if there is no authenticated JWT
     *         or it carries no email claim.
     */
    public static String email() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated email not available");
    }
}
