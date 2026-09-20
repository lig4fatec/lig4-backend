package br.com.lig4.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PartidaInvalidaMapper implements ExceptionMapper<PartidaInvalidaException> {

    @Override
    public Response toResponse(PartidaInvalidaException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErroResponse(exception.getMessage()))
                .build();
    }
}
