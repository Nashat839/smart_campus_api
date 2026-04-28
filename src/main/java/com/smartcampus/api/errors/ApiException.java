package com.smartcampus.api.errors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public final class ApiException extends WebApplicationException {

    private ApiException(Response.Status status, String message) {
        super(message, Response.status(status)
                .entity(new ErrorResponse(status.getStatusCode(), message))
                .type("application/json")
                .build());
    }

    public static ApiException badRequest(String message) {
        return new ApiException(Response.Status.BAD_REQUEST, message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(Response.Status.NOT_FOUND, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(Response.Status.CONFLICT, message);
    }
}
