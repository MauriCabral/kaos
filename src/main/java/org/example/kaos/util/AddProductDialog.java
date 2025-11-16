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
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.Combo;
import org.example.kaos.entity.Extra;
import org.example.kaos.service.IBurgerService;
import org.example.kaos.service.IComboService;
import org.example.kaos.service.IExtraService;
import org.example.kaos.service.implementation.BurgerServiceImpl;
import org.example.kaos.service.implementation.ComboServiceImpl;
import org.example.kaos.service.implementation.ExtraServiceImpl;

import java.io.File;
import java.nio.file.Files;

public class AddProductDialog {

    private final IBurgerService burgerService = new BurgerServiceImpl();
    private final IExtraService extraService = new ExtraServiceImpl();
    private final IComboService comboService = new ComboServiceImpl();

    private Runnable onSuccessCallback;

    public void setOnSuccessCallback(Runnable onSuccessCallback) {
        this.onSuccessCallback = onSuccessCallback;
    }

    public void show() {
        Stage dialog = new Stage();
        dialog.setTitle("Agregar Producto");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Agregar Nuevo Producto");
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
        TextField descriptionField = new TextField();
        descriptionField.setStyle("-fx-pref-width: 280px; -fx-pref-height: 30px;");
        descriptionBox.getChildren().addAll(descriptionLabel, descriptionField);

        VBox priceBox = new VBox(3);
        Label priceLabel = new Label("Precio *");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        TextField priceField = new TextField();
        priceField.setStyle("-fx-pref-width: 120px; -fx-pref-height: 30px;");
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

        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            String selectedType = typeComboBox.getValue();
            if (validateForm(selectedType, nameField.getText(), codeField.getText(),
                    descriptionField.getText(), priceField.getText(),
                    simplePriceField.getText(), doblePriceField.getText(), triplePriceField.getText(),
                    selectedImage[0], errorLabel)) {
                try {
                    byte[] imageData = Files.readAllBytes(selectedImage[0].toPath());

                    switch (selectedType) {
                        case "Burger":
                            saveBurger(nameField.getText(), codeField.getText(), imageData,
                                    Double.parseDouble(simplePriceField.getText()),
                                    Double.parseDouble(doblePriceField.getText()),
                                    Double.parseDouble(triplePriceField.getText()));
                            break;
                        case "Extra":
                            if (extraService.hasExtra()) {
                                DialogUtil.showWarning("Atención", "Ya existe un Extra. Solo puede haber uno.");
                                return;
                            }
                            saveExtra(nameField.getText(), descriptionField.getText(),
                                    Double.parseDouble(priceField.getText()), imageData);
                            break;
                        case "Combo":
                            saveCombo(nameField.getText(), descriptionField.getText(),
                                    Double.parseDouble(priceField.getText()), imageData);
                            break;
                    }

                    DialogUtil.showInfo("Éxito", "Producto agregado correctamente");
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

        Scene scene = new Scene(mainLayout, 350, 600);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void updateFormForType(String type, VBox codeBox, VBox descriptionBox, VBox priceBox, VBox burgerPricesBox) {
        switch (type) {
            case "Burger":
                codeBox.setVisible(true);
                codeBox.setManaged(true);
                descriptionBox.setVisible(false);
                descriptionBox.setManaged(false);
                priceBox.setVisible(false);
                priceBox.setManaged(false);
                burgerPricesBox.setVisible(true);
                burgerPricesBox.setManaged(true);
                break;

            case "Extra":
                codeBox.setVisible(false);
                codeBox.setManaged(false);
                descriptionBox.setVisible(true);
                descriptionBox.setManaged(true);
                priceBox.setVisible(true);
                priceBox.setManaged(true);
                burgerPricesBox.setVisible(false);
                burgerPricesBox.setManaged(false);
                break;

            case "Combo":
                codeBox.setVisible(false);
                codeBox.setManaged(false);
                descriptionBox.setVisible(true);
                descriptionBox.setManaged(true);
                priceBox.setVisible(true);
                priceBox.setManaged(true);
                burgerPricesBox.setVisible(false);
                burgerPricesBox.setManaged(false);
                break;
        }
    }

    private boolean validateForm(String type, String name, String code, String description,
                                 String price, String simplePrice, String doblePrice, String triplePrice,
                                 File image, Label errorLabel) {
        if (name == null || name.trim().isEmpty()) {
            errorLabel.setText("El nombre es obligatorio");
            errorLabel.setVisible(true);
            return false;
        }

        if (image == null) {
            errorLabel.setText("Debe seleccionar una imagen");
            errorLabel.setVisible(true);
            return false;
        }

        switch (type) {
            case "Burger":
                if (code == null || code.trim().length() != 2) {
                    errorLabel.setText("El código debe tener exactamente 2 caracteres");
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
                if (burgerService.nameExists(name.trim())) {
                    errorLabel.setText("Ya existe una burger con ese nombre");
                    errorLabel.setVisible(true);
                    return false;
                }
                if (burgerService.codeExists(code.trim())) {
                    errorLabel.setText("Ya existe una burger con ese código");
                    errorLabel.setVisible(true);
                    return false;
                }
                break;

            case "Extra":
            case "Combo":
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

    private void saveBurger(String name, String code, byte[] imageData, double simplePrice, double doblePrice, double triplePrice) {
        Burger burger = Burger.builder()
                .name(name.trim())
                .code(code.trim().toUpperCase())
                .imageData(imageData)
                .createdByUser(Session.getInstance().getCurrentUser().getId())
                .build();
        burgerService.saveBurgerWithVariants(burger, simplePrice, doblePrice, triplePrice);
    }

    private void saveExtra(String name, String description, double price, byte[] imageData) {
        Extra extra = Extra.builder()
                .name(name.trim())
                .description(description.trim())
                .price(price)
                .imageData(imageData)
                .createdByUser(Session.getInstance().getCurrentUser().getId())
                .build();
        extraService.saveExtra(extra);
    }

    private void saveCombo(String name, String description, double price, byte[] imageData) {
        Combo combo = Combo.builder()
                .name(name.trim())
                .description(description.trim())
                .price(price)
                .imageData(imageData)
                .createdByUser(Session.getInstance().getCurrentUser().getId())
                .build();
        comboService.saveCombo(combo);
    }
}