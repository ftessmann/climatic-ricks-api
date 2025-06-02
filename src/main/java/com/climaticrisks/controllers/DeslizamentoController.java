package com.climaticrisks.controllers;

import com.climaticrisks.models.Deslizamento;
import com.climaticrisks.models.Endereco;
import com.climaticrisks.repositories.DeslizamentoRepository;
import com.climaticrisks.repositories.EnderecoRepository;
import com.climaticrisks.requests.DeslizamentoRequest;
import com.climaticrisks.requests.DeslizamentoUpdateRequest;
import com.climaticrisks.responses.DeslizamentoResponse;
import com.climaticrisks.responses.ErrorResponse;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Path("/deslizamentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeslizamentoController {

    @Inject
    DeslizamentoRepository deslizamentoRepository;

    @Inject
    EnderecoRepository enderecoRepository;

    @Inject
    JsonWebToken jwt;

    @POST
    @Authenticated
    public Response create(DeslizamentoRequest request) {
        try {
            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            if (request.getEndereco() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Endereço é obrigatório", List.of("endereco não pode ser nulo")))
                        .build();
            }

            if (request.getEndereco().getLogradouro() == null || request.getEndereco().getLogradouro().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Logradouro é obrigatório", List.of("logradouro não pode ser vazio")))
                        .build();
            }

            if (request.getEndereco().getBairro() == null || request.getEndereco().getBairro().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Bairro é obrigatório", List.of("bairro não pode ser vazio")))
                        .build();
            }

            Endereco savedEndereco = enderecoRepository.save(request.getEndereco());

            Deslizamento deslizamento = new Deslizamento();
            deslizamento.setUsuarioId(usuarioId);
            deslizamento.setEnderecoId(savedEndereco.getId());
            deslizamento.setDescricao(request.getDescricao());
            deslizamento.setDataOcorrencia(request.getDataOcorrencia());

            if (deslizamento.getDataOcorrencia() == null) {
                deslizamento.setDataOcorrencia(LocalDateTime.now());
            }

            Deslizamento savedDeslizamento = deslizamentoRepository.save(deslizamento);

            DeslizamentoResponse response = new DeslizamentoResponse(savedDeslizamento, savedEndereco);

            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Token inválido", List.of("ID do usuário no token não é válido")))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao criar deslizamento", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/meus")
    @Authenticated
    public Response findMyDeslizamentos() {
        try {
            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            List<Deslizamento> deslizamentos = deslizamentoRepository.findByUsuarioId(usuarioId);

            List<DeslizamentoResponse> responses = deslizamentos.stream()
                    .map(deslizamento -> {
                        Optional<Endereco> endereco = enderecoRepository.findById(deslizamento.getEnderecoId());
                        return new DeslizamentoResponse(deslizamento, endereco.orElse(null));
                    })
                    .toList();

            return Response.ok(responses).build();
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
    @Authenticated
    public Response findById(@PathParam("id") Integer id) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            Optional<Deslizamento> deslizamento = deslizamentoRepository.findById(id);
            if (deslizamento.isPresent()) {
                String userIdStr = jwt.getClaim("userId");
                Integer usuarioId = Integer.parseInt(userIdStr);
                boolean isDefesaCivil = jwt.getGroups().contains("DEFESA_CIVIL");

                if (!isDefesaCivil && !deslizamento.get().getUsuarioId().equals(usuarioId)) {
                    return Response.status(Response.Status.FORBIDDEN)
                            .entity(new ErrorResponse("Acesso negado", List.of("Você só pode ver seus próprios deslizamentos")))
                            .build();
                }

                Optional<Endereco> endereco = enderecoRepository.findById(deslizamento.get().getEnderecoId());
                DeslizamentoResponse response = new DeslizamentoResponse(deslizamento.get(), endereco.orElse(null));

                return Response.ok(response).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Deslizamento não encontrado", List.of("Deslizamento com ID " + id + " não existe")))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Authenticated
    public Response update(@PathParam("id") Integer id, DeslizamentoUpdateRequest request) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            Optional<Deslizamento> existingDeslizamento = deslizamentoRepository.findById(id);
            if (existingDeslizamento.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Deslizamento não encontrado", List.of("Deslizamento com ID " + id + " não existe")))
                        .build();
            }

            if (!existingDeslizamento.get().getUsuarioId().equals(usuarioId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("Acesso negado", List.of("Você só pode editar seus próprios deslizamentos")))
                        .build();
            }

            Deslizamento deslizamento = existingDeslizamento.get();
            deslizamento.setDescricao(request.getDescricao());
            deslizamento.setDataOcorrencia(request.getDataOcorrencia());

            Deslizamento updatedDeslizamento = deslizamentoRepository.update(deslizamento);

            Optional<Endereco> endereco = enderecoRepository.findById(updatedDeslizamento.getEnderecoId());
            DeslizamentoResponse response = new DeslizamentoResponse(updatedDeslizamento, endereco.orElse(null));

            return Response.ok(response).build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Token inválido", List.of("ID do usuário no token não é válido")))
                    .build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Deslizamento não encontrado", List.of(e.getMessage())))
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
    @Authenticated
    public Response delete(@PathParam("id") Integer id) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            Optional<Deslizamento> existingDeslizamento = deslizamentoRepository.findById(id);
            if (existingDeslizamento.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Deslizamento não encontrado", List.of("Deslizamento com ID " + id + " não existe")))
                        .build();
            }

            if (!existingDeslizamento.get().getUsuarioId().equals(usuarioId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("Acesso negado", List.of("Você só pode excluir seus próprios deslizamentos")))
                        .build();
            }

            deslizamentoRepository.delete(id);
            return Response.noContent().build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Token inválido", List.of("ID do usuário no token não é válido")))
                    .build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Deslizamento não encontrado", List.of(e.getMessage())))
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
    @PermitAll
    public Response findAll() {
        try {
            List<Deslizamento> deslizamentos = deslizamentoRepository.findAll();

            List<DeslizamentoResponse> responses = deslizamentos.stream()
                    .map(deslizamento -> {
                        Optional<Endereco> endereco = enderecoRepository.findById(deslizamento.getEnderecoId());
                        return new DeslizamentoResponse(deslizamento, endereco.orElse(null));
                    })
                    .toList();

            return Response.ok(responses).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erro interno", List.of(e.getMessage())))
                    .build();
        }
    }

}
