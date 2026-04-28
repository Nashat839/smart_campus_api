package com.smartcampus.store;

import com.smartcampus.api.errors.ApiException;
import com.smartcampus.api.errors.LinkedResourceNotFoundException;
import com.smartcampus.api.errors.RoomNotEmptyException;
import com.smartcampus.api.errors.SensorUnavailableException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class InMemoryStore {
    private static final InMemoryStore INSTANCE = new InMemoryStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, ConcurrentLinkedDeque<SensorReading>> readingsBySensorId = new ConcurrentHashMap<>();

    private InMemoryStore() {
        seedData();
    }

    public static InMemoryStore getInstance() {
        return INSTANCE;
    }

    public List<Room> listRooms() {
        List<Room> list = new ArrayList<>(rooms.values());
        list.sort(Comparator.comparing(Room::getId, Comparator.nullsLast(String::compareTo)));
        return list;
    }

    public Room getRoom(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw ApiException.notFound("Room with id '" + roomId + "' does not exist.");
        }
        return room;
    }

    public Room createRoom(Room room) {
        requireNonBlank(room.getId(), "Room.id is required");
        if (room.getSensorIds() == null) {
            room.setSensorIds(List.of());
        }
        if (rooms.putIfAbsent(room.getId(), room) != null) {
            throw ApiException.conflict("Room already exists: " + room.getId());
        }
        return room;
    }

    public Room updateRoom(String roomId, Room patch) {
        Objects.requireNonNull(patch, "Room body is required");
        synchronized (this) {
            Room existing = getRoom(roomId);
            if (patch.getName() != null) existing.setName(patch.getName());
            if (patch.getCapacity() != 0) existing.setCapacity(patch.getCapacity());
            return existing;
        }
    }

    public void deleteRoom(String roomId) {
        synchronized (this) {
            Room room = getRoom(roomId);
            if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
                throw new RoomNotEmptyException(roomId, room.getSensorIds());
            }
            rooms.remove(roomId);
        }
    }

    public List<Sensor> listSensors() {
        List<Sensor> list = new ArrayList<>(sensors.values());
        list.sort(Comparator.comparing(Sensor::getId, Comparator.nullsLast(String::compareTo)));
        return list;
    }

    public List<Sensor> listSensorsForRoom(String roomId) {
        Room room = getRoom(roomId);
        List<Sensor> list = new ArrayList<>();
        for (String sensorId : room.getSensorIds()) {
            Sensor sensor = sensors.get(sensorId);
            if (sensor != null) list.add(sensor);
        }
        list.sort(Comparator.comparing(Sensor::getId, Comparator.nullsLast(String::compareTo)));
        return list;
    }

    public Sensor getSensor(String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            throw ApiException.notFound("Sensor with id '" + sensorId + "' does not exist.");
        }
        return sensor;
    }

    public Sensor createSensor(Sensor sensor) {
        requireNonBlank(sensor.getId(), "Sensor.id is required");
        requireNonBlank(sensor.getRoomId(), "Sensor.roomId is required");
        synchronized (this) {
            Room room = rooms.get(sensor.getRoomId());
            if (room == null) {
                throw new LinkedResourceNotFoundException("Room", sensor.getRoomId(),
                        "Room with id '" + sensor.getRoomId() + "' was not found.");
            }
            if (sensors.putIfAbsent(sensor.getId(), sensor) != null) {
                throw ApiException.conflict("Sensor already exists: " + sensor.getId());
            }
            List<String> updated = new ArrayList<>(room.getSensorIds());
            if (!updated.contains(sensor.getId())) updated.add(sensor.getId());
            room.setSensorIds(updated);
            readingsBySensorId.putIfAbsent(sensor.getId(), new ConcurrentLinkedDeque<>());
            return sensor;
        }
    }

    public Sensor updateSensor(String sensorId, Sensor patch) {
        Objects.requireNonNull(patch, "Sensor body is required");
        synchronized (this) {
            Sensor existing = getSensor(sensorId);
            if (patch.getType() != null) existing.setType(patch.getType());
            if (patch.getStatus() != null) existing.setStatus(patch.getStatus());
            existing.setCurrentValue(patch.getCurrentValue());
            return existing;
        }
    }

    public void deleteSensor(String sensorId) {
        synchronized (this) {
            Sensor sensor = getSensor(sensorId);
            Room room = rooms.get(sensor.getRoomId());
            if (room != null && room.getSensorIds() != null) {
                List<String> updated = new ArrayList<>(room.getSensorIds());
                updated.remove(sensorId);
                room.setSensorIds(updated);
            }
            readingsBySensorId.remove(sensorId);
            sensors.remove(sensorId);
        }
    }

    public List<SensorReading> listReadings(String sensorId) {
        getSensor(sensorId);
        ConcurrentLinkedDeque<SensorReading> deque = readingsBySensorId.get(sensorId);
        return deque == null ? List.of() : new ArrayList<>(deque);
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        Objects.requireNonNull(reading, "Reading body is required");
        synchronized (this) {
            Sensor sensor = getSensor(sensorId);
            if (sensor.getStatus() == null || !sensor.getStatus().equalsIgnoreCase("ACTIVE")) {
                throw new SensorUnavailableException(sensorId, sensor.getStatus());
            }
            if (reading.getId() == null || reading.getId().isBlank()) {
                reading.setId(UUID.randomUUID().toString());
            }
            if (reading.getTimestamp() == 0L) {
                reading.setTimestamp(System.currentTimeMillis());
            }
            readingsBySensorId.putIfAbsent(sensorId, new ConcurrentLinkedDeque<>());
            readingsBySensorId.get(sensorId).add(reading);
            sensor.setCurrentValue(reading.getValue());
            return reading;
        }
    }

    private void seedData() {
        Room r1 = new Room("LIB-301", "Library Quiet Study", 30, null);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 50, null);
        Room r3 = new Room("HALL-B2", "Main Lecture Hall B", 200, null);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-002", "CO2", "ACTIVE", 412.0, "LAB-101");
        Sensor s3 = new Sensor("OCC-003", "Occupancy", "MAINTENANCE", 0.0, "HALL-B2");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        r1.setSensorIds(List.of("TEMP-001"));
        r2.setSensorIds(List.of("CO2-002"));
        r3.setSensorIds(List.of("OCC-003"));

        readingsBySensorId.put("TEMP-001", new ConcurrentLinkedDeque<>());
        readingsBySensorId.put("CO2-002", new ConcurrentLinkedDeque<>());
        readingsBySensorId.put("OCC-003", new ConcurrentLinkedDeque<>());
        readingsBySensorId.get("TEMP-001").add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 20.1));
        readingsBySensorId.get("TEMP-001").add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 21.0));
        readingsBySensorId.get("TEMP-001").add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 21.5));
        readingsBySensorId.get("CO2-002").add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 400.0));
        readingsBySensorId.get("CO2-002").add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 412.0));
    }

    private static void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw ApiException.badRequest(message);
        }
    }
}
