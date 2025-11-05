package com.FourWings.atcSystem.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class MainPageController {
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
        email = emailInput.getText();
        password = passInput.getText();
        phone = phoneInput.getText();
        user = userInput.getText();
        name = nevInput.getText();
        System.out.println(email);
        System.out.println(password);
        System.out.println(phone);
        System.out.println(user);
        System.out.println(name);
    }

}
