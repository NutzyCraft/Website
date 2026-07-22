package com.nutzycraft.backend.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final String FROM_ADDRESS = "NutzyCraft <info@nutzycraft.com>";

    private final Resend resend;

    public EmailNotificationService(Resend resend) {
        this.resend = resend;
    }

    /**
     * Sends a transactional email using a Resend Hosted Template.
     *
     * @param recipientEmail the recipient's email address
     * @param templateId     the Resend template ID (e.g., "tpl_xxxxxxxxxxxx")
     * @param variables      dynamic template variables (e.g., {"username": "John", "jobTitle": "Developer"})
     */
    public void sendTemplateEmail(String recipientEmail, String templateName, Map<String, Object> variables) {
        try {
            String htmlContent = generateHtmlForTemplate(templateName, variables);
            String subject = generateSubjectForTemplate(templateName);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(FROM_ADDRESS)
                    .to(recipientEmail)
                    .subject(subject)
                    .html(htmlContent)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("Email sent successfully to {} using template {}. Resend ID: {}",
                    recipientEmail, templateName, response.getId());

        } catch (Exception e) {
            log.error("Failed to send email to {} using template {}: {}",
                    recipientEmail, templateName, e.getMessage(), e);
        }
    }

    private String generateHtmlForTemplate(String templateName, Map<String, Object> variables) {
        String name = variables.containsKey("name") ? variables.get("name").toString() : "User";
        if ("account-creation-freelancer".equals(templateName)) {
            return "<h1>Welcome to NutzyCraft, " + name + "!</h1><p>We are excited to have you as a freelancer on our platform.</p>";
        } else if ("account-creation-clients".equals(templateName)) {
            return "<h1>Welcome to NutzyCraft, " + name + "!</h1><p>We are excited to help you find the best freelancers.</p>";
        }
        return "<p>Hello " + name + ", this is a notification from NutzyCraft.</p>";
    }

    private String generateSubjectForTemplate(String templateName) {
        if ("account-creation-freelancer".equals(templateName) || "account-creation-clients".equals(templateName)) {
            return "Welcome to NutzyCraft!";
        }
        return "NutzyCraft Notification";
    }
}
