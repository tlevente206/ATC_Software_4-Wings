package com.FourWings.atcSystem.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class AvatarPickerDialogController {

    // Avatár csempék (StackPane) + bennük az ImageView-ok
    @FXML private StackPane avatar1Pane;
    @FXML private StackPane avatar2Pane;
    @FXML private StackPane avatar3Pane;
    @FXML private StackPane avatar4Pane;
    @FXML private StackPane avatar5Pane;
    @FXML private StackPane avatar6Pane;
    @FXML private StackPane avatar7Pane;
    @FXML private StackPane avatar8Pane;
    @FXML private StackPane avatar9Pane;
    @FXML private StackPane avatar10Pane;

    @FXML private ImageView avatar1View;
    @FXML private ImageView avatar2View;
    @FXML private ImageView avatar3View;
    @FXML private ImageView avatar4View;
    @FXML private ImageView avatar5View;
    @FXML private ImageView avatar6View;
    @FXML private ImageView avatar7View;
    @FXML private ImageView avatar8View;
    @FXML private ImageView avatar9View;
    @FXML private ImageView avatar10View;

    @FXML private Label errorLabel;
    @FXML private Label selectedNameLabel;

    private StackPane selectedPane;      // melyik avatar csempe van kijelölve
    private String selectedImagePath;    // pl. "/images/avatars/avatar3.png"

    private static final String TILE_BASE_STYLE =
            "-fx-background-radius: 10; -fx-padding: 4;";

    private static final String SELECTED_BORDER_STYLE =
            "-fx-border-color: #0078ff; -fx-border-width: 2; -fx-border-radius: 10;";

    public String getSelectedImagePath() {
        return selectedImagePath;
    }

    @FXML
    public void initialize() {
        // Avatárok betöltése – ide tedd a képeket:
        // src/main/resources/images/avatars/avatar1.png ... avatar10.png
        loadAvatar(avatar1View, "/images/avatars/avatar1.png");
        loadAvatar(avatar2View, "/images/avatars/avatar2.png");
        loadAvatar(avatar3View, "/images/avatars/avatar3.png");
        loadAvatar(avatar4View, "/images/avatars/avatar4.png");
        loadAvatar(avatar5View, "/images/avatars/avatar5.png");
        loadAvatar(avatar6View, "/images/avatars/avatar6.png");
        loadAvatar(avatar7View, "/images/avatars/avatar7.png");
        loadAvatar(avatar8View, "/images/avatars/avatar8.png");
        loadAvatar(avatar9View, "/images/avatars/avatar9.png");
        loadAvatar(avatar10View, "/images/avatars/avatar10.png");

        // alap stílus a csempékre
        avatar1Pane.setStyle(TILE_BASE_STYLE);
        avatar2Pane.setStyle(TILE_BASE_STYLE);
        avatar3Pane.setStyle(TILE_BASE_STYLE);
        avatar4Pane.setStyle(TILE_BASE_STYLE);
        avatar5Pane.setStyle(TILE_BASE_STYLE);
        avatar6Pane.setStyle(TILE_BASE_STYLE);
        avatar7Pane.setStyle(TILE_BASE_STYLE);
        avatar8Pane.setStyle(TILE_BASE_STYLE);
        avatar9Pane.setStyle(TILE_BASE_STYLE);
        avatar10Pane.setStyle(TILE_BASE_STYLE);

        if (selectedNameLabel != null) {
            selectedNameLabel.setText("Nincs kiválasztva kép");
        }
    }

    private void loadAvatar(ImageView view, String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                Image img = new Image(is);
                view.setImage(img);
            } else {
                System.out.println("Nem találom: " + resourcePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================== KIJELÖLÉS STÍLUS =======================

    private void clearSelectionStyle() {
        StackPane[] panes = {
                avatar1Pane, avatar2Pane, avatar3Pane, avatar4Pane, avatar5Pane,
                avatar6Pane, avatar7Pane, avatar8Pane, avatar9Pane, avatar10Pane
        };
        for (StackPane p : panes) {
            if (p != null) {
                p.setStyle(TILE_BASE_STYLE);
            }
        }
    }

    private void highlightPane(StackPane pane) {
        if (pane == null) return;
        pane.setStyle(TILE_BASE_STYLE + " " + SELECTED_BORDER_STYLE);
    }

    // ===================== AVATÁR KIJELÖLÉS =======================

    private void selectPredefinedAvatar(StackPane pane, String resourcePath) {
        clearSelectionStyle();
        selectedPane = pane;
        highlightPane(pane);

        selectedImagePath = resourcePath;

        if (selectedNameLabel != null) {
            String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            selectedNameLabel.setText("Kiválasztott: " + fileName);
        }
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    @FXML private void onAvatar1Click(MouseEvent e) { selectPredefinedAvatar(avatar1Pane,  "/images/avatars/avatar1.png"); }
    @FXML private void onAvatar2Click(MouseEvent e) { selectPredefinedAvatar(avatar2Pane,  "/images/avatars/avatar2.png"); }
    @FXML private void onAvatar3Click(MouseEvent e) { selectPredefinedAvatar(avatar3Pane,  "/images/avatars/avatar3.png"); }
    @FXML private void onAvatar4Click(MouseEvent e) { selectPredefinedAvatar(avatar4Pane,  "/images/avatars/avatar4.png"); }
    @FXML private void onAvatar5Click(MouseEvent e) { selectPredefinedAvatar(avatar5Pane,  "/images/avatars/avatar5.png"); }
    @FXML private void onAvatar6Click(MouseEvent e) { selectPredefinedAvatar(avatar6Pane,  "/images/avatars/avatar6.png"); }
    @FXML private void onAvatar7Click(MouseEvent e) { selectPredefinedAvatar(avatar7Pane,  "/images/avatars/avatar7.png"); }
    @FXML private void onAvatar8Click(MouseEvent e) { selectPredefinedAvatar(avatar8Pane,  "/images/avatars/avatar8.png"); }
    @FXML private void onAvatar9Click(MouseEvent e) { selectPredefinedAvatar(avatar9Pane,  "/images/avatars/avatar9.png"); }
    @FXML private void onAvatar10Click(MouseEvent e){ selectPredefinedAvatar(avatar10Pane, "/images/avatars/avatar10.png"); }

    // ===================== OK / MÉGSE =============================

    @FXML
    private void onOk(ActionEvent event) {
        if (selectedImagePath == null) {
            if (errorLabel != null) {
                errorLabel.setText("Először válassz képet.");
            }
            return;
        }
        closeWindow(event);
    }

    @FXML
    private void onCancel(ActionEvent event) {
        selectedImagePath = null; // semmit nem választ
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Node src = (Node) event.getSource();
        Stage stage = (Stage) src.getScene().getWindow();
        stage.close();
    }
}