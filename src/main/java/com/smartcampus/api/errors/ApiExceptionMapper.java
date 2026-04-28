package com.smartcampus.api.errors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    @Override
    public Response toResponse(ApiException exception) {
        Response original = exception.getResponse();
        Object entity = original.getEntity();
        if (entity == null) {
            entity = new ErrorResponse(original.getStatus(), exception.getMessage());
        }
        return Response.status(original.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(entity)
                .build();
    }
}
