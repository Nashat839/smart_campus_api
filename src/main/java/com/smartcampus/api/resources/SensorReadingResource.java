package com.smartcampus.api.resources;

import com.smartcampus.api.errors.ApiException;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.InMemoryStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final InMemoryStore store;

    @Context
    private UriInfo uriInfo;

    public SensorReadingResource(String sensorId, InMemoryStore store) {
        this.sensorId = sensorId;
        this.store = store;
    }

    @GET
    public List<SensorReading> listReadings() {
        return store.listReadings(sensorId);
    }

    @POST
    public Response addReading(SensorReading reading) {
        if (reading == null) {
            throw ApiException.badRequest("Reading body is required");
        }

        SensorReading created = store.addReading(sensorId, reading);
        URI location = UriBuilder.fromUri(uriInfo.getAbsolutePath()).path(created.getId()).build();
        return Response.created(location).entity(created).build();
    }
}
