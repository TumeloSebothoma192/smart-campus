package com.campus.store;

import com.campus.model.User;
import com.campus.rest.JsonUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public final class SmartCampusStore {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final AtomicLong roomSeq = new AtomicLong(1);
    private static final AtomicLong bookingSeq = new AtomicLong(1);
    private static final AtomicLong moduleSeq = new AtomicLong(1);
    private static final AtomicLong maintenanceSeq = new AtomicLong(1);
    private static final AtomicLong appointmentSeq = new AtomicLong(1);
    private static final AtomicLong notificationSeq = new AtomicLong(1);
    private static final AtomicLong timetableSeq = new AtomicLong(1);

    public static final List<Map<String, String>> rooms = PostgresRecordStore.load("rooms");
    public static final List<Map<String, String>> bookings = PostgresRecordStore.load("bookings");
    public static final List<Map<String, String>> modules = PostgresRecordStore.load("modules");
    public static final List<Map<String, String>> maintenance = PostgresRecordStore.load("maintenance");
    public static final List<Map<String, String>> appointments = PostgresRecordStore.load("appointments");
    public static final List<Map<String, String>> notifications = PostgresRecordStore.load("notifications");
    public static final List<Map<String, String>> timetables = PostgresRecordStore.load("timetables");

    static {
        resetSequences();
        if (rooms.isEmpty()) {
            Map<String, String> lab = addRoom("ICT Lab A1", "45", "lab", "Engineering Block", "true", "Projector, Computers, Wi-Fi");
            addRoom("Study Room B2", "12", "study-room", "Library", "true", "Whiteboard, Smart Screen");
            addRoom("Lecture Hall C3", "120", "lecture-hall", "Science Block", "true", "Projector, Sound System");
            addModule("Web Technologies", "Learn frontend, backend, and API integration.", "WEB202", "Admin User", "admin");
            addModule("Network Security", "Campus network defence fundamentals.", "NET301", "Admin User", "admin");
            addMaintenance("4", "Student Demo", "Study Room B2", "Lights not working", "pending");
            addBooking("4", "Student Demo", lab.get("id"), lab.get("name"), "2026-06-25T09:00", "2026-06-25T11:00", "Studying", "active");
            addNotification("4", "Welcome to Smart Campus. Your dashboard is ready.", "", "false");
        }
    }

    private SmartCampusStore() {
    }

    public static synchronized Map<String, String> addRoom(String name, String capacity, String type, String building, String isAvailable, String equipment) {
        Map<String, String> room = base(roomSeq.getAndIncrement());
        room.put("name", value(name, "Room"));
        room.put("capacity", value(capacity, "1"));
        room.put("type", value(type, "lecture-hall"));
        room.put("building", value(building, "Main Campus"));
        room.put("isAvailable", value(isAvailable, "true"));
        room.put("equipment", value(equipment, ""));
        rooms.add(room);
        PostgresRecordStore.save("rooms", room);
        return room;
    }

    public static synchronized Map<String, String> addBooking(String userId, String userName, String roomId, String roomName, String startTime, String endTime, String purpose, String status) {
        Map<String, String> booking = base(bookingSeq.getAndIncrement());
        booking.put("userId", value(userId, ""));
        booking.put("userName", value(userName, ""));
        booking.put("roomId", value(roomId, ""));
        booking.put("roomName", value(roomName, ""));
        booking.put("startTime", value(startTime, ""));
        booking.put("endTime", value(endTime, ""));
        booking.put("purpose", value(purpose, "Campus booking"));
        booking.put("status", value(status, "active"));
        bookings.add(0, booking);
        PostgresRecordStore.save("bookings", booking);
        return booking;
    }

    public static synchronized Map<String, String> addModule(String moduleName, String description, String subjectCode, String createdBy, String createdByRole) {
        Map<String, String> module = base(moduleSeq.getAndIncrement());
        module.put("moduleName", value(moduleName, "Module"));
        module.put("description", value(description, ""));
        module.put("subjectCode", value(subjectCode, ""));
        module.put("createdBy", value(createdBy, ""));
        module.put("createdByRole", value(createdByRole, ""));
        module.put("registeredUsers", "");
        module.put("contentTitle", "");
        module.put("contentBody", "");
        modules.add(module);
        PostgresRecordStore.save("modules", module);
        return module;
    }

    public static synchronized Map<String, String> addMaintenance(String userId, String userName, String roomName, String issue, String status) {
        Map<String, String> item = base(maintenanceSeq.getAndIncrement());
        item.put("userId", value(userId, ""));
        item.put("userName", value(userName, ""));
        item.put("roomName", value(roomName, ""));
        item.put("issue", value(issue, ""));
        item.put("status", value(status, "pending"));
        maintenance.add(0, item);
        PostgresRecordStore.save("maintenance", item);
        return item;
    }

    public static synchronized Map<String, String> addAppointment(String studentId, String studentName, String lecturerId, String moduleId, String date) {
        Map<String, String> item = base(appointmentSeq.getAndIncrement());
        item.put("studentId", value(studentId, ""));
        item.put("studentName", value(studentName, ""));
        item.put("lecturerId", value(lecturerId, ""));
        item.put("moduleId", value(moduleId, ""));
        item.put("date", value(date, ""));
        item.put("status", "pending");
        appointments.add(0, item);
        PostgresRecordStore.save("appointments", item);
        return item;
    }

    public static synchronized Map<String, String> addNotification(String userId, String content, String module, String read) {
        Map<String, String> item = base(notificationSeq.getAndIncrement());
        item.put("userId", value(userId, ""));
        item.put("content", value(content, ""));
        item.put("module", value(module, ""));
        item.put("read", value(read, "false"));
        notifications.add(0, item);
        PostgresRecordStore.save("notifications", item);
        return item;
    }

    public static synchronized Map<String, String> addTimetable(String moduleId, String day, String startTime, String endTime, String roomName, String createdBy) {
        Map<String, String> item = base(timetableSeq.getAndIncrement());
        item.put("moduleId", value(moduleId, ""));
        item.put("day", value(day, "Monday"));
        item.put("startTime", value(startTime, "08:00"));
        item.put("endTime", value(endTime, "10:00"));
        item.put("roomName", value(roomName, ""));
        item.put("createdBy", value(createdBy, ""));
        timetables.add(item);
        PostgresRecordStore.save("timetables", item);
        return item;
    }

    public static Map<String, String> find(List<Map<String, String>> items, String id) {
        return items.stream().filter(item -> id.equals(item.get("id"))).findFirst().orElse(null);
    }

    public static List<Map<String, String>> mine(List<Map<String, String>> items, User user) {
        return items.stream().filter(item -> String.valueOf(user.getId()).equals(item.get("userId"))
                || String.valueOf(user.getId()).equals(item.get("studentId"))
                || String.valueOf(user.getId()).equals(item.get("lecturerId"))).collect(Collectors.toList());
    }

    public static String listJson(String name, List<Map<String, String>> items) {
        StringBuilder json = new StringBuilder("{\"").append(name).append("\":[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(mapJson(items.get(i)));
        }
        json.append("]}");
        return json.toString();
    }

    public static String mapJson(Map<String, String> map) {
        if (map == null) {
            return "null";
        }
        StringBuilder json = new StringBuilder("{");
        int index = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (index++ > 0) {
                json.append(",");
            }
            json.append(JsonUtil.quote(entry.getKey())).append(":").append(JsonUtil.quote(entry.getValue()));
        }
        json.append("}");
        return json.toString();
    }

    public static String smartStatsJson() {
        return "{"
                + "\"users\":" + AppStore.users.size()
                + ",\"rooms\":" + rooms.size()
                + ",\"bookings\":" + bookings.size()
                + ",\"modules\":" + modules.size()
                + ",\"maintenance\":" + maintenance.size()
                + ",\"appointments\":" + appointments.size()
                + ",\"notifications\":" + notifications.size()
                + ",\"timetables\":" + timetables.size()
                + "}";
    }

    private static Map<String, String> base(long id) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("id", String.valueOf(id));
        item.put("createdAt", LocalDateTime.now().format(FORMAT));
        item.put("updatedAt", LocalDateTime.now().format(FORMAT));
        return item;
    }

    public static void touch(Map<String, String> item) {
        if (item != null) {
            item.put("updatedAt", LocalDateTime.now().format(FORMAT));
            persistKnown(item);
        }
    }

    public static String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public static void delete(String entity, List<Map<String, String>> items, String id) {
        items.removeIf(item -> id.equals(item.get("id")));
        PostgresRecordStore.delete(entity, id);
    }

    public static void saveKnown(Map<String, String> item) {
        persistKnown(item);
    }

    private static void persistKnown(Map<String, String> item) {
        if (rooms.contains(item)) PostgresRecordStore.save("rooms", item);
        if (bookings.contains(item)) PostgresRecordStore.save("bookings", item);
        if (modules.contains(item)) PostgresRecordStore.save("modules", item);
        if (maintenance.contains(item)) PostgresRecordStore.save("maintenance", item);
        if (appointments.contains(item)) PostgresRecordStore.save("appointments", item);
        if (notifications.contains(item)) PostgresRecordStore.save("notifications", item);
        if (timetables.contains(item)) PostgresRecordStore.save("timetables", item);
    }

    private static void resetSequences() {
        roomSeq.set(nextId(rooms));
        bookingSeq.set(nextId(bookings));
        moduleSeq.set(nextId(modules));
        maintenanceSeq.set(nextId(maintenance));
        appointmentSeq.set(nextId(appointments));
        notificationSeq.set(nextId(notifications));
        timetableSeq.set(nextId(timetables));
    }

    private static long nextId(List<Map<String, String>> items) {
        return items.stream()
                .map(item -> item.get("id"))
                .filter(id -> id != null && id.matches("\\d+"))
                .mapToLong(Long::parseLong)
                .max()
                .orElse(0) + 1;
    }
}
