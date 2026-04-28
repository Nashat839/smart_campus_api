package com.smartcampus.api.errors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        String detail = "Sensor status is " + exception.getStatus();
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(403, "FORBIDDEN", exception.getMessage(), detail))
                .build();
    }
}
