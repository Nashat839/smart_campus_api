package com.smartcampus.api.errors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        String detail = "Referenced " + exception.getLinkType() + " id: " + exception.getLinkId();
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(422, "UNPROCESSABLE_ENTITY", exception.getMessage(), detail))
                .build();
    }
}
