package com.climaticrisks.infrastructure;

import org.eclipse.microprofile.config.ConfigProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    static String URL = ConfigProvider.getConfig().getValue("quarkus.datasource.jdbc.url", String.class);
    static String USER = ConfigProvider.getConfig().getValue("quarkus.datasource.username", String.class);
    static String KEY = ConfigProvider.getConfig().getValue("quarkus.datasource.password", String.class);

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, KEY);
    }
}