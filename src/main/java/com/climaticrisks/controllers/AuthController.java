package com.climaticrisks.controllers;

import com.climaticrisks.requests.LoginRequest;
import com.climaticrisks.responses.ErrorResponse;
import com.climaticrisks.responses.LoginResponse;
import com.climaticrisks.responses.LogoutResponse;
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
    @Path("/logout")
    public Response logout() {
        return Response.ok(new LogoutResponse("Logout realizado com sucesso")).build();
    }
}
