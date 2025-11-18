package com.FourWings.atcSystem.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UserAdminPageController {
    @FXML
    private Button addUserButton;

    @FXML
    private TableColumn<?, ?> adminColumn;

    @FXML
    private Button deleteUserButton;

    @FXML
    private Button editUserButton;

    @FXML
    private TableColumn<?, ?> emailColumn;

    @FXML
    private TableColumn<?, ?> idColumn;

    @FXML
    private TableColumn<?, ?> nameColumn;

    @FXML
    private TableColumn<?, ?> phoneColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private TableColumn<?, ?> usernameColumn;

    @FXML
    private TableView<?> usersTable;
}
