package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ArrivalsDialogController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    @FXML private TableView<Flight> arrivalsTable;

    public void init(Airports airport, List<Flight> arrivals) {
        if (airport != null && titleLabel != null) {
            String city = airport.getCity() != null ? airport.getCity() : "";
            titleLabel.setText(
                    safe(airport.getIcaoCode()) + " – " +
                            safe(airport.getName()) +
                            (city.isBlank() ? "" : " (" + city + ")")
            );
        }

        if (subtitleLabel != null) {
            subtitleLabel.setText("Mai érkező járatok áttekintése – csak UI, backend később.");
        }

        // Később itt lehet majd ténylegesen feltölteni a táblát:
        // if (arrivalsTable != null && arrivals != null) {
        //     arrivalsTable.getItems().setAll(arrivals);
        // }
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    @FXML
    private void onClose() {
        if (titleLabel != null && titleLabel.getScene() != null) {
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.close();
        }
    }
}