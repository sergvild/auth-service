package com.vizavi.authserver.service;

import com.vizavi.authserver.entity.User;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.mail.from}")
    private String from;

    public void sendVerificationEmail(User user, String token) {
        String link = frontendUrl + "/verify?token=" + token;
        String html = """
                <div style="font-family:sans-serif;max-width:480px;margin:auto">
                  <h2 style="color:#4f6ef7">Verify your email</h2>
                  <p>Click the button below to verify your email address.</p>
                  <a href="%s"
                     style="display:inline-block;padding:12px 24px;background:#4f6ef7;
                            color:#fff;border-radius:8px;text-decoration:none;font-weight:500">
                    Verify Email
                  </a>
                  <p style="color:#888;font-size:13px;margin-top:24px">
                    This link expires in 24 hours.<br>
                    If you did not create an account, you can safely ignore this email.
                  </p>
                </div>
                """.formatted(link);

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(user.getEmail());
            helper.setSubject("Verify your email address");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}