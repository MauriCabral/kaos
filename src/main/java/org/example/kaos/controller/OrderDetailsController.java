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
import org.example.kaos.service.IDeliveryService;
import org.example.kaos.service.IOrderDetailService;
import org.example.kaos.service.IOrderService;
import org.example.kaos.service.implementation.DeliveryServiceImpl;
import org.example.kaos.service.implementation.OrderDetailServiceImpl;
import org.example.kaos.service.implementation.OrderServiceImpl;
import org.example.kaos.service.implementation.TicketPrintServiceImpl;
import org.example.kaos.util.DialogUtil;
import org.example.kaos.util.Session;

import java.math.BigDecimal;
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
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;
    @FXML private Button btnBack;
    @FXML private CheckBox deletedCheckBox;
    @FXML private HBox adminContainer;
    @FXML private ComboBox<Delivery> deliveryComboBox;
    @FXML private Button btnPrint;

    private final IOrderService orderService = new OrderServiceImpl();
    private final IOrderDetailService orderDetailService = new OrderDetailServiceImpl();
    private final IDeliveryService deliveryService = new DeliveryServiceImpl();
    private final TicketPrintServiceImpl ticketPrintService = new TicketPrintServiceImpl();

    private List<OrderDetail> orderDetails;
    private List<Delivery> deliveriesList = new ArrayList<>();
    private Order order;
    private Stage stage;
    private boolean isEditMode = false;
    private boolean isViewMode = false;
    private boolean isAdmin = false;

    private double cash = 0;
    private double transfer = 0;
    private double delivery = 0;
    private double subtotal = 0;
    private double total = 0;

    public void initialize() {
        setupDefaultData();
        setupListeners();

        isAdmin = Session.getInstance().getCurrentUser().getId() == 1;
        adminContainer.setVisible(isAdmin);
        deletedCheckBox.setVisible(isAdmin);
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
        this.isViewMode = false;
        this.isEditMode = false;
        setupNewOrderMode();
    }

    public void setOrder(Order order, boolean isEditMode) {
        this.order = order;
        this.isViewMode = true;
        this.isEditMode = isEditMode;

        if (isEditMode) {
            setupEditMode();
        } else {
            setupViewMode();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setupNewOrderMode() {
        btnConfirm.setText("Confirmar Pedido");
        btnConfirm.setVisible(true);
        btnCancel.setVisible(true);
        btnBack.setVisible(true);
        btnPrint.setVisible(false);

        setFieldsEditable(true);

        deliveryCheckBox.setVisible(true);
        cashCheckBox.setVisible(true);
        transferCheckBox.setVisible(true);
        deletedCheckBox.setVisible(false);

        setupDefaultData();
        updateOrderDisplay();
    }

    private void setupViewMode() {
        loadOrderData();

        btnPrint.setVisible(true);
        btnConfirm.setVisible(false);
        btnCancel.setVisible(false);
        btnBack.setVisible(true);
        btnBack.setText("Salir");

        setFieldsEditable(false);

        deliveryCheckBox.setDisable(true);
        cashCheckBox.setDisable(true);
        transferCheckBox.setDisable(true);
        deliveryComboBox.setDisable(true);

        showPaymentValues();

        updateOrderDisplay();
    }

    private void setupEditMode() {
        loadOrderData();

        btnConfirm.setText("Guardar");
        btnConfirm.setVisible(true);
        btnCancel.setVisible(true);
        btnBack.setVisible(false);
        btnPrint.setVisible(false);

        setFieldsEditable(true);

        deliveryCheckBox.setVisible(true);
        cashCheckBox.setVisible(true);
        transferCheckBox.setVisible(true);

        boolean isAdmin = Session.getInstance().getCurrentUser().getId() == 1;
        adminContainer.setVisible(isAdmin);
        deletedCheckBox.setVisible(isAdmin);
        deletedCheckBox.setDisable(false);

        updateOrderDisplay();
    }

    private void setFieldsEditable(boolean editable) {
        customerNameField.setEditable(editable);
        customerAddressField.setEditable(editable);
        customerPhoneField.setEditable(editable);
        deliveryPriceField.setEditable(editable);
        cashAmountField.setEditable(editable);
        transferAmountField.setEditable(editable);
        notesTextArea.setEditable(editable);

        if (!editable) {
            customerNameField.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0;");
            customerAddressField.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0;");
            customerPhoneField.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0;");
            deliveryPriceField.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0;");
            cashAmountField.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0;");
            transferAmountField.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0;");
            notesTextArea.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0;");
        } else {
            customerNameField.setStyle("");
            customerAddressField.setStyle("");
            customerPhoneField.setStyle("");
            deliveryPriceField.setStyle("");
            cashAmountField.setStyle("");
            transferAmountField.setStyle("");
            notesTextArea.setStyle("");
        }
    }

    private void showPaymentValues() {
        if (order != null) {
            if (order.getIsDelivery() != null && order.getIsDelivery()) {
                deliveryPriceField.setText(String.format("%.0f", order.getDeliveryAmount()));
            }

            if (order.getCashAmount() != null && order.getCashAmount().compareTo(BigDecimal.ZERO) > 0) {
                cashAmountField.setText(String.format("%.0f", order.getCashAmount()));
            }

            if (order.getTransferAmount() != null && order.getTransferAmount().compareTo(BigDecimal.ZERO) > 0) {
                transferAmountField.setText(String.format("%.0f", order.getTransferAmount()));
            }
        }
    }

    private void setupDefaultData() {
        orderNumberLabel.setText("#ORD-");
        customerNameField.setText("");
        deliveryPriceField.setText("2000");
        cashAmountField.setText("0");
        transferAmountField.setText("0");
        notesTextArea.setText("");
    }

    private void setupListeners() {
        deliveryCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            deliveryPriceField.setVisible(newVal);
            if (!isViewMode) {
                updateOrderDisplay();
            }
        });

        deliveryPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isViewMode) {
                updateOrderDisplay();
            }
        });

        cashCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            cashAmountField.setVisible(newVal);
            if (isEditMode) cashAmountField.setText(order.getCashAmount().toString().trim());
            else if (!newVal) cashAmountField.setText("0");
        });

        transferCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            transferAmountField.setVisible(newVal);
            if (isEditMode) transferAmountField.setText(order.getTransferAmount().toString().trim());
            else if (!newVal) transferAmountField.setText("0");
        });
    }

    private void loadOrderData() {
        if (order == null) return;

        orderNumberLabel.setText(order.getOrderNumber());

        customerNameField.setText(order.getCustomerName());
        customerAddressField.setText(order.getCustomerAddress() != null ? order.getCustomerAddress() : "");
        customerPhoneField.setText(order.getCustomerPhone() != null ? order.getCustomerPhone() : "");
        notesTextArea.setText(order.getNotes() != null ? order.getNotes() : "");

        if (order.getIsDelivery() != null) {
            deliveryCheckBox.setSelected(Boolean.TRUE.equals(order.getIsDelivery()));
            if (order.getDeliveryAmount() != null) {
                deliveryPriceField.setText(String.format("%.0f", order.getDeliveryAmount()));
            }
            setupDeliveryComboBox();
        }

        if (order.getCashAmount() != null && order.getCashAmount().compareTo(BigDecimal.ZERO) > 0) {
            cashCheckBox.setSelected(true);
            cashAmountField.setText(String.format("%.0f", order.getCashAmount()));
        }

        if (order.getTransferAmount() != null && order.getTransferAmount().compareTo(BigDecimal.ZERO) > 0) {
            transferCheckBox.setSelected(true);
            transferAmountField.setText(String.format("%.0f", order.getTransferAmount()));
        }

        boolean isAdmin = Session.getInstance().getCurrentUser().getId() == 1;
        if (isAdmin && deletedCheckBox != null) {
            deletedCheckBox.setSelected(order.getDeletedAt() != null);
        }

        subtotalLabel.setText(String.format("$%.0f", order.getSubtotal()));
        totalLabel.setText(String.format("$%.0f", order.getTotal()));
        deliveryPriceLabel.setText(String.format("$%.0f",
                order.getDeliveryAmount() != null ? order.getDeliveryAmount() : 0.0));
    }

    private void updateOrderDisplay() {
        List<OrderDetail> detailsToShow = null;

        if (isViewMode && order != null) {
            detailsToShow = order.getOrderDetails();
        } else if (!isViewMode && orderDetails != null) {
            detailsToShow = orderDetails;
        }

        if (detailsToShow == null || detailsToShow.isEmpty()) {
            showEmptyOrderMessage();
            return;
        }

        itemsContainer.getChildren().clear();
        double subtotal = 0;

        for (OrderDetail detail : detailsToShow) {
            HBox itemCard = createItemCard(detail);
            itemsContainer.getChildren().add(itemCard);

            boolean hasBurgerVariant = detail.getBurgerVariant() != null;
            boolean hasToppings = detail.getOrderDetailToppings() != null && !detail.getOrderDetailToppings().isEmpty();

            if (hasBurgerVariant || hasToppings) {
                subtotal += detail.getTotal();
            } else {
                subtotal += detail.getSubtotal();
            }
        }

        if (!isViewMode || isEditMode) {
            double deliveryPrice = 0;
            if (deliveryCheckBox.isSelected()) {
                try {
                    deliveryPrice = Double.parseDouble(deliveryPriceField.getText());
                } catch (NumberFormatException e) {
                    deliveryPrice = 0;
                }
            }

            double total = subtotal + deliveryPrice;

            subtotalLabel.setText(String.format("$%.0f", subtotal));
            totalLabel.setText(String.format("$%.0f", total));
            deliveryPriceLabel.setText(String.format("$%.0f", deliveryPrice));
        }
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

        if (!isViewMode && !isEditMode) {
            Button deleteBtn = new Button("✕");
            deleteBtn.getStyleClass().add("delete-btn");
            deleteBtn.setOnAction(e -> removeItem(detail));
            card.getChildren().addAll(infoBox, spacer, deleteBtn);
        } else {
            card.getChildren().addAll(infoBox);
        }

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
        if (isViewMode && isEditMode) {
            saveOrderChanges();
        } else if (!isViewMode) {
            createNewOrder();
        }
    }

    private void saveOrderChanges() {
        try {
            if (customerNameField.getText().trim().isEmpty()) {
                DialogUtil.showWarning("Error", "El nombre del cliente es obligatorio");
                customerNameField.requestFocus();
                return;
            }

            if (!validator()) return;

            order.setCustomerName(customerNameField.getText().trim());
            order.setCustomerAddress(customerAddressField.getText().trim());
            order.setCustomerPhone(customerPhoneField.getText().trim());
            order.setIsDelivery(deliveryCheckBox.isSelected());
            order.setNotes(notesTextArea.getText());
            order.setDelivery(deliveryComboBox.getValue());

            try {
                if (deliveryCheckBox.isSelected()) {
                    order.setDeliveryAmount(BigDecimal.valueOf(Double.parseDouble(deliveryPriceField.getText())));
                } else {
                    order.setDeliveryAmount(BigDecimal.ZERO);
                }

                if (cashCheckBox.isSelected()) {
                    order.setCashAmount(BigDecimal.valueOf(Double.parseDouble(cashAmountField.getText())));
                } else {
                    order.setCashAmount(BigDecimal.ZERO);
                }

                if (transferCheckBox.isSelected()) {
                    order.setTransferAmount(BigDecimal.valueOf(Double.parseDouble(transferAmountField.getText())));
                } else {
                    order.setTransferAmount(BigDecimal.ZERO);
                }
            } catch (NumberFormatException e) {
                DialogUtil.showError("Error", "Los montos deben ser números válidos");
                return;
            }
            order.setTotal(BigDecimal.valueOf(total));

            if (isAdmin && deletedCheckBox != null) {
                if (deletedCheckBox.isSelected()) {
                    if (order.getDeletedAt() == null) {
                        order.softDelete();
                    }
                } else {
                    order.setDeletedAt(null);
                }
            }

            Order updatedOrder = orderService.updateOrder(order);
            if (updatedOrder != null) {
                DialogUtil.showInfo("Éxito", "Orden actualizada correctamente");
                this.order = updatedOrder;
                goBack();
            } else {
                DialogUtil.showError("Error", "No se pudo actualizar la orden");
            }
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Error", "Error al actualizar la orden: " + e.getMessage());
        }
    }

    private void createNewOrder() {
        try {
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
                        .cashAmount(BigDecimal.valueOf(cash))
                        .transferAmount(BigDecimal.valueOf(transfer))
                        .deliveryAmount(BigDecimal.valueOf(delivery))
                        .subtotal(BigDecimal.valueOf(subtotal))
                        .total(BigDecimal.valueOf(total))
                        .notes(notesTextArea.getText())
                        .store(store)
                        .createdByUser(currentUser)
                        .build();

                Order savedOrder = orderService.createOrder(order);

                if (savedOrder == null || savedOrder.getId() == null) {
                    DialogUtil.showError("Error", "No se pudo crear la orden");
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

                DialogUtil.showInfo("Éxito","Pedido confirmado correctamente");

                ticketPrintService.print(savedOrder.getId());

                if (stage != null) {
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

        /*if (customerPhoneField.getText().trim().isEmpty()) {
            DialogUtil.showError("Error", "El teléfono del cliente es obligatorio");
            customerPhoneField.requestFocus();
            return false;
        }*/

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
            DialogUtil.showError("Error", "Verifique que todos los montos sean números válidos");
            return false;
        }

        return res;
    }

    private double calculateSubtotal() {
        List<OrderDetail> detailsToCalculate = null;
        if (isEditMode && order != null) {
            detailsToCalculate = order.getOrderDetails();
        } else if (!isViewMode) {
            detailsToCalculate = orderDetails;
        }
        if (detailsToCalculate == null || detailsToCalculate.isEmpty()) {
            return 0;
        }
        return detailsToCalculate.stream().mapToDouble(detail -> {
            if (detail.getOrderDetailToppings() != null && !detail.getOrderDetailToppings().isEmpty()) {
                return detail.getTotal();
            } else {
                return detail.getSubtotal();
            }
        }).sum();
    }

    @FXML
    private void cancelOrder() {
        if (isViewMode && isEditMode) {
            goBack();
        } else {
            if (DialogUtil.showConfirmation("Cancelar", "¿Estás seguro de cancelar?")) {
                goBack();
            }
        }
    }

    @FXML
    private void goBack() {
        if (stage != null) stage.close();
    }

    private void setupDeliveryComboBox() {
        List<Delivery> deliveries = deliveryService.findByStoreId(Session.getInstance().getCurrentUser().getStore().getId());
        deliveryComboBox.getItems().setAll(deliveries);

        deliveryComboBox.setVisible(true);

        deliveryComboBox.setCellFactory(param -> new ListCell<Delivery>() {
            @Override
            protected void updateItem(Delivery delivery, boolean empty) {
                super.updateItem(delivery, empty);
                if (empty || delivery == null) {
                    setText(null);
                } else {
                    setText(delivery.getName());
                }
            }
        });

        deliveryComboBox.setButtonCell(new ListCell<Delivery>() {
            @Override
            protected void updateItem(Delivery delivery, boolean empty) {
                super.updateItem(delivery, empty);
                if (empty || delivery == null) {
                    setText(null);
                } else {
                    setText(delivery.getName());
                }
            }
        });

        if (order.getDelivery() != null) {
            for (Delivery delivery : deliveryComboBox.getItems()) {
                if (delivery.getId().equals(order.getDelivery().getId())) {
                    deliveryComboBox.setValue(delivery);
                    break;
                }
            }
        }
    }

    public void orderPrint(ActionEvent actionEvent) {
        ticketPrintService.generatePDF(order.getId());
    }
}