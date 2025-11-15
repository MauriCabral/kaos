package org.example.kaos.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DBConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties was not found");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error reading application.properties", e);
        }
    }

    public static String getUrl() {
        return properties.getProperty("db.url");
    }

    public static String getUser() {
        return properties.getProperty("db.user");
    }

    public static String getPassword() {
        return properties.getProperty("db.password");
    }

    public static int getPoolSize() {
        return Integer.parseInt(properties.getProperty("db.pool.size", "10"));
    }
}
