package com.climaticrisks.controllers;

import com.climaticrisks.models.RelatorioEvento;
import com.climaticrisks.repositories.RelatorioEventoRepository;
import com.climaticrisks.requests.RelatorioRequest;
import com.climaticrisks.responses.ErrorResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Path("/relatorios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class RelatorioEventoController {

    @Inject
    RelatorioEventoRepository relatorioRepository;

    @POST
    public Response create(RelatorioEvento relatorio) {
        try {
            if (relatorio.getPeriodo() == null || relatorio.getPeriodo().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Período é obrigatório", List.of("periodo não pode ser vazio")))
                        .build();
            }

            if (relatorio.getRegiao() == null || relatorio.getRegiao().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Região é obrigatória", List.of("regiao não pode ser vazia")))
                        .build();
            }

            RelatorioEvento savedRelatorio = relatorioRepository.save(relatorio);
            return Response.status(Response.Status.CREATED).entity(savedRelatorio).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao criar relatório", List.of(e.getMessage())))
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

            Optional<RelatorioEvento> relatorio = relatorioRepository.findById(id);
            if (relatorio.isPresent()) {
                return Response.ok(relatorio.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Relatório não encontrado", List.of("Relatório com ID " + id + " não existe")))
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
            List<RelatorioEvento> relatorios = relatorioRepository.findAll();
            return Response.ok(relatorios).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/periodo/{periodo}")
    public Response findByPeriodo(@PathParam("periodo") String periodo) {
        try {
            if (periodo == null || periodo.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Período inválido", List.of("periodo não pode ser vazio")))
                        .build();
            }

            List<RelatorioEvento> relatorios = relatorioRepository.findByPeriodo(periodo);
            return Response.ok(relatorios).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/regiao/{regiao}")
    public Response findByRegiao(@PathParam("regiao") String regiao) {
        try {
            if (regiao == null || regiao.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Região inválida", List.of("regiao não pode ser vazia")))
                        .build();
            }

            List<RelatorioEvento> relatorios = relatorioRepository.findByRegiao(regiao);
            return Response.ok(relatorios).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @POST
    @Path("/gerar-automatico")
    public Response gerarRelatorioAutomatico(RelatorioRequest request) {
        try {
            if (request.getPeriodo() == null || request.getPeriodo().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Período é obrigatório", List.of("periodo não pode ser vazio (formato: YYYY-MM)")))
                        .build();
            }

            if (request.getRegiao() == null || request.getRegiao().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Região é obrigatória", List.of("regiao não pode ser vazia")))
                        .build();
            }

            RelatorioEvento relatorio = relatorioRepository.gerarRelatorioAutomatico(
                    request.getPeriodo(),
                    request.getRegiao()
            );

            return Response.status(Response.Status.CREATED).entity(relatorio).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao gerar relatório automático", List.of(e.getMessage())))
                    .build();
        }
    }

    @POST
    @Path("/gerar-consolidado/{periodo}")
    public Response gerarRelatorioConsolidado(@PathParam("periodo") String periodo) {
        try {
            if (periodo == null || periodo.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Período inválido", List.of("periodo não pode ser vazio (formato: YYYY-MM)")))
                        .build();
            }

            RelatorioEvento relatorio = relatorioRepository.gerarRelatorioConsolidado(periodo);
            return Response.status(Response.Status.CREATED).entity(relatorio).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao gerar relatório consolidado", List.of(e.getMessage())))
                    .build();
        }
    }

    @POST
    @Path("/gerar-por-periodo/{periodo}")
    public Response gerarRelatoriosPorPeriodo(@PathParam("periodo") String periodo) {
        try {
            if (periodo == null || periodo.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Período inválido", List.of("periodo não pode ser vazio (formato: YYYY-MM)")))
                        .build();
            }

            List<RelatorioEvento> relatorios = relatorioRepository.gerarRelatoriosPorPeriodo(periodo);
            return Response.status(Response.Status.CREATED).entity(relatorios).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao gerar relatórios por período", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/mes-atual")
    public Response gerarRelatorioMesAtual() {
        try {
            String periodoAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            List<RelatorioEvento> relatorios = relatorioRepository.gerarRelatoriosPorPeriodo(periodoAtual);
            return Response.ok(relatorios).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, RelatorioEvento relatorio) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            // Verificar se relatório existe
            Optional<RelatorioEvento> existingRelatorio = relatorioRepository.findById(id);
            if (existingRelatorio.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Relatório não encontrado", List.of("Relatório com ID " + id + " não existe")))
                        .build();
            }

            relatorio.setId(id);
            RelatorioEvento updatedRelatorio = relatorioRepository.update(relatorio);
            return Response.ok(updatedRelatorio).build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Relatório não encontrado", List.of(e.getMessage())))
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

            relatorioRepository.delete(id);
            return Response.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Relatório não encontrado", List.of(e.getMessage())))
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
