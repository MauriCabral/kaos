package org.example.kaos.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.kaos.config.DBConfig;

import java.sql.Connection;
import java.sql.SQLException;


public class DBConnection {
    private static final HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DBConfig.getUrl());
            config.setUsername(DBConfig.getUser());
            config.setPassword(DBConfig.getPassword());
            config.setMaximumPoolSize(DBConfig.getPoolSize());
            config.setMinimumIdle(2);
            config.setDriverClassName("org.postgresql.Driver");

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new RuntimeException("Error initializing the connection pool", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
