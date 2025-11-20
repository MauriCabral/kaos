package org.example.kaos.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.kaos.controller.OrderDetailsController;
import org.example.kaos.entity.OrderDetail;

import java.io.IOException;
import java.util.List;

public class WindowManager {

    public static void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openOrderDetailsWindow(List<OrderDetail> orderDetails) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource("/fxml/order-details.fxml"));
            Parent root = loader.load();

            OrderDetailsController controller = loader.getController();

            controller.setOrderDetails(orderDetails);

            Stage stage = new Stage();
            stage.setTitle("Detalle del Pedido - Kaos Burgers");
            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            DialogUtil.showError("Error", "No se pudo abrir el detalle del pedido.");
        }
    }
}