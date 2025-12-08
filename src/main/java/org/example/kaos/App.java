package org.example.kaos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.kaos.util.JpaUtil;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        JpaUtil.getEntityManager().close();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }
}