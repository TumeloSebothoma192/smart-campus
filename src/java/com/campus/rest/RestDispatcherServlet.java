package com.campus.rest;

import com.campus.model.ServiceRequest;
import com.campus.model.User;
import com.campus.store.AppStore;
import com.campus.store.SmartCampusStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class RestDispatcherServlet extends HttpServlet {
    private final CampusRequestResource resource = new CampusRequestResource();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        String method = req.getMethod();
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();
        if ("OPTIONS".equals(method)) {
            addCors(resp);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        addCors(resp);
        if ("GET".equals(method) && ("".equals(path) || "/".equals(path) || "/health".equals(path))) {
            writeJson(resp, "{\"status\":\"UP\",\"service\":\"Campus Service Management Backend\",\"login\":\"Use the React frontend to sign in.\"}");
            return;
        }
        if ("GET".equals(method) && "/api/health".equals(path)) {
            writeJson(resp, "{\"status\":\"UP\",\"service\":\"Smart Campus Java API\",\"runtime\":\"Java servlet\",\"netbeansReady\":true}");
            return;
        }
        if ("POST".equals(method) && "/api/auth/login".equals(path)) {
            login(readBody(req), resp);
            return;
        }
        if ("POST".equals(method) && "/api/auth/register".equals(path)) {
            register(readBody(req), resp);
            return;
        }
        User user = authenticate(req, resp);
        if (user == null) {
            return;
        }
        if (!isAllowed(user, method, path)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "This REST endpoint requires an admin account.");
            return;
        }

        try {
            dispatch(method, path, req, resp, user);
        } catch (NumberFormatException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request id.");
        }
    }

    private void dispatch(String method, String path, HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        if (path.startsWith("/api/")) {
            dispatchSmartApi(method, path, req, resp, user);
            return;
        }
        if ("GET".equals(method) && "/auth/me".equals(path)) {
            writeJson(resp, JsonUtil.userToJson(user));
            return;
        }
        if ("GET".equals(method) && "/lookups".equals(path)) {
            writeJson(resp, "{"
                    + "\"categories\":" + JsonUtil.lookupsToJson(AppStore.CATEGORIES)
                    + ",\"departments\":" + JsonUtil.lookupsToJson(AppStore.DEPARTMENTS)
                    + ",\"statuses\":" + JsonUtil.stringsToJson(AppStore.STATUSES)
                    + ",\"priorities\":" + JsonUtil.stringsToJson(AppStore.PRIORITIES)
                    + "}");
            return;
        }
        if ("GET".equals(method) && "/reports/summary".equals(path)) {
            writeJson(resp, JsonUtil.statsToJson());
            return;
        }
        if ("GET".equals(method) && "/users/get/all".equals(path)) {
            writeJson(resp, JsonUtil.usersToJson(AppStore.users));
            return;
        }
        if ("GET".equals(method) && "/request/get/mine".equals(path)) {
            writeJson(resp, JsonUtil.requestsToJson(AppStore.mine(user.getId())));
            return;
        }
        if ("GET".equals(method) && path.matches("/request/get/\\d+")) {
            long id = Long.parseLong(path.substring("/request/get/".length()));
            writeJson(resp, JsonUtil.requestToJson(resource.getRequest(id)));
            return;
        }
        if ("GET".equals(method) && "/request/get/all".equals(path)) {
            writeJson(resp, JsonUtil.formToJson(resource.getAllRequests()));
            return;
        }
        if ("POST".equals(method) && "/request/store".equals(path)) {
            ServiceRequest request = JsonUtil.parseServiceRequest(readBody(req));
            writeText(resp, resource.storeRequest(user, request));
            return;
        }
        if ("PUT".equals(method) && "/request/update".equals(path)) {
            ServiceRequest request = JsonUtil.parseServiceRequest(readBody(req));
            writeText(resp, resource.updateRequest(request));
            return;
        }
        if ("POST".equals(method) && path.matches("/request/comment/\\d+")) {
            long id = Long.parseLong(path.substring("/request/comment/".length()));
            ServiceRequest request = resource.getRequest(id);
            if (request == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Request not found.");
                return;
            }
            request.getComments().add(new com.campus.model.RequestComment(user.getFullName(), JsonUtil.parseComment(readBody(req))));
            writeJson(resp, JsonUtil.requestToJson(request));
            return;
        }
        if ("DELETE".equals(method) && path.matches("/request/delete/\\d+")) {
            long id = Long.parseLong(path.substring("/request/delete/".length()));
            writeText(resp, resource.deleteRequest(id));
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "REST endpoint not found.");
    }

    private void dispatchSmartApi(String method, String path, HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        if ("GET".equals(method) && "/api/auth/me".equals(path)) {
            writeJson(resp, JsonUtil.userToJson(user));
            return;
        }
        if ("GET".equals(method) && "/api/users/profile".equals(path)) {
            writeJson(resp, JsonUtil.userToJson(user));
            return;
        }
        if ("GET".equals(method) && "/api/admin/stats".equals(path)) {
            writeJson(resp, SmartCampusStore.smartStatsJson());
            return;
        }

        if ("GET".equals(method) && "/api/rooms".equals(path)) {
            writeJson(resp, SmartCampusStore.listJson("rooms", SmartCampusStore.rooms));
            return;
        }
        if ("GET".equals(method) && path.startsWith("/api/rooms/available")) {
            writeJson(resp, SmartCampusStore.listJson("rooms", SmartCampusStore.rooms.stream()
                    .filter(room -> "true".equalsIgnoreCase(room.get("isAvailable")))
                    .toList()));
            return;
        }
        if ("POST".equals(method) && "/api/rooms".equals(path)) {
            requireAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            String body = readBody(req);
            writeJson(resp, SmartCampusStore.mapJson(SmartCampusStore.addRoom(
                    JsonUtil.string(body, "name"),
                    JsonUtil.string(body, "capacity"),
                    JsonUtil.string(body, "type"),
                    JsonUtil.string(body, "building"),
                    "true",
                    JsonUtil.string(body, "equipment"))));
            return;
        }
        if ("PUT".equals(method) && path.matches("/api/rooms/\\d+")) {
            requireAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            Map<String, String> room = SmartCampusStore.find(SmartCampusStore.rooms, idFrom(path));
            updateRoom(room, readBody(req));
            writeJson(resp, SmartCampusStore.mapJson(room));
            return;
        }
        if ("DELETE".equals(method) && path.matches("/api/rooms/\\d+")) {
            requireAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            SmartCampusStore.delete("rooms", SmartCampusStore.rooms, idFrom(path));
            writeJson(resp, "{\"status\":\"succeeded\",\"message\":\"Room deleted\"}");
            return;
        }

        if ("POST".equals(method) && path.matches("/api/bookings/room/\\d+")) {
            String roomId = idFrom(path);
            Map<String, String> room = SmartCampusStore.find(SmartCampusStore.rooms, roomId);
            if (room == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Room not found.");
                return;
            }
            String body = readBody(req);
            writeJson(resp, SmartCampusStore.mapJson(SmartCampusStore.addBooking(
                    String.valueOf(user.getId()), user.getFullName(), roomId, room.get("name"),
                    JsonUtil.string(body, "startTime"), JsonUtil.string(body, "endTime"),
                    JsonUtil.string(body, "purpose"), "active")));
            return;
        }
        if ("GET".equals(method) && "/api/bookings/my-bookings".equals(path)) {
            writeJson(resp, SmartCampusStore.listJson("bookings", SmartCampusStore.mine(SmartCampusStore.bookings, user)));
            return;
        }
        if ("GET".equals(method) && path.matches("/api/bookings/\\d+")) {
            writeJson(resp, SmartCampusStore.mapJson(SmartCampusStore.find(SmartCampusStore.bookings, idFrom(path))));
            return;
        }
        if ("PUT".equals(method) && path.matches("/api/bookings/\\d+/cancel")) {
            Map<String, String> booking = SmartCampusStore.find(SmartCampusStore.bookings, path.split("/")[3]);
            if (booking != null) {
                booking.put("status", "cancelled");
                SmartCampusStore.touch(booking);
            }
            writeJson(resp, SmartCampusStore.mapJson(booking));
            return;
        }

        if ("GET".equals(method) && "/api/modules/all-modules".equals(path)) {
            writeJson(resp, SmartCampusStore.listJson("modules", SmartCampusStore.modules));
            return;
        }
        if ("GET".equals(method) && "/api/modules/view".equals(path)) {
            writeJson(resp, SmartCampusStore.listJson("modules", SmartCampusStore.modules));
            return;
        }
        if ("POST".equals(method) && "/api/modules/create".equals(path)) {
            requireAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            String body = readBody(req);
            writeJson(resp, SmartCampusStore.mapJson(SmartCampusStore.addModule(
                    JsonUtil.string(body, "moduleName"), JsonUtil.string(body, "description"),
                    JsonUtil.string(body, "subjectCode"), user.getFullName(), roleName(user))));
            return;
        }
        if ("POST".equals(method) && "/api/modules/register".equals(path)) {
            Map<String, String> module = SmartCampusStore.find(SmartCampusStore.modules, JsonUtil.string(readBody(req), "moduleId"));
            if (module != null) {
                module.put("registeredUsers", appendValue(module.get("registeredUsers"), user.getFullName()));
                SmartCampusStore.touch(module);
            }
            writeJson(resp, SmartCampusStore.mapJson(module));
            return;
        }
        if ("POST".equals(method) && "/api/modules/add/content".equals(path)) {
            requireLecturerOrAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            String body = readBody(req);
            Map<String, String> module = SmartCampusStore.find(SmartCampusStore.modules, JsonUtil.string(body, "moduleId"));
            if (module != null) {
                module.put("contentTitle", SmartCampusStore.value(JsonUtil.string(body, "title"), module.get("contentTitle")));
                module.put("contentBody", SmartCampusStore.value(JsonUtil.string(body, "body"), module.get("contentBody")));
                SmartCampusStore.touch(module);
            }
            writeJson(resp, SmartCampusStore.mapJson(module));
            return;
        }
        if ("DELETE".equals(method) && path.matches("/api/modules/\\d+")) {
            requireAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            SmartCampusStore.delete("modules", SmartCampusStore.modules, idFrom(path));
            writeJson(resp, "{\"status\":\"succeeded\",\"message\":\"Module deleted\"}");
            return;
        }

        if ("POST".equals(method) && "/api/maintenance/submit".equals(path)) {
            String body = readBody(req);
            writeJson(resp, SmartCampusStore.mapJson(SmartCampusStore.addMaintenance(
                    String.valueOf(user.getId()), user.getFullName(), JsonUtil.string(body, "roomName"),
                    JsonUtil.string(body, "issue"), "pending")));
            return;
        }
        if ("GET".equals(method) && "/api/maintenance/my-issues".equals(path)) {
            writeJson(resp, SmartCampusStore.listJson("issues", SmartCampusStore.mine(SmartCampusStore.maintenance, user)));
            return;
        }
        if ("GET".equals(method) && "/api/maintenance/all".equals(path)) {
            requireAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            writeJson(resp, SmartCampusStore.listJson("issues", SmartCampusStore.maintenance));
            return;
        }
        if ("PUT".equals(method) && path.matches("/api/maintenance/status/\\d+")) {
            requireAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            Map<String, String> issue = SmartCampusStore.find(SmartCampusStore.maintenance, idFrom(path));
            if (issue != null) {
                issue.put("status", SmartCampusStore.value(JsonUtil.string(readBody(req), "status"), issue.get("status")));
                SmartCampusStore.touch(issue);
            }
            writeJson(resp, SmartCampusStore.mapJson(issue));
            return;
        }

        if ("POST".equals(method) && "/api/appointments/book".equals(path)) {
            requireStudent(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            String body = readBody(req);
            writeJson(resp, SmartCampusStore.mapJson(SmartCampusStore.addAppointment(
                    String.valueOf(user.getId()), user.getFullName(), JsonUtil.string(body, "lecturerId"),
                    JsonUtil.string(body, "moduleId"), JsonUtil.string(body, "date"))));
            return;
        }
        if ("POST".equals(method) && "/api/appointments/respond".equals(path)) {
            requireLecturerOrAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            String body = readBody(req);
            Map<String, String> appointment = SmartCampusStore.find(SmartCampusStore.appointments, JsonUtil.string(body, "appointmentId"));
            if (appointment != null) {
                appointment.put("status", SmartCampusStore.value(JsonUtil.string(body, "status"), "accepted"));
                SmartCampusStore.touch(appointment);
            }
            writeJson(resp, SmartCampusStore.mapJson(appointment));
            return;
        }
        if ("GET".equals(method) && ("/api/appointments/myAppointments".equals(path) || "/api/appointments/lecturer/appointments".equals(path))) {
            writeJson(resp, SmartCampusStore.listJson("appointments", SmartCampusStore.mine(SmartCampusStore.appointments, user)));
            return;
        }

        if ("POST".equals(method) && "/api/notifications/sendNotification".equals(path)) {
            requireLecturerOrAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            String body = readBody(req);
            writeJson(resp, SmartCampusStore.mapJson(SmartCampusStore.addNotification(
                    JsonUtil.string(body, "userId"), JsonUtil.string(body, "content"),
                    JsonUtil.string(body, "module"), "false")));
            return;
        }
        if ("GET".equals(method) && "/api/notifications/getNotifications".equals(path)) {
            writeJson(resp, SmartCampusStore.listJson("notifications", SmartCampusStore.mine(SmartCampusStore.notifications, user)));
            return;
        }
        if ("PATCH".equals(method) && path.matches("/api/notifications/markAsRead/\\d+")) {
            Map<String, String> notification = SmartCampusStore.find(SmartCampusStore.notifications, idFrom(path));
            if (notification != null) {
                notification.put("read", "true");
                SmartCampusStore.touch(notification);
            }
            writeJson(resp, SmartCampusStore.mapJson(notification));
            return;
        }
        if ("PUT".equals(method) && "/api/notifications/updateNotification".equals(path)) {
            requireLecturerOrAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            String body = readBody(req);
            Map<String, String> notification = SmartCampusStore.find(SmartCampusStore.notifications, JsonUtil.string(body, "notificationId"));
            if (notification != null) {
                notification.put("content", SmartCampusStore.value(JsonUtil.string(body, "newContent"), notification.get("content")));
                SmartCampusStore.touch(notification);
            }
            writeJson(resp, SmartCampusStore.mapJson(notification));
            return;
        }

        if ("POST".equals(method) && ("/api/timetables/create".equals(path) || path.matches("/api/timetables/modules/\\d+"))) {
            requireAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            String body = readBody(req);
            String moduleId = path.matches("/api/timetables/modules/\\d+") ? idFrom(path) : JsonUtil.string(body, "moduleId");
            writeJson(resp, SmartCampusStore.mapJson(SmartCampusStore.addTimetable(
                    moduleId, JsonUtil.string(body, "day"), JsonUtil.string(body, "startTime"),
                    JsonUtil.string(body, "endTime"), JsonUtil.string(body, "roomName"), user.getFullName())));
            return;
        }
        if ("GET".equals(method) && "/api/timetables/myTimetable".equals(path)) {
            writeJson(resp, SmartCampusStore.listJson("timetable", SmartCampusStore.timetables));
            return;
        }
        if ("GET".equals(method) && path.matches("/api/timetables/modules/\\d+")) {
            String moduleId = idFrom(path);
            writeJson(resp, SmartCampusStore.listJson("timetable", SmartCampusStore.timetables.stream()
                    .filter(item -> moduleId.equals(item.get("moduleId")))
                    .toList()));
            return;
        }
        if ("PUT".equals(method) && path.matches("/api/timetables/\\d+")) {
            requireAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            String body = readBody(req);
            Map<String, String> timetable = SmartCampusStore.find(SmartCampusStore.timetables, idFrom(path));
            if (timetable != null) {
                putIfPresent(timetable, "day", JsonUtil.string(body, "day"));
                putIfPresent(timetable, "startTime", JsonUtil.string(body, "startTime"));
                putIfPresent(timetable, "endTime", JsonUtil.string(body, "endTime"));
                putIfPresent(timetable, "roomName", JsonUtil.string(body, "roomName"));
                SmartCampusStore.touch(timetable);
            }
            writeJson(resp, SmartCampusStore.mapJson(timetable));
            return;
        }
        if ("DELETE".equals(method) && path.matches("/api/timetables/\\d+")) {
            requireAdmin(user, resp);
            if (resp.isCommitted()) {
                return;
            }
            SmartCampusStore.delete("timetables", SmartCampusStore.timetables, idFrom(path));
            writeJson(resp, "{\"status\":\"succeeded\",\"message\":\"Timetable deleted\"}");
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Smart Campus API endpoint not found.");
    }

    private User authenticate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String header = req.getHeader("Authorization");
        if (header == null) {
            challenge(resp);
            return null;
        }
        if (header.startsWith("Bearer ")) {
            User user = userFromToken(header.substring("Bearer ".length()).trim());
            if (user == null) {
                challenge(resp);
            }
            return user;
        }
        if (!header.startsWith("Basic ")) {
            challenge(resp);
            return null;
        }
        String token = header.substring("Basic ".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        int colon = credentials.indexOf(':');
        if (colon < 1) {
            challenge(resp);
            return null;
        }
        User user = AppStore.login(credentials.substring(0, colon), credentials.substring(colon + 1));
        if (user == null) {
            challenge(resp);
            return null;
        }
        return user;
    }

    private boolean isAllowed(User user, String method, String path) {
        if (path.startsWith("/api/")) {
            return true;
        }
        if ("GET".equals(method) && (path.startsWith("/request/get/") || "/auth/me".equals(path) || "/lookups".equals(path))) {
            return true;
        }
        if ("POST".equals(method) && "/request/store".equals(path)) {
            return true;
        }
        if ("POST".equals(method) && path.startsWith("/request/comment/")) {
            return true;
        }
        if ("PUT".equals(method) && "/request/update".equals(path)) {
            return isAdmin(user) || "DEPARTMENT_MANAGER".equals(user.getRole()) || "LECTURER".equals(user.getRole());
        }
        return isAdmin(user);
    }

    private void addCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Authorization,Content-Type");
    }

    private void login(String body, HttpServletResponse resp) throws IOException {
        String username = firstPresent(JsonUtil.string(body, "username"), JsonUtil.string(body, "email"));
        String password = JsonUtil.string(body, "password");
        User user = AppStore.users.stream()
                .filter(item -> item.getEmail().equalsIgnoreCase(username)
                || item.getNumber().equalsIgnoreCase(username))
                .filter(item -> item.getPassword().equals(password))
                .findFirst().orElse(null);
        if (user == null) {
            challenge(resp);
            return;
        }
        writeJson(resp, authJson(user));
    }

    private void register(String body, HttpServletResponse resp) throws IOException {
        String name = SmartCampusStore.value(JsonUtil.string(body, "name"), "Campus");
        String surname = SmartCampusStore.value(JsonUtil.string(body, "surname"), "User");
        String role = normalizeRole(JsonUtil.string(body, "role"));
        String number = firstPresent(JsonUtil.string(body, "studentNumber"), JsonUtil.string(body, "staffNumber"));
        if (number == null || number.isBlank()) {
            number = role.substring(0, 1) + (AppStore.users.size() + 1);
        }
        String email = firstPresent(JsonUtil.string(body, "email"), generatedEmail(name, surname, role, number));
        String password = SmartCampusStore.value(JsonUtil.string(body, "password"), "Password123");
        User user = AppStore.addUser(name + " " + surname, email, password, role, number, "");
        resp.setStatus(HttpServletResponse.SC_CREATED);
        writeJson(resp, authJson(user));
    }

    private String authJson(User user) {
        return "{"
                + "\"status\":\"succeeded\""
                + ",\"token\":" + JsonUtil.quote(tokenFor(user))
                + ",\"user\":" + JsonUtil.userToJson(user)
                + "}";
    }

    private String tokenFor(User user) {
        return Base64.getEncoder().encodeToString((user.getEmail() + ":" + user.getPassword()).getBytes(StandardCharsets.UTF_8));
    }

    private User userFromToken(String token) {
        try {
            String credentials = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            int colon = credentials.indexOf(':');
            if (colon < 1) {
                return null;
            }
            return AppStore.login(credentials.substring(0, colon), credentials.substring(colon + 1));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void updateRoom(Map<String, String> room, String body) {
        if (room == null) {
            return;
        }
        putIfPresent(room, "name", JsonUtil.string(body, "name"));
        putIfPresent(room, "capacity", JsonUtil.string(body, "capacity"));
        putIfPresent(room, "type", JsonUtil.string(body, "type"));
        putIfPresent(room, "building", JsonUtil.string(body, "building"));
        putIfPresent(room, "isAvailable", JsonUtil.string(body, "isAvailable"));
        putIfPresent(room, "equipment", JsonUtil.string(body, "equipment"));
        SmartCampusStore.touch(room);
    }

    private void putIfPresent(Map<String, String> item, String key, String value) {
        if (value != null) {
            item.put(key, value);
        }
    }

    private String appendValue(String existing, String value) {
        if (existing == null || existing.isBlank()) {
            return value;
        }
        return existing.contains(value) ? existing : existing + ", " + value;
    }

    private String idFrom(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    private String firstPresent(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private String generatedEmail(String name, String surname, String role, String number) {
        if ("STUDENT".equals(role)) {
            return number + "@tut4life.ac.za";
        }
        String prefix = name.substring(0, 1).toLowerCase() + "." + surname.toLowerCase();
        if ("ADMIN".equals(role)) {
            return prefix + "@admin.tut.ac.za";
        }
        return prefix + "@tut.ac.za";
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "STUDENT";
        }
        String normalized = role.trim().toUpperCase().replace("-", "_");
        return "LECTURER".equals(normalized) ? "LECTURER" : AppStore.ROLES.contains(normalized) ? normalized : "STUDENT";
    }

    private String roleName(User user) {
        return user.getRole().toLowerCase();
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getRole());
    }

    private void requireAdmin(User user, HttpServletResponse resp) throws IOException {
        if (!isAdmin(user)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access is required.");
        }
    }

    private void requireLecturerOrAdmin(User user, HttpServletResponse resp) throws IOException {
        if (!isAdmin(user) && !"LECTURER".equals(user.getRole()) && !"DEPARTMENT_MANAGER".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Lecturer or admin access is required.");
        }
    }

    private void requireStudent(User user, HttpServletResponse resp) throws IOException {
        if (!"STUDENT".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Student access is required.");
        }
    }

    private void challenge(HttpServletResponse resp) throws IOException {
        resp.setHeader("WWW-Authenticate", "Basic realm=\"CampusServiceRealm\"");
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "REST API requires Basic authentication.");
    }

    private String readBody(HttpServletRequest req) throws IOException {
        return new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void writeJson(HttpServletResponse resp, String json) throws IOException {
        resp.setContentType("application/json");
        resp.getWriter().write(json);
    }

    private void writeText(HttpServletResponse resp, String text) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().write(text);
    }
}
