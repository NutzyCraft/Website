package com.nutzycraft.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final String FROM_ADDRESS = "NutzyCraft <info@nutzycraft.com>";
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Value("${resend.api-key}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Sends a transactional email using a Resend Hosted Template.
     * Uses the Resend REST API directly since the resend-java SDK does not
     * yet expose template support in its released versions.
     *
     * @param recipientEmail the recipient's email address
     * @param templateId     the Resend template alias or ID (e.g., "account-creation-clients")
     * @param variables      dynamic template variables (e.g., {"name": "John"})
     */
    public void sendTemplateEmail(String recipientEmail, String templateId, Map<String, Object> variables) {
        try {
            Map<String, Object> body = Map.of(
                    "from",    FROM_ADDRESS,
                    "to",      List.of(recipientEmail),
                    "template", Map.of(
                            "id",        templateId,
                            "variables", variables
                    )
            );

            String json = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Email sent to {} using template '{}'. Response: {}",
                        recipientEmail, templateId, response.body());
            } else {
                log.error("Resend rejected email to {} using template '{}'. Status: {}, Body: {}",
                        recipientEmail, templateId, response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Failed to send email to {} using template '{}': {}",
                    recipientEmail, templateId, e.getMessage(), e);
        }
    }
}
