package com.climaticrisks.controllers;

import com.climaticrisks.services.AuthService;
import com.climaticrisks.services.AuthService.AuthResult;
import com.climaticrisks.validators.UsuarioValidator;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    AuthService authService;

    @Inject
    UsuarioValidator usuarioValidator;

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Email é obrigatório", List.of("Email não pode ser vazio")))
                        .build();
            }

            if (request.getSenha() == null || request.getSenha().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Senha é obrigatória", List.of("Senha não pode ser vazia")))
                        .build();
            }

            AuthResult result = authService.authenticate(request.getEmail(), request.getSenha());

            if (result.isSuccess()) {
                LoginResponse response = new LoginResponse(
                        result.getToken(),
                        result.getRefreshToken(),
                        "Bearer",
                        86400,
                        result.getMessage()
                );
                return Response.ok(response).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("Falha na autenticação", List.of(result.getMessage())))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @POST
    @Path("/refresh")
    public Response refresh(RefreshTokenRequest request) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(new ErrorResponse("Não implementado", List.of("Funcionalidade em desenvolvimento")))
                .build();
    }

    @POST
    @Path("/logout")
    public Response logout() {
        return Response.ok(new LogoutResponse("Logout realizado com sucesso")).build();
    }

    public static class LoginRequest {
        private String email;
        private String senha;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
    }

    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private String message;

        public LoginResponse(String accessToken, String refreshToken, String tokenType, long expiresIn, String message) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.message = message;
        }

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }

        public long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class LogoutResponse {
        private String message;

        public LogoutResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ErrorResponse {
        private String message;
        private List<String> errors;

        public ErrorResponse(String message, List<String> errors) {
            this.message = message;
            this.errors = errors;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
}
