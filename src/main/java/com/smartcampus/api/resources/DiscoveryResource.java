package com.smartcampus.api.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @Context
    private UriInfo uriInfo;

    @GET
    public Response getDiscovery() {
        String base = uriInfo.getBaseUri().toString();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("version", "1.0.0");
        body.put("description", "Smart Campus Sensor Management API");
        body.put("contact", "admin@smartcampus.ac.uk");
        body.put("status", "UP");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", base);
        links.put("rooms", base + "rooms");
        links.put("sensors", base + "sensors");
        body.put("links", links);

        return Response.ok(body).build();
    }
}
