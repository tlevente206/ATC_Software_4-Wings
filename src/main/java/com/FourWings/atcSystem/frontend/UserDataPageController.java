package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

@Component
public class UserDataPageController {
    @FXML
    private Label emailLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label phoneLabel;

    private User loggedUser;

    public void initWithUser(User user) {
        this.loggedUser = user;

        nameLabel.setText(loggedUser.getName());
        phoneLabel.setText(loggedUser.getPhone());
        emailLabel.setText(loggedUser.getEmail());
    }
}
