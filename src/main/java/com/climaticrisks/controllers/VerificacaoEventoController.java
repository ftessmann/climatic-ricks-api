package com.climaticrisks.controllers;

import com.climaticrisks.models.VerificacaoEvento;
import com.climaticrisks.repositories.VerificacaoEventoRepository;
import com.climaticrisks.repositories.VerificacaoEventoRepository.EstatisticasVerificacao;
import com.climaticrisks.requests.VerificacaoRequest;
import com.climaticrisks.requests.VerificacaoUpdateRequest;
import com.climaticrisks.responses.ErrorResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.util.List;
import java.util.Optional;

@Path("/verificacoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class VerificacaoEventoController {

    @Inject
    VerificacaoEventoRepository verificacaoRepository;

    @Inject
    JsonWebToken jwt;

    @POST
    public Response create(VerificacaoRequest request) {
        try {
            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            if (request.getAlagamentoId() == null && request.getDeslizamentoId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Evento é obrigatório",
                                List.of("Deve informar alagamentoId OU deslizamentoId")))
                        .build();
            }

            if (request.getAlagamentoId() != null && request.getDeslizamentoId() != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Evento inválido",
                                List.of("Não pode informar alagamentoId E deslizamentoId ao mesmo tempo")))
                        .build();
            }

            if (request.getConfirmacao() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Confirmação é obrigatória",
                                List.of("confirmacao não pode ser nula")))
                        .build();
            }

            boolean jaVerificou = verificacaoRepository.jaVerificouEvento(
                    usuarioId,
                    request.getAlagamentoId(),
                    request.getDeslizamentoId()
            );

            if (jaVerificou) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ErrorResponse("Verificação já existe",
                                List.of("Você já verificou este evento")))
                        .build();
            }

            VerificacaoEvento verificacao = new VerificacaoEvento();
            verificacao.setUsuarioId(usuarioId);
            verificacao.setAlagamentoId(request.getAlagamentoId());
            verificacao.setDeslizamentoId(request.getDeslizamentoId());
            verificacao.setConfirmacao(request.getConfirmacao());

            VerificacaoEvento savedVerificacao = verificacaoRepository.save(verificacao);
            return Response.status(Response.Status.CREATED).entity(savedVerificacao).build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Token inválido", List.of("ID do usuário no token não é válido")))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao criar verificação", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/minhas")
    public Response findMinhasVerificacoes() {
        try {
            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            List<VerificacaoEvento> verificacoes = verificacaoRepository.findByUsuarioId(usuarioId);
            return Response.ok(verificacoes).build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Token inválido", List.of("ID do usuário no token não é válido")))
                    .build();
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

            Optional<VerificacaoEvento> verificacao = verificacaoRepository.findById(id);
            if (verificacao.isPresent()) {
                return Response.ok(verificacao.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Verificação não encontrada", List.of("Verificação com ID " + id + " não existe")))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/alagamento/{alagamentoId}")
    public Response findByAlagamentoId(@PathParam("alagamentoId") Integer alagamentoId) {
        try {
            if (alagamentoId == null || alagamentoId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID do alagamento inválido", List.of("alagamentoId deve ser um número positivo")))
                        .build();
            }

            List<VerificacaoEvento> verificacoes = verificacaoRepository.findByAlagamentoId(alagamentoId);
            return Response.ok(verificacoes).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/deslizamento/{deslizamentoId}")
    public Response findByDeslizamentoId(@PathParam("deslizamentoId") Integer deslizamentoId) {
        try {
            if (deslizamentoId == null || deslizamentoId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID do deslizamento inválido", List.of("deslizamentoId deve ser um número positivo")))
                        .build();
            }

            List<VerificacaoEvento> verificacoes = verificacaoRepository.findByDeslizamentoId(deslizamentoId);
            return Response.ok(verificacoes).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/estatisticas/alagamento/{alagamentoId}")
    public Response getEstatisticasAlagamento(@PathParam("alagamentoId") Integer alagamentoId) {
        try {
            if (alagamentoId == null || alagamentoId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID do alagamento inválido", List.of("alagamentoId deve ser um número positivo")))
                        .build();
            }

            EstatisticasVerificacao estatisticas = verificacaoRepository.getEstatisticasAlagamento(alagamentoId);
            return Response.ok(estatisticas).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/estatisticas/deslizamento/{deslizamentoId}")
    public Response getEstatisticasDeslizamento(@PathParam("deslizamentoId") Integer deslizamentoId) {
        try {
            if (deslizamentoId == null || deslizamentoId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID do deslizamento inválido", List.of("deslizamentoId deve ser um número positivo")))
                        .build();
            }

            EstatisticasVerificacao estatisticas = verificacaoRepository.getEstatisticasDeslizamento(deslizamentoId);
            return Response.ok(estatisticas).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, VerificacaoUpdateRequest request) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            Optional<VerificacaoEvento> existingVerificacao = verificacaoRepository.findById(id);
            if (existingVerificacao.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Verificação não encontrada", List.of("Verificação com ID " + id + " não existe")))
                        .build();
            }

            if (!existingVerificacao.get().getUsuarioId().equals(usuarioId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("Acesso negado", List.of("Você só pode editar suas próprias verificações")))
                        .build();
            }

            VerificacaoEvento verificacao = existingVerificacao.get();
            verificacao.setConfirmacao(request.getConfirmacao());

            VerificacaoEvento updatedVerificacao = verificacaoRepository.update(verificacao);
            return Response.ok(updatedVerificacao).build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Token inválido", List.of("ID do usuário no token não é válido")))
                    .build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Verificação não encontrada", List.of(e.getMessage())))
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

            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            Optional<VerificacaoEvento> existingVerificacao = verificacaoRepository.findById(id);
            if (existingVerificacao.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Verificação não encontrada", List.of("Verificação com ID " + id + " não existe")))
                        .build();
            }

            if (!existingVerificacao.get().getUsuarioId().equals(usuarioId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("Acesso negado", List.of("Você só pode excluir suas próprias verificações")))
                        .build();
            }

            verificacaoRepository.delete(id);
            return Response.noContent().build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Token inválido", List.of("ID do usuário no token não é válido")))
                    .build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Verificação não encontrada", List.of(e.getMessage())))
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
