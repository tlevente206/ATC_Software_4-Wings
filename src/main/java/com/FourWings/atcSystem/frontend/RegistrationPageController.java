package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.config.SpringContext;
import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.InputStream;
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

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9._-]{3,20}$");

    @FXML private ImageView profileImageView;
    @FXML private Label imageErrorLabel;

    // itt tároljuk a kiválasztott avatar elérési útját
    // pl.: "/images/avatars/avatar3.png"
    private String selectedProfileImagePath;

    @FXML
    private void onSelectProfileImage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/AvatarPickerDialog.fxml"));
            loader.setControllerFactory(SpringContext::getBean);
            Parent root = loader.load();

            AvatarPickerDialogController ctrl = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.initOwner(profileImageView.getScene().getWindow());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Profilkép választása");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            String chosenPath = ctrl.getSelectedImagePath();
            if (chosenPath != null) {
                selectedProfileImagePath = chosenPath;

                // előnézet beállítása
                try (InputStream is = getClass().getResourceAsStream(chosenPath)) {
                    if (is != null) {
                        Image img = new Image(is);
                        profileImageView.setImage(img);
                    } else {
                        profileImageView.setImage(null);
                        if (imageErrorLabel != null) {
                            imageErrorLabel.setText("Nem található a kiválasztott avatar képe.");
                        }
                    }
                }

                if (imageErrorLabel != null) {
                    imageErrorLabel.setText("");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            if (imageErrorLabel != null) {
                imageErrorLabel.setText("Nem sikerült megnyitni az avatar választót: " + ex.getMessage());
            }
        }
    }

    private void clearForm() {
        nevInput.clear();
        userInput.clear();
        emailInput.clear();
        passInput.clear();
        phoneInput.clear();
        selectedProfileImagePath = null;
        if (profileImageView != null) {
            profileImageView.setImage(null);
        }
    }

    @FXML
    private void backToMainPage(ActionEvent event) throws Exception {
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

        // Profilkép kötelező
        if (selectedProfileImagePath == null) {
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
                        .profileImage(selectedProfileImagePath)  // ⬅ itt már String megy az adatbázisba
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