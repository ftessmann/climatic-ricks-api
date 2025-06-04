package com.climaticrisks.controllers;

import com.climaticrisks.models.Alagamento;
import com.climaticrisks.models.Endereco;
import com.climaticrisks.repositories.AlagamentoRepository;
import com.climaticrisks.repositories.EnderecoRepository;
import com.climaticrisks.requests.AlagamentoRequest;
import com.climaticrisks.requests.AlagamentoUpdateRequest;
import com.climaticrisks.responses.AlagamentoResponse;
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

@Path("/alagamentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlagamentoController {

    @Inject
    AlagamentoRepository alagamentoRepository;

    @Inject
    EnderecoRepository enderecoRepository;

    @Inject
    JsonWebToken jwt;

    @POST
    public Response create(AlagamentoRequest request) {
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

            Alagamento alagamento = new Alagamento();
            alagamento.setUsuarioId(usuarioId);
            alagamento.setEnderecoId(savedEndereco.getId());
            alagamento.setDescricao(request.getDescricao());
            alagamento.setDataOcorrencia(request.getDataOcorrencia());

            if (alagamento.getDataOcorrencia() == null) {
                alagamento.setDataOcorrencia(LocalDateTime.now());
            }

            Alagamento savedAlagamento = alagamentoRepository.save(alagamento);

            AlagamentoResponse response = new AlagamentoResponse(savedAlagamento, savedEndereco);

            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Token inválido", List.of("ID do usuário no token não é válido")))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Erro ao criar alagamento", List.of(e.getMessage())))
                    .build();
        }
    }

    @GET
    @Path("/meus")
    @Authenticated
    public Response findMyAlagamentos() {
        try {
            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            List<Alagamento> alagamentos = alagamentoRepository.findByUsuarioId(usuarioId);

            List<AlagamentoResponse> responses = alagamentos.stream()
                    .map(alagamento -> {
                        Optional<Endereco> endereco = enderecoRepository.findById(alagamento.getEnderecoId());
                        return new AlagamentoResponse(alagamento, endereco.orElse(null));
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

            Optional<Alagamento> alagamento = alagamentoRepository.findById(id);
            if (alagamento.isPresent()) {
                String userIdStr = jwt.getClaim("userId");
                Integer usuarioId = Integer.parseInt(userIdStr);

                if (!alagamento.get().getUsuarioId().equals(usuarioId)) {
                    return Response.status(Response.Status.FORBIDDEN)
                            .entity(new ErrorResponse("Acesso negado", List.of("Você só pode ver seus próprios alagamentos")))
                            .build();
                }

                Optional<Endereco> endereco = enderecoRepository.findById(alagamento.get().getEnderecoId());
                AlagamentoResponse response = new AlagamentoResponse(alagamento.get(), endereco.orElse(null));

                return Response.ok(response).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Alagamento não encontrado", List.of("Alagamento com ID " + id + " não existe")))
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
    public Response update(@PathParam("id") Integer id, AlagamentoUpdateRequest request) {
        try {
            if (id == null || id <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("ID inválido", List.of("ID deve ser um número positivo")))
                        .build();
            }

            String userIdStr = jwt.getClaim("userId");
            Integer usuarioId = Integer.parseInt(userIdStr);

            Optional<Alagamento> existingAlagamento = alagamentoRepository.findById(id);
            if (existingAlagamento.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Alagamento não encontrado", List.of("Alagamento com ID " + id + " não existe")))
                        .build();
            }

            if (!existingAlagamento.get().getUsuarioId().equals(usuarioId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("Acesso negado", List.of("Você só pode editar seus próprios alagamentos")))
                        .build();
            }

            Alagamento alagamento = existingAlagamento.get();
            alagamento.setDescricao(request.getDescricao());
            alagamento.setDataOcorrencia(request.getDataOcorrencia());

            Alagamento updatedAlagamento = alagamentoRepository.update(alagamento);

            Optional<Endereco> endereco = enderecoRepository.findById(updatedAlagamento.getEnderecoId());
            AlagamentoResponse response = new AlagamentoResponse(updatedAlagamento, endereco.orElse(null));

            return Response.ok(response).build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Token inválido", List.of("ID do usuário no token não é válido")))
                    .build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Alagamento não encontrado", List.of(e.getMessage())))
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

            Optional<Alagamento> existingAlagamento = alagamentoRepository.findById(id);
            if (existingAlagamento.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Alagamento não encontrado", List.of("Alagamento com ID " + id + " não existe")))
                        .build();
            }

            if (!existingAlagamento.get().getUsuarioId().equals(usuarioId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("Acesso negado", List.of("Você só pode excluir seus próprios alagamentos")))
                        .build();
            }

            alagamentoRepository.delete(id);
            return Response.noContent().build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Token inválido", List.of("ID do usuário no token não é válido")))
                    .build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Alagamento não encontrado", List.of(e.getMessage())))
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
            List<Alagamento> alagamentos = alagamentoRepository.findAll();

            List<AlagamentoResponse> responses = alagamentos.stream()
                    .map(alagamento -> {
                        Optional<Endereco> endereco = enderecoRepository.findById(alagamento.getEnderecoId());
                        return new AlagamentoResponse(alagamento, endereco.orElse(null));
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
