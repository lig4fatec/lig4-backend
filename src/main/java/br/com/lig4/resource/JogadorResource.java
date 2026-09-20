package br.com.lig4.resource;

import br.com.lig4.dto.JogadorRequestDTO;
import br.com.lig4.dto.JogadorResponseDTO;
import br.com.lig4.service.JogadorService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/jogadores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JogadorResource {

    @Inject
    JogadorService service;

    @GET
    public List<JogadorResponseDTO> listar() {
        return service.listarTodos();
    }

    @GET
    @Path("/{id}")
    public JogadorResponseDTO buscarPorId(@PathParam("id") Long id) {
        return service.buscarPorId(id);
    }

    @POST
    public Response criar(@Valid JogadorRequestDTO dto) {
        JogadorResponseDTO criado = service.criar(dto);
        return Response.created(URI.create("/jogadores/" + criado.id()))
                .entity(criado)
                .build();
    }

    @PUT
    @Path("/{id}")
    public JogadorResponseDTO atualizar(@PathParam("id") Long id, @Valid JogadorRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
