package org.example.kaos.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.kaos.controller.BurgerSelectionController;
import org.example.kaos.controller.OrderDetailsController;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.Order;
import org.example.kaos.entity.OrderDetail;

import java.io.IOException;
import java.util.List;

public class WindowManager {

    public static <T> T openWindow(String fxmlPath, String title, Object initData) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            T controller = loader.getController();

            if (initData != null) {
                if (controller instanceof BurgerSelectionController && initData instanceof Burger) {
                    ((BurgerSelectionController) controller).setBurger((Burger) initData);
                }
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Stage openOrderDetailsWindow(List<OrderDetail> orderDetails, Order order, boolean isEditMode) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource("/fxml/order-details.fxml"));
            Parent root = loader.load();

            OrderDetailsController controller = loader.getController();

            Stage stage = new Stage();

            if (order != null) {
                controller.setOrder(order, isEditMode);
                String title = isEditMode ?
                        "Editar Pedido - " + order.getOrderNumber() :
                        "Detalle del Pedido - " + order.getOrderNumber();
                stage.setTitle(title);
            } else if (orderDetails != null && !orderDetails.isEmpty()) {
                controller.setOrderDetails(orderDetails);
                stage.setTitle("Detalle del Pedido - Kaos Burgers");
            }

            controller.setStage(stage);
            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(false);
            stage.show();

            return stage;
        } catch (IOException e) {
            e.printStackTrace();
            DialogUtil.showError("Error", "No se pudo abrir el detalle del pedido.");
            return null;
        }
    }
}