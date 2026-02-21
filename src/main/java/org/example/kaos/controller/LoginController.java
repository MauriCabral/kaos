package org.example.kaos.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.example.kaos.entity.User;
import org.example.kaos.service.IUserService;
import org.example.kaos.service.implementation.UserServiceImpl;
import org.example.kaos.util.DialogUtil;
import org.example.kaos.util.Session;
import org.example.kaos.util.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ToggleButton togglePasswordButton;
    @FXML private Label versionLabel;

    private final IUserService userService = new UserServiceImpl();
    private final String version = "v1.0.26";

    @FXML
    public void initialize() {
        logger.info("Inicializando LoginController. Versión: {}", version);
        usernameField.setText("Despeñaderos");
        passwordField.setText("despe123");

//        usernameField.setText("SanAgustin");
//        passwordField.setText("sanagustin123");

        setupPasswordFields();
        versionLabel.setText(version);

        Platform.runLater(() -> {
            Scene scene = usernameField.getScene();
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        logger.debug("Tecla ENTER presionada. Iniciando login...");
                        handleLogin(null);
                        event.consume();
                    }
                });
            } else {
                logger.warn("No se pudo obtener la escena del campo de usuario");
            }
        });
        logger.debug("LoginController inicializado correctamente");
    }

    private void setupPasswordFields() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        togglePasswordButton.setText("○");
    }

    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        logger.info("Intento de login para usuario: {}", username);

        if (username.isEmpty() || password.isEmpty()) {
            logger.warn("Login fallido - Campos requeridos vacíos para usuario: {}", username);
            DialogUtil.showError("Campos requeridos", "Por favor, ingrese usuario y contraseña");
            return;
        }

        try {
            User user = userService.login(username, password);

            if (user == null) {
                logger.warn("Login fallido - Credenciales incorrectas para usuario: {}", username);
                DialogUtil.showError("Error de login", "Usuario o contraseña incorrectos");
            } else {
                logger.info("Login exitoso para usuario: {} (ID: {})", username, user.getId());
                Session.getInstance().setCurrentUser(user);
                logger.debug("Abriendo ventana principal...");
                WindowManager.openWindow("/fxml/main.fxml", "Menú", null);
                ((Stage) usernameField.getScene().getWindow()).close();
            }
        } catch (Exception e) {
            logger.error("Error durante el login para usuario: {}", username, e);
            DialogUtil.showError("Error de login", "Ocurrió un error durante el login: " + e.getMessage());
        }
    }

    @FXML
    public void togglePasswordVisibility(ActionEvent actionEvent) {
        if (togglePasswordButton.isSelected()) {
            // Mostrar contraseña
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            togglePasswordButton.setText("●");
        } else {
            // Ocultar contraseña
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordButton.setText("○");
        }
    }
}