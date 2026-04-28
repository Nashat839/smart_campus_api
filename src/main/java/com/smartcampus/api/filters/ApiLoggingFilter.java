package com.smartcampus.api.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(ApiLoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = (requestContext.getUriInfo() == null) ? "" : requestContext.getUriInfo().getRequestUri().toString();
        LOG.info(() -> "IN  " + method + " " + uri);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = (requestContext.getUriInfo() == null) ? "" : requestContext.getUriInfo().getRequestUri().toString();
        int status = responseContext.getStatus();
        LOG.info(() -> "OUT " + status + " " + method + " " + uri);
    }
}
