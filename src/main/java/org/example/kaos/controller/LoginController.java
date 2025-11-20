package org.example.kaos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import org.example.kaos.entity.User;
import org.example.kaos.service.IUserService;
import org.example.kaos.service.implementation.UserServiceImpl;
import org.example.kaos.util.DialogUtil;
import org.example.kaos.util.Session;
import org.example.kaos.util.WindowManager;

public class LoginController {

    private final IUserService IUserService = new UserServiceImpl();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ToggleButton togglePasswordButton;

    @FXML
    public void initialize() {
        setupPasswordFields();
    }

    private void setupPasswordFields() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        togglePasswordButton.setText("○");
    }

    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Campos vacíos");
            return;
        }

        User user = IUserService.login(username, password);

        if (user == null) {
            DialogUtil.showError("Error de login", "Usuario o contraseña incorrectos");
        } else {
            Session.getInstance().setCurrentUser(user);
            WindowManager.openWindow("/fxml/main.fxml", "Menú");
            ((Stage) usernameField.getScene().getWindow()).close();
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

    public String getPassword() {
        return passwordField.getText();
    }
}