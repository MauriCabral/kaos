package org.example.kaos;

import javafx.application.Application;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Launcher {
    public static void main(String[] args) {
        // Check for update file and replace if exists
        String exePath = ProcessHandle.current().info().command().orElse(null);
        if (exePath != null) {
            Path exeFile = Paths.get(exePath);
            Path tempUpdate = Paths.get(System.getProperty("java.io.tmpdir"), "kaos_update.exe");
            if (Files.exists(tempUpdate)) {
                try {
                    Files.move(tempUpdate, exeFile, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    // Log error if needed
                }
            }
        }

        Application.launch(App.class, args);
    }
}
