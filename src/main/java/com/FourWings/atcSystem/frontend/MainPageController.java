package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.User;
import com.FourWings.atcSystem.model.UserService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class MainPageController {

    private final UserService userService;
    public MainPageController(UserService userService) {
        this.userService = userService;
    }

    @FXML private TextField emailInput;
    @FXML private TextField nevInput;
    @FXML private PasswordField passInput;
    @FXML private TextField phoneInput;
    @FXML private Button registerButton;
    @FXML private TextField userInput;
    @FXML private Label registerLabel;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,20}$");

    private void clearForm() {
        nevInput.clear();
        userInput.clear();
        emailInput.clear();
        passInput.clear();
        phoneInput.clear();
    }

    public void register(ActionEvent event) {
        String username = userInput.getText().trim();
        String password = passInput.getText();

        if (username.isEmpty() || password.isEmpty()) {
            registerLabel.setText("Kitöltés kötelező: felhasználónév és jelszó.");
            return;
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            registerLabel.setText("Felhasználónév: 3–20 karakter, csak betűk, számok, ._- engedélyezett.");
            return;
        }
        if (password.length() < 8) {
            registerLabel.setText("Jelszó túl rövid (min. 8 karakter).");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // DB ellenőrzés
                boolean available = userService.isUsernameAvailable(username);
                if (!available) {
                    throw new IllegalStateException("A felhasználónév már foglalt.");
                }

                User u = User.builder()
                        .name(nevInput.getText().trim())
                        .username(username)
                        .email(emailInput.getText().trim())
                        .password(password)   // a service fogja hash-elni
                        .phone(phoneInput.getText().trim())
                        .IsAdmin(false)
                        .build();

                userService.register(u); // mentés + jelszó-hash a service-ben
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            registerLabel.setText("Sikeres regisztráció.");
            clearForm();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            registerLabel.setText("Hiba: " + (ex != null ? ex.getMessage() : "ismeretlen"));
        });

        new Thread(task, "check-and-save-user").start();

    }

}
