package com.hcmus.forumus_backend.dto.email;

import java.util.List;
import java.util.Map;

public class ReportEmailRequest {
    private String recipientEmail;
    private String userName;
    private String userStatus;
    private List<Map<String, String>> reportedPosts;

    public ReportEmailRequest() {
    }

    public ReportEmailRequest(String recipientEmail, String userName, String userStatus,
            List<Map<String, String>> reportedPosts) {
        this.recipientEmail = recipientEmail;
        this.userName = userName;
        this.userStatus = userStatus;
        this.reportedPosts = reportedPosts;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public List<Map<String, String>> getReportedPosts() {
        return reportedPosts;
    }

    public void setReportedPosts(List<Map<String, String>> reportedPosts) {
        this.reportedPosts = reportedPosts;
    }
}
