package org.example.kaos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.example.kaos.util.JpaUtil;
import org.example.kaos.util.UpdateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage stage) throws Exception {
        logger.info("Iniciando aplicación Kaos...");
        
        try {
            // Initialize JPA
            logger.debug("Inicializando JPA...");
            JpaUtil.getEntityManagerFactory();
            logger.debug("JPA inicializado correctamente");

            // Check for updates
            logger.debug("Verificando actualizaciones...");
            if (UpdateManager.checkForUpdate()) {
                logger.info("Nueva versión disponible: {}", UpdateManager.getLatestVersion());
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Actualización Disponible");
                alert.setHeaderText("Hay una nueva versión disponible: " + UpdateManager.getLatestVersion());
                alert.setContentText("¿Desea descargar e instalar la actualización? La aplicación se reiniciará.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        logger.info("Iniciando descarga de actualización...");
                        UpdateManager.downloadUpdate();
                        logger.info("Actualización descargada correctamente. Reiniciando aplicación...");
                        return; // Exit since restart will happen
                    } catch (IOException | InterruptedException e) {
                        logger.error("Error al descargar la actualización", e);
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Actualización Fallida");
                        errorAlert.setContentText("Error al descargar la actualización. Continuando con la versión actual.");
                        errorAlert.showAndWait();
                    }
                } else {
                    logger.info("Usuario canceló la actualización");
                }
            } else {
                logger.debug("Aplicación está en la última versión");
            }

            logger.debug("Cargando ventana de login...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
            logger.info("Ventana de login mostrada correctamente");
        } catch (Exception e) {
            logger.error("Error al iniciar la aplicación", e);
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error de Inicio");
            errorAlert.setContentText("Error al iniciar la aplicación: " + e.getMessage());
            errorAlert.showAndWait();
            throw e;
        }
    }
}