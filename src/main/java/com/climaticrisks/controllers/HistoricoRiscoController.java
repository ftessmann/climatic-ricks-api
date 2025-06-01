package com.climaticrisks.controllers;

import com.climaticrisks.models.HistoricoRisco;
import com.climaticrisks.enums.TipoRisco;
import com.climaticrisks.repositories.HistoricoRiscoRepository;
import com.climaticrisks.responses.ErrorResponse;
import com.climaticrisks.responses.SuccessResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/historico-risco")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class HistoricoRiscoController {

    @Inject
    HistoricoRiscoRepository historicoRepository;

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Integer id) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            Optional<HistoricoRisco> historico = historicoRepository.findById(id);
            if (historico.isPresent()) {
                return Response.ok(historico.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Histórico não encontrado", List.of("Histórico com ID " + id + " não existe")))
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
            List<HistoricoRisco> historicos = historicoRepository.findAll();
            return Response.ok(historicos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/endereco/{enderecoId}")
    public Response findByEnderecoId(@PathParam("enderecoId") Integer enderecoId) {
        try {
            if (enderecoId == null || enderecoId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID do endereço inválido", List.of("enderecoId deve ser um número positivo")))
                        .build();
            }

            Optional<HistoricoRisco> historico = historicoRepository.findByEnderecoId(enderecoId);
            if (historico.isPresent()) {
                return Response.ok(historico.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Histórico não encontrado", List.of("Nenhum histórico encontrado para o endereço " + enderecoId)))
                        .build();
            }
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

            List<HistoricoRisco> historicos = historicoRepository.findByNivelRisco(tipoRisco);
            return Response.ok(historicos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @PUT
    @Path("/atualizar/{enderecoId}")
    public Response atualizarHistorico(@PathParam("enderecoId") Integer enderecoId) {
        try {
            if (enderecoId == null || enderecoId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID do endereço inválido", List.of("enderecoId deve ser um número positivo")))
                        .build();
            }

            historicoRepository.atualizarHistoricoRisco(enderecoId);
            return Response.ok(new SuccessResponse("Histórico de risco atualizado com sucesso")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @PUT
    @Path("/recalcular-todos")
    public Response recalcularTodos() {
        try {
            historicoRepository.recalcularTodosHistoricos();
            return Response.ok(new SuccessResponse("Todos os históricos foram recalculados com sucesso")).build();
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

            historicoRepository.delete(id);
            return Response.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Histórico não encontrado", List.of(e.getMessage())))
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
}
