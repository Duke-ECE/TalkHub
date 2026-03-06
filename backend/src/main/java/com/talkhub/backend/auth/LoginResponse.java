package com.talkhub.backend.auth;

public class LoginResponse {
    private final String token;
    private final UserProfile user;

    public LoginResponse(String token, UserProfile user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public UserProfile getUser() {
        return user;
    }

    public record UserProfile(Long id, String username, String nickname, String avatarUrl) {
    }
}

