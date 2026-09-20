package br.com.lig4.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PartidaNotFoundMapper implements ExceptionMapper<PartidaNotFoundException> {

    @Override
    public Response toResponse(PartidaNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErroResponse(exception.getMessage()))
                .build();
    }
}
