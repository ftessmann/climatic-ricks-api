package com.climaticrisks.controllers;

import com.climaticrisks.models.Usuario;
import com.climaticrisks.repositories.UsuarioRepository;
import com.climaticrisks.responses.ErrorResponse;
import com.climaticrisks.services.AuthService;
import com.climaticrisks.validators.UsuarioValidator;
import com.climaticrisks.validators.UsuarioValidator.ValidationResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioController {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    UsuarioValidator usuarioValidator;

    @Inject
    AuthService authService;

    @POST
    public Response create(Usuario usuario) {
        try {
            ValidationResult validation = usuarioValidator.validate(usuario);
            if (!validation.isValid()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Dados inválidos", validation.getErrors()))
                        .build();
            }

            if (usuario.getSenha() != null) {
                usuario.setSenha(authService.hashSenha(usuario.getSenha()));
            }

            Usuario savedUsuario = usuarioRepository.save(usuario);
            return Response.status(Response.Status.CREATED).entity(savedUsuario).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Integer id) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            Optional<Usuario> usuario = usuarioRepository.findById(id);
            if (usuario.isPresent()) {
                return Response.ok(usuario.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Usuário não encontrado", List.of("Usuário com ID " + id + " não existe")))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    public Response findAll() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            return Response.ok(usuarios).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, Usuario usuario) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            Optional<Usuario> existingUser = usuarioRepository.findById(id);
            if (existingUser.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Usuário não encontrado", List.of("Usuário com ID " + id + " não existe")))
                        .build();
            }

            ValidationResult validation = usuarioValidator.validateForUpdate(usuario, id);
            if (!validation.isValid()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Dados inválidos", validation.getErrors()))
                        .build();
            }

            usuario.setId(id);
            Usuario updatedUsuario = usuarioRepository.update(usuario);
            return Response.ok(updatedUsuario).build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Usuário não encontrado", List.of(e.getMessage())))
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao atualizar", List.of(e.getMessage())))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            usuarioRepository.delete(id);
            return Response.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Usuário não encontrado", List.of(e.getMessage())))
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao excluir", List.of(e.getMessage())))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/email/{email}")
    public Response findByEmail(@PathParam("email") String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Email inválido", List.of("Email não pode ser vazio")))
                        .build();
            }

            Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
            if (usuario.isPresent()) {
                return Response.ok(usuario.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Usuário não encontrado", List.of("Usuário com email " + email + " não existe")))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/defesa-civil")
    public Response findDefesaCivil(@QueryParam("ativo") @DefaultValue("true") Boolean ativo) {
        try {
            List<Usuario> usuarios = usuarioRepository.findByDefesaCivil(ativo);
            return Response.ok(usuarios).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }
}
