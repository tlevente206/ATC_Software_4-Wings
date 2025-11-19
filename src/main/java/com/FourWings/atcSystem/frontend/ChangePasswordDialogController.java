package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordDialogController {

    private final UserService userService;

    public ChangePasswordDialogController(UserService userService) {
        this.userService = userService;
    }

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField newPasswordAgainField;
    @FXML private Label errorLabel;

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (user == null) {
            errorLabel.setText("Nincs felhasználó.");
            return;
        }

        String oldPw = oldPasswordField.getText();
        String newPw = newPasswordField.getText();
        String newPw2 = newPasswordAgainField.getText();

        if (oldPw == null || oldPw.isBlank()
                || newPw == null || newPw.isBlank()
                || newPw2 == null || newPw2.isBlank()) {
            errorLabel.setText("Minden mező kitöltése kötelező.");
            return;
        }

        if (!newPw.equals(newPw2)) {
            errorLabel.setText("Az új jelszó kétszer nem egyezik.");
            return;
        }

        try {
            userService.changePassword(user.getId(), oldPw, newPw);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Jelszó módosítva");
            ok.setHeaderText("A jelszó sikeresen módosítva.");
            ok.setContentText("Mostantól az új jelszóval tudsz bejelentkezni.");
            ok.showAndWait();

            closeWindow();
        } catch (IllegalArgumentException ex) {
            // pl. régi jelszó nem jó, vagy új túl rövid
            errorLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            errorLabel.setText("Ismeretlen hiba történt.");
            ex.printStackTrace();
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) oldPasswordField.getScene().getWindow();
        stage.close();
    }
}