package org.example.kaos.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    private final IVariantService variantService = new VariantServiceImpl();
    private final IToppingService toppingService = new ToppingServiceImpl();
    private final IBurgerService burgerService = new BurgerServiceImpl();

    private Burger selectedBurger;
    private BurgerVariant selectedVariant;
    private List<Topping> availableToppings;
    private Map<Topping, ToggleGroup> toppingRadioGroups = new HashMap<>();
    private List<OrderDetailTopping> selectedToppings = new ArrayList<>();

    private boolean confirmed = false;
    private OrderDetail resultOrderDetail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAvailableToppings();
        toppingsContainer.setDisable(true);
        if (toppingsDisabledLabel != null) {
            toppingsDisabledLabel.setVisible(true);
            toppingsDisabledLabel.setText("Selecciona una variante para ver toppings");
        }

        setupVariantComboBox();
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
        toppingRadioGroups.clear();
        selectedToppings.clear();

        for (Topping topping : availableToppings) {
            HBox row = new HBox(6);
            row.getStyleClass().add("topping-row");
            row.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(topping.getName());
            nameLabel.getStyleClass().add("topping-name");

            Label priceLabel = new Label(String.format("+$%.2f", topping.getPrice()));
            priceLabel.getStyleClass().add("topping-price");

            ToggleGroup tg = new ToggleGroup();
            RadioButton rbYes = new RadioButton("Sí");
            RadioButton rbNo = new RadioButton("No");

            rbYes.getStyleClass().add("radio-button");
            rbNo.getStyleClass().add("radio-button");

            rbYes.setToggleGroup(tg);
            rbNo.setToggleGroup(tg);
            rbNo.setSelected(true);

            HBox radioBox = new HBox(8, rbYes, rbNo);
            radioBox.getStyleClass().add("radio-box");
            radioBox.setAlignment(Pos.CENTER_RIGHT);

            tg.selectedToggleProperty().addListener((o, oldVal, newVal) -> {
                handleToppingSelection(topping, newVal == rbYes);
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(nameLabel, priceLabel, spacer, radioBox);
            toppingsContainer.getChildren().add(row);

            toppingRadioGroups.put(topping, tg);
        }
    }

    private void handleToppingSelection(Topping topping, boolean isSelected) {
        selectedToppings.removeIf(odt -> odt.getTopping().equals(topping));

        if (isSelected) {
            OrderDetailTopping odt = new OrderDetailTopping();
            odt.setTopping(topping);
            odt.setIsAdded(true);
            odt.setPricePerUnit(topping.getPrice());
            odt.calculateTotalPrice();
            selectedToppings.add(odt);
        }

        updatePrice();
    }

    private void updatePrice() {
        double basePrice = selectedVariant != null ? selectedVariant.getPrice() : 0.0;
        double toppingsPrice = selectedToppings.stream()
                .mapToDouble(OrderDetailTopping::getTotalPrice)
                .sum();
        double totalPrice = basePrice + toppingsPrice;

        basePriceLabel.setText(String.format("$%.2f", basePrice));
        toppingsPriceLabel.setText(String.format("$%.2f", toppingsPrice));
        totalPriceLabel.setText(String.format("$%.2f", totalPrice));
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

        // Crear una nueva instancia desacoplada
        BurgerVariant detachedVariant = new BurgerVariant();
        detachedVariant.setId(selectedVariant.getId());

        // variantType es EAGER, así que debería estar disponible
        // Solo necesitamos crear una nueva instancia si selectedVariant.getVariantType() es null
        if (selectedVariant.getVariantType() != null) {
            // Crear una copia del VariantType
            VariantType variantType = new VariantType();
            variantType.setId(selectedVariant.getVariantType().getId());
            variantType.setName(selectedVariant.getVariantType().getName());
            detachedVariant.setVariantType(variantType);
        }

        detachedVariant.setPrice(selectedVariant.getPrice());
        detachedVariant.setIsAvailable(selectedVariant.getIsAvailable());

        orderDetail.setBurgerVariant(detachedVariant);
        orderDetail.setProductName(selectedBurger.getName());

        // Obtener el nombre de la variante
        String variantName = selectedVariant.getVariantType() != null ?
                selectedVariant.getVariantType().getName() : "Desconocido";
        orderDetail.setVariantName(variantName);

        orderDetail.setQuantity(1);
        orderDetail.setObservations(observationsField.getText());

        double basePrice = selectedVariant.getPrice();
        double toppingsTotal = 0;

        List<OrderDetailTopping> toppingsForOrder = new ArrayList<>();
        for (OrderDetailTopping toppingSelection : selectedToppings) {
            if (Boolean.TRUE.equals(toppingSelection.getIsAdded())) {
                OrderDetailTopping odt = new OrderDetailTopping();
                odt.setTopping(toppingSelection.getTopping());
                odt.setIsAdded(true);
                odt.setPricePerUnit(toppingSelection.getPricePerUnit());
                odt.calculateTotalPrice();
                odt.setOrderDetail(orderDetail);
                toppingsForOrder.add(odt);

                toppingsTotal += odt.getTotalPrice();
            }
        }

        orderDetail.setUnitPrice(basePrice + toppingsTotal);
        orderDetail.calculateSubtotal();

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
}