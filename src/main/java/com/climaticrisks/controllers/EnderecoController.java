package com.climaticrisks.controllers;

import com.climaticrisks.models.Endereco;
import com.climaticrisks.repositories.EnderecoRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/enderecos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnderecoController {

    @Inject
    EnderecoRepository enderecoRepository;

    @POST
    public Response create(Endereco endereco) {
        try {
            Endereco savedEndereco = enderecoRepository.save(endereco);
            return Response.status(Response.Status.CREATED).entity(savedEndereco).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erro ao criar endereço: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Integer id) {
        try {
            Optional<Endereco> endereco = enderecoRepository.findById(id);
            if (endereco.isPresent()) {
                return Response.ok(endereco.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Endereço não encontrado").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro ao buscar endereço: " + e.getMessage()).build();
        }
    }

    @GET
    public Response findAll() {
        try {
            List<Endereco> enderecos = enderecoRepository.findAll();
            return Response.ok(enderecos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro ao buscar endereços: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, Endereco endereco) {
        try {
            endereco.setId(id);
            Endereco updatedEndereco = enderecoRepository.update(endereco);
            return Response.ok(updatedEndereco).build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Endereço não encontrado").build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erro ao atualizar endereço: " + e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        try {
            enderecoRepository.delete(id);
            return Response.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Endereço não encontrado").build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erro ao excluir endereço: " + e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno: " + e.getMessage()).build();
        }
    }
}
