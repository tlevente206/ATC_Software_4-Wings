package com.FourWings.atcSystem.frontend;

import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

@Component
public class UserAdminPageController {

    private final UserService userService;

    public UserAdminPageController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    private TableView<User> usersTable;   // <-- FXML-ben is usersTable

    @FXML
    private TableColumn<User, Long> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> phoneColumn;

    @FXML
    private TableColumn<User, Boolean> adminColumn;  // <-- FXML-ben adminColumn

    private final ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Oszlop bindingok
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        adminColumn.setCellValueFactory(new PropertyValueFactory<>("admin"));

        // Adatok betöltése
        users.setAll(userService.getAllUsers());
        usersTable.setItems(users);

        // DEBUG: nézzük meg, hány user jön vissza
        System.out.println("Betöltött userek száma: " + users.size());

        // Dupla kattintás sorra
        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    User selected = row.getItem();
                    System.out.println("Kiválasztott user: " + selected.getUsername());
                    // Itt majd: openUserEditDialog(selected);
                }
            });
            return row;
        });
    }
}