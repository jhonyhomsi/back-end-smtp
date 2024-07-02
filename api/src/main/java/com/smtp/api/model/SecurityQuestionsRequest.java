package com.smtp.api.model;

import java.util.List;

public class SecurityQuestionsRequest {
    private String username;
    private List<String> securityQuestions;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getSecurityQuestions() {
        return securityQuestions;
    }

    public void setSecurityQuestions(List<String> securityQuestions) {
        this.securityQuestions = securityQuestions;
    }
}
