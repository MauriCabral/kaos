package org.example.kaos.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.BurgerVariant;
import org.example.kaos.entity.ExtraItem;
import org.example.kaos.service.IBurgerService;
import org.example.kaos.service.IExtraItemService;
import org.example.kaos.service.implementation.BurgerServiceImpl;
import org.example.kaos.service.implementation.ExtraItemServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class ProductDialog {

    private final IBurgerService burgerService = new BurgerServiceImpl();
    private final IExtraItemService extraItemService = new ExtraItemServiceImpl();

    @Setter private Runnable onSuccessCallback;

    @Setter private ProductMode mode = ProductMode.ADD;
    @Setter private Object productToEdit;
    public enum ProductMode { ADD, EDIT }

    boolean isBurger = false, isExtra = false, isCombo = false;
    byte[] existingImageData;

    public void show() {
        Stage dialog = new Stage();
        dialog.setTitle(mode == ProductMode.ADD ? "Agregar Producto" : "Editar Producto");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label(mode == ProductMode.ADD ? "Agregar Nuevo Producto" : "Editar Producto");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e9500e;");

        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Tipo de Producto *");
        typeLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Burger", "Extra", "Combo");
        typeComboBox.setStyle("-fx-pref-width: 280px;");
        typeComboBox.setValue("Burger");

        typeBox.getChildren().addAll(typeLabel, typeComboBox);

        ImageView previewImage = new ImageView();
        previewImage.setFitWidth(100);
        previewImage.setFitHeight(100);
        previewImage.setPreserveRatio(true);
        previewImage.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8px;");

        VBox previewBox = new VBox(8);
        previewBox.setAlignment(Pos.CENTER);
        Label previewLabel = new Label("Vista Previa");
        previewLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        previewBox.getChildren().addAll(previewLabel, previewImage);

        VBox formLayout = new VBox(10);
        formLayout.setAlignment(Pos.CENTER_LEFT);

        VBox nameBox = new VBox(3);
        Label nameLabel = new Label("Nombre *");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        TextField nameField = new TextField();
        nameField.setStyle("-fx-pref-width: 280px; -fx-pref-height: 30px;");
        nameBox.getChildren().addAll(nameLabel, nameField);

        VBox codeBox = new VBox(3);
        Label codeLabel = new Label("Código (2 caracteres) *");
        codeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        TextField codeField = new TextField();
        codeField.setStyle("-fx-pref-width: 280px; -fx-pref-height: 30px;");

        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 2) {
                codeField.setText(oldValue);
            }
        });
        codeBox.getChildren().addAll(codeLabel, codeField);

        VBox descriptionBox = new VBox(3);
        Label descriptionLabel = new Label("Descripción *");
        descriptionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        TextArea descriptionField = new TextArea();
        descriptionField.setStyle("-fx-pref-width: 280px; -fx-pref-height: 60px; -fx-wrap-text: true;");
        descriptionField.setPrefRowCount(3);
        descriptionBox.getChildren().addAll(descriptionLabel, descriptionField);

        VBox priceBox = new VBox(3);
        Label priceLabel = new Label("Precio *");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        TextField priceField = new TextField();
        priceField.setStyle("-fx-pref-width: 60px; -fx-pref-height: 30px;");
        priceBox.getChildren().addAll(priceLabel, priceField);

        VBox burgerPricesBox = new VBox(3);
        burgerPricesBox.setVisible(false);
        burgerPricesBox.setManaged(false);

        Label burgerPricesLabel = new Label("Precios de Variantes *");
        burgerPricesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        HBox pricesRow = new HBox(8);
        pricesRow.setAlignment(Pos.CENTER_LEFT);

        VBox simplePriceBox = new VBox(2);
        Label simplePriceLabel = new Label("Simple *");
        simplePriceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        TextField simplePriceField = new TextField();
        simplePriceField.setStyle("-fx-pref-width: 85px; -fx-pref-height: 30px;");
        simplePriceBox.getChildren().addAll(simplePriceLabel, simplePriceField);

        VBox doblePriceBox = new VBox(2);
        Label doblePriceLabel = new Label("Doble *");
        doblePriceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        TextField doblePriceField = new TextField();
        doblePriceField.setStyle("-fx-pref-width: 85px; -fx-pref-height: 30px;");
        doblePriceBox.getChildren().addAll(doblePriceLabel, doblePriceField);

        VBox triplePriceBox = new VBox(2);
        Label triplePriceLabel = new Label("Triple *");
        triplePriceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        TextField triplePriceField = new TextField();
        triplePriceField.setStyle("-fx-pref-width: 85px; -fx-pref-height: 30px;");
        triplePriceBox.getChildren().addAll(triplePriceLabel, triplePriceField);

        pricesRow.getChildren().addAll(simplePriceBox, doblePriceBox, triplePriceBox);
        burgerPricesBox.getChildren().addAll(burgerPricesLabel, pricesRow);

        VBox imageBox = new VBox(3);
        Label imageLabel = new Label("Imagen *");
        imageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        HBox imageButtonBox = new HBox(8);
        imageButtonBox.setAlignment(Pos.CENTER_LEFT);

        Button selectImageBtn = new Button("Seleccionar Imagen");
        selectImageBtn.setStyle("-fx-background-color: #e9500e; -fx-text-fill: white; -fx-padding: 6px 12px; -fx-font-size: 12px;");

        Label fileNameLabel = new Label("No se seleccionó imagen");
        fileNameLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666; -fx-font-size: 11px;");

        imageButtonBox.getChildren().addAll(selectImageBtn, fileNameLabel);
        imageBox.getChildren().addAll(imageLabel, imageButtonBox);

        formLayout.getChildren().addAll(nameBox, codeBox, descriptionBox, priceBox, burgerPricesBox, imageBox);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 11px;");
        errorLabel.setVisible(false);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd; -fx-padding: 8px 16px; -fx-font-size: 12px;");

        Button saveBtn = new Button("Guardar");
        saveBtn.setStyle("-fx-background-color: #e9500e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-font-size: 12px;");

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        final File[] selectedImage = {null};

        typeComboBox.setOnAction(e -> {
            String selectedType = typeComboBox.getValue();
            updateFormForType(selectedType, codeBox, descriptionBox, priceBox, burgerPricesBox);
        });

        updateFormForType("Burger", codeBox, descriptionBox, priceBox, burgerPricesBox);

        selectImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar una imagen");

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "Archivos de imagen", "*.png", "*.jpg", "*.jpeg", "*.gif"
            );
            fileChooser.getExtensionFilters().add(extFilter);

            File file = fileChooser.showOpenDialog(dialog);
            if (file != null) {
                selectedImage[0] = file;
                fileNameLabel.setText(file.getName());

                try {
                    Image image = new Image(file.toURI().toString());
                    previewImage.setImage(image);
                    errorLabel.setVisible(false);
                } catch (Exception ex) {
                    errorLabel.setText("Error al cargar la imagen");
                    errorLabel.setVisible(true);
                }
            }
        });

        if (mode == ProductMode.EDIT) {
            int id = 0;
            if (productToEdit instanceof ExtraItem) {
                ExtraItem extraItem = (ExtraItem) productToEdit;
                if (extraItem.getExtraItemId() == 1) {
                    isExtra = true;
                    id = 1;
                } else {
                    isCombo = true;
                    id = 2;
                }
                existingImageData = extraItem.getImageData();
            } else if (productToEdit instanceof Burger) {
                isBurger = true;
                existingImageData = ((Burger) productToEdit).getImageData();
            }

            typeComboBox.setValue(typeComboBox.getItems().get(id));
            typeComboBox.setDisable(true);

            if (existingImageData != null && existingImageData.length > 0) {
                Image image = new Image(new ByteArrayInputStream(existingImageData));
                previewImage.setImage(image);
            } else {
                previewImage.setImage(null);
            }

            updateFormForType(typeComboBox.getValue().toString(), codeBox, descriptionBox, priceBox, burgerPricesBox);

            if (isBurger) {
                Burger burger = ((Burger) productToEdit);
                List<BurgerVariant> burgerVariantList = burgerService.getVariantsByBurgerId(burger.getId());

                nameField.setText(burger.getName());
                codeField.setText(burger.getCode());
                descriptionField.setText(burger.getDescription());
                for (BurgerVariant variant : burgerVariantList) {
                    if (variant.getVariantType().getId() == 1) { // SIMPLE
                        simplePriceField.setText(variant.getPrice().toString());
                    } else if (variant.getVariantType().getId() == 2) { // DOBLE
                        doblePriceField.setText(variant.getPrice().toString());
                    } else { // TRIPLE
                        triplePriceField.setText(variant.getPrice().toString());
                    }
                }
            } else if (isExtra || isCombo) {
                ExtraItem extraItem = (ExtraItem) productToEdit;
                nameField.setText(extraItem.getName());
                descriptionField.setText(extraItem.getDescription());
                priceField.setText(extraItem.getPrice().toString());
            }
        }

        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            String selectedType = typeComboBox.getValue();
            if (validateForm(selectedType, nameField.getText(), codeField.getText(),
                    descriptionField.getText(), priceField.getText(),
                    simplePriceField.getText(), doblePriceField.getText(), triplePriceField.getText(),
                    selectedImage[0], errorLabel)) {
                try {
                    byte[] imageDataToUse;

                    if (mode == ProductMode.ADD) {
                        imageDataToUse = Files.readAllBytes(selectedImage[0].toPath());
                    } else {
                        imageDataToUse = (selectedImage[0] != null)
                                ? Files.readAllBytes(selectedImage[0].toPath())
                                : existingImageData;
                    }

                    switch (selectedType) {
                        case "Burger":
                            saveBurger(nameField.getText().trim(), codeField.getText().trim(), descriptionField.getText().trim(), imageDataToUse,
                                    Double.parseDouble(simplePriceField.getText()),
                                    Double.parseDouble(doblePriceField.getText()),
                                    Double.parseDouble(triplePriceField.getText()));
                            break;
                        case "Extra":
                            if (mode == ProductMode.ADD && extraItemService.hasExtra()) {
                                DialogUtil.showWarning("Atención", "Ya existe un Extra. Solo puede haber uno.");
                                return;
                            }
                            saveExtraItem(nameField.getText().trim(), descriptionField.getText().trim(),
                                    Double.parseDouble(priceField.getText()), imageDataToUse, 1); // extra_id = 1
                            break;
                        case "Combo":
                            saveExtraItem(nameField.getText().trim(), descriptionField.getText().trim(),
                                    Double.parseDouble(priceField.getText()), imageDataToUse, 2); // extra_id = 2
                            break;
                    }
                    dialog.close();

                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }

                } catch (Exception ex) {
                    errorLabel.setText("Error al guardar: " + ex.getMessage());
                    errorLabel.setVisible(true);
                    ex.printStackTrace();
                }
            }
        });

        mainLayout.getChildren().addAll(titleLabel, typeBox, previewBox, formLayout, errorLabel, buttonBox);

        Scene scene = new Scene(mainLayout, 350, 650);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void updateFormForType(String type, VBox codeBox, VBox descriptionBox, VBox priceBox, VBox burgerPricesBox) {
        boolean isBurger = "Burger".equals(type);
        boolean isExtraOrCombo = "Extra".equals(type) || "Combo".equals(type);

        codeBox.setVisible(isBurger);
        codeBox.setManaged(isBurger);
        descriptionBox.setVisible(true);
        descriptionBox.setManaged(true);
        priceBox.setVisible(isExtraOrCombo);
        priceBox.setManaged(isExtraOrCombo);
        burgerPricesBox.setVisible(isBurger);
        burgerPricesBox.setManaged(isBurger);
    }

    private boolean validateForm(String type, String name, String code, String description,
                                 String price, String simplePrice, String doblePrice, String triplePrice,
                                 File image, Label errorLabel) {
        long idProduct = 0L;
        if (name == null || name.trim().isEmpty()) {
            errorLabel.setText("El nombre es obligatorio");
            errorLabel.setVisible(true);
            return false;
        }

        if (mode == ProductMode.ADD && image == null) {
            errorLabel.setText("Debe seleccionar una imagen");
            errorLabel.setVisible(true);
            return false;
        }

        switch (type) {
            case "Burger":
                if (productToEdit != null) {
                    idProduct = ((Burger) productToEdit).getId();
                }
                if (code == null || code.trim().length() != 2) {
                    errorLabel.setText("El código debe tener exactamente 2 caracteres");
                    errorLabel.setVisible(true);
                    return false;
                }
                if (description == null || description.trim().isEmpty()) {
                    errorLabel.setText("La descripción es obligatoria");
                    errorLabel.setVisible(true);
                    return false;
                }
                if (simplePrice == null || simplePrice.trim().isEmpty()) {
                    errorLabel.setText("El precio simple es obligatorio");
                    errorLabel.setVisible(true);
                    return false;
                }
                if (doblePrice == null || doblePrice.trim().isEmpty()) {
                    errorLabel.setText("El precio doble es obligatorio");
                    errorLabel.setVisible(true);
                    return false;
                }
                if (triplePrice == null || triplePrice.trim().isEmpty()) {
                    errorLabel.setText("El precio triple es obligatorio");
                    errorLabel.setVisible(true);
                    return false;
                }
                try {
                    double simple = Double.parseDouble(simplePrice.trim());
                    double doble = Double.parseDouble(doblePrice.trim());
                    double triple = Double.parseDouble(triplePrice.trim());
                    if (simple <= 0 || doble <= 0 || triple <= 0) {
                        errorLabel.setText("Todos los precios deben ser mayores a 0");
                        errorLabel.setVisible(true);
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    errorLabel.setText("Los precios deben ser números válidos");
                    errorLabel.setVisible(true);
                    return false;
                }
                if (burgerService.nameExists(idProduct, name.trim())) {
                    errorLabel.setText("Ya existe una burger con ese nombre");
                    errorLabel.setVisible(true);
                    return false;
                }
                if (burgerService.codeExists(idProduct, code.trim())) {
                    errorLabel.setText("Ya existe una burger con ese código");
                    errorLabel.setVisible(true);
                    return false;
                }
                break;

            case "Extra":
                break;

            case "Combo":
                if (productToEdit != null && productToEdit instanceof ExtraItem) {
                    idProduct = ((ExtraItem) productToEdit).getId();
                }
                if (extraItemService.nameExists(idProduct, name.trim())) {
                    errorLabel.setText("Ya existe un combo con ese nombre");
                    errorLabel.setVisible(true);
                    return false;
                }
                if (description == null || description.trim().isEmpty()) {
                    errorLabel.setText("La descripción es obligatoria");
                    errorLabel.setVisible(true);
                    return false;
                }
                if (price == null || price.trim().isEmpty()) {
                    errorLabel.setText("El precio es obligatorio");
                    errorLabel.setVisible(true);
                    return false;
                }
                try {
                    double priceValue = Double.parseDouble(price.trim());
                    if (priceValue <= 0) {
                        errorLabel.setText("El precio debe ser mayor a 0");
                        errorLabel.setVisible(true);
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    errorLabel.setText("El precio debe ser un número válido");
                    errorLabel.setVisible(true);
                    return false;
                }
                break;
        }

        errorLabel.setVisible(false);
        return true;
    }

    private void saveBurger(String name, String code, String description, byte[] imageData, double simplePrice, double doblePrice, double triplePrice) {
        boolean res = false;
        Burger burger = Burger.builder()
                .id(isBurger ? ((Burger) productToEdit).getId() : 0)
                .name(name.trim())
                .code(code.trim().toUpperCase())
                .description(description.trim())
                .imageData(imageData)
                .createdByUser(Session.getInstance().getCurrentUser().getId())
                .build();
        if (mode == ProductMode.EDIT) {
            res = burgerService.updateBurgerWithVariants(burger, simplePrice, doblePrice, triplePrice);
            if (res) {
                DialogUtil.showInfo("Éxito", "Burger actualizado correctamente");
            } else {
                DialogUtil.showError("Error", "Error al actualizar la burger.");
            }
        } else {
            res = burgerService.saveBurgerWithVariants(burger, simplePrice, doblePrice, triplePrice);
            if (res) {
                DialogUtil.showInfo("Éxito", "Burger agregado correctamente");
            } else {
                DialogUtil.showError("Error", "Error al agregadar el burger.");
            }
        }
    }

    private void saveExtraItem(String name, String description, double price, byte[] imageData, int extraItemId) {
        boolean res = false;
        Long id = (productToEdit instanceof ExtraItem) ? ((ExtraItem) productToEdit).getId() : null;

        ExtraItem extraItem = ExtraItem.builder()
                .id(id)
                .extraItemId(extraItemId) // 1 para Extra, 2 para Combo
                .name(name.trim())
                .description(description.trim())
                .price(price)
                .imageData(imageData)
                .createdByUser(Session.getInstance().getCurrentUser().getId())
                .build();

        boolean isNew = (mode == ProductMode.ADD);
        res = extraItemService.saveOrUpdateExtraItem(extraItem, isNew);

        if (res) {
            String productType = (extraItemId == 1) ? "Extra" : "Combo";
            String action = isNew ? "agregado" : "actualizado";
            DialogUtil.showInfo("Éxito", productType + " " + action + " correctamente");
        } else {
            String productType = (extraItemId == 1) ? "Extra" : "Combo";
            String action = isNew ? "agregar" : "actualizar";
            DialogUtil.showError("Error", "Error al " + action + " el " + productType.toLowerCase());
        }
    }
}