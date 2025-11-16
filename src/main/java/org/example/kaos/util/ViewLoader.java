package org.example.kaos.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ViewLoader {
    public static void loadIn(StackPane target, String fxmlPath) {
        try {
            Node node = FXMLLoader.load(ViewLoader.class.getResource(fxmlPath));
            target.getChildren().setAll(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
