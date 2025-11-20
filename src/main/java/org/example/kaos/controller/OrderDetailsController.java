package org.example.kaos.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.kaos.entity.OrderDetail;
import org.example.kaos.util.DialogUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderDetailsController {

    @FXML private Label orderNumberLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label orderTimeLabel;
    @FXML private TextArea notesTextArea;
    @FXML private VBox itemsContainer;

    private List<OrderDetail> orderDetails;
    private Stage stage;

    public void initialize() {
        updateOrderTime();
        setupDefaultData();
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
        updateOrderDisplay();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void updateOrderTime() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        orderTimeLabel.setText("Hora: " + currentTime);
    }

    private void setupDefaultData() {
        orderNumberLabel.setText("#ORD-" + (System.currentTimeMillis() % 10000));
        customerNameLabel.setText("Cliente: Consumidor Final");
    }

    private void updateOrderDisplay() {
        if (orderDetails == null || orderDetails.isEmpty()) {
            showEmptyOrderMessage();
            return;
        }

        itemsContainer.getChildren().clear();

        double subtotal = 0;

        for (OrderDetail detail : orderDetails) {
            HBox itemCard = createItemCard(detail);
            itemsContainer.getChildren().add(itemCard);
            subtotal += detail.getSubtotal();
        }

        //double tax = subtotal * 0.10; // 10% IVA
        double total = subtotal; // + tax;

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        //taxLabel.setText(String.format("$%.2f", tax));
        totalLabel.setText(String.format("$%.2f", total));
    }

    private void showEmptyOrderMessage() {
        itemsContainer.getChildren().clear();
        Label emptyLabel = new Label("No hay items en el pedido");
        emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
        itemsContainer.getChildren().add(emptyLabel);

        subtotalLabel.setText("$0.00");
        taxLabel.setText("$0.00");
        totalLabel.setText("$0.00");
    }

    private HBox createItemCard(OrderDetail detail) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        card.setAlignment(Pos.CENTER_LEFT);

        VBox details = new VBox(5);

        Label nameLabel = new Label(detail.getProductName());
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);

        HBox infoRow = new HBox(15);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(String.format("$%.2f c/u", detail.getUnitPrice()));
        priceLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        Label quantityLabel = new Label("Cant: " + detail.getQuantity());
        quantityLabel.setStyle("-fx-font-size: 12; -fx-text-fill: white; -fx-background-color: #e9500e; -fx-padding: 2 8; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label subtotalLabel = new Label(String.format("Subtotal: $%.2f", detail.getSubtotal()));
        subtotalLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #e9500e;");

        infoRow.getChildren().addAll(priceLabel, quantityLabel, subtotalLabel);
        details.getChildren().addAll(nameLabel, infoRow);

        card.getChildren().addAll(details);
        return card;
    }

    @FXML
    private void printOrder() {
        DialogUtil.showInfo("Impresión", "Comanda enviada a impresión");
    }

    @FXML
    private void confirmOrder() {
        DialogUtil.showInfo("Éxito", "Pedido confirmado correctamente");
    }

    @FXML
    private void markAsReady() {
        DialogUtil.showInfo("Listo", "Pedido marcado como listo");
    }

    @FXML
    private void sendToKitchen() {
        DialogUtil.showInfo("Enviado", "Pedido enviado a cocina");
    }

    @FXML
    private void editOrder() {
        DialogUtil.showInfo("Editar", "Volviendo al menú para editar");
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void cancelOrder() {
        boolean confirm = DialogUtil.showConfirmation("Cancelar Pedido",
                "¿Estás seguro de que deseas cancelar este pedido?");
        if (confirm && stage != null) {
            stage.close();
        }
    }

    @FXML
    private void goBack() {
        if (stage != null) {
            stage.close();
        }
    }
}