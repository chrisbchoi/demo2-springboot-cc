package com.cc.data.demo2springboot.dto;

public class AuthResponse {
    private String token;

    // Default constructor for Jackson
    public AuthResponse() {
    }

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
