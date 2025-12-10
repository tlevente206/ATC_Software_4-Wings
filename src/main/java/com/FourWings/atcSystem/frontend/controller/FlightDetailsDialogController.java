package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.flight.Flight;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class FlightDetailsDialogController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    // régi
// @FXML private Pane mapPane;

    // új
    @FXML private javafx.scene.web.WebView mapWebView;

    private javafx.scene.web.WebEngine webEngine;
    private boolean mapLoaded = false;

    // Légitársaság
    @FXML private Label airlineNameLabel;
    @FXML private Label airlineCodeLabel;
    @FXML private Label airlineCountryLabel;

    // Gép
    @FXML private Label aircraftRegLabel;
    @FXML private Label aircraftTypeLabel;
    @FXML private Label aircraftSeatsLabel;
    @FXML private Label aircraftYearLabel;
    @FXML private Label aircraftStatusLabel;

    // Térkép
    @FXML private Label routeLabel;

    private Flight flight;
    // útvonalhoz használt normalizált koordináták (double-ben)
    private double depLat;
    private double depLon;
    private double arrLat;
    private double arrLon;

    public void init(Flight flight) {
        this.flight = flight;
        if (flight == null) return;

        if (titleLabel != null) {
            titleLabel.setText("Járat részletei – " + flight.getFlightNumber());
        }

        if (subtitleLabel != null) {
            subtitleLabel.setText("Légitársaság, repülőgép és útvonal áttekintése.");
        }

        fillAirlineInfo();
        fillAircraftInfo();
        drawRouteOnMap();
    }

    private void fillAirlineInfo() {
        if (flight.getAirline() != null) {
            var a = flight.getAirline();
            airlineNameLabel.setText(
                    a.getName() != null ? a.getName() : "—"
            );
            airlineCodeLabel.setText(
                    a.getIataCode() != null ? a.getIataCode() :
                            (a.getIcaoCode() != null ? a.getIcaoCode() : "—")
            );
            airlineCountryLabel.setText(
                    a.getCountry() != null ? a.getCountry() : "—"
            );
        } else {
            airlineNameLabel.setText("—");
            airlineCodeLabel.setText("—");
            airlineCountryLabel.setText("—");
        }
    }

    private void fillAircraftInfo() {
        if (flight.getAircraft() != null) {
            var ac = flight.getAircraft();
            aircraftRegLabel.setText(
                    ac.getRegistration() != null ? ac.getRegistration() : "—"
            );
            aircraftTypeLabel.setText(
                    ac.getTypeIcao() != null ? ac.getTypeIcao() : "—"
            );
            aircraftSeatsLabel.setText(
                    ac.getMaxSeatCapacity() != null ?
                            ac.getMaxSeatCapacity().toString() : "—"
            );
            aircraftYearLabel.setText(
                    ac.getManufactureYear() != null ?
                            ac.getManufactureYear().toString() : "—"
            );
            aircraftStatusLabel.setText(
                    ac.getStatus() != null ? ac.getStatus().name() : "—"
            );
        } else {
            aircraftRegLabel.setText("—");
            aircraftTypeLabel.setText("—");
            aircraftSeatsLabel.setText("—");
            aircraftYearLabel.setText("—");
            aircraftStatusLabel.setText("—");
        }
    }

    private void drawRouteOnMap() {
        Airports dep = flight.getDepartureAirport();
        Airports arr = flight.getArrivalAirport();
        if (dep == null || arr == null) {
            routeLabel.setText("Nincs induló / érkező repülőtér.");
            return;
        }

        var depLatBD = dep.getLatitude();
        var depLonBD = dep.getLongitude();
        var arrLatBD = arr.getLatitude();
        var arrLonBD = arr.getLongitude();

        if (depLatBD == null || depLonBD == null || arrLatBD == null || arrLonBD == null) {
            routeLabel.setText("Nincs koordináta adat ehhez a járathoz.");
            return;
        }

        double depLat = depLatBD.doubleValue();
        double depLon = depLonBD.doubleValue();
        double arrLat = arrLatBD.doubleValue();
        double arrLon = arrLonBD.doubleValue();

        routeLabel.setText(
                String.format("%s → %s (útvonal térképen)",
                        safe(dep.getIcaoCode()), safe(arr.getIcaoCode()))
        );

        webEngine = mapWebView.getEngine();          // MINDIG az aktuális WebView engine-je
        webEngine.setJavaScriptEnabled(true);

        var resource = FlightDetailsDialogController.class.getResource("/web.html");
        if (resource == null) {
            routeLabel.setText("Nem található a web.html resource.");
            return;
        }
        String url = resource.toExternalForm();

        mapLoaded = false;

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
                mapLoaded = true;
                callConnectAirports(depLat, depLon, arrLat, arrLon);
            }
        });

        webEngine.load(url);
    }



    private String safe(String s) {
        return s != null ? s : "";
    }

    private void callConnectAirports(double depLat, double depLon,
                                     double arrLat, double arrLon) {
        // route-map.html-ben definiált JS függvény neve:
        String script = String.format(
                "if (typeof connectAirports === 'function') { connectAirports(%f, %f, %f, %f); }",
                depLat, depLon, arrLat, arrLon
        );
        webEngine.executeScript(script);


    }

    @FXML
    private void onClose() {
        if (titleLabel != null && titleLabel.getScene() != null) {
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.close();
        }
    }
}