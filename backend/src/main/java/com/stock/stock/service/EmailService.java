package com.stock.stock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@codeandstock.local}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendApprovalEmail(String toEmail, String username, String password) {
        String subject = "CodeAndStock — Your account has been approved!";
        String body = String.format(
                "Hello,\n\n" +
                        "Your registration request for CodeAndStock has been approved.\n\n" +
                        "Here are your login credentials:\n" +
                        "  Username: %s\n" +
                        "  Password: %s\n\n" +
                        "Please change your password after your first login.\n\n" +
                        "Best regards,\n" +
                        "CodeAndStock Admin Team",
                username, password);

        sendEmailAsync(toEmail, subject, body);
    }

    public void sendRejectionEmail(String toEmail, String reason) {
        String subject = "CodeAndStock — Registration request update";
        String body = String.format(
                "Hello,\n\n" +
                        "We regret to inform you that your registration request for CodeAndStock was not approved.\n\n"
                        +
                        "%s\n\n" +
                        "If you believe this is an error, please contact the administrator.\n\n" +
                        "Best regards,\n" +
                        "CodeAndStock Admin Team",
                (reason != null && !reason.isBlank())
                        ? "Reason: " + reason
                        : "We found a better candidate for this position.");

        sendEmailAsync(toEmail, subject, body);
    }

    @Async
    public void sendEmailAsync(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("=== EMAIL (not sent, mail disabled) ===");
            log.info("To: {}", to);
            log.info("Subject: {}", subject);
            log.info("Body:\n{}", body);
            log.info("=== END EMAIL ===");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
