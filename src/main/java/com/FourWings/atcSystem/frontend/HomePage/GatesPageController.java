package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
import com.FourWings.atcSystem.model.gate.Gate;
import com.FourWings.atcSystem.model.gate.GateService;
import com.FourWings.atcSystem.model.gate.GateStatus; // Importálni kell az Enumot!
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GatesPageController {

    private final AirportsService airportService;
    private final GateService gateService;
    private final FlightService flightService;

    @FXML private ComboBox<String> menuComboBox;
    @FXML private ComboBox<Airports> airportSelector;
    @FXML private FlowPane gatesContainer;
    @FXML private Label statusLabel;

    public GatesPageController(AirportsService airportService, GateService gateService, FlightService flightService) {
        this.airportService = airportService;
        this.gateService = gateService;
        this.flightService = flightService;
    }

    @FXML
    public void initialize() {
        setupMenuNavigation();
        setupAirportSelector();
        loadAirports();
    }

    private void loadGatesForAirport(Airports airport) {
        gatesContainer.getChildren().clear();
        statusLabel.setText("Kapuk betöltése...");

        if (airport == null) return;

        List<Gate> gates = gateService.getGatesForAirport(airport);

        // Fontos: Itt a 'findDeparturesByAirportWithDetails' hívódik meg a háttérben a Service-en keresztül
        List<Flight> departures = flightService.getDeparturesForAirport(airport);

        // Map készítése: Gate ID -> Flight
        Map<Long, Flight> gateFlightMap = departures.stream()
                .filter(f -> f.getGate() != null)
                .collect(Collectors.toMap(
                        f -> f.getGate().getId(), // JAVÍTVA: getId() a Gate entitásból
                        f -> f,
                        (existing, replacement) -> existing
                ));

        for (Gate gate : gates) {
            VBox gateBox = createGateBox(gate, gateFlightMap.get(gate.getId())); // JAVÍTVA: getId()
            gatesContainer.getChildren().add(gateBox);
        }

        statusLabel.setText(airport.getName() + ": " + gates.size() + " kapu megjelenítve.");
    }

    private VBox createGateBox(Gate gate, Flight flight) {
        VBox box = new VBox();
        box.setPrefSize(180, 130);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.TOP_CENTER);
        box.setSpacing(5);

        // Alap stílus
        String baseStyle = "-fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);";

        // Kapu kódja (A1, B2...)
        Label codeLabel = new Label(gate.getCode());
        codeLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        Label infoLabel = new Label();
        Label detailLabel = new Label();
        detailLabel.setStyle("-fx-text-alignment: center; -fx-font-size: 11px;");

        // 1. ESET: A kapu státusza NEM aktív (pl. Closed, Maintenance)
        // Feltételezve, hogy van GateStatus enum és OPEN az aktív
        if (gate.getStatus() != null && !gate.getStatus().name().equals("OPEN") && !gate.getStatus().name().equals("ACTIVE")) {
            box.setStyle(baseStyle + "-fx-background-color: #cfd8dc;"); // Szürke
            infoLabel.setText(gate.getStatus().name()); // Pl. CLOSED
            infoLabel.setStyle("-fx-text-fill: #546e7a; -fx-font-weight: bold;");
            detailLabel.setText(gate.getNote() != null ? gate.getNote() : "Lezárva");
        }
        // 2. ESET: Van hozzárendelve járat (FOGLALT)
        else if (flight != null) {
            box.setStyle(baseStyle + "-fx-background-color: #ef9a9a;"); // Pirosas
            infoLabel.setText("FOGLALT");
            infoLabel.setStyle("-fx-text-fill: #b71c1c; -fx-font-weight: bold;");

            detailLabel.setText(
                    flight.getFlightNumber() + "\n" +
                            flight.getAirlineName() + "\n" +
                            "Indul: " + flight.getScheduledDepartureText().substring(11) // Csak az óra:perc
            );
        }
        // 3. ESET: Nincs járat, státusz OK (SZABAD)
        else {
            box.setStyle(baseStyle + "-fx-background-color: #a5d6a7;"); // Zöldes
            infoLabel.setText("SZABAD");
            infoLabel.setStyle("-fx-text-fill: #1b5e20; -fx-font-weight: bold;");

            // Ha van a kapunak megjegyzése, kiírjuk (pl. "Csak A320")
            detailLabel.setText(gate.getNote() != null ? gate.getNote() : "-");
        }

        box.getChildren().addAll(codeLabel, infoLabel, detailLabel);
        return box;
    }

    // --- SETUP RÉSZEK (Változatlan) ---
    private void setupAirportSelector() {
        airportSelector.setConverter(new StringConverter<Airports>() {
            @Override
            public String toString(Airports airport) {
                return (airport == null) ? "" : airport.getIcaoCode() + " - " + airport.getName();
            }
            @Override
            public Airports fromString(String string) { return null; }
        });

        airportSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            loadGatesForAirport(newVal);
        });
    }

    private void loadAirports() {
        try {
            airportSelector.setItems(FXCollections.observableArrayList(airportService.getAllAirports()));
        } catch (Exception e) {
            statusLabel.setText("Hiba a repterek betöltésekor!");
        }
    }

    @FXML
    private void onRefresh() {
        loadGatesForAirport(airportSelector.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }

    private void setupMenuNavigation() {
        if (menuComboBox == null) return;
        menuComboBox.setValue("Kapuk(Ez inkább a repterekhez menne)");
        menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            switch (newVal) {
                case "Főoldal" -> SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", 800, 600);
                case "Repülők" -> SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", 800, 600);
                case "Repterek" -> SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", 800, 600);
                case "Repülőutak" -> SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Útvonalak", 1200, 600);
                case "Terminál(Ez is inkább reptér)" -> SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", 800, 600);
            }
        });
    }
}