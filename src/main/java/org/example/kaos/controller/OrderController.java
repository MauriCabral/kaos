package org.example.kaos.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.kaos.entity.*;
import org.example.kaos.service.IBurgerService;
import org.example.kaos.service.IVariantService;
import org.example.kaos.service.IComboService;
import org.example.kaos.service.IExtraService;
import org.example.kaos.service.implementation.BurgerServiceImpl;
import org.example.kaos.service.implementation.VariantServiceImpl;
import org.example.kaos.service.implementation.ComboServiceImpl;
import org.example.kaos.service.implementation.ExtraServiceImpl;
import org.example.kaos.util.AddProductDialog;
import org.example.kaos.util.Session;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class OrderController implements Initializable {

    @FXML private FlowPane productsFlowPane;
    @FXML private VBox selectedProductPanel;
    @FXML private ImageView selectedProductImage;
    @FXML private Label selectedProductName;
    @FXML private Label quantityLabel;
    @FXML private ListView<String> orderItemsList;
    @FXML private Label totalAmount;

    private final IBurgerService burgerService = new BurgerServiceImpl();
    private final IExtraService extraService = new ExtraServiceImpl();
    private final IComboService comboService = new ComboServiceImpl();
    private final IVariantService variantService = new VariantServiceImpl();

    private Extra singleExtra;
    private List<Combo> combos;
    private int quantity = 1;

    private Burger currentSelectedBurger;
    private Long currentSelectedVariantId;
    private String currentSelectedVariantName;
    private Extra currentSelectedExtra;
    private Combo currentSelectedCombo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        quantityLabel.setText(String.valueOf(quantity));
        loadAllProductsFromDatabase();
    }

    private void loadAllProductsFromDatabase() {
        try {
            productsFlowPane.getChildren().clear();

            // 1. CARGAR BURGERS
            List<Burger> burgers = burgerService.getAllBurgers();
            for (Burger burger : burgers) {
                VBox burgerCard = createBurgerCard(burger);
                productsFlowPane.getChildren().add(burgerCard);
            }

            // 2. CARGAR EL ÚNICO EXTRA (PAPAS)
            singleExtra = extraService.getSingleExtra();
            if (singleExtra != null) {
                VBox extraCard = createSimpleProductCard(singleExtra, "EXTRA");
                productsFlowPane.getChildren().add(extraCard);
            }

            // 3. CARGAR COMBOS
            combos = comboService.getAllCombos();
            for (Combo combo : combos) {
                VBox comboCard = createSimpleProductCard(combo, "COMBO");
                productsFlowPane.getChildren().add(comboCard);
            }

            if (Session.getInstance().getCurrentUser().getId() == 1) {
                showAddButton();
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (Session.getInstance().getCurrentUser().getId() == 1) {
                showAddButton();
            }
        }
    }

    private VBox createBurgerCard(Burger burger) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");

        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("image-container");
        imageContainer.setMaxSize(100, 100);

        ImageView imageView = createProductImageView(burger.getImageData());

        Button menuButton = createMenuButton(burger);
        StackPane.setAlignment(menuButton, Pos.TOP_RIGHT);
        StackPane.setMargin(menuButton, new Insets(5, 5, 0, 0));

        imageContainer.getChildren().addAll(imageView, menuButton);

        Label nameLabel = new Label(burger.getName());
        nameLabel.getStyleClass().add("product-name");

        card.getChildren().addAll(imageContainer, nameLabel);
        card.setAlignment(Pos.CENTER);

        return card;
    }

    private Button createMenuButton(Burger burger) {
        Button menuButton = new Button("⋮");
        menuButton.getStyleClass().add("menu-button");
        menuButton.setOnAction(e -> showVariantMenu(menuButton, burger));
        menuButton.setMouseTransparent(false);
        return menuButton;
    }

    private void showVariantMenu(Button menuButton, Burger burger) {
        ContextMenu variantMenu = new ContextMenu();

        List<VariantType> variants = variantService.getAllVariants();

        for (VariantType variant : variants) {
            MenuItem menuItem = new MenuItem(variant.getName());

            final Long variantId = variant.getId();
            final String variantName = variant.getName();

            menuItem.setOnAction(e -> {
                selectBurgerVariant(burger, variantId, variantName);
            });

            variantMenu.getItems().add(menuItem);
        }

        variantMenu.show(menuButton, Side.BOTTOM, 0, 0);
    }

    private void selectBurgerVariant(Burger burger, Long variantId, String variantName) {
        selectedProductPanel.setVisible(true);

        try {
            if (burger.getImageData() != null && burger.getImageData().length > 0) {
                Image image = new Image(new ByteArrayInputStream(burger.getImageData()));
                selectedProductImage.setImage(image);
            } else {
                loadDefaultImage(selectedProductImage);
            }
        } catch (Exception e) {
            loadDefaultImage(selectedProductImage);
        }

        selectedProductName.setText(burger.getName() + " (" + variantName + ")");

        currentSelectedBurger = burger;
        currentSelectedVariantId = variantId;
        currentSelectedVariantName = variantName;
        currentSelectedExtra = null;
        currentSelectedCombo = null;

        quantity = 1;
        quantityLabel.setText(String.valueOf(quantity));
    }

    private ImageView createProductImageView(byte[] imageData) {
        ImageView imageView = new ImageView();
        try {
            if (imageData != null && imageData.length > 0) {
                Image image = new Image(new ByteArrayInputStream(imageData));
                imageView.setImage(image);
            } else {
                loadDefaultImage(imageView);
            }
        } catch (Exception e) {
            loadDefaultImage(imageView);
        }

        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("product-image");

        return imageView;
    }

    private VBox createSimpleProductCard(Object product, String type) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");

        String name = "";
        byte[] imageData = null;
        double price = 0.0;

        if (type.equals("EXTRA")) {
            Extra extra = (Extra) product;
            name = extra.getName();
            imageData = extra.getImageData();
            price = extra.getPrice();
        } else if (type.equals("COMBO")) {
            Combo combo = (Combo) product;
            name = combo.getName();
            imageData = combo.getImageData();
            price = combo.getPrice();
        }

        ImageView imageView = createProductImageView(imageData);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("product-name");

        Label priceLabel = new Label("$" + price);
        priceLabel.getStyleClass().add("product-price");

        card.getChildren().addAll(imageView, nameLabel, priceLabel);
        card.setAlignment(Pos.CENTER);

        /*card.setOnMouseClicked(event -> {
            if (type.equals("EXTRA")) {
                selectExtra((Extra) product);
            } else if (type.equals("COMBO")) {
                selectCombo((Combo) product);
            }
        });*/

        return card;
    }

    /*private void selectExtra(Extra extra) {
        selectedProductPanel.setVisible(true);
        loadDefaultImage(selectedProductImage);
        selectedProductName.setText(extra.getName() + " - $" + extra.getPrice());

        currentSelectedExtra = extra;
        currentSelectedBurger = null;
        currentSelectedCombo = null;

        quantity = 1;
        quantityLabel.setText(String.valueOf(quantity));
    }*/

    private void selectCombo(Combo combo) {
        selectedProductPanel.setVisible(true);
        loadDefaultImage(selectedProductImage);
        selectedProductName.setText(combo.getName() + " - $" + combo.getPrice());

        currentSelectedCombo = combo;
        currentSelectedBurger = null;
        currentSelectedExtra = null;

        quantity = 1;
        quantityLabel.setText(String.valueOf(quantity));
    }

    private void loadDefaultImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/image/logo.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen por defecto");
        }
    }

    private void showAddButton() {
        VBox addButton = new VBox();
        addButton.getStyleClass().add("add-button");
        addButton.setAlignment(Pos.CENTER);

        Label plusLabel = new Label("+");
        plusLabel.getStyleClass().add("plus-label");

        addButton.getChildren().add(plusLabel);
        addButton.setOnMouseClicked(event -> openAddProductForm());

        productsFlowPane.getChildren().add(addButton);
    }

    private void openAddProductForm() {
        AddProductDialog dialog = new AddProductDialog();
        dialog.setOnSuccessCallback(this::loadAllProductsFromDatabase);
        dialog.show();
    }

    @FXML
    private void increaseQuantity() {
        quantity++;
        quantityLabel.setText(String.valueOf(quantity));
    }

    @FXML
    private void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            quantityLabel.setText(String.valueOf(quantity));
        }
    }

    @FXML
    private void addToOrder() {
        if (currentSelectedBurger != null) {
            System.out.println("Agregar burger: " + currentSelectedBurger.getName() +
                    " (" + currentSelectedVariantName + ")");
        } else if (currentSelectedExtra != null) {
            System.out.println("Agregar extra: " + currentSelectedExtra.getName());
        } else if (currentSelectedCombo != null) {
            System.out.println("Agregar combo: " + currentSelectedCombo.getName());
        }

        // Limpiar selección
        clearSelection();
    }

    private void clearSelection() {
        currentSelectedBurger = null;
        currentSelectedVariantId = 0L;
        currentSelectedVariantName = null;
        currentSelectedExtra = null;
        currentSelectedCombo = null;
        selectedProductPanel.setVisible(false);
    }

    @FXML
    private void confirmOrder() {
        // Lógica para confirmar pedido
    }

    @FXML
    private void clearOrder() {
        // Lógica para limpiar pedido
    }
}