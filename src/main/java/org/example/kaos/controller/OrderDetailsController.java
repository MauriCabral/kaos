package org.example.kaos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.kaos.entity.*;
import org.example.kaos.service.IOrderDetailService;
import org.example.kaos.service.IOrderService;
import org.example.kaos.service.implementation.OrderDetailServiceImpl;
import org.example.kaos.service.implementation.OrderServiceImpl;
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

    private final IOrderService orderService = new OrderServiceImpl();
    private final IOrderDetailService orderDetailService = new OrderDetailServiceImpl();

    private List<OrderDetail> orderDetails;
    private Stage stage;
    private boolean orderConfirmed = false;
    private double cash = 0;
    private double transfer = 0;
    private double delivery = 0;
    private double subtotal = 0;
    private double total = 0;

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
        orderNumberLabel.setText("#ORD-"/* + (System.currentTimeMillis() % 10000)*/);
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

        Label priceLabel = new Label("$" + String.valueOf(detail.getUnitPrice().intValue()).trim() + " c/u");
        priceLabel.getStyleClass().add("item-price");

        Label quantityLabel = new Label("Cant: " + detail.getQuantity());
        quantityLabel.getStyleClass().add("item-quantity");

        boolean hasToppings = detail.getOrderDetailToppings() != null && !detail.getOrderDetailToppings().isEmpty();

        String labelText = hasToppings ? "Subtotal: $" : "Total: $";
        Label subtotalTotalLabel = new Label(labelText + String.valueOf(detail.getSubtotal().intValue()).trim());
        subtotalTotalLabel.getStyleClass().add("item-subtotal");

        HBox pricesContainer = new HBox(20);
        pricesContainer.setAlignment(Pos.CENTER_LEFT);
        pricesContainer.getChildren().addAll(priceLabel, quantityLabel, subtotalTotalLabel);

        if (hasToppings) {
            Label totalLabel = new Label("Total: $" + String.valueOf(detail.getTotal().intValue()).trim());
            totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e9500e; -fx-font-size: 13px;");
            pricesContainer.getChildren().add(totalLabel);
        }

        infoBox.getChildren().addAll(nameLabel, pricesContainer);

        if (hasToppings) {
            VBox toppingsBox = new VBox(3);
            toppingsBox.setPadding(new Insets(5, 0, 0, 10));

            List<OrderDetailTopping> addedToppings = new ArrayList<>();
            List<OrderDetailTopping> notAddedToppings = new ArrayList<>();

            for (OrderDetailTopping topping : detail.getOrderDetailToppings()) {
                if (Boolean.TRUE.equals(topping.getIsAdded())) {
                    addedToppings.add(topping);
                } else {
                    notAddedToppings.add(topping);
                }
            }

            if (!addedToppings.isEmpty() || !notAddedToppings.isEmpty()) {
                HBox toppingsColumns = new HBox(20);

                if (!addedToppings.isEmpty()) {
                    VBox addedColumn = new VBox(3);
                    Label addedLabel = new Label("Agregados:");
                    addedLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    addedColumn.getChildren().add(addedLabel);

                    for (OrderDetailTopping topping : addedToppings) {
                        Label toppingLabel = new Label("✓ " + topping.getTopping().getName() + " -  $" + String.valueOf(topping.getTopping().getPrice().intValue()).trim());
                        toppingLabel.setStyle("-fx-text-fill: #28a745;");
                        addedColumn.getChildren().add(toppingLabel);
                    }
                    toppingsColumns.getChildren().add(addedColumn);
                }

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
    private void confirmOrder(ActionEvent event) {
        try {
            orderConfirmed = false;

            boolean valid = validator();

            if (valid) {

                Store store = Session.getInstance().getCurrentUser().getStore();
                User currentUser = Session.getInstance().getCurrentUser();

                Order order = Order.builder()
                        .orderNumber(orderNumberLabel.getText())
                        .customerName(customerNameField.getText().trim())
                        .customerAddress(customerAddressField.getText().trim())
                        .customerPhone(customerPhoneField.getText().trim())
                        .isDelivery(deliveryCheckBox.isSelected())
                        .cashAmount(cash)
                        .transferAmount(transfer)
                        .deliveryAmount(delivery)
                        .subtotal(subtotal)
                        .total(total)
                        .notes(notesTextArea.getText())
                        .store(store)
                        .createdByUser(currentUser)
                        .build();

                Order savedOrder = orderService.createOrder(order);

                if (savedOrder == null || savedOrder.getId() == null) {
                    DialogUtil.showError("Error", "No se pudo crear la orden");
                    event.consume();
                    return;
                }

                boolean allDetailsSaved = true;
                for (OrderDetail detail : orderDetails) {
                    try {
                        detail.setOrder(savedOrder);
                        orderDetailService.saveOrderDetail(detail);
                    } catch (Exception e) {
                        allDetailsSaved = false;
                        e.printStackTrace();
                        DialogUtil.showError("Error", "Error al guardar un item: " + e.getMessage());
                    }
                }

                if (!allDetailsSaved) {
                    DialogUtil.showError("Error", "No se pudieron guardar todos los items de la orden");
                    return;
                }

                orderConfirmed = true;

                /*DialogUtil.showInfo("Éxito",
                        String.format("Pedido %s confirmado correctamente\n\n" +
                                        "Cliente: %s\n" +
                                        "Total: $%.2f\n" +
                                        "Efectivo: $%.2f\n" +
                                        "Transferencia: $%.2f",
                                savedOrder.getOrderNumber(),
                                savedOrder.getCustomerName(),
                                savedOrder.getTotal(),
                                savedOrder.getCashAmount(),
                                savedOrder.getTransferAmount()));*/
                DialogUtil.showInfo("Éxito","Pedido confirmado correctamente");

                if (stage != null && orderConfirmed) {
                    stage.close();
                }
            }

        } catch (NumberFormatException e) {
            DialogUtil.showError("Error", "Verifique que todos los montos sean números válidos\nEjemplo: 1500.50");
        }
    }

    private boolean validator() {
        boolean res = true;

        if (customerNameField.getText().trim().isEmpty()) {
            DialogUtil.showWarning("Error", "El nombre del cliente es obligatorio");
            customerNameField.requestFocus();
            return false;
        }

        if (customerAddressField.getText().trim().isEmpty()) {
            DialogUtil.showError("Error", "La dirección del cliente es obligatoria");
            customerAddressField.requestFocus();
            return false;
        }

        if (customerPhoneField.getText().trim().isEmpty()) {
            DialogUtil.showError("Error", "El teléfono del cliente es obligatorio");
            customerPhoneField.requestFocus();
            return false;
        }

        try {
            cash = 0;
            transfer = 0;
            delivery = 0;

            if (cashCheckBox.isSelected()) {
                cash = Double.parseDouble(cashAmountField.getText());
                if (cash < 0) {
                    DialogUtil.showError("Error", "El monto en efectivo no puede ser negativo");
                    cashAmountField.requestFocus();
                    return false;
                }
            }

            if (transferCheckBox.isSelected()) {
                transfer = Double.parseDouble(transferAmountField.getText());
                if (transfer < 0) {
                    DialogUtil.showError("Error", "El monto de transferencia no puede ser negativo");
                    transferAmountField.requestFocus();
                    return false;
                }
            }

            if (deliveryCheckBox.isSelected()) {
                delivery = Double.parseDouble(deliveryPriceField.getText());
                if (delivery < 0) {
                    DialogUtil.showError("Error", "El monto de delivery no puede ser negativo");
                    deliveryPriceField.requestFocus();
                    return false;
                }
            }

            subtotal = calculateSubtotal();
            total = subtotal + delivery;

            double totalPayment = cash + transfer;
            if (Math.abs(totalPayment - total) > 0.01) {
                DialogUtil.showError("Error",
                        String.format("El total de pagos ($%.2f) no coincide con el total de la orden ($%.2f)\n\nDiferencia: $%.2f",
                                totalPayment, total, Math.abs(totalPayment - total)));
                return false;
            }

        } catch (NumberFormatException e) {
            DialogUtil.showError("Error", "Verifique que todos los montos sean números válidos\nEjemplo: 1500.50");
            return false;
        }

        return res;
    }

    private double calculateSubtotal() {
        if (orderDetails == null) return 0;

        double subtotal = orderDetails.stream().mapToDouble(OrderDetail::getSubtotal).sum();
        return subtotal;
    }
    /*private double calculateSubtotal() {
        if (orderDetails == null) return 0;

        double subtotal = orderDetails.stream().mapToDouble(OrderDetail::getSubtotal).sum();
        if (deliveryCheckBox.isSelected()) {
            try {
                subtotal += Double.parseDouble(deliveryPriceField.getText());
            } catch (Exception e) {}
        }
        return subtotal;
    }*/

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