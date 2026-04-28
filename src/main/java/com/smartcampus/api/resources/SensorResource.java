package com.smartcampus.api.resources;

import com.smartcampus.api.errors.ApiException;
import com.smartcampus.api.errors.ErrorResponse;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.InMemoryStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final InMemoryStore store = InMemoryStore.getInstance();

    @Context
    private UriInfo uriInfo;

    private Map<String, Object> sensorView(Sensor sensor) {
        String base = uriInfo.getBaseUri().toString();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", sensor.getId());
        result.put("type", sensor.getType());
        result.put("status", sensor.getStatus());
        result.put("currentValue", sensor.getCurrentValue());
        result.put("roomId", sensor.getRoomId());

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", base + "sensors/" + sensor.getId());
        links.put("readings", base + "sensors/" + sensor.getId() + "/readings");
        links.put("room", base + "rooms/" + sensor.getRoomId());
        result.put("_links", links);
        return result;
    }

    @GET
    public Response listSensors(@QueryParam("type") String type) {
        List<Sensor> all = store.listSensors();
        List<Sensor> selected = new ArrayList<>();
        for (Sensor sensor : all) {
            if (type == null || type.isBlank()
                    || (sensor.getType() != null && sensor.getType().equalsIgnoreCase(type))) {
                selected.add(sensor);
            }
        }
        List<Map<String, Object>> body = selected.stream().map(this::sensorView).collect(Collectors.toList());
        return Response.ok(body).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        try {
            if (sensor == null) {
                throw ApiException.badRequest("Sensor body is required");
            }
            Sensor created = store.createSensor(sensor);
            URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
            return Response.created(location).entity(sensorView(created)).build();
        } catch (ApiException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "BAD_REQUEST", ex.getMessage(), null))
                    .build();
        } catch (IllegalStateException ex) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(409, "CONFLICT", ex.getMessage(), null))
                    .build();
        }
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        return Response.ok(sensorView(store.getSensor(sensorId))).build();
    }

    @PUT
    @Path("/{sensorId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor patch) {
        try {
            return Response.ok(sensorView(store.updateSensor(sensorId, patch))).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "BAD_REQUEST", ex.getMessage(), null))
                    .build();
        }
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        store.deleteSensor(sensorId);
        return Response.noContent().build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource readings(@PathParam("sensorId") String sensorId) {
        store.getSensor(sensorId);
        return new SensorReadingResource(sensorId, store);
    }
}
