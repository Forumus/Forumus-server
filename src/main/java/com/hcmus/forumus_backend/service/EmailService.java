package com.hcmus.forumus_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hcmus.forumus_backend.enums.UserStatus;

import java.util.List;
import java.util.Map;
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

  public boolean sendReportEmail(String recipientEmail, String userName, UserStatus userStatus,
      List<Map<String, String>> reportedPosts) {
    try {
      logger.debug("Starting report email send to: {}", recipientEmail);

      String reportHtml = createReportEmailHTML(userName, userStatus, reportedPosts);

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(emailFrom, emailFromName);
      helper.setTo(recipientEmail);
      helper.setSubject("Forumus Account Status Update");
      helper.setText("Dear " + userName + ", your account status has been updated to " + userStatus.getValue() + ".",
          reportHtml);

      mailSender.send(message);
      logger.info("Report email sent successfully to {}", recipientEmail);

      return true;

    } catch (Exception e) {
      logger.error("Failed to send report email to {}: {}", recipientEmail, e.getMessage(), e);
      return false;
    }
  }

  private String createOTPEmailHTML(String otpCode, String recipientEmail) {
    String template = """
        <!DOCTYPE html>
        <html>
          <body style='font-family: Arial, sans-serif; background:#ffffff; padding:20px;'>
            <table width='100%%' cellpadding='0' cellspacing='0' style='max-width: 600px; margin:auto; border:1px solid #e0e0e0; border-radius:8px;'>
              <tr>
                <td style='background:#4a64d8; padding:20px; text-align:center; color:white; font-size:22px; border-radius:8px 8px 0 0;'>
                  Forumus Email Verification
                </td>
              </tr>
              <tr>
                <td style='padding:25px; color:#333; font-size:15px;'>
                  <p style='margin:0 0 12px 0;'>Hi %s,</p>
                  <p style='margin:0 0 12px 0;'>Use the verification code below to continue:</p>
                  <p style='font-size:32px; margin:25px 0; text-align:center; font-weight:bold; color:#4a64d8;'>
                    %s
                  </p>
                  <p style='margin:0 0 12px 0;'>This code expires in 5 minutes.</p>
                  <p style='margin:0;'>If you didn't request this code, you can ignore this email.</p>
                </td>
              </tr>
              <tr>
                <td style='background:#f5f5f5; padding:15px; text-align:center; font-size:13px; color:#666; border-radius:0 0 8px 8px;'>
                  Forumus – Learning Community
                </td>
              </tr>
            </table>
          </body>
        </html>
        """;
    return String.format(template, recipientEmail, otpCode);
  }

  private String createWelcomeEmailHTML(String userName) {
    String template = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset='UTF-8'>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
            </style>
        </head>
        <body>
            <div class='container'>
                <div class='header'>
                    <h1>🎉 Welcome to Forumus!</h1>
                </div>
                <div class='content'>
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
        """;
    return String.format(template, userName);
  }

  private String createReportEmailHTML(String userName, UserStatus userStatus,
      List<Map<String, String>> reportedPosts) {
    // Determine status color and message based on UserStatus
    String statusColor;
    String statusMessage;
    String statusBadgeColor;

    switch (userStatus) {
      case NORMAL:
        statusColor = "#28a745"; // Green
        statusBadgeColor = "#d4edda";
        statusMessage = "Your account is in good standing.";
        break;
      case REMINDED:
        statusColor = "#ffc107"; // Yellow
        statusBadgeColor = "#fff3cd";
        statusMessage = "You've received a reminder about community guidelines.";
        break;
      case WARNED:
        statusColor = "#fd7e14"; // Orange
        statusBadgeColor = "#ffe5d0";
        statusMessage = "Warning: Your account has been flagged for violating community guidelines.";
        break;
      case BANNED:
        statusColor = "#dc3545"; // Red
        statusBadgeColor = "#f8d7da";
        statusMessage = "Your account has been banned due to multiple violations.";
        break;
      default:
        statusColor = "#6c757d"; // Gray
        statusBadgeColor = "#e2e3e5";
        statusMessage = "Account status updated.";
    }

    // Build reported posts HTML
    StringBuilder postsHtml = new StringBuilder();
    if (reportedPosts != null && !reportedPosts.isEmpty()) {
      postsHtml.append("<div style='margin-top: 20px;'>");
      postsHtml.append("<h3 style='color: #333; font-size: 16px; margin-bottom: 15px;'>Reported Posts:</h3>");

      for (Map<String, String> post : reportedPosts) {
        String postTitle = post.getOrDefault("title", "Untitled Post");
        String postReason = post.getOrDefault("reason", "Not specified");
        String postDate = post.getOrDefault("date", "Unknown date");

        postsHtml.append("<div style='background: #f8f9fa; border-left: 4px solid ").append(statusColor)
            .append("; padding: 15px; margin-bottom: 10px; border-radius: 4px;'>");
        postsHtml.append("<p style='margin: 0 0 8px 0; font-weight: bold; color: #333;'>").append(postTitle)
            .append("</p>");
        postsHtml.append("<p style='margin: 0 0 5px 0; font-size: 14px; color: #666;'><strong>Reason:</strong> ")
            .append(postReason).append("</p>");
        postsHtml.append("<p style='margin: 0; font-size: 13px; color: #999;'><strong>Date:</strong> ").append(postDate)
            .append("</p>");
        postsHtml.append("</div>");
      }
      postsHtml.append("</div>");
    }

    String template = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset='UTF-8'>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background: #f5f5f5; }
                .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; overflow: hidden; }
                .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; }
                .content { padding: 30px; }
                .status-badge { display: inline-block; padding: 10px 20px; border-radius: 20px; font-weight: bold; margin: 20px 0; }
                .footer { background: #f8f9fa; padding: 20px; text-align: center; font-size: 13px; color: #666; }
            </style>
        </head>
        <body>
            <div class='container'>
                <div class='header'>
                    <h1>Account Status Update</h1>
                </div>
                <div class='content'>
                    <p>Dear %s,</p>
                    <p>We're writing to inform you about an update to your Forumus account status.</p>

                    <div style='text-align: center; margin: 25px 0;'>
                        <span class='status-badge' style='background: %s; color: %s; border: 2px solid %s;'>
                            Status: %s
                        </span>
                    </div>

                    <p style='font-size: 15px; padding: 15px; background: %s; border-radius: 6px; border-left: 4px solid %s;'>
                        %s
                    </p>

                    %s

                    <div style='margin-top: 25px; padding: 15px; background: #e9ecef; border-radius: 6px;'>
                        <p style='margin: 0 0 10px 0; font-size: 14px;'><strong>What this means:</strong></p>
                        <ul style='margin: 0; padding-left: 20px; font-size: 14px;'>
                            <li style='margin-bottom: 5px;'>Please review our community guidelines</li>
                            <li style='margin-bottom: 5px;'>Future violations may result in further action</li>
                            <li style='margin-bottom: 5px;'>Contact support if you have questions</li>
                        </ul>
                    </div>

                    <p style='margin-top: 25px;'>If you believe this is a mistake, please contact our support team.</p>
                    <p>Best regards,<br>The Forumus Team</p>
                </div>
                <div class='footer'>
                    Forumus – Learning Community<br>
                    This is an automated message, please do not reply directly to this email.
                </div>
            </div>
        </body>
        </html>
        """;

    return String.format(template,
        userName, // %s - userName
        statusBadgeColor, // %s - badge background
        statusColor, // %s - badge text color
        statusColor, // %s - badge border
        userStatus.getValue(), // %s - status value
        statusBadgeColor, // %s - message background
        statusColor, // %s - message border
        statusMessage, // %s - status message
        postsHtml.toString() // %s - reported posts HTML
    );
  }
}
