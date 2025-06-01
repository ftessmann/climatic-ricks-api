package com.climaticrisks.controllers;

import com.climaticrisks.models.Usuario;
import com.climaticrisks.repositories.UsuarioRepository;
import com.climaticrisks.responses.ErrorResponse;
import com.climaticrisks.responses.RegisterSuccessResponse;
import com.climaticrisks.services.AuthService;
import com.climaticrisks.validators.UsuarioValidator;
import com.climaticrisks.validators.UsuarioValidator.ValidationResult;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegisterController {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    UsuarioValidator usuarioValidator;

    @Inject
    AuthService authService;

    @POST
    @PermitAll
    public Response register(Usuario usuario) {
        try {
            ValidationResult validation = usuarioValidator.validate(usuario);
            if (!validation.isValid()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Dados inválidos", validation.getErrors()))
                        .build();
            }
            System.out.println("Validação OK");

            if (usuario.getSenha() != null) {
                usuario.setSenha(authService.hashSenha(usuario.getSenha()));
            }

            usuario.setDefesaCivil(false);

            Usuario savedUsuario = usuarioRepository.save(usuario);

            savedUsuario.setSenha(null);

            return Response.status(Response.Status.CREATED)
                    .entity(new RegisterSuccessResponse(
                            "Usuário cadastrado com sucesso",
                            savedUsuario.getId(),
                            savedUsuario.getEmail(),
                            savedUsuario.getNome()
                    ))
                    .build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Email já está cadastrado") ||
                    e.getMessage().contains("Email já cadastrado")) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ErrorResponse("Email já existe",
                                List.of("Este email já está sendo usado por outro usuário")))
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao cadastrar", List.of(e.getMessage())))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/check-email/{email}")
    @PermitAll
    public Response checkEmailAvailability(@PathParam("email") String email) {
        try {
            ValidationResult validation = usuarioValidator.validateEmailUnique(email);

            if (validation.isValid()) {
                return Response.ok(new EmailCheckResponse(true, "Email disponível")).build();
            } else {
                return Response.ok(new EmailCheckResponse(false, "Email já está em uso")).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }


    public static class EmailCheckResponse {
        private boolean available;
        private String message;

        public EmailCheckResponse(boolean available, String message) {
            this.available = available;
            this.message = message;
        }

        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
