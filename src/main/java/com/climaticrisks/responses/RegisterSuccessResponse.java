package com.climaticrisks.responses;

public class RegisterSuccessResponse {
    private String message;
    private Integer userId;
    private String email;
    private String nome;

    public RegisterSuccessResponse(String message, Integer userId, String email, String nome) {
        this.message = message;
        this.userId = userId;
        this.email = email;
        this.nome = nome;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}