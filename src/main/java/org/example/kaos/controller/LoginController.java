package org.example.kaos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.kaos.entity.User;
import org.example.kaos.service.UserService;
import org.example.kaos.service.implementation.UserServiceImpl;
import org.example.kaos.util.DialogUtil;
import org.example.kaos.util.Session;
import org.example.kaos.util.WindowManager;

public class LoginController {

    private final UserService userService = new UserServiceImpl();

    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Campos vacíos");
            return;
        }

        User user = userService.login(username, password);

        if (user == null) {
            DialogUtil.showError("Error de login", "Usuario o contraseña incorrectos");
        } else {
            Session.getInstance().setCurrentUser(user);
            DialogUtil.showInfo("Login exitoso", "Bienvenido, " + user.getUsername() + "!");

            //WindowManager.openWindow("/fxml/main.fxml", "Panel Principal");
            // Cerrar login
            ((Stage) usernameField.getScene().getWindow()).close();
        }
    }
}
