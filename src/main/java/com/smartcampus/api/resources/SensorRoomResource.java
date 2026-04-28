package com.smartcampus.api.resources;

import com.smartcampus.api.errors.ApiException;
import com.smartcampus.api.errors.ErrorResponse;
import com.smartcampus.model.Room;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private final InMemoryStore store = InMemoryStore.getInstance();

    @Context
    private UriInfo uriInfo;

    private Map<String, Object> roomView(Room room) {
        String base = uriInfo.getBaseUri().toString();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", room.getId());
        result.put("name", room.getName());
        result.put("capacity", room.getCapacity());
        result.put("sensorIds", room.getSensorIds());

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", base + "rooms/" + room.getId());
        links.put("sensors", base + "sensors");
        result.put("_links", links);
        return result;
    }

    @GET
    public Response listRooms() {
        List<Map<String, Object>> rooms = store.listRooms().stream()
                .map(this::roomView)
                .collect(Collectors.toList());
        return Response.ok(rooms).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        try {
            if (room == null) {
                throw ApiException.badRequest("Room body is required");
            }
            Room created = store.createRoom(room);
            URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
            return Response.created(location).entity(roomView(created)).build();
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
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        return Response.ok(roomView(store.getRoom(roomId))).build();
    }

    @PUT
    @Path("/{roomId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRoom(@PathParam("roomId") String roomId, Room patch) {
        return Response.ok(roomView(store.updateRoom(roomId, patch))).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        store.deleteRoom(roomId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{roomId}/sensors")
    public Response listRoomSensors(@PathParam("roomId") String roomId) {
        List<Sensor> sensors = store.listSensorsForRoom(roomId);
        return Response.ok(sensors).build();
    }
}
