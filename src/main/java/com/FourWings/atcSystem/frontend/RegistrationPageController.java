package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.regex.Pattern;

@Component
public class RegistrationPageController {

    private final UserService userService;

    public RegistrationPageController(UserService userService) {
        this.userService = userService;
    }

    @FXML private TextField emailInput;
    @FXML private TextField nevInput;
    @FXML private PasswordField passInput;
    @FXML private TextField phoneInput;
    @FXML private Button registerButton;
    @FXML private TextField userInput;
    @FXML private Label registerLabel;
    @FXML private Button backToMainPageButton;

    @FXML private ImageView profileImageView;
    @FXML private Label imageErrorLabel;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,20}$");

    // itt tároljuk a kiválasztott kép byte-jait
    private byte[] selectedProfileImage;

    @FXML
    private void onSelectProfileImage(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Profilkép kiválasztása");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Képfájlok", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fc.showOpenDialog(profileImageView.getScene().getWindow());
        if (file != null) {
            try {
                selectedProfileImage = java.nio.file.Files.readAllBytes(file.toPath());

                // előnézet
                Image img = new Image(file.toURI().toString());
                profileImageView.setImage(img);

                if (imageErrorLabel != null) {
                    imageErrorLabel.setText("");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (imageErrorLabel != null) {
                    imageErrorLabel.setText("Nem sikerült beolvasni a képet.");
                }
            }
        }
    }

    private void clearForm() {
        nevInput.clear();
        userInput.clear();
        emailInput.clear();
        passInput.clear();
        phoneInput.clear();
        profileImageView.setImage(null);
        selectedProfileImage = null;
        if (imageErrorLabel != null) imageErrorLabel.setText("");
    }

    @FXML
    private void backToMainPage(ActionEvent event) {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }

    @FXML
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
        if (selectedProfileImage == null) {
            if (imageErrorLabel != null) {
                imageErrorLabel.setText("Profilkép megadása kötelező.");
            }
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                boolean available = userService.isUsernameAvailable(username);
                if (!available) {
                    throw new IllegalStateException("A felhasználónév már foglalt.");
                }

                User u = User.builder()
                        .name(nevInput.getText().trim())
                        .username(username)
                        .email(emailInput.getText().trim())
                        .password(password)
                        .phone(phoneInput.getText().trim())
                        .admin(false)
                        .profileImage(selectedProfileImage)
                        .build();

                userService.registerSelf(u);
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