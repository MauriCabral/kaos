package org.example.kaos;

import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        logger.info("Iniciando Launcher de Kaos...");
        try {
            // Check for update file and replace if exists
            String exePath = ProcessHandle.current().info().command().orElse(null);
            if (exePath != null) {
                logger.debug("Ruta del ejecutable: {}", exePath);
                Path exeFile = Paths.get(exePath);
                Path tempUpdate = Paths.get(System.getProperty("java.io.tmpdir"), "kaos_update.exe");
                if (Files.exists(tempUpdate)) {
                    logger.info("Archivo de actualización encontrado. Reemplazando ejecutable...");
                    try {
                        Files.move(tempUpdate, exeFile, StandardCopyOption.REPLACE_EXISTING);
                        logger.info("Ejecutable reemplazado correctamente");
                    } catch (Exception e) {
                        logger.error("Error al reemplazar el ejecutable con la actualización", e);
                    }
                } else {
                    logger.debug("No se encontró archivo de actualización");
                }
            } else {
                logger.debug("No se pudo obtener la ruta del ejecutable");
            }

            logger.debug("Iniciando aplicación principal...");
            Application.launch(App.class, args);
        } catch (Exception e) {
            logger.error("Error crítico en el Launcher", e);
            System.exit(1);
        }
    }
}
