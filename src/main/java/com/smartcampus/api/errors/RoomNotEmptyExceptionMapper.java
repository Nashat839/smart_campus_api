package com.smartcampus.api.errors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        String detail = "Remove sensors from room " + exception.getRoomId() + " before deleting it.";
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(409, "CONFLICT", exception.getMessage(), detail))
                .build();
    }
}
