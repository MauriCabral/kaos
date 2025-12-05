package org.example.kaos.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.kaos.entity.Order;
import org.example.kaos.entity.OrderDetail;
import org.example.kaos.entity.OrderDetailTopping;
import org.example.kaos.entity.Store;
import org.example.kaos.util.DialogUtil;
import org.example.kaos.util.Session;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailsController {

    @FXML private Label orderNumberLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label totalLabel;
    @FXML private Label deliveryPriceLabel;
    @FXML private TextArea notesTextArea;
    @FXML private VBox itemsContainer;
    @FXML private CheckBox deliveryCheckBox;
    @FXML private TextField deliveryPriceField;
    @FXML private TextField customerNameField;
    @FXML private TextField customerAddressField;
    @FXML private TextField customerPhoneField;
    @FXML private CheckBox cashCheckBox;
    @FXML private TextField cashAmountField;
    @FXML private CheckBox transferCheckBox;
    @FXML private TextField transferAmountField;

    private List<OrderDetail> orderDetails;
    private Stage stage;

    public void initialize() {
        setupDefaultData();
        setupListeners();
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
        updateOrderDisplay();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setupDefaultData() {
        orderNumberLabel.setText("#ORD-" + (System.currentTimeMillis() % 10000));
        customerNameField.setText("");
        deliveryPriceField.setText("2000");
        cashAmountField.setText("0.00");
        transferAmountField.setText("0.00");
    }

    private void setupListeners() {
        deliveryCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            deliveryPriceField.setVisible(newVal);
            updateOrderDisplay();
        });

        deliveryPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateOrderDisplay();
        });

        cashCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            cashAmountField.setVisible(newVal);
            if (!newVal) cashAmountField.setText("0.00");
        });

        transferCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            transferAmountField.setVisible(newVal);
            if (!newVal) transferAmountField.setText("0.00");
        });
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

        double deliveryPrice = 0;
        if (deliveryCheckBox.isSelected()) {
            try {
                deliveryPrice = Double.parseDouble(deliveryPriceField.getText());
                subtotal += deliveryPrice;
            } catch (NumberFormatException e) {
                deliveryPrice = 0;
            }
        }

        double total = subtotal;

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        totalLabel.setText(String.format("$%.2f", total));
        deliveryPriceLabel.setText(String.format("$%.2f", deliveryPrice));
    }

    private void showEmptyOrderMessage() {
        itemsContainer.getChildren().clear();
        Label emptyLabel = new Label("No hay items en el pedido");
        emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
        itemsContainer.getChildren().add(emptyLabel);
        subtotalLabel.setText("$0.00");
        totalLabel.setText("$0.00");
        deliveryPriceLabel.setText("$0.00");
    }

    private HBox createItemCard(OrderDetail detail) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("item-card");

        VBox infoBox = new VBox(5);
        infoBox.setPrefWidth(400);

        String displayName = detail.getProductName();
        if (detail.getVariantName() != null && !detail.getVariantName().trim().isEmpty()) {
            displayName += " (" + detail.getVariantName() + ")";
        }

        Label nameLabel = new Label(displayName);
        nameLabel.getStyleClass().add("item-name");
        nameLabel.setWrapText(true);

        HBox detailsRow = new HBox(15);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(String.format("$%.2f c/u", detail.getUnitPrice()));
        priceLabel.getStyleClass().add("item-price");

        Label quantityLabel = new Label("Cant: " + detail.getQuantity());
        quantityLabel.getStyleClass().add("item-quantity");

        Label subtotalLabel = new Label(String.format("Subtotal: $%.2f", detail.getSubtotal()));
        subtotalLabel.getStyleClass().add("item-subtotal");

        detailsRow.getChildren().addAll(priceLabel, quantityLabel, subtotalLabel);
        infoBox.getChildren().addAll(nameLabel, detailsRow);

        if (detail.getOrderDetailToppings() != null && !detail.getOrderDetailToppings().isEmpty()) {
            VBox toppingsBox = new VBox(3);
            toppingsBox.setPadding(new Insets(5, 0, 0, 10));

            // Separar toppings
            List<OrderDetailTopping> addedToppings = new ArrayList<>();
            List<OrderDetailTopping> notAddedToppings = new ArrayList<>();

            for (OrderDetailTopping topping : detail.getOrderDetailToppings()) {
                if (Boolean.TRUE.equals(topping.getIsAdded())) {
                    addedToppings.add(topping);
                } else {
                    notAddedToppings.add(topping);
                }
            }

            // Solo mostrar toppings si hay de algún tipo
            if (!addedToppings.isEmpty() || !notAddedToppings.isEmpty()) {
                HBox toppingsColumns = new HBox(20);

                // Columna agregados
                if (!addedToppings.isEmpty()) {
                    VBox addedColumn = new VBox(3);
                    Label addedLabel = new Label("Agregados:");
                    addedLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    addedColumn.getChildren().add(addedLabel);

                    for (OrderDetailTopping topping : addedToppings) {
                        Label toppingLabel = new Label("✓ " + topping.getTopping().getName());
                        toppingLabel.setStyle("-fx-text-fill: #28a745;");
                        addedColumn.getChildren().add(toppingLabel);
                    }
                    toppingsColumns.getChildren().add(addedColumn);
                }

                // Columna no agregados
                if (!notAddedToppings.isEmpty()) {
                    VBox notAddedColumn = new VBox(3);
                    Label notAddedLabel = new Label("No agregados:");
                    notAddedLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    notAddedColumn.getChildren().add(notAddedLabel);

                    for (OrderDetailTopping topping : notAddedToppings) {
                        Label toppingLabel = new Label("✗ " + topping.getTopping().getName());
                        toppingLabel.setStyle("-fx-text-fill: #dc3545;");
                        notAddedColumn.getChildren().add(toppingLabel);
                    }
                    toppingsColumns.getChildren().add(notAddedColumn);
                }

                toppingsBox.getChildren().add(toppingsColumns);
                infoBox.getChildren().add(toppingsBox);
            }
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botón eliminar pequeño y redondo
        Button deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setOnAction(e -> removeItem(detail));

        card.getChildren().addAll(infoBox, spacer, deleteBtn);
        return card;
    }

    private void removeItem(OrderDetail detail) {
        if (DialogUtil.showConfirmation("Eliminar Item", "¿Estás seguro de eliminar este item del pedido?")) {
            orderDetails.remove(detail);
            updateOrderDisplay();
        }
    }

    @FXML
    private void confirmOrder() {
        if (customerNameField.getText().trim().isEmpty()) {
            DialogUtil.showError("Error", "El nombre del cliente es obligatorio");
            return;
        }

        try {
            double cash = cashCheckBox.isSelected() ? Double.parseDouble(cashAmountField.getText()) : 0;
            double transfer = transferCheckBox.isSelected() ? Double.parseDouble(transferAmountField.getText()) : 0;
            double total = calculateTotal();
            double delivery = deliveryCheckBox.isSelected() ? Double.parseDouble(deliveryPriceField.getText()) : 0;

            Store store = Session.getInstance().getCurrentUser().getStore();

            Order order = Order.builder()
                    .isDelivery(deliveryCheckBox.isSelected())
                    .cashAmount(cash)
                    .transferAmount(transfer)
                    .deliveryAmount(delivery)
                    .total(total)
                    .notes(notesTextArea.getText())
                    .store(store)
                    .build();

            DialogUtil.showInfo("Éxito", "Pedido confirmado correctamente");
            if (stage != null) stage.close();

        } catch (Exception e) {
            DialogUtil.showError("Error", "Verifique que todos los montos sean números válidos");
        }
    }

    private double calculateTotal() {
        if (orderDetails == null) return 0;

        double subtotal = orderDetails.stream().mapToDouble(OrderDetail::getSubtotal).sum();
        if (deliveryCheckBox.isSelected()) {
            try {
                subtotal += Double.parseDouble(deliveryPriceField.getText());
            } catch (Exception e) {}
        }
        return subtotal;
    }

    @FXML
    private void cancelOrder() {
        if (DialogUtil.showConfirmation("Cancelar Orden", "¿Estás seguro de cancelar esta orden?")) {
            if (stage != null) {
                orderDetails.clear();
                stage.close();
            }
        }
    }

    @FXML
    private void goBack() {
        if (stage != null) stage.close();
    }
}