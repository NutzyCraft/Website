package com.nutzycraft.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutzycraft.backend.service.AuthService;
import com.svix.Webhook;
import com.svix.exceptions.WebhookVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

/**
 * Receives Clerk webhook events (e.g. user.deleted) so the local database
 * stays in sync with account deletions performed directly in Clerk.
 *
 * Every request is verified against CLERK_WEBHOOK_SECRET using Svix's HMAC
 * signature scheme (Clerk webhooks are delivered via Svix) — requests that
 * fail verification are rejected before any data is touched.
 */
@RestController
@RequestMapping("/api/webhooks")
@Slf4j
public class ClerkWebhookController {

    private final AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${clerk.webhook.secret}")
    private String webhookSecret;

    public ClerkWebhookController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/clerk")
    public ResponseEntity<?> handleClerkWebhook(
            @RequestBody String payload,
            HttpServletRequest request) {
        try {
            Map<String, List<String>> rawHeaders = Map.of(
                    "svix-id", List.of(valueOrEmpty(request.getHeader("svix-id"))),
                    "svix-timestamp", List.of(valueOrEmpty(request.getHeader("svix-timestamp"))),
                    "svix-signature", List.of(valueOrEmpty(request.getHeader("svix-signature")))
            );
            HttpHeaders headers = HttpHeaders.of(rawHeaders, (name, value) -> true);

            Webhook webhook = new Webhook(webhookSecret);
            webhook.verify(payload, headers);

            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.path("type").asText();

            if ("user.deleted".equals(eventType)) {
                String clerkUserId = event.path("data").path("id").asText(null);
                if (clerkUserId != null) {
                    authService.deleteUserByProviderId(clerkUserId);
                }
            }

            return ResponseEntity.ok().build();
        } catch (WebhookVerificationException e) {
            log.warn("Rejected Clerk webhook: signature verification failed");
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Error processing Clerk webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    private static String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
