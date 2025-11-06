package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.service.AuthService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainPageController {

    private final AuthService authService;

    public MainPageController(AuthService authService) {
        this.authService = authService;
    }

    @FXML Label statusLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    @FXML
    private void goToRegister(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegistrationPage.fxml"));
        // FONTOS: Spring példányosítja a controllert
        loader.setControllerFactory(SpringContext::getBean);
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Regisztráció");
        stage.show();
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // háttérszálon futtatni érdemes, mint a regisztrációt
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return authService.login(username, password);
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                statusLabel.setText("Sikeres bejelentkezés!");
                // itt lehet átmenni másik ablakra
            } else {
                statusLabel.setText("Hibás felhasználónév vagy jelszó");
            }
        });

        new Thread(task).start();
    }
}
