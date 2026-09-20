package br.com.lig4.resource;

import br.com.lig4.dto.PartidaFinalizarDTO;
import br.com.lig4.dto.PartidaRequestDTO;
import br.com.lig4.dto.PartidaResponseDTO;
import br.com.lig4.service.PartidaService;
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

@Path("/partidas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PartidaResource {

    @Inject
    PartidaService service;

    @GET
    public List<PartidaResponseDTO> listar() {
        return service.listarTodas();
    }

    @GET
    @Path("/{id}")
    public PartidaResponseDTO buscarPorId(@PathParam("id") Long id) {
        return service.buscarPorId(id);
    }

    @POST
    public Response criar(@Valid PartidaRequestDTO dto) {
        PartidaResponseDTO criada = service.criar(dto);
        return Response.created(URI.create("/partidas/" + criada.id()))
                .entity(criada)
                .build();
    }

    /**
     * Encerra a partida, registrando o vencedor (ou empate, se vencedorId vier nulo).
     * A lógica de quem venceu (RT03/RT04) fica no motor de regras do subgrupo;
     * este endpoint só registra o resultado final.
     */
    @PUT
    @Path("/{id}")
    public PartidaResponseDTO finalizar(@PathParam("id") Long id, PartidaFinalizarDTO dto) {
        return service.finalizar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
