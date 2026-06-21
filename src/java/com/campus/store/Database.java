package com.campus.store;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static boolean initialized;

    private Database() {
    }

    public static boolean enabled() {
        return url() != null;
    }

    public static synchronized Connection connect() throws SQLException {
        String url = url();
        if (url == null) {
            throw new SQLException("DATABASE_URL is not configured.");
        }
        Connection connection = DriverManager.getConnection(url);
        if (!initialized) {
            initialize(connection);
            initialized = true;
        }
        return connection;
    }

    private static String url() {
        String jdbcUrl = System.getenv("JDBC_DATABASE_URL");
        if (jdbcUrl != null && !jdbcUrl.isBlank()) {
            return jdbcUrl;
        }
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return null;
        }
        if (databaseUrl.startsWith("jdbc:")) {
            return databaseUrl;
        }
        try {
            URI uri = URI.create(databaseUrl);
            String userInfo = uri.getUserInfo();
            String[] credentials = userInfo == null ? new String[]{"", ""} : userInfo.split(":", 2);
            String user = credentials.length > 0 ? credentials[0] : "";
            String password = credentials.length > 1 ? credentials[1] : "";
            String query = uri.getQuery() == null ? "" : "?" + uri.getQuery();
            return "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath()
                    + query + (query.isEmpty() ? "?" : "&") + "user=" + user + "&password=" + password;
        } catch (IllegalArgumentException ex) {
            return databaseUrl;
        }
    }

    private static void initialize(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                create table if not exists campus_users (
                    id bigserial primary key,
                    full_name text not null,
                    email text not null unique,
                    password text not null,
                    role text not null,
                    number text,
                    phone text
                )
            """);
            statement.executeUpdate("""
                create table if not exists service_requests (
                    id bigserial primary key,
                    created_by_id bigint,
                    created_by_name text,
                    title text not null,
                    description text,
                    category text,
                    department text,
                    priority text,
                    status text,
                    created_at timestamp default now(),
                    updated_at timestamp default now()
                )
            """);
            statement.executeUpdate("""
                create table if not exists smart_records (
                    entity text not null,
                    id bigserial not null,
                    data jsonb not null,
                    primary key(entity, id)
                )
            """);
        }
    }
}
