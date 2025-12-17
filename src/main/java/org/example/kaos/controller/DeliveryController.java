package org.example.kaos.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.kaos.entity.Delivery;
import org.example.kaos.entity.Store;
import org.example.kaos.service.IDeliveryService;
import org.example.kaos.service.IStoreService;
import org.example.kaos.service.implementation.DeliveryServiceImpl;
import org.example.kaos.service.implementation.StoreServiceImpl;
import org.example.kaos.util.DialogUtil;
import org.example.kaos.util.Session;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class DeliveryController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TextField nameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<Store> storeCombo;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label counterLabel;
    @FXML private TableView<Delivery> deliveryTable;
    @FXML private TableColumn<Delivery, Long> idColumn;
    @FXML private TableColumn<Delivery, String> nameColumn;
    @FXML private TableColumn<Delivery, String> lastNameColumn;
    @FXML private TableColumn<Delivery, String> phoneColumn;
    @FXML private TableColumn<Delivery, String> storeColumn;
    @FXML private TableColumn<Delivery, Void> actionsColumn;

    private final IStoreService storeService = new StoreServiceImpl();
    private final IDeliveryService deliveryService = new DeliveryServiceImpl();

    private ObservableList<Delivery> deliveriesList;
    private Delivery deliveryToEdit;
    private boolean isEditing = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStoreComboBox();
        setupTable();
        loadDeliveries();
        clearForm();
    }

    private void setupStoreComboBox() {
        List<Store> stores = storeService.getAllStore();
        storeCombo.getItems().setAll(stores);

        storeCombo.setCellFactory(lv -> new ListCell<Store>() {
            @Override
            protected void updateItem(Store store, boolean empty) {
                super.updateItem(store, empty);
                setText(empty || store == null ? null : store.getName());
            }
        });

        storeCombo.setButtonCell(new ListCell<Store>() {
            @Override
            protected void updateItem(Store store, boolean empty) {
                super.updateItem(store, empty);
                setText(empty || store == null ? "Selecciona sucursal" : store.getName());
            }
        });
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        storeColumn.setCellValueFactory(cellData -> {
            Store store = cellData.getValue().getStore();
            return new SimpleStringProperty(store != null ? store.getName() : "Sin asignar");
        });

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Delivery, Void>, TableCell<Delivery, Void>> cellFactory =
                param -> new TableCell<>() {
                    private final Button editBtn = new Button("Editar");
                    private final Button deleteBtn = new Button("Eliminar");
                    private final HBox pane = new HBox(5, editBtn, deleteBtn);

                    {
                        editBtn.getStyleClass().add("edit-btn");
                        deleteBtn.getStyleClass().add("delete-btn");

                        editBtn.setOnAction(event -> {
                            Delivery delivery = getTableView().getItems().get(getIndex());
                            editDelivery(delivery);
                        });

                        deleteBtn.setOnAction(event -> {
                            Delivery delivery = getTableView().getItems().get(getIndex());
                            deleteDelivery(delivery);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };

        actionsColumn.setCellFactory(cellFactory);
    }

    private void loadDeliveries() {
        List<Delivery> deliveries = deliveryService.findAll();
        deliveriesList = FXCollections.observableArrayList(deliveries);
        deliveryTable.setItems(deliveriesList);
        updateCounter();
    }

    private void updateCounter() {
        int count = deliveriesList.size();
        counterLabel.setText(count + " deliverys");
    }

    @FXML
    public void handleSearch(ActionEvent actionEvent) {
        String searchText = searchField.getText().trim();
        List<Delivery> deliveries;

        if (searchText.isEmpty()) {
            deliveries = deliveryService.findAll();
        } else {
            deliveries = deliveryService.findByName(searchText);
        }

        deliveriesList.setAll(deliveries);
        updateCounter();
    }

    @FXML
    public void handleSave(ActionEvent actionEvent) {
        if (!validateForm()) {
            return;
        }

        try {
            Delivery delivery;

            if (isEditing && deliveryToEdit != null) {
                // Modo edición
                delivery = deliveryToEdit;
                delivery.setName(nameField.getText().trim());
                delivery.setLastName(lastNameField.getText().trim());
                delivery.setPhoneNumber(phoneField.getText().trim());
                delivery.setStore(storeCombo.getValue());

                deliveryService.update(delivery);
                DialogUtil.showWarning("Éxito", "Delivery actualizado");
            } else {
                // Modo creación
                delivery = Delivery.builder()
                        .name(nameField.getText().trim())
                        .lastName(lastNameField.getText().trim())
                        .phoneNumber(phoneField.getText().trim())
                        .store(storeCombo.getValue())
                        .createdByUser(Session.getInstance().getCurrentUser().getId())
                        .createdDate(LocalDateTime.now())
                        .build();

                deliveryService.save(delivery);
                DialogUtil.showWarning("Éxito", "Delivery creado");
            }

            loadDeliveries();
            clearForm();

        } catch (Exception e) {
            DialogUtil.showWarning("Error", e.getMessage());
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errors.append("• El nombre es obligatorio\n");
            nameField.getStyleClass().add("error-field");
            isValid = false;
        } else {
            nameField.getStyleClass().removeAll("error-field");
        }

        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errors.append("• El apellido es obligatorio\n");
            lastNameField.getStyleClass().add("error-field");
            isValid = false;
        } else {
            lastNameField.getStyleClass().removeAll("error-field");
        }

        if (phoneField.getText() == null || phoneField.getText().trim().isEmpty()) {
            errors.append("• El teléfono es obligatorio\n");
            phoneField.getStyleClass().add("error-field");
            isValid = false;
        } else {
            phoneField.getStyleClass().removeAll("error-field");
        }

        if (storeCombo.getValue() == null) {
            errors.append("• Debe seleccionar una sucursal\n");
            storeCombo.getStyleClass().add("error-field");
            isValid = false;
        } else {
            storeCombo.getStyleClass().removeAll("error-field");
        }

        if (!isValid) {
            DialogUtil.showWarning("Validación", errors.toString());
        }

        return isValid;
    }

    private void editDelivery(Delivery delivery) {
        deliveryToEdit = delivery;
        isEditing = true;

        nameField.setText(delivery.getName());
        lastNameField.setText(delivery.getLastName());
        phoneField.setText(delivery.getPhoneNumber());

        if (delivery.getStore() != null) {
            Long storeId = delivery.getStore().getId();

            for (Store store : storeCombo.getItems()) {
                if (store.getId().equals(storeId)) {
                    storeCombo.setValue(store);
                    break;
                }
            }
        }

        saveButton.setText("Actualizar");
        cancelButton.setVisible(true);

        Platform.runLater(() -> nameField.requestFocus());
    }

    private void deleteDelivery(Delivery delivery) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar delivery?");
        alert.setContentText(delivery.getName() + " " + delivery.getLastName());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    deliveryService.delete(delivery.getId());
                    DialogUtil.showWarning("Éxito", "Delivery eliminado");
                    loadDeliveries();

                    if (isEditing && deliveryToEdit != null && deliveryToEdit.getId().equals(delivery.getId())) {
                        clearForm();
                    }

                } catch (Exception e) {
                    DialogUtil.showWarning("Error", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void handleCancel(ActionEvent actionEvent) {
        clearForm();
    }

    private void clearForm() {
        nameField.clear();
        lastNameField.clear();
        phoneField.clear();
        storeCombo.setValue(null);

        nameField.getStyleClass().removeAll("error-field");
        lastNameField.getStyleClass().removeAll("error-field");
        phoneField.getStyleClass().removeAll("error-field");
        storeCombo.getStyleClass().removeAll("error-field");

        isEditing = false;
        deliveryToEdit = null;
        saveButton.setText("Guardar");
        cancelButton.setVisible(false);

        deliveryTable.getSelectionModel().clearSelection();
        searchField.clear();

        Platform.runLater(() -> nameField.requestFocus());
    }

    @FXML
    public void handleClose(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}