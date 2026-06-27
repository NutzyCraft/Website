package com.nutzycraft.backend.service;

import com.nutzycraft.backend.dto.AuthDTOs.SyncResponse;
import com.nutzycraft.backend.dto.UserProfileDTO;
import com.nutzycraft.backend.entity.Client;
import com.nutzycraft.backend.entity.Freelancer;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.ClientRepository;
import com.nutzycraft.backend.repository.FreelancerRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    /**
     * Allowlist of emails permitted to register during early testing.
     * Any email NOT in this set will be rejected by syncUser().
     */
    private static final Set<String> ALLOWED_EMAILS = Set.of(
            "nutzycraft@gmail.com",
            "soeshcooray@gmail.com",
            "client@test.com",
            "freelancer@test.com");

    /**
     * The only email permitted to hold the ADMIN role.
     */
    private static final String ADMIN_EMAIL = "nutzycraft@gmail.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreelancerRepository freelancerRepository;

    @Autowired
    private ClientRepository clientRepository;

    /**
     * Synchronize a Neon Auth identity with the local database.
     * Called after successful Neon Auth sign-in/sign-up.
     *
     * @param providerId Neon Auth subject ID (from JWT "sub" claim)
     * @param email      Email from the JWT
     * @param name       Full name from the JWT (may be null)
     * @param roleStr    Requested role — only used for first-time registration
     * @return SyncResponse with user details and isNew flag
     */
    @Transactional
    public SyncResponse syncUser(String providerId, String email, String name, String roleStr) {
        SyncResponse response = new SyncResponse();

        // 1. Look up by providerId (fastest path for returning users)
        Optional<User> byProvider = userRepository.findByProviderId(providerId);
        if (byProvider.isPresent()) {
            User user = byProvider.get();
            populateResponse(response, user, false);
            return response;
        }

        // 2. Look up by email (handles migration of legacy users)
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            // Link the Neon Auth identity to the existing local user
            user.setProviderId(providerId);
            if (name != null && !name.isBlank() && (user.getFullName() == null || user.getFullName().isBlank())) {
                user.setFullName(name);
            }
            userRepository.save(user);
            populateResponse(response, user, false);
            return response;
        }

        // 3. First-time registration — create new user
        User.Role role = resolveRole(email, roleStr);

        User user = new User();
        user.setEmail(email);
        user.setFullName(name != null ? name : "");
        user.setProviderId(providerId);
        user.setRole(role);
        userRepository.save(user);

        // Create the corresponding role-specific record
        if (role == User.Role.FREELANCER) {
            Freelancer freelancer = new Freelancer();
            freelancer.setUser(user);
            freelancerRepository.save(freelancer);
        } else if (role == User.Role.CLIENT) {
            Client client = new Client();
            client.setUser(user);
            clientRepository.save(client);
        }
        // ADMIN does not get a Client/Freelancer record

        populateResponse(response, user, true);
        return response;
    }

    /**
     * Resolve the role for a new user.
     * ADMIN is hardcoded to ADMIN_EMAIL only — it can never be claimed via the role
     * parameter.
     */
    private User.Role resolveRole(String email, String roleStr) {
        // 1. Immediately hardcode the master admin email
        if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
            return User.Role.ADMIN;
        }

        // 2. Validate that a role string was actually provided
        if (roleStr == null || roleStr.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Role is required for new user registration (CLIENT or FREELANCER).");
        }

        try {
            // 3. Parse the string into the Enum inside the correct scope
            User.Role role = User.Role.valueOf(roleStr.toUpperCase());

            // 4. Block anyone else from trying to claim the ADMIN role
            if (role == User.Role.ADMIN) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Cannot register as ADMIN.");
            }

            return role;
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Invalid role specified. Use CLIENT or FREELANCER.");
        }
    }

    private void populateResponse(SyncResponse response, User user, boolean isNew) {
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().name());
        response.setNew(isNew);
    }

    /**
     * Get user profile information for display.
     * Kept from the original AuthService — used by UserController.
     */
    public UserProfileDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());

        if (user.getRole() == User.Role.CLIENT) {
            clientRepository.findByUser(user).ifPresent(client -> {
                dto.setCompanyName(client.getCompanyName());
                dto.setIndustry(client.getIndustry());
                dto.setProfileImage(client.getProfileImage());
            });
        } else if (user.getRole() == User.Role.FREELANCER) {
            freelancerRepository.findByUser_Email(user.getEmail()).ifPresent(freelancer -> {
                dto.setProfileImage(freelancer.getProfileImage());
            });
        }

        return dto;
    }
}
