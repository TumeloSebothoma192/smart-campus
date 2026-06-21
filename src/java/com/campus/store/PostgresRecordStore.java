package com.campus.store;

import com.campus.rest.JsonUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PostgresRecordStore {
    private PostgresRecordStore() {
    }

    public static boolean enabled() {
        return Database.enabled();
    }

    public static List<Map<String, String>> load(String entity) {
        List<Map<String, String>> records = new ArrayList<>();
        if (!enabled()) {
            return records;
        }
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement("select id, data::text from smart_records where entity = ? order by id")) {
            statement.setString(1, entity);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Map<String, String> map = parseJsonObject(result.getString("data"));
                    map.put("id", String.valueOf(result.getLong("id")));
                    records.add(map);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Postgres load failed for " + entity + ": " + ex.getMessage());
        }
        return records;
    }

    public static void save(String entity, Map<String, String> map) {
        if (!enabled() || map == null) {
            return;
        }
        try (Connection connection = Database.connect()) {
            long id = Long.parseLong(map.get("id"));
            try (PreparedStatement statement = connection.prepareStatement("""
                insert into smart_records(entity, id, data)
                values (?, ?, ?::jsonb)
                on conflict(entity, id) do update set data = excluded.data
            """)) {
                statement.setString(1, entity);
                statement.setLong(2, id);
                statement.setString(3, SmartCampusStore.mapJson(map));
                statement.executeUpdate();
            }
        } catch (SQLException | NumberFormatException ex) {
            System.err.println("Postgres save failed for " + entity + ": " + ex.getMessage());
        }
    }

    public static void delete(String entity, String id) {
        if (!enabled()) {
            return;
        }
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement("delete from smart_records where entity = ? and id = ?")) {
            statement.setString(1, entity);
            statement.setLong(2, Long.parseLong(id));
            statement.executeUpdate();
        } catch (SQLException | NumberFormatException ex) {
            System.err.println("Postgres delete failed for " + entity + ": " + ex.getMessage());
        }
    }

    private static Map<String, String> parseJsonObject(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        Matcher matcher = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(json);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2)
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\\\", "\\");
            map.put(key, value);
        }
        if (map.isEmpty() && json != null) {
            String id = JsonUtil.string(json, "id");
            if (id != null) {
                map.put("id", id);
            }
        }
        return map;
    }
}
