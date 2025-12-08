package org.example.kaos.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.kaos.entity.Order;
import org.example.kaos.service.IOrderService;
import org.example.kaos.service.implementation.OrderServiceImpl;
import org.example.kaos.util.DialogUtil;
import org.example.kaos.util.Session;
import org.example.kaos.util.WindowManager;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class OrderHistoryController implements Initializable {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Long> idColumn;
    @FXML private TableColumn<Order, String> orderNumberColumn;
    @FXML private TableColumn<Order, String> dateColumn;
    @FXML private TableColumn<Order, String> customerNameColumn;
    @FXML private TableColumn<Order, String> customerAddressColumn;
    @FXML private TableColumn<Order, String> customerPhoneColumn;
    @FXML private TableColumn<Order, String> typeColumn;
    @FXML private TableColumn<Order, String> storeColumn;
    @FXML private TableColumn<Order, String> createdByColumn;
    @FXML private TableColumn<Order, Double> subtotalColumn;
    @FXML private TableColumn<Order, Double> totalColumn;
    @FXML private TableColumn<Order, String> paymentMethodColumn;
    @FXML private TableColumn<Order, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Button clearBtn;
    @FXML private CheckBox deliveryCheck;
    @FXML private CheckBox pickupCheck;
    @FXML private CheckBox cashCheck;
    @FXML private CheckBox transferCheck;
    @FXML private CheckBox todayCheck;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private HBox filterContainer;
    @FXML private Label titleLabel;

    private final IOrderService orderService = new OrderServiceImpl();
    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    boolean isAdmin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            isAdmin = Session.getInstance().getCurrentUser().getId() == 1;
            setupView();
            setupTableColumns();
            loadOrders();
            setupActionsColumn();
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Error de Inicialización", "No se pudo inicializar la pantalla: " + e.getMessage());
        }
    }

    private void setupView() {

        if (!isAdmin) {
            idColumn.setVisible(false);
            storeColumn.setVisible(false);
        }

        customerPhoneColumn.setVisible(false);
        customerAddressColumn.setVisible(false);
        createdByColumn.setVisible(false);

        orderNumberColumn.setPrefWidth(100);

        if (filterContainer != null) {
            filterContainer.setSpacing(10);
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime createdAt = cellData.getValue().getCreatedAt();
            return new javafx.beans.property.SimpleStringProperty(
                    createdAt != null ? createdAt.format(dateFormatter) : ""
            );
        });

        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        customerAddressColumn.setCellValueFactory(new PropertyValueFactory<>("customerAddress"));
        customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));

        typeColumn.setCellValueFactory(cellData -> {
            Boolean isDelivery = cellData.getValue().getIsDelivery();
            if (isDelivery == null) {
                return new javafx.beans.property.SimpleStringProperty("🏪");
            }
            return new javafx.beans.property.SimpleStringProperty(
                    Boolean.TRUE.equals(isDelivery) ? "🚚" : "🏪"
            );
        });

        storeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getStore() != null && cellData.getValue().getStore().getName() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStore().getName());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        createdByColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedByUser() != null &&
                    cellData.getValue().getCreatedByUser().getUsername() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatedByUser().getUsername());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        paymentMethodColumn.setCellValueFactory(cellData -> {
            Double cashAmount = cellData.getValue().getCashAmount();
            Double transferAmount = cellData.getValue().getTransferAmount();

            String paymentIcon = "-";

            if (cashAmount != null && cashAmount > 0 && transferAmount != null && transferAmount > 0) {
                paymentIcon = "💰+💳";
            }
            else if (cashAmount != null && cashAmount > 0 && (transferAmount == null || transferAmount <= 0)) {
                paymentIcon = "💰";
            }
            else if ((cashAmount == null || cashAmount <= 0) && transferAmount != null && transferAmount > 0) {
                paymentIcon = "💳";
            }

            return new javafx.beans.property.SimpleStringProperty(paymentIcon);
        });

        subtotalColumn.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    getStyleClass().removeAll("subtotal-cell");
                } else {
                    setText(String.format("$%.0f", amount));
                    getStyleClass().add("subtotal-cell");
                    setStyle("-fx-padding: 0 5 0 0;");
                }
            }
        });

        totalColumn.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    getStyleClass().removeAll("total-cell");
                } else {
                    setText(String.format("$%.0f", amount));
                    getStyleClass().add("total-cell");
                    setStyle("-fx-padding: 0 10 0 0;");
                }
            }
        });

        ordersTable.setRowFactory(tv -> new TableRow<Order>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);

                if (empty || order == null) {
                    setStyle("");
                } else {
                    if (order.getDeletedAt() != null) {
                        setStyle("-fx-background-color: #ffebee; " +
                                "-fx-text-fill: #c62828;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button detailsBtn = new Button("👁");
            private final Button editBtn = new Button("🖊");
            private final Button deleteBtn = new Button("🗑");
            private final HBox buttons = new HBox(5, detailsBtn, editBtn, deleteBtn);

            {
                detailsBtn.getStyleClass().add("compact-action-btn");
                editBtn.getStyleClass().add("compact-action-btn");
                deleteBtn.getStyleClass().add("compact-action-btn");

                detailsBtn.setMinSize(24, 24);
                detailsBtn.setMaxSize(24, 24);
                editBtn.setMinSize(24, 24);
                editBtn.setMaxSize(24, 24);
                deleteBtn.setMinSize(24, 24);
                deleteBtn.setMaxSize(24, 24);

                buttons.setMinHeight(36);
                buttons.setPrefHeight(36);

                detailsBtn.setTooltip(new Tooltip("Ver detalles"));
                editBtn.setTooltip(new Tooltip("Editar"));
                deleteBtn.setTooltip(new Tooltip("Eliminar"));

                detailsBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    viewOrderDetails(order);
                });

                editBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    editOrder(order);
                });

                deleteBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    deleteOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void loadOrders() {
        try {
            List<Order> allOrders = orderService.getAllOrders(isAdmin);
            orders.setAll(allOrders);
            ordersTable.setItems(orders);

        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Error", "No se pudieron cargar las órdenes: " + e.getMessage());
        }
    }

    private void viewOrderDetails(Order order) {
        try {
            if (order.getOrderDetails() != null) {
                WindowManager.openOrderDetailsWindow(null, order, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Error", "No se pudo abrir los detalles: " + e.getMessage());
        }
    }

    private String getPaymentDetail(Order order) {
        Double cashAmount = order.getCashAmount();
        Double transferAmount = order.getTransferAmount();

        if (cashAmount != null && cashAmount > 0 && transferAmount != null && transferAmount > 0) {
            return String.format("Mixto (💰 $%.0f + 💳 $%.0f)", cashAmount, transferAmount);
        } else if (cashAmount != null && cashAmount > 0) {
            return String.format("Efectivo (💰 $%.0f)", cashAmount);
        } else if (transferAmount != null && transferAmount > 0) {
            return String.format("Transferencia (💳 $%.0f)", transferAmount);
        } else {
            return "No especificado";
        }
    }

    private void editOrder(Order order) {
        try {
            if (order.getOrderDetails() != null) {
                Stage editStage = WindowManager.openOrderDetailsWindow(null, order, true);
                if (editStage != null) {
                    editStage.setOnHidden(e -> {
                        loadOrders();
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Error", "No se pudo abrir los detalles: " + e.getMessage());
        }
    }

    private void deleteOrder(Order order) {
        boolean res = DialogUtil.showConfirmation("Confirmar Eliminación","¿Estás seguro de eliminar la orden " + order.getOrderNumber() +"?");

        if (res) {
            try {
                order.softDelete();
                orderService.updateOrder(order);
                loadOrders();
            } catch (Exception e) {
                DialogUtil.showError("Error", "No se pudo eliminar la orden: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        LocalDate today = LocalDate.now();

        ObservableList<Order> filtered = FXCollections.observableArrayList();

        for (Order order : orders) {
            boolean matches = true;

            if (!searchText.isEmpty()) {
                boolean found = (order.getOrderNumber() != null && order.getOrderNumber().toLowerCase().contains(searchText)) ||
                        (order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(searchText)) ||
                        (order.getCustomerPhone() != null && order.getCustomerPhone().toLowerCase().contains(searchText)) ||
                        (order.getCustomerAddress() != null && order.getCustomerAddress().toLowerCase().contains(searchText));

                if (!found) {
                    matches = false;
                }
            }

            if (matches && deliveryCheck != null && deliveryCheck.isSelected()) {
                Boolean isDelivery = order.getIsDelivery();
                if (isDelivery == null || !isDelivery) {
                    matches = false;
                }
            }
            if (matches && pickupCheck != null && pickupCheck.isSelected()) {
                Boolean isDelivery = order.getIsDelivery();
                if (isDelivery != null && isDelivery) {
                    matches = false;
                }
            }

            if (matches && cashCheck != null && cashCheck.isSelected()) {
                Double cashAmount = order.getCashAmount();
                if (cashAmount == null || cashAmount <= 0) {
                    matches = false;
                }
            }
            if (matches && transferCheck != null && transferCheck.isSelected()) {
                Double transferAmount = order.getTransferAmount();
                if (transferAmount == null || transferAmount <= 0) {
                    matches = false;
                }
            }

            if (matches && todayCheck != null && todayCheck.isSelected() && order.getCreatedAt() != null) {
                LocalDate orderDate = order.getCreatedAt().toLocalDate();
                matches = orderDate.equals(today);
            }

            if (matches && dateFromPicker != null && dateFromPicker.getValue() != null &&
                    order.getCreatedAt() != null) {
                LocalDate orderDate = order.getCreatedAt().toLocalDate();
                matches = !orderDate.isBefore(dateFromPicker.getValue());
            }
            if (matches && dateToPicker != null && dateToPicker.getValue() != null &&
                    order.getCreatedAt() != null) {
                LocalDate orderDate = order.getCreatedAt().toLocalDate();
                matches = !orderDate.isAfter(dateToPicker.getValue());
            }

            if (matches) {
                filtered.add(order);
            }
        }

        ordersTable.setItems(filtered);
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        if (deliveryCheck != null) deliveryCheck.setSelected(false);
        if (pickupCheck != null) pickupCheck.setSelected(false);
        if (cashCheck != null) cashCheck.setSelected(false);
        if (transferCheck != null) transferCheck.setSelected(false);
        if (todayCheck != null) todayCheck.setSelected(false);
        if (dateFromPicker != null) dateFromPicker.setValue(null);
        if (dateToPicker != null) dateToPicker.setValue(null);
        ordersTable.setItems(orders);
    }
}