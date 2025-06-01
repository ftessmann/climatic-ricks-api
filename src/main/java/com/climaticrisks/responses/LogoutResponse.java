package com.climaticrisks.responses;

public class LogoutResponse {
    private String message;

    public LogoutResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}