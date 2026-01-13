package org.example.kaos.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.kaos.util.DialogUtil;
import org.example.kaos.util.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML private StackPane contentArea;
    @FXML private Button btnLogout;
    @FXML private HBox tabContainer;
    @FXML private Button btnDelivery;
    @FXML private Button btnStatistics;

    private Map<String, TabInfo> openTabs = new HashMap<>();
    private String currentTabId;
    private int orderCounter = 1;
    private static final int MAX_TABS = 10;

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
        if (Session.getInstance().getCurrentUser().getId() == 1 || "ADMIN".equals(Session.getInstance().getCurrentUser().getRole())) {
            btnDelivery.setVisible(true);
            btnStatistics.setVisible(true);
        } else {
            btnDelivery.setVisible(false);
            btnStatistics.setVisible(false);
        }

        // Add keyboard shortcuts
        Platform.runLater(() -> {
            Scene scene = tabContainer.getScene();
            if (scene != null) {
                scene.setOnKeyPressed(this::handleKeyPressed);
            }
        });
    }

    @FXML
    public void handleOrderAction(ActionEvent actionEvent) {
        if (openTabs.size() >= MAX_TABS) {
            DialogUtil.showWarning("Límite alcanzado", "No puedes tener más de " + MAX_TABS + " pestañas abiertas.");
            return;
        }
        openNewOrderTab("Pedido " + orderCounter, "/fxml/order.fxml");
    }

    @FXML
    public void handleLogoutAction(ActionEvent actionEvent) {
        boolean confirm = DialogUtil.showConfirmation("Cerrar Sesión", "¿Estás seguro de que deseas cerrar sesión?");

        if (confirm) {
            performLogout();
        }
    }

    private void performLogout() {
        try {
            logger.info("Cerrando sesión y reiniciando aplicación...");

            Stage currentStage = (Stage) btnLogout.getScene().getWindow();
            currentStage.close();

            restartApplication();

        } catch (Exception e) {
            logger.error("Error during logout", e);
            DialogUtil.showError("Error", "Error during logout: " + e.getMessage());
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
            DialogUtil.showError("Error", "No se pudo cargar la ventana de login: " + e.getMessage() + ". La aplicación se cerrará.");
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
            DialogUtil.showError("Error", "No se pudo cargar: " + title + ". " + e.getMessage());
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

        // Add context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem closeItem = new MenuItem("Cerrar");
        closeItem.setOnAction(e -> closeTab(tabId));
        MenuItem closeOthersItem = new MenuItem("Cerrar otras");
        closeOthersItem.setOnAction(e -> closeOtherTabs(tabId));
        MenuItem closeAllItem = new MenuItem("Cerrar todas");
        closeAllItem.setOnAction(e -> closeAllTabs());
        contextMenu.getItems().addAll(closeItem, closeOthersItem, closeAllItem);
        tabItem.setOnContextMenuRequested(event -> {
            contextMenu.show(tabItem, event.getScreenX(), event.getScreenY());
        });

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

    private void handleKeyPressed(KeyEvent event) {
        if (event.isControlDown()) {
            switch (event.getCode()) {
                case N:
                    handleOrderAction(null);
                    event.consume();
                    break;
                case W:
                    if (currentTabId != null) {
                        closeTab(currentTabId);
                    }
                    event.consume();
                    break;
                case TAB:
                    switchToNextTab();
                    event.consume();
                    break;
            }
        }
    }

    private void switchToNextTab() {
        if (openTabs.isEmpty()) return;
        List<String> tabIds = new ArrayList<>(openTabs.keySet());
        if (currentTabId == null) {
            switchToTab(tabIds.get(0));
            return;
        }
        int currentIndex = tabIds.indexOf(currentTabId);
        int nextIndex = (currentIndex + 1) % tabIds.size();
        switchToTab(tabIds.get(nextIndex));
    }

    public void openNewOrderTab(String title, String fxmlPath) {
        openTab(title, fxmlPath);
        orderCounter++;
    }

    public void handleOrderHistoryAction(ActionEvent actionEvent) {
        openNewOrderTab("Histórico Pedidos " + orderCounter, "/fxml/order-history.fxml");
    }

    public void handleDelivery(ActionEvent actionEvent) {
        openNewOrderTab("Gestión Delivery " + orderCounter, "/fxml/delivery-manager.fxml");
    }

    public void handleStatistics(ActionEvent actionEvent) {
        openNewOrderTab("Estadísticas Pedidos " + orderCounter, "/fxml/order-statistics.fxml");
    }

    private void closeOtherTabs(String keepTabId) {
        List<String> toClose = new ArrayList<>();
        for (String tabId : openTabs.keySet()) {
            if (!tabId.equals(keepTabId)) {
                toClose.add(tabId);
            }
        }
        for (String tabId : toClose) {
            closeTab(tabId);
        }
    }

    private void closeAllTabs() {
        List<String> toClose = new ArrayList<>(openTabs.keySet());
        for (String tabId : toClose) {
            closeTab(tabId);
        }
    }
}