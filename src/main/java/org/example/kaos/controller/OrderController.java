package org.example.kaos.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import org.example.kaos.entity.*;
import org.example.kaos.service.IBurgerService;
import org.example.kaos.service.IVariantService;
import org.example.kaos.service.IExtraItemService;
import org.example.kaos.service.implementation.BurgerServiceImpl;
import org.example.kaos.service.implementation.VariantServiceImpl;
import org.example.kaos.service.implementation.ExtraItemServiceImpl;
import org.example.kaos.util.DialogUtil;
import org.example.kaos.util.ProductDialog;
import org.example.kaos.util.Session;
import org.example.kaos.entity.OrderDetail;
import org.example.kaos.util.WindowManager;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OrderController implements Initializable {

    @FXML private FlowPane productsFlowPane;
    @FXML private VBox selectedProductPanel;
    @FXML private ImageView selectedProductImage;
    @FXML private Label selectedProductName;
    @FXML private Label quantityLabel;
    @FXML private VBox orderItemsContainer;
    @FXML private Label totalAmount;
    @FXML private Label totalLabel;
    @FXML private VBox totalsContainer;
    @FXML private Button editProductsBtn;
    @FXML private Button toppingsBtn;

    private static ContextMenu currentContextMenu;

    private final IBurgerService burgerService = new BurgerServiceImpl();
    private final IExtraItemService extraItemService = new ExtraItemServiceImpl();
    private final IVariantService variantService = new VariantServiceImpl();

    private ExtraItem singleExtra;
    private List<ExtraItem> combos;
    private int quantity = 1;
    private boolean isEditMode = false;
    private final List<OrderDetail> currentOrderDetails = new ArrayList<>();
    private Burger currentSelectedBurger;
    private Long currentSelectedVariantId;
    private String currentSelectedVariantName;
    private ExtraItem currentSelectedExtra;
    private ExtraItem currentSelectedCombo;
    private boolean isAdmin = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        quantityLabel.setText(String.valueOf(quantity));
        isAdmin = Session.getInstance().getCurrentUser().getId() == 1;
        configureButtonsByUser();
        loadAllProductsFromDatabase();

        // Add keyboard support for space key to trigger addToOrder and enter key to trigger viewOrder
        Platform.runLater(() -> {
            Scene scene = productsFlowPane.getScene();
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.SPACE && selectedProductPanel.isVisible()) {
                        addToOrder();
                        event.consume();
                    } else if (event.getCode() == KeyCode.ENTER) {
                        viewOrder();
                        event.consume();
                    }
                });
            }
        });
    }

    private void updateOrderSummary() {
        orderItemsContainer.getChildren().clear();
        double subtotal = 0;
        for (OrderDetail detail : currentOrderDetails) {
            HBox itemBox = new HBox(10);
            itemBox.setAlignment(Pos.CENTER_LEFT);
            String displayName = detail.getProductName();
            if (detail.getVariantName() != null && !detail.getVariantName().isEmpty()) {
                displayName += " (" + detail.getVariantName() + ")";
            }
            Label nameLabel = new Label(displayName);
            nameLabel.setStyle("-fx-font-size: 12px;");
            Label qtyLabel = new Label("x" + detail.getQuantity());
            qtyLabel.setStyle("-fx-font-size: 12px;");
            Label priceLabel = new Label("$" + String.valueOf(detail.getSubtotal().intValue()));
            priceLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            itemBox.getChildren().addAll(nameLabel, spacer, qtyLabel, priceLabel);
            orderItemsContainer.getChildren().add(itemBox);

            // Add toppings if any
            if (detail.getOrderDetailToppings() != null && !detail.getOrderDetailToppings().isEmpty()) {
                for (OrderDetailTopping topping : detail.getOrderDetailToppings()) {
                    if (topping.getQuantity() > 0) {
                        HBox toppingBox = new HBox(10);
                        toppingBox.setAlignment(Pos.CENTER_LEFT);
                        toppingBox.setPadding(new Insets(0, 0, 0, 20)); // Indent by 20 pixels

                        String toppingDisplay = "+ " + topping.getTopping().getName() + " x" + topping.getQuantity();
                        Label toppingNameLabel = new Label(toppingDisplay);
                        toppingNameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                        Label toppingPriceLabel = new Label("$" + String.valueOf(topping.getTotalPrice().intValue()));
                        toppingPriceLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #666;");
                        Region toppingSpacer = new Region();
                        HBox.setHgrow(toppingSpacer, Priority.ALWAYS);
                        toppingBox.getChildren().addAll(toppingNameLabel, toppingSpacer, toppingPriceLabel);
                        orderItemsContainer.getChildren().add(toppingBox);
                    }
                }
            }

            subtotal += detail.getSubtotal();
        }
        totalsContainer.setVisible(!currentOrderDetails.isEmpty());
        if (!currentOrderDetails.isEmpty()) {
            totalLabel.setText("$" + String.valueOf((int)subtotal));
        }
    }

    private void configureButtonsByUser() {
        editProductsBtn.setVisible(isAdmin);
        toppingsBtn.setVisible(isAdmin);

        editProductsBtn.setManaged(isAdmin);
        toppingsBtn.setManaged(isAdmin);
    }

    private void loadAllProductsFromDatabase() {
        try {
            productsFlowPane.getChildren().clear();

            List<Burger> burgers = burgerService.getAllBurgers();
            for (Burger burger : burgers) {
                VBox burgerCard = createBurgerCard(burger);
                productsFlowPane.getChildren().add(burgerCard);
            }

            singleExtra = extraItemService.getSingleExtra();
            if (singleExtra != null) {
                VBox extraCard = createExtraItemCard(singleExtra, "EXTRA");
                productsFlowPane.getChildren().add(extraCard);
            }

            combos = extraItemService.getAllCombos();
            for (ExtraItem combo : combos) {
                VBox comboCard = createExtraItemCard(combo, "COMBO");
                productsFlowPane.getChildren().add(comboCard);
            }

            if (isAdmin && isEditMode) {
                showAddButton();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (isAdmin && isEditMode) {
                showAddButton();
            }
        }
    }

    private VBox createBurgerCard(Burger burger) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setUserData(burger);

        if (burger.getDescription() != null && !burger.getDescription().trim().isEmpty()) {
            Tooltip tooltip = new Tooltip(burger.getDescription());
            tooltip.setStyle("-fx-font-size: 12px; -fx-max-width: 300px; -fx-wrap-text: true;");
            Tooltip.install(card, tooltip);
        }

        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("image-container");
        imageContainer.setMaxSize(100, 100);

        ImageView imageView = createProductImageView(burger.getImageData());

        if (!isEditMode) {
            Button menuButton = createMenuButton(burger);
            StackPane.setAlignment(menuButton, Pos.TOP_RIGHT);
            StackPane.setMargin(menuButton, new Insets(5, 5, 0, 0));
            imageContainer.getChildren().addAll(imageView, menuButton);
        } else {
            if (isAdmin) {
                Button deleteButton = createDeleteButton(burger);
                StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);
                StackPane.setMargin(deleteButton, new Insets(5, 5, 0, 0));
                imageContainer.getChildren().addAll(imageView, deleteButton);
            } else {
                imageContainer.getChildren().add(imageView);
            }
        }

        Label nameLabel = new Label(burger.getName());
        nameLabel.getStyleClass().add("product-name");

        card.getChildren().addAll(imageContainer, nameLabel);
        card.setAlignment(Pos.CENTER);

        card.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                if (currentContextMenu != null) {
                    currentContextMenu.hide();
                }
                ContextMenu variantMenu = createVariantMenu(burger);
                variantMenu.show(card, event.getScreenX(), event.getScreenY());
                currentContextMenu = variantMenu;
            } else {
                if (isEditMode) {
                    editProduct(burger);
                } else {
                    openBurgerSelection(burger);
                }
            }
        });

        return card;
    }

    private Button createMenuButton(Burger burger) {
        Button menuButton = new Button("⋮");
        menuButton.getStyleClass().add("menu-button");
        menuButton.setOnAction(e -> {
            if (currentContextMenu != null) {
                currentContextMenu.hide();
            }
            ContextMenu variantMenu = createVariantMenu(burger);
            variantMenu.show(menuButton, Side.BOTTOM, 0, 0);
            currentContextMenu = variantMenu;
        });
        return menuButton;
    }

    private ContextMenu createVariantMenu(Burger burger) {
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

        return variantMenu;
    }

    private Button createDeleteButton(Object product) {
        Button deleteButton = new Button("X");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> deleteProduct(product));
        return deleteButton;
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

    private VBox createExtraItemCard(ExtraItem extraItem, String type) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setUserData(extraItem);

        if (extraItem.getDescription() != null && !extraItem.getDescription().trim().isEmpty()) {
            Tooltip tooltip = new Tooltip(extraItem.getDescription());
            tooltip.setStyle("-fx-font-size: 12px; -fx-max-width: 300px; -fx-wrap-text: true;");
            Tooltip.install(card, tooltip);
        }

        StackPane imageContainer = new StackPane();
        imageContainer.setMaxSize(100, 100);

        ImageView imageView = createProductImageView(extraItem.getImageData());

        if (isEditMode && "COMBO".equals(type)) {
            Button deleteButton = createDeleteButton(extraItem);
            StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);
            StackPane.setMargin(deleteButton, new Insets(5, 5, 0, 0));
            imageContainer.getChildren().addAll(imageView, deleteButton);
        } else {
            imageContainer.getChildren().add(imageView);
        }

        Label nameLabel = new Label(extraItem.getName());
        nameLabel.getStyleClass().add("product-name");

        Label priceLabel = new Label("$" + String.valueOf(extraItem.getPrice().intValue()).trim());
        priceLabel.getStyleClass().add("product-price");

        card.getChildren().addAll(imageContainer, nameLabel, priceLabel);
        card.setAlignment(Pos.CENTER);

        card.setOnMouseClicked(event -> {
            if (isEditMode) {
                editProduct(extraItem);
            } else {
                if ("EXTRA".equals(type)) {
                    selectExtra(extraItem);
                } else if ("COMBO".equals(type)) {
                    selectCombo(extraItem);
                }
            }
        });

        return card;
    }

    private void selectExtra(ExtraItem extra) {
        selectedProductPanel.setVisible(true);

        try {
            if (extra.getImageData() != null && extra.getImageData().length > 0) {
                Image image = new Image(new ByteArrayInputStream(extra.getImageData()));
                selectedProductImage.setImage(image);
            } else {
                loadDefaultImage(selectedProductImage);
            }
        } catch (Exception e) {
            loadDefaultImage(selectedProductImage);
        }

        selectedProductName.setText(extra.getName() + " - $" + String.valueOf(extra.getPrice().intValue()).trim());

        currentSelectedExtra = extra;
        currentSelectedBurger = null;
        currentSelectedCombo = null;

        quantity = 1;
        quantityLabel.setText(String.valueOf(quantity));
    }

    private void selectCombo(ExtraItem combo) {
        selectedProductPanel.setVisible(true);

        try {
            if (combo.getImageData() != null && combo.getImageData().length > 0) {
                Image image = new Image(new ByteArrayInputStream(combo.getImageData()));
                selectedProductImage.setImage(image);
            } else {
                loadDefaultImage(selectedProductImage);
            }
        } catch (Exception e) {
            loadDefaultImage(selectedProductImage);
        }

        selectedProductName.setText(combo.getName() + " - $" + String.valueOf(combo.getPrice().intValue()).trim());

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
        addButton.setOnMouseClicked(event -> openAddProductForm(false));
        productsFlowPane.getChildren().add(addButton);
    }

    private void openAddProductForm(boolean isEditMode) {
        ProductDialog dialog = new ProductDialog();
        dialog.setMode(isEditMode ? ProductDialog.ProductMode.EDIT : ProductDialog.ProductMode.ADD);
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
        OrderDetail orderDetail = null;

        if (currentSelectedBurger != null) {
            Double unitPrice = getBurgerVariantPrice(currentSelectedBurger.getId(), currentSelectedVariantId);
            if (unitPrice == null) unitPrice = 0.0;

            BurgerVariant variant = new BurgerVariant();
            variant.setId(currentSelectedVariantId);

            VariantType variantType = new VariantType();
            variantType.setId(currentSelectedVariantId);
            variantType.setName(currentSelectedVariantName);
            variant.setVariantType(variantType);

            variant.setPrice(unitPrice);
            variant.setIsAvailable(true);

            orderDetail = new OrderDetail();
            orderDetail.setBurgerVariant(variant);
            orderDetail.setProductName(currentSelectedBurger.getName());
            orderDetail.setVariantName(currentSelectedVariantName);
            orderDetail.setUnitPrice(unitPrice);
            orderDetail.setQuantity(quantity);
            orderDetail.calculateSubtotal();
            orderDetail.setObservations(null);
            orderDetail.setTotal(orderDetail.getSubtotal());

        } else if (currentSelectedExtra != null) {
            orderDetail = new OrderDetail();
            orderDetail.setExtraItem(currentSelectedExtra);
            orderDetail.setProductName(currentSelectedExtra.getName());
            orderDetail.setUnitPrice(currentSelectedExtra.getPrice());
            orderDetail.setQuantity(quantity);
            orderDetail.calculateSubtotal();
            orderDetail.setObservations(null);
            orderDetail.setTotal(orderDetail.getSubtotal());

        } else if (currentSelectedCombo != null) {
            orderDetail = new OrderDetail();
            orderDetail.setExtraItem(currentSelectedCombo);
            orderDetail.setProductName(currentSelectedCombo.getName());
            orderDetail.setUnitPrice(currentSelectedCombo.getPrice());
            orderDetail.setQuantity(quantity);
            orderDetail.calculateSubtotal();
            orderDetail.setObservations(null);
            orderDetail.setTotal(orderDetail.getSubtotal());
        }

        if (orderDetail != null) {
            currentOrderDetails.add(orderDetail);
            updateOrderSummary();
            System.out.println("Agregado al pedido: " + orderDetail.getProductName());
        }

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
    private void viewOrder() {
        if (currentOrderDetails.isEmpty()) {
            DialogUtil.showWarning("Carrito Vacío", "No hay productos en el pedido.");
            return;
        }

        WindowManager.openOrderDetailsWindow(currentOrderDetails, null, false);
        updateOrderSummary();
    }

    @FXML
    private void clearOrder() {
    }

    @FXML
    public void openEditProducts(ActionEvent actionEvent) {
        isEditMode = !isEditMode;

//        for (javafx.scene.Node node : productsFlowPane.getChildren()) {
//            if (node instanceof VBox) {
//                VBox productCard = (VBox) node;
//
//                if (productCard.getStyleClass().contains("add-button")) {
//                    productCard.setVisible(!isEditMode);
//                    continue;
//                }
//
//                if (isEditMode) {
//                    productCard.getStyleClass().add("edit-mode");
//                    productCard.setStyle("-fx-opacity: 0.8;");
//                } else {
//                    productCard.getStyleClass().remove("edit-mode");
//                    productCard.setStyle("-fx-opacity: 1.0;");
//                }
//            }
//        }

        if (isEditMode) {
            editProductsBtn.getStyleClass().add("edit-mode-active");
            editProductsBtn.setText("❌ Salir Edición");

            toppingsBtn.setVisible(false);
            toppingsBtn.setManaged(false);
        } else {
            editProductsBtn.getStyleClass().remove("edit-mode-active");
            editProductsBtn.setText("");

            toppingsBtn.setVisible(isAdmin);
            toppingsBtn.setManaged(isAdmin);
        }

        loadAllProductsFromDatabase();
        updateOrderSummary();
    }

    private void editProduct(Object product) {
        ProductDialog dialog = new ProductDialog();
        dialog.setMode(ProductDialog.ProductMode.EDIT);
        dialog.setProductToEdit(product);
        dialog.setOnSuccessCallback(this::loadAllProductsFromDatabase);
        dialog.show();
    }

    public void deleteProduct(Object product) {
        if (product == null) {
            System.out.println("Error: Producto nulo");
            return;
        }

        boolean confirm = DialogUtil.showConfirmation("Eliminar producto", "¿Estás seguro de que deseas eliminar el producto?");
        if (confirm) {
            try {
                if (product instanceof Burger) {
                    Burger burger = (Burger) product;
                    System.out.println("Eliminando burger: " + burger.getName() + " (ID: " + burger.getId() + ")");
                    confirm = burgerService.deleteBurgerById(burger.getId());
                    if (confirm) {
                        DialogUtil.showInfo("Éxito", "Burger eliminada correctamente");
                    } else {
                        DialogUtil.showError("Error", "Error al borrar la burger.");
                    }

                } else if (product instanceof ExtraItem) {
                    ExtraItem extraItem = (ExtraItem) product;
                    System.out.println("Eliminando extra/combo: " + extraItem.getName() + " (ID: " + extraItem.getId() + ")");
                    boolean deleted = extraItemService.deleteExtraItem(extraItem.getId());
                    if (deleted) {
                        DialogUtil.showInfo("Éxito", "Producto eliminado correctamente");
                    } else {
                        DialogUtil.showError("Error", "Error al eliminar el producto.");
                    }

                } else {
                    System.out.println("Error: Tipo de producto no reconocido: " + product.getClass().getSimpleName());
                    return;
                }

                loadAllProductsFromDatabase();

            } catch (Exception e) {
                System.err.println("Error al eliminar producto: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Double getBurgerVariantPrice(Long burgerId, Long variantId) {
        try {
            List<BurgerVariant> variants = burgerService.getVariantsByBurgerId(burgerId.intValue());

            for (BurgerVariant variant : variants) {
                if (variant.getVariantType() != null &&
                        variant.getVariantType().getId().equals(variantId)) {
                    return variant.getPrice();
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener precio: " + e.getMessage());
        }
        return 0.0;
    }

    private void openBurgerSelection(Burger burger) {
        BurgerSelectionController controller = WindowManager.openWindow("/fxml/burger-selection.fxml", "Personalizar Burger - Kaos Burgers", burger);

        if (controller != null && controller.isConfirmed()) {
            OrderDetail orderDetail = controller.getResultOrderDetail();
            List<OrderDetailTopping> toppings = controller.getSelectedToppings();

            currentOrderDetails.add(orderDetail);
            updateOrderSummary();

            if (toppings != null && !toppings.isEmpty()) {
                System.out.println("Toppings agregados: " + toppings.size());
            }
        }
    }

    public void openToppingsManager(ActionEvent actionEvent) {
        try {
            WindowManager.openWindow("/fxml/toppings-manager.fxml", "Gestión de Toppings - Kaos Burgers", null);
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Error", "No se pudo abrir el gestor de toppings.");
        }
    }
}