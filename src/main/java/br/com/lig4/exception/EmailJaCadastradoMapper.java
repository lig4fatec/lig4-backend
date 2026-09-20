package br.com.lig4.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EmailJaCadastradoMapper implements ExceptionMapper<EmailJaCadastradoException> {

    @Override
    public Response toResponse(EmailJaCadastradoException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErroResponse(exception.getMessage()))
                .build();
    }
}
