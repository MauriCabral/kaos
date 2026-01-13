package org.example.kaos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.example.kaos.util.JpaUtil;
import org.example.kaos.util.UpdateManager;

import java.io.IOException;
import java.util.Optional;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Check for updates
        if (UpdateManager.checkForUpdate()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Actualización Disponible");
            alert.setHeaderText("Hay una nueva versión disponible: " + UpdateManager.getLatestVersion());
            alert.setContentText("¿Desea descargar e instalar la actualización? La aplicación se reiniciará.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    UpdateManager.downloadUpdate();
                    return; // Exit since restart will happen
                } catch (IOException | InterruptedException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Actualización Fallida");
                    errorAlert.setContentText("Error al descargar la actualización. Continuando con la versión actual.");
                    errorAlert.showAndWait();
                }
            }
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }
}