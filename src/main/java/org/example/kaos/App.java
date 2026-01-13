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
            alert.setTitle("Update Available");
            alert.setHeaderText("A new version is available: " + UpdateManager.getLatestVersion());
            alert.setContentText("Do you want to download and install the update? The application will restart.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    UpdateManager.downloadUpdate();
                    return; // Exit since restart will happen
                } catch (IOException | InterruptedException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Update Failed");
                    errorAlert.setContentText("Failed to download the update. Continuing with current version.");
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