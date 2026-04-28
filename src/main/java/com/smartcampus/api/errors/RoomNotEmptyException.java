package com.smartcampus.api.errors;

import java.util.List;

public class RoomNotEmptyException extends RuntimeException {
    private final String roomId;
    private final List<String> sensorIds;

    public RoomNotEmptyException(String roomId, List<String> sensorIds) {
        super("Room '" + roomId + "' still has sensors assigned to it. Remove all sensors from the room before deleting it.");
        this.roomId = roomId;
        this.sensorIds = sensorIds;
    }

    public String getRoomId() { return roomId; }
    public List<String> getSensorIds() { return sensorIds; }
}
