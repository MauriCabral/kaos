package org.example.kaos.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class SchemaManager {
    private static boolean initialized = false; 

    public static void createSchemaAndTables() {
        if (initialized) {
            System.out.println("Schema already initialized, skipping...");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {

            executeSqlFile("/db/schema.sql", stmt);

            executeSqlFile("/db/data.sql", stmt);

            initialized = true;
            System.out.println("✅ Schema and initial data created successfully");

        } catch (Exception e) {
            throw new RuntimeException("Error creating schema and tables", e);
        }
    }

    private static void executeSqlFile(String filePath, Statement stmt) {
        try (InputStream input = SchemaManager.class.getResourceAsStream(filePath)) {
            if (input == null) {
                throw new RuntimeException("No se pudo encontrar " + filePath + " en el classpath");
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
                String sql = br.lines().collect(Collectors.joining("\n"));
                stmt.execute(sql);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error executing " + filePath, e);
        }
    }

    public static void reset() {
        initialized = false;
    }
}