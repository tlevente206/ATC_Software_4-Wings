package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.User;
import com.FourWings.atcSystem.model.UserService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainPageController {

    private final UserService userService;
    public MainPageController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    private TextField emailInput;

    @FXML
    private TextField nevInput;

    @FXML
    private PasswordField passInput;

    @FXML
    private TextField phoneInput;

    @FXML
    private Button registerButton;

    @FXML
    private TextField userInput;

    String email;
    String password;
    String phone;
    String user;
    String name;

    public void register(ActionEvent event) {
        if (userInput.getText().isBlank() || passInput.getText().isBlank()) {
            System.out.println("Felhasználónév és jelszó kötelező.");
            return;
        }
        User u = User.builder()
                .name(nevInput.getText().trim())
                .username(userInput.getText().trim())
                .email(emailInput.getText().trim())
                .password(passInput.getText())          // (élesben: hash-eld!)
                .phone(phoneInput.getText().trim())
                .IsAdmin(false)
                .build();


        new Thread(() -> {
            try {
                userService.register(u);
                Platform.runLater(() -> System.out.println("Sikeres regisztráció"));
            } catch (Exception ex) {
                Platform.runLater(() -> System.out.println("Hiba: " + ex.getMessage()));
            }
        }, "save-user").start();
    }

}
