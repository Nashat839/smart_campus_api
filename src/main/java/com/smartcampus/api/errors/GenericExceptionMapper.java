package com.smartcampus.api.errors;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof ApiException) {
            return ((ApiException) exception).getResponse();
        }
        if (exception instanceof NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(404, "NOT_FOUND", "Requested path was not found.", null))
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(500, "INTERNAL_SERVER_ERROR", "An unexpected server error occurred.", null))
                .build();
    }
}
