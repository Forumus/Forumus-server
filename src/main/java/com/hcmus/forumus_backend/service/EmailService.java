package com.hcmus.forumus_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final JavaMailSender mailSender;

    @Value("${email.from.address}")
    private String emailFrom;

    @Value("${email.from.name}")
    private String emailFromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send OTP email to user
     *
     * @param recipientEmail The recipient's email address
     * @param otpCode        The OTP code to send
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendOTPEmail(String recipientEmail, String otpCode) {
        try {
            logger.debug("Starting OTP email send to: {}", recipientEmail);

            // Validate email format
            if (!EMAIL_PATTERN.matcher(recipientEmail).matches()) {
                logger.error("Invalid email format: {}", recipientEmail);
                return false;
            }

            String otpHtml = createOTPEmailHTML(otpCode, recipientEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom, emailFromName);
            helper.setTo(recipientEmail);
            helper.setSubject("Your Forumus Verification Code");
            helper.setText("Your Forumus verification code is: " + otpCode, otpHtml);

            mailSender.send(message);
            logger.info("OTP email sent successfully to {}", recipientEmail);

            return true;

        } catch (MessagingException e) {
            logger.error("MessagingException while sending OTP email to {}: {}", recipientEmail, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while sending OTP email to {}: {}", recipientEmail, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send welcome email after successful verification
     *
     * @param recipientEmail The recipient's email address
     * @param userName       The user's name
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendWelcomeEmail(String recipientEmail, String userName) {
        try {
            logger.debug("Starting welcome email send to: {}", recipientEmail);

            String welcomeHtml = createWelcomeEmailHTML(userName);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom, emailFromName);
            helper.setTo(recipientEmail);
            helper.setSubject("Welcome to Forumus! 🎉");
            helper.setText("Welcome to Forumus, " + userName + "!", welcomeHtml);

            mailSender.send(message);
            logger.info("Welcome email sent successfully to {}", recipientEmail);

            return true;

        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}: {}", recipientEmail, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generate HTML email template for OTP
     *
     * @param otpCode        The OTP code
     * @param recipientEmail The recipient's email
     * @return HTML email content
     */
    private String createOTPEmailHTML(String otpCode, String recipientEmail) {
        return """
                <!DOCTYPE html>
                <html>
                  <body style="font-family: Arial, sans-serif; background:#ffffff; padding:20px;">
                    <table width="100%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin:auto; border:1px solid #e0e0e0; border-radius:8px;">
                      <tr>
                        <td style="background:#4a64d8; padding:20px; text-align:center; color:white; font-size:22px; border-radius:8px 8px 0 0;">
                          Forumus Email Verification
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:25px; color:#333; font-size:15px;">
                          <p style="margin:0 0 12px 0;">Hi %s,</p>
                          <p style="margin:0 0 12px 0;">Use the verification code below to continue:</p>
                          <p style="font-size:32px; margin:25px 0; text-align:center; font-weight:bold; color:#4a64d8;">
                            %s
                          </p>
                          <p style="margin:0 0 12px 0;">This code expires in 5 minutes.</p>
                          <p style="margin:0;">If you didn't request this code, you can ignore this email.</p>
                        </td>
                      </tr>
                      <tr>
                        <td style="background:#f5f5f5; padding:15px; text-align:center; font-size:13px; color:#666; border-radius:0 0 8px 8px;">
                          Forumus – Learning Community
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """
                .formatted(recipientEmail, otpCode);
    }

    /**
     * Generate HTML email template for welcome email
     *
     * @param userName The user's name
     * @return HTML email content
     */
    private String createWelcomeEmailHTML(String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Welcome to Forumus!</h1>
                        </div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p>Congratulations! Your email has been successfully verified and your Forumus account is now active.</p>
                            <p>You can now:</p>
                            <ul>
                                <li>Join discussions and forums</li>
                                <li>Ask questions and get answers</li>
                                <li>Connect with students and teachers</li>
                                <li>Share your knowledge with the community</li>
                            </ul>
                            <p>Welcome to the Forumus community!</p>
                            <p>Best regards,<br>The Forumus Team</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(userName);
    }
}
