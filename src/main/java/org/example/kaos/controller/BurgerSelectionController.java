package org.example.kaos.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.kaos.entity.*;
import org.example.kaos.service.IBurgerService;
import org.example.kaos.service.IToppingService;
import org.example.kaos.service.IVariantService;
import org.example.kaos.service.implementation.BurgerServiceImpl;
import org.example.kaos.service.implementation.ToppingServiceImpl;
import org.example.kaos.service.implementation.VariantServiceImpl;

import java.net.URL;
import java.util.*;

public class BurgerSelectionController implements Initializable {

    @FXML private ImageView burgerImageView;
    @FXML private Label burgerNameLabel;
    @FXML private Label burgerDescriptionLabel;
    @FXML private ComboBox<VariantType> variantComboBox;
    @FXML private Label basePriceLabel;
    @FXML private Label toppingsPriceLabel;
    @FXML private Label totalPriceLabel;
    @FXML private VBox toppingsContainer;
    @FXML private Label toppingsDisabledLabel;
    @FXML private Label llbCbo;
    @FXML private TextArea observationsField;
    @FXML private Button decreaseBtn;
    @FXML private Button increaseBtn;
    @FXML private TextField quantityField;

    private final IVariantService variantService = new VariantServiceImpl();
    private final IToppingService toppingService = new ToppingServiceImpl();
    private final IBurgerService burgerService = new BurgerServiceImpl();

    private Burger selectedBurger;
    private BurgerVariant selectedVariant;
    private List<Topping> availableToppings;
    private List<OrderDetailTopping> selectedToppings = new ArrayList<>();
    private boolean confirmed = false;
    private OrderDetail resultOrderDetail;
    private int quantity = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAvailableToppings();
        toppingsContainer.setDisable(true);
        if (toppingsDisabledLabel != null) {
            toppingsDisabledLabel.setVisible(true);
            toppingsDisabledLabel.setText("Selecciona una variante para ver toppings");
        }

        setupVariantComboBox();
        setupCounterBurger();

        Platform.runLater(() -> {
            Scene scene = toppingsContainer.getScene();
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        handleCancel();
                        event.consume();
                    } else if (event.getCode() == KeyCode.SPACE) {
                        if (scene.getFocusOwner() != observationsField) {
                            handleAddToOrder();
                            event.consume();
                        }
                    }
                });

                scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                    if (scene.getFocusOwner() == observationsField && event.getTarget() != observationsField) {
                        scene.getRoot().requestFocus();
                    }
                });
            }
        });
    }

    private void setupVariantComboBox() {
        List<VariantType> variants = variantService.getAllVariants();
        variantComboBox.getItems().setAll(variants);

        variantComboBox.setCellFactory(lv -> new ListCell<VariantType>() {
            @Override
            protected void updateItem(VariantType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        variantComboBox.setButtonCell(new ListCell<VariantType>() {
            @Override
            protected void updateItem(VariantType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Selecciona un tipo" : item.getName());
            }
        });

        variantComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                llbCbo.setVisible(false);

                List<BurgerVariant> burgerVariants = burgerService.getVariantsByBurgerId(selectedBurger.getId());

                selectedVariant = burgerVariants.stream()
                        .filter(v -> v.getVariantType().getId().equals(newVal.getId()))
                        .findFirst()
                        .orElse(null);

                if (selectedVariant == null) {
                    selectedBurger.getVariants().size();

                    selectedVariant = selectedBurger.getVariants().stream()
                            .filter(v -> v.getVariantType().getId().equals(newVal.getId()))
                            .findFirst()
                            .orElse(null);
                }

                toppingsContainer.setDisable(selectedVariant == null);
                if (selectedVariant != null && toppingsDisabledLabel != null) {
                    toppingsDisabledLabel.setVisible(false);
                }

                updatePrice();
            }
        });
    }

    public void setBurger(Burger burger) {
        this.selectedBurger = burger;

        if (selectedBurger != null) {
            List<BurgerVariant> variants = burgerService.getVariantsByBurgerId(selectedBurger.getId());
            selectedBurger.setVariants(new ArrayList<>(variants));
        }

        updateBurgerInfo();
        updatePrice();

        toppingsContainer.setDisable(true);
        if (toppingsDisabledLabel != null) {
            toppingsDisabledLabel.setVisible(true);
            toppingsDisabledLabel.setText("Selecciona una variante para ver toppings");
        }

        variantComboBox.getSelectionModel().clearSelection();
        selectedVariant = null;
    }

    private void loadAvailableToppings() {
        availableToppings = toppingService.getAllToppings();
        if (availableToppings == null) availableToppings = new ArrayList<>();
        updateToppingsGrid();
    }

    private void updateBurgerInfo() {
        if (selectedBurger != null) {
            burgerNameLabel.setText(selectedBurger.getName());
            burgerDescriptionLabel.setText(selectedBurger.getDescription());

            if (selectedBurger.getImageData() != null && selectedBurger.getImageData().length > 0) {
                Image image = new Image(new java.io.ByteArrayInputStream(selectedBurger.getImageData()));
                burgerImageView.setImage(image);
            }
        }
    }

    private void updateToppingsGrid() {
        toppingsContainer.getChildren().clear();
        selectedToppings.clear();

        for (Topping topping : availableToppings) {
            HBox row = new HBox(6);
            row.getStyleClass().add("topping-row");
            row.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(topping.getName());
            nameLabel.getStyleClass().add("topping-name");

            Label unitLabel = new Label("c/u ");
            unitLabel.getStyleClass().add("topping-unit");

            Label priceLabel = new Label("$" + String.valueOf(topping.getPrice().intValue()).trim());
            priceLabel.getStyleClass().add("topping-price");

            Button decreaseBtn = new Button("-");
            decreaseBtn.getStyleClass().add("counter-btn");
            decreaseBtn.setMinWidth(26);
            decreaseBtn.setMaxWidth(26);

            TextField quantityField = new TextField("0");
            quantityField.getStyleClass().add("counter-field");
            quantityField.setMinWidth(40);
            quantityField.setMaxWidth(40);
            quantityField.setAlignment(Pos.CENTER);

            Button increaseBtn = new Button("+");
            increaseBtn.getStyleClass().add("counter-btn");
            increaseBtn.setMinWidth(26);
            increaseBtn.setMaxWidth(26);

            HBox counterBox = new HBox(4, decreaseBtn, quantityField, increaseBtn);
            counterBox.setAlignment(Pos.CENTER_RIGHT);

            int currentQuantity = selectedToppings.stream()
                    .filter(odt -> odt.getTopping().equals(topping))
                    .mapToInt(OrderDetailTopping::getQuantity)
                    .findFirst()
                    .orElse(0);
            quantityField.setText(String.valueOf(currentQuantity));

            decreaseBtn.setOnAction(e -> {
                int qty = Integer.parseInt(quantityField.getText());
                if (qty > 0) {
                    qty--;
                    quantityField.setText(String.valueOf(qty));
                    handleToppingSelection(topping, qty);
                    updatePrice();
                }
            });

            increaseBtn.setOnAction(e -> {
                int qty = Integer.parseInt(quantityField.getText());
                qty++;
                quantityField.setText(String.valueOf(qty));
                handleToppingSelection(topping, qty);
                updatePrice();
            });

            quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    quantityField.setText(oldVal);
                } else {
                    int qty = newVal.isEmpty() ? 0 : Integer.parseInt(newVal);
                    handleToppingSelection(topping, qty);
                    updatePrice();
                }
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(nameLabel, unitLabel, priceLabel, spacer, counterBox);
            toppingsContainer.getChildren().add(row);
        }
    }

    private void handleToppingSelection(Topping topping, int quantity) {
        selectedToppings.removeIf(odt -> odt.getTopping().equals(topping));

        if (quantity > 0) {
            OrderDetailTopping odt = new OrderDetailTopping();
            odt.setTopping(topping);
            odt.setQuantity(quantity);
            odt.setPricePerUnit(topping.getPrice());
            odt.calculateTotalPrice();
            selectedToppings.add(odt);
        }
    }

    private void updatePrice() {
        double basePrice = selectedVariant != null ? selectedVariant.getPrice() : 0.0;
        double toppingsPrice = selectedToppings.stream()
                .mapToDouble(OrderDetailTopping::getTotalPrice)
                .sum();

        double totalBasePrice = basePrice * quantity;
        double totalToppingsPrice = toppingsPrice * quantity;
        double totalPrice = totalBasePrice + totalToppingsPrice;

        basePriceLabel.setText("$" + (int)basePrice);
        toppingsPriceLabel.setText("$" + (int)totalToppingsPrice);
        totalPriceLabel.setText("$" + (int)totalPrice);
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        closeWindow();
    }

    @FXML
    private void handleAddToOrder() {
        if (selectedVariant == null) {
            showAlert("Error", "Por favor selecciona un tipo de burger.");
            return;
        }

        OrderDetail orderDetail = new OrderDetail();

        BurgerVariant detachedVariant = new BurgerVariant();
        detachedVariant.setId(selectedVariant.getId());

        if (selectedVariant.getVariantType() != null) {
            VariantType variantType = new VariantType();
            variantType.setId(selectedVariant.getVariantType().getId());
            variantType.setName(selectedVariant.getVariantType().getName());
            detachedVariant.setVariantType(variantType);
        }

        detachedVariant.setPrice(selectedVariant.getPrice());
        detachedVariant.setIsAvailable(selectedVariant.getIsAvailable());

        orderDetail.setBurgerVariant(detachedVariant);
        orderDetail.setProductName(selectedBurger.getName());

        String variantName = selectedVariant.getVariantType() != null ?
                selectedVariant.getVariantType().getName() : "Desconocido";
        orderDetail.setVariantName(variantName);

        orderDetail.setQuantity(quantity);
        String obs = observationsField.getText().trim();
        orderDetail.setObservations(obs.isEmpty() ? null : obs);

        double basePrice = selectedVariant.getPrice();
        double toppingsTotal = 0;

        for (OrderDetailTopping toppingSelection : selectedToppings) {
            OrderDetailTopping odt = new OrderDetailTopping();
            odt.setTopping(toppingSelection.getTopping());
            odt.setQuantity(toppingSelection.getQuantity());
            odt.setPricePerUnit(toppingSelection.getPricePerUnit());
            odt.calculateTotalPrice();

            odt.setOrderDetail(orderDetail);
            orderDetail.getOrderDetailToppings().add(odt);

            toppingsTotal += odt.getTotalPrice();
        }

        orderDetail.setUnitPrice(basePrice);
        orderDetail.calculateSubtotal();
        orderDetail.setTotal(orderDetail.getSubtotal() + toppingsTotal);

        resultOrderDetail = orderDetail;
        confirmed = true;

        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) burgerNameLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isConfirmed() { return confirmed; }
    public OrderDetail getResultOrderDetail() { return resultOrderDetail; }
    public List<OrderDetailTopping> getSelectedToppings() { return selectedToppings; }

    private void setupCounterBurger() {
        quantityField.setText("1");
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantityField.setText(oldValue);
            } else if (!newValue.isEmpty()) {
                quantity = Integer.parseInt(newValue);
                updatePrice();
            }
        });

        decreaseBtn.setOnAction(e -> {
            if (quantity > 1) {
                quantity--;
                quantityField.setText(String.valueOf(quantity));
                updatePrice();
            }
        });

        increaseBtn.setOnAction(e -> {
            quantity++;
            quantityField.setText(String.valueOf(quantity));
            updatePrice();
        });
    }
}