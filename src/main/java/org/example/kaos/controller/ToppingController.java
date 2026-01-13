package org.example.kaos.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.kaos.entity.Topping;
import org.example.kaos.service.IToppingService;
import org.example.kaos.service.implementation.ToppingServiceImpl;

import java.net.URL;
import java.util.ResourceBundle;

public class ToppingController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private Button actionButton;
    @FXML private Button cancelEditBtn;
    @FXML private TableView<Topping> toppingsTable;

    private final IToppingService toppingService = new ToppingServiceImpl();
    private Topping toppingToEdit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadToppings();
        setupTable();
    }

    private void loadToppings() {
        toppingsTable.getItems().setAll(toppingService.getAllToppings());
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {

        TableColumn<Topping, String> nameCol = new TableColumn<>("Nombre");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        nameCol.setPrefWidth(210);

        TableColumn<Topping, String> priceCol = new TableColumn<>("Precio");
        priceCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getPrice()))
        );
        priceCol.setPrefWidth(120);

        TableColumn<Topping, Void> actionsCol = new TableColumn<>("");
        actionsCol.setPrefWidth(220);

        actionsCol.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn = new Button("Editar");
            private final Button deleteBtn = new Button("Eliminar");
            {
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
                deleteBtn.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; -fx-background-radius: 5;");

                editBtn.setOnAction(event -> {
                    Topping topping = getTableView().getItems().get(getIndex());
                    editTopping(topping);
                });

                deleteBtn.setOnAction(event -> {
                    Topping topping = getTableView().getItems().get(getIndex());
                    deleteTopping(topping);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8, editBtn, deleteBtn);
                    box.setStyle("-fx-alignment: center;");
                    setGraphic(box);
                }
            }
        });

        toppingsTable.getColumns().setAll(nameCol, priceCol, actionsCol);
    }

    @FXML
    private void handleAddOrUpdate() {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty()) {
            showAlert("Error", "Por favor completa todos los campos.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);

            if (toppingToEdit != null) {
                // Actualizar topping existente
                toppingToEdit.setName(name);
                toppingToEdit.setPrice(price);
                toppingService.createTopping(toppingToEdit);
            } else {
                // Crear nuevo topping
                Topping topping = new Topping();
                topping.setName(name);
                topping.setPrice(price);
                toppingService.createTopping(topping);
            }

            clearForm();
            loadToppings();

        } catch (NumberFormatException e) {
            showAlert("Error", "Por favor ingresa un precio válido.");
        }
    }

    @FXML
    private void cancelEdit() {
        clearForm();
    }

    private void editTopping(Topping topping) {
        toppingToEdit = topping;
        nameField.setText(topping.getName());
        priceField.setText(String.valueOf(topping.getPrice()));

        actionButton.getStyleClass().remove("add-btn");
        if (!actionButton.getStyleClass().contains("edit-btn")) {
            actionButton.getStyleClass().add("edit-btn");
        }

        Label icon = (Label) actionButton.getGraphic();
        icon.setText("✓");
        icon.getStyleClass().setAll("check-icon");

        cancelEditBtn.setVisible(true);
    }

    private void clearForm() {
        nameField.clear();
        priceField.clear();
        toppingToEdit = null;

        actionButton.getStyleClass().remove("edit-btn");
        if (!actionButton.getStyleClass().contains("add-btn")) {
            actionButton.getStyleClass().add("add-btn");
        }

        Label icon = (Label) actionButton.getGraphic();
        icon.setText("+");
        icon.getStyleClass().setAll("plus-icon");

        cancelEditBtn.setVisible(false);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void deleteTopping(Topping topping) {
        boolean confirm = showConfirmation("Eliminar Topping",
                "¿Estás seguro de que deseas eliminar el topping: " + topping.getName() + "?");

        if (confirm) {
            toppingService.deleteTopping(topping.getId());
            loadToppings();
        }
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}