package com.nutzycraft.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        logger.info("=== ATTEMPTING TO SEND EMAIL ===");
        logger.info("To: {}", toEmail);
        logger.info("From: {}", fromEmail);
        logger.info("Code: {}", verificationCode);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("NutzyCraft - Verify Your Email");

            String htmlContent = buildVerificationEmailHtml(toEmail, verificationCode);
            helper.setText(htmlContent, true);

            logger.info("Sending email via SMTP...");
            mailSender.send(message);
            logger.info("✅ Verification email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("❌ MessagingException while sending email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ Unexpected error while sending email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    private String buildVerificationEmailHtml(String email, String code) {
        String verifyUrl = appUrl + "/verify-email.html?email=" + email;
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                              color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .code-box { background: white; border: 2px solid #667eea; border-radius: 8px; 
                                padding: 20px; margin: 20px 0; text-align: center; }
                    .code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 8px; }
                    .button { display: inline-block; background: #667eea; color: white; 
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; 
                              margin: 20px 0; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Welcome to NutzyCraft!</h1>
                    </div>
                    <div class="content">
                        <p>Hello,</p>
                        <p>Thank you for registering with NutzyCraft! To complete your registration, 
                           please verify your email address using the code below:</p>
                        
                        <div class="code-box">
                            <div class="code">""" + code + """
</div>
                        </div>
                        
                        <p>Alternatively, you can click the button below to verify your email:</p>
                        <div style="text-align: center;">
                            <a href='""" + verifyUrl + """
' class="button">Verify Email</a>
                        </div>
                        
                        <p><strong>Important:</strong> This code is valid for 24 hours. 
                           If you didn't create an account with NutzyCraft, please ignore this email.</p>
                        
                        <p>Best regards,<br>The NutzyCraft Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                        <p>&copy; 2025 NutzyCraft. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}
