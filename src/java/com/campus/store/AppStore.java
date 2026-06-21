package com.campus.store;

import com.campus.model.LookupItem;
import com.campus.model.ServiceRequest;
import com.campus.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class AppStore {
    public static final List<String> ROLES = Arrays.asList("STUDENT", "STAFF", "LECTURER", "DEPARTMENT_MANAGER", "ADMIN");
    public static final List<LookupItem> CATEGORIES = Arrays.asList(new LookupItem(1, "IT Support"), new LookupItem(2, "Library Support"), new LookupItem(3, "Maintenance"), new LookupItem(4, "Finance Office"), new LookupItem(5, "Student Residence"), new LookupItem(6, "Security"), new LookupItem(7, "Academic Administration"));
    public static final List<LookupItem> DEPARTMENTS = Arrays.asList(new LookupItem(1, "ICT Department"), new LookupItem(2, "Library Services"), new LookupItem(3, "Facilities & Maintenance"), new LookupItem(4, "Finance Office"), new LookupItem(5, "Residence Office"), new LookupItem(6, "Campus Security"), new LookupItem(7, "Academic Administration"));
    public static final List<String> STATUSES = Arrays.asList("PENDING", "APPROVED", "REJECTED", "IN_PROGRESS", "RESOLVED", "CLOSED");
    public static final List<String> PRIORITIES = Arrays.asList("LOW", "MEDIUM", "HIGH", "URGENT");

    private static final AtomicLong userSeq = new AtomicLong(1);
    private static final AtomicLong reqSeq = new AtomicLong(1);
    public static final List<User> users = new ArrayList<>();
    public static final List<ServiceRequest> requests = new ArrayList<>();

    static {
        loadFromDatabase();
        if (users.isEmpty()) {
            User admin = addUser("Admin User", "admin@campus.local", "admin123", "ADMIN", "A001", "000");
            addUser("Department Manager", "manager@campus.local", "manager123", "DEPARTMENT_MANAGER", "M001", "000");
            addUser("Lecturer Demo", "lecturer@campus.local", "lecturer123", "LECTURER", "L001", "000");
            User staff = addUser("Staff Demo", "staff@campus.local", "staff123", "STAFF", "E001", "000");
            User student = addUser("Student Demo", "student@campus.local", "student123", "STUDENT", "S001", "000");
            addRequest(student, "Wi-Fi not working in lab", "The computer lab Wi-Fi keeps dropping during practical sessions.", "IT Support", "ICT Department");
            addRequest(student, "Residence light broken", "Room B12 corridor light is broken.", "Maintenance", "Facilities & Maintenance");
            addRequest(staff, "Projector not powering on", "The boardroom projector does not turn on before meetings.", "IT Support", "ICT Department");
            addRequest(admin, "Backend deployment check", "Confirm hosted API and database are connected.", "Academic Administration", "Academic Administration");
        }
        resetSequences();
    }

    public static synchronized User addUser(String name, String email, String password, String role, String number, String phone) {
        User existing = users.stream().filter(user -> user.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
        if (existing != null) {
            return existing;
        }
        User user = new User(userSeq.getAndIncrement(), name, email, password, role, number, phone);
        users.add(user);
        persistUser(user);
        return user;
    }

    public static User login(String email, String password) {
        return users.stream().filter(user -> user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)).findFirst().orElse(null);
    }

    public static User byId(long id) {
        return users.stream().filter(user -> user.getId() == id).findFirst().orElse(null);
    }

    public static synchronized ServiceRequest addRequest(User user, String title, String description, String category, String department) {
        ServiceRequest request = new ServiceRequest(reqSeq.getAndIncrement(), user.getId(), user.getFullName(), title, description, category, department);
        requests.add(0, request);
        persistRequest(request);
        return request;
    }

    public static ServiceRequest request(long id) {
        return requests.stream().filter(request -> request.getId() == id).findFirst().orElse(null);
    }

    public static List<ServiceRequest> mine(long userId) {
        return requests.stream().filter(request -> request.getCreatedById() == userId).collect(Collectors.toList());
    }

    public static long count(List<ServiceRequest> list, String status) {
        return list.stream().filter(request -> request.getStatus().equals(status)).count();
    }

    public static void persistRequest(ServiceRequest request) {
        if (!Database.enabled() || request == null) {
            return;
        }
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement("""
                insert into service_requests(id, created_by_id, created_by_name, title, description, category, department, priority, status)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict(id) do update set
                    title = excluded.title,
                    description = excluded.description,
                    category = excluded.category,
                    department = excluded.department,
                    priority = excluded.priority,
                    status = excluded.status,
                    updated_at = now()
             """)) {
            statement.setLong(1, request.getId());
            statement.setLong(2, request.getCreatedById());
            statement.setString(3, request.getCreatedByName());
            statement.setString(4, request.getTitle());
            statement.setString(5, request.getDescription());
            statement.setString(6, request.getCategoryName());
            statement.setString(7, request.getDepartmentName());
            statement.setString(8, request.getPriority());
            statement.setString(9, request.getStatus());
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Service request save failed: " + ex.getMessage());
        }
    }

    public static void deleteRequest(long id) {
        if (!Database.enabled()) {
            return;
        }
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement("delete from service_requests where id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Service request delete failed: " + ex.getMessage());
        }
    }

    private static void persistUser(User user) {
        if (!Database.enabled()) {
            return;
        }
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement("""
                insert into campus_users(id, full_name, email, password, role, number, phone)
                values (?, ?, ?, ?, ?, ?, ?)
                on conflict(email) do nothing
             """)) {
            statement.setLong(1, user.getId());
            statement.setString(2, user.getFullName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.setString(5, user.getRole());
            statement.setString(6, user.getNumber());
            statement.setString(7, user.getPhone());
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("User save failed: " + ex.getMessage());
        }
    }

    private static void loadFromDatabase() {
        if (!Database.enabled()) {
            return;
        }
        try (Connection connection = Database.connect()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from campus_users order by id");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    users.add(new User(result.getLong("id"), result.getString("full_name"), result.getString("email"), result.getString("password"), result.getString("role"), result.getString("number"), result.getString("phone")));
                }
            }
            try (PreparedStatement statement = connection.prepareStatement("select * from service_requests order by id desc");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ServiceRequest request = new ServiceRequest(result.getLong("id"), result.getLong("created_by_id"), result.getString("created_by_name"), result.getString("title"), result.getString("description"), result.getString("category"), result.getString("department"));
                    request.setPriority(result.getString("priority"));
                    request.setStatus(result.getString("status"));
                    requests.add(request);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Database load failed: " + ex.getMessage());
        }
    }

    private static void resetSequences() {
        userSeq.set(users.stream().mapToLong(User::getId).max().orElse(0) + 1);
        reqSeq.set(requests.stream().mapToLong(ServiceRequest::getId).max().orElse(0) + 1);
    }
}
