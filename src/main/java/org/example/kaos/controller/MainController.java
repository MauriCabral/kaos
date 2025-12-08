package org.example.kaos.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.kaos.util.DialogUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {
    @FXML private StackPane contentArea;
    @FXML private Button btnLogout;
    @FXML private HBox tabContainer;

    private Map<String, TabInfo> openTabs = new HashMap<>();
    private String currentTabId;
    private int orderCounter = 1;

    private static class TabInfo {
        String title;
        String fxmlPath;
        Parent content;

        TabInfo(String title, String fxmlPath, Parent content) {
            this.title = title;
            this.fxmlPath = fxmlPath;
            this.content = content;
        }
    }

    @FXML
    private void initialize() {
    }

    @FXML
    public void handleOrderAction(ActionEvent actionEvent) {
        openNewOrderTab("Pedido " + orderCounter, "/fxml/order.fxml");
    }

    @FXML
    public void handleLogoutAction(ActionEvent actionEvent) {
        boolean confirm = DialogUtil.showConfirmation("Cerrar Sesión", "¿Estás seguro de que deseas cerrar sesión?"
        );

        if (confirm) {
            performLogout();
        }
    }

    private void performLogout() {
        try {
            System.out.println("Cerrando sesión y reiniciando aplicación...");

            Stage currentStage = (Stage) btnLogout.getScene().getWindow();
            currentStage.close();

            restartApplication();

        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    private void restartApplication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Inicio de Sesión - Sistema");
            loginStage.setScene(new Scene(root));
            loginStage.setResizable(false);
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            DialogUtil.showError("Error", "No se pudo cargar la ventana de login. La aplicación se cerrará.");
            Platform.exit();
        }
    }

    private void openTab(String title, String fxmlPath) {
        String tabId = title + "_" + System.currentTimeMillis();

        try {
            Parent content = FXMLLoader.load(getClass().getResource(fxmlPath));

            openTabs.put(tabId, new TabInfo(title, fxmlPath, content));

            createTab(tabId, title);

            switchToTab(tabId);

        } catch (IOException e) {
            e.printStackTrace();
            DialogUtil.showError("Error", "No se pudo cargar: " + title);
        }
    }

    private void createTab(String tabId, String title) {
        HBox tabItem = new HBox();
        tabItem.getStyleClass().add("tab-item");
        tabItem.setUserData(tabId);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("tab-label");

        Button closeBtn = new Button("×");
        closeBtn.getStyleClass().add("tab-close-btn");
        closeBtn.setOnAction(e -> closeTab(tabId));

        tabItem.getChildren().addAll(titleLabel, closeBtn);
        tabItem.setOnMouseClicked(e -> switchToTab(tabId));

        tabContainer.getChildren().add(tabItem);
    }

    private void switchToTab(String tabId) {
        if (currentTabId != null) {
            deactivateTab(currentTabId);
        }

        activateTab(tabId);

        TabInfo tabInfo = openTabs.get(tabId);
        if (tabInfo != null) {
            contentArea.getChildren().setAll(tabInfo.content);
            currentTabId = tabId;
        }
    }

    private void activateTab(String tabId) {
        for (javafx.scene.Node node : tabContainer.getChildren()) {
            if (tabId.equals(node.getUserData())) {
                node.getStyleClass().add("active");
                break;
            }
        }
    }

    private void deactivateTab(String tabId) {
        for (javafx.scene.Node node : tabContainer.getChildren()) {
            if (tabId.equals(node.getUserData())) {
                node.getStyleClass().remove("active");
                break;
            }
        }
    }

    private void closeTab(String tabId) {
        openTabs.remove(tabId);

        tabContainer.getChildren().removeIf(node -> tabId.equals(node.getUserData()));

        if (openTabs.isEmpty()) {
            contentArea.getChildren().clear();
            currentTabId = null;
            orderCounter = 1;
            return;
        }

        if (tabId.equals(currentTabId)) {
            String nextTabId = openTabs.keySet().iterator().next();
            switchToTab(nextTabId);
        }
    }

    public void openNewOrderTab(String title, String fxmlPath) {
        openTab(title, fxmlPath);
        orderCounter++;
    }

    public void handleOrderHistoryAction(ActionEvent actionEvent) {
        openNewOrderTab("Histórico Pedidos " + orderCounter, "/fxml/order-history.fxml");
    }
}