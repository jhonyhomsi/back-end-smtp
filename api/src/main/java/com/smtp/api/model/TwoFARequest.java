package com.smtp.api.model;

public class TwoFARequest {
    private String username;
    private int token;
    private boolean twoFAEnabled;
    private String password; // Add password field for authentication

    // Getters and setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public boolean isTwoFAEnabled() {
        return twoFAEnabled;
    }

    public void setTwoFAEnabled(boolean twoFAEnabled) {
        this.twoFAEnabled = twoFAEnabled;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
