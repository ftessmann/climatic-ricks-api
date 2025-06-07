package com.climaticrisks.controllers;

import com.climaticrisks.models.Notificacao;
import com.climaticrisks.repositories.NotificacaoRepository;
import com.climaticrisks.responses.ErrorResponse;
import com.climaticrisks.responses.SuccessResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.util.List;
import java.util.Optional;

@Path("/notificacoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class NotificacaoController {

    @Inject
    NotificacaoRepository notificacaoRepository;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/minhas")
    public Response findMinhasNotificacoes() {
        try {
            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioId(usuarioId);
            return Response.ok(notificacoes).build();
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
    @Path("/nao-lidas")
    public Response findNaoLidas() {
        try {
            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            List<Notificacao> notificacoes = notificacaoRepository.findNaoLidasByUsuarioId(usuarioId);
            return Response.ok(notificacoes).build();
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

    @PUT
    @Path("/{id}/marcar-lida")
    public Response marcarComoLida(@PathParam("id") Integer id) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            Optional<Notificacao> notificacao = notificacaoRepository.findById(id);
            if (notificacao.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Notificação não encontrada", List.of("Notificação com ID " + id + " não existe")))
                        .build();
            }

            if (!notificacao.get().getUsuarioId().equals(usuarioId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("Acesso negado", List.of("Você só pode marcar suas próprias notificações como lidas")))
                        .build();
            }

            notificacaoRepository.marcarComoLida(id);
            return Response.ok(new SuccessResponse("Notificação marcada como lida")).build();
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

    @PUT
    @Path("/marcar-todas-lidas")
    public Response marcarTodasComoLidas() {
        try {
            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            notificacaoRepository.marcarTodasComoLidas(usuarioId);
            return Response.ok(new SuccessResponse("Todas as notificações foram marcadas como lidas")).build();
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
}
