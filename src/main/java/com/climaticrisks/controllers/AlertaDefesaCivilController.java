package com.climaticrisks.controllers;

import com.climaticrisks.models.AlertaDefesaCivil;
import com.climaticrisks.enums.TipoRisco;
import com.climaticrisks.repositories.AlertaDefesaCivilRepository;
import com.climaticrisks.responses.ErrorResponse;
import com.climaticrisks.responses.SuccessResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Path("/alertas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class AlertaDefesaCivilController {

    @Inject
    AlertaDefesaCivilRepository alertaRepository;

    @POST
    public Response create(AlertaDefesaCivil alerta) {
        try {
            if (alerta.getTitulo() == null || alerta.getTitulo().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Título é obrigatório", List.of("titulo não pode ser vazio")))
                        .build();
            }

            if (alerta.getTitulo().length() > 200) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Título muito longo", List.of("titulo deve ter no máximo 200 caracteres")))
                        .build();
            }

            if (alerta.getDataInicio() == null) {
                alerta.setDataInicio(LocalDateTime.now());
            }

            if (alerta.getNivelAlerta() == null) {
                alerta.setNivelAlerta(TipoRisco.BAIXO);
            }

            AlertaDefesaCivil savedAlerta = alertaRepository.save(alerta);
            return Response.status(Response.Status.CREATED).entity(savedAlerta).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao criar alerta", List.of(e.getMessage())))
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

            Optional<AlertaDefesaCivil> alerta = alertaRepository.findById(id);
            if (alerta.isPresent()) {
                return Response.ok(alerta.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Alerta não encontrado", List.of("Alerta com ID " + id + " não existe")))
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
            List<AlertaDefesaCivil> alertas = alertaRepository.findAll();
            return Response.ok(alertas).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/ativos")
    public Response findAtivos() {
        try {
            List<AlertaDefesaCivil> alertas = alertaRepository.findAtivos();
            return Response.ok(alertas).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/nivel/{nivel}")
    public Response findByNivel(@PathParam("nivel") String nivel) {
        try {
            TipoRisco tipoRisco;
            try {
                switch (nivel.toLowerCase()) {
                    case "baixo" -> tipoRisco = TipoRisco.BAIXO;
                    case "medio", "médio" -> tipoRisco = TipoRisco.MEDIO;
                    case "alto" -> tipoRisco = TipoRisco.ALTO;
                    default -> throw new IllegalArgumentException("Nível inválido");
                }
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Nível inválido", List.of("Use: baixo, medio ou alto")))
                        .build();
            }

            List<AlertaDefesaCivil> alertas = alertaRepository.findByNivelAlerta(tipoRisco);
            return Response.ok(alertas).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, AlertaDefesaCivil alerta) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            Optional<AlertaDefesaCivil> existingAlerta = alertaRepository.findById(id);
            if (existingAlerta.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Alerta não encontrado", List.of("Alerta com ID " + id + " não existe")))
                        .build();
            }

            if (alerta.getTitulo() == null || alerta.getTitulo().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Título é obrigatório", List.of("titulo não pode ser vazio")))
                        .build();
            }

            alerta.setId(id);
            AlertaDefesaCivil updatedAlerta = alertaRepository.update(alerta);
            return Response.ok(updatedAlerta).build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Alerta não encontrado", List.of(e.getMessage())))
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

            alertaRepository.delete(id);
            return Response.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Alerta não encontrado", List.of(e.getMessage())))
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

    @PUT
    @Path("/{id}/desativar")
    public Response desativar(@PathParam("id") Integer id) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            alertaRepository.desativar(id);
            return Response.ok(new SuccessResponse("Alerta desativado com sucesso")).build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Alerta não encontrado", List.of(e.getMessage())))
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao desativar", List.of(e.getMessage())))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }
}
