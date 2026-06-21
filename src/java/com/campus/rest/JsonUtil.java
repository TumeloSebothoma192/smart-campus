package com.campus.rest;

import com.campus.model.LookupItem;
import com.campus.model.RequestComment;
import com.campus.model.ServiceRequest;
import com.campus.model.User;
import com.campus.store.AppStore;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static ServiceRequest parseServiceRequest(String json) {
        ServiceRequest request = new ServiceRequest();
        request.setId(number(json, "id", number(json, "requestId", 0)));
        request.setTitle(string(json, "title"));
        request.setDescription(string(json, "description"));
        setLookup(request, json);
        request.setPriority(string(json, "priority"));
        request.setStatus(string(json, "status"));
        return request;
    }

    public static String parseComment(String json) {
        String comment = string(json, "comment");
        return comment == null ? "" : comment;
    }

    public static String requestToJson(ServiceRequest request) {
        if (request == null) {
            return "null";
        }
        StringBuilder json = new StringBuilder();
        json.append("{");
        field(json, "requestId", String.valueOf(request.getRequestId()), false);
        field(json, "title", quote(request.getTitle()), true);
        field(json, "description", quote(request.getDescription()), true);
        field(json, "category", quote(request.getCategoryName()), true);
        field(json, "department", quote(request.getDepartmentName()), true);
        field(json, "priority", quote(request.getPriority()), true);
        field(json, "status", quote(request.getStatus()), true);
        field(json, "createdById", String.valueOf(request.getCreatedById()), true);
        field(json, "createdByName", quote(request.getCreatedByName()), true);
        field(json, "createdAt", quote(request.getFormattedCreatedAt()), true);
        field(json, "updatedAt", quote(request.getFormattedUpdatedAt()), true);
        json.append(",\"comments\":[");
        for (int i = 0; i < request.getComments().size(); i++) {
            RequestComment comment = request.getComments().get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"author\":").append(quote(comment.getAuthor()))
                    .append(",\"text\":").append(quote(comment.getText()))
                    .append(",\"createdAt\":").append(quote(comment.getFormattedCreatedAt()))
                    .append("}");
        }
        json.append("]}");
        return json.toString();
    }

    public static String formToJson(RequestForm form) {
        return requestsToJson(form.getRequests());
    }

    public static String requestsToJson(List<ServiceRequest> requests) {
        StringBuilder json = new StringBuilder("{\"requests\":[");
        for (int i = 0; i < requests.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(requestToJson(requests.get(i)));
        }
        json.append("]}");
        return json.toString();
    }

    public static String userToJson(User user) {
        if (user == null) {
            return "null";
        }
        return "{"
                + "\"id\":" + user.getId()
                + ",\"fullName\":" + quote(user.getFullName())
                + ",\"email\":" + quote(user.getEmail())
                + ",\"role\":" + quote(user.getRole())
                + ",\"number\":" + quote(user.getNumber())
                + ",\"phone\":" + quote(user.getPhone())
                + "}";
    }

    public static String usersToJson(List<User> users) {
        StringBuilder json = new StringBuilder("{\"users\":[");
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(userToJson(users.get(i)));
        }
        json.append("]}");
        return json.toString();
    }

    public static String lookupsToJson(List<LookupItem> items) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            LookupItem item = items.get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"id\":").append(item.getId()).append(",\"name\":").append(quote(item.getName())).append("}");
        }
        json.append("]");
        return json.toString();
    }

    public static String stringsToJson(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(quote(values.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    public static String statsToJson() {
        List<ServiceRequest> requests = AppStore.requests;
        return "{"
                + "\"totalRequests\":" + requests.size()
                + ",\"pendingRequests\":" + AppStore.count(requests, "PENDING")
                + ",\"inProgressRequests\":" + AppStore.count(requests, "IN_PROGRESS")
                + ",\"resolvedRequests\":" + AppStore.count(requests, "RESOLVED")
                + ",\"totalUsers\":" + AppStore.users.size()
                + ",\"totalDepartments\":" + AppStore.DEPARTMENTS.size()
                + ",\"totalCategories\":" + AppStore.CATEGORIES.size()
                + "}";
    }

    public static String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n") + "\"";
    }

    private static void field(StringBuilder json, String name, String value, boolean comma) {
        if (comma) {
            json.append(",");
        }
        json.append("\"").append(name).append("\":").append(value);
    }

    private static void setLookup(ServiceRequest request, String json) {
        String category = string(json, "category");
        if (category == null) {
            category = string(json, "categoryName");
        }
        String department = string(json, "department");
        if (department == null) {
            department = string(json, "departmentName");
        }
        if (category != null) {
            request.setCategory(category);
        }
        if (department != null) {
            request.setDepartment(department);
        }
    }

    public static String string(String json, String name) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL).matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r").replace("\\\\", "\\");
    }

    public static long number(String json, String name, long fallback) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:\\s*(\\d+)").matcher(json);
        if (!matcher.find()) {
            return fallback;
        }
        return Long.parseLong(matcher.group(1));
    }
}
