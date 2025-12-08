package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
import com.FourWings.atcSystem.model.gate.Gate;
import com.FourWings.atcSystem.model.gate.GateService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;@Component
public class TerminalPageController {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 600;
    private final Random rand = new Random();

    // SERVICE-ek használata (GatesPage mintára)
    private final AirportsService airportService;
    private final GateService gateService;
    private final FlightService flightService;

    @FXML private ComboBox<String> menuComboBox;
    @FXML private ComboBox<Airports> airportSelector;
    @FXML private FlowPane gatesContainer;
    @FXML private ScrollPane scrollPane;

    // Statisztikai Label-ek (Ezek a Terminál oldal specifikumai)
    @FXML private Label totalGatesLabel;
    @FXML private Label occupiedGatesLabel;
    @FXML private Label passengerFlowLabel;
    @FXML private Label statusLabel; // Alsó információs sáv

    public TerminalPageController(AirportsService airportService, GateService gateService, FlightService flightService) {
        this.airportService = airportService;
        this.gateService = gateService;
        this.flightService = flightService;
    }

    @FXML
    public void initialize() {
        setupMenuNavigation();
        setupAirportSelector();
        loadAirportsList();

        if (scrollPane != null) {
            scrollPane.setFitToWidth(true);
        }
    }

    // --- FŐ LOGIKA: ADATOK BETÖLTÉSE ---
    private void loadTerminalDataForAirport(Airports airport) {
        // Törlés és alaphelyzet
        gatesContainer.getChildren().clear();
        updateStats(0, 0, 0);

        if (airport == null) {
            statusLabel.setText("Válassz repteret!");
            return;
        }

        statusLabel.setText("Terminál adatok betöltése...");

        // 1. Kapuk lekérése az adott reptérhez
        List<Gate> gates = gateService.getGatesForAirport(airport);

        // 2. Induló járatok lekérése (ezek vannak a terminálon)
        List<Flight> departures = flightService.getDeparturesForAirport(airport);

        // 3. Map készítése: Melyik kapunál van járat?
        Map<Long, Flight> gateFlightMap = departures.stream()
                .filter(f -> f.getGate() != null)
                .collect(Collectors.toMap(
                        f -> f.getGate().getId(),
                        f -> f,
                        (existing, replacement) -> existing
                ));

        // 4. Kártyák generálása
        for (Gate gate : gates) {
            Flight flightAtGate = gateFlightMap.get(gate.getId());
            VBox gateCard = createGateCard(gate, flightAtGate);
            gatesContainer.getChildren().add(gateCard);
        }

        // 5. Statisztikák frissítése
        int total = gates.size();
        int occupied = gateFlightMap.size();
        // Utasbecslés: Csak a foglalt kapukon lévő járatokra
        int passengers = gateFlightMap.values().stream()
                .mapToInt(f -> rand.nextInt(50, 250)) // Demo adat, ha nincs Aircraft kapacitás
                .sum();

        updateStats(total, occupied, passengers);
        statusLabel.setText(airport.getName() + " terminál áttekintés betöltve.");
    }

    private VBox createGateCard(Gate gate, Flight flight) {
        VBox box = new VBox();
        box.setPrefSize(180, 130);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.TOP_CENTER);
        box.setSpacing(5);

        // GatesPage stílus
        String baseStyle = "-fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);";

        Label codeLabel = new Label(gate.getCode());
        codeLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        Label infoLabel = new Label();
        Label detailLabel = new Label();
        detailLabel.setStyle("-fx-text-alignment: center; -fx-font-size: 11px;");

        // 1. Inaktív kapu
        if (gate.getStatus() != null && !gate.getStatus().name().equals("OPEN") && !gate.getStatus().name().equals("ACTIVE")) {
            box.setStyle(baseStyle + "-fx-background-color: #cfd8dc;");
            infoLabel.setText(gate.getStatus().name());
            infoLabel.setStyle("-fx-text-fill: #546e7a; -fx-font-weight: bold;");
        }
        // 2. Foglalt kapu
        else if (flight != null) {
            box.setStyle(baseStyle + "-fx-background-color: #ffcccc;"); // Pirosas
            infoLabel.setText("FOGLALT");
            infoLabel.setStyle("-fx-text-fill: #d9534f; -fx-font-weight: bold;");

            String dest = (flight.getDestinationName() != null) ? flight.getDestinationName() : "N/A";
            detailLabel.setText(
                    flight.getFlightNumber() + "\n" +
                            "Cél: " + dest
            );
        }
        // 3. Szabad kapu
        else {
            box.setStyle(baseStyle + "-fx-background-color: #dff0d8;"); // Zöldes
            infoLabel.setText("SZABAD");
            infoLabel.setStyle("-fx-text-fill: #3c763d; -fx-font-weight: bold;");
            detailLabel.setText("Várakozás...");
        }

        box.getChildren().addAll(codeLabel, infoLabel, detailLabel);
        return box;
    }

    private void updateStats(int total, int occupied, int passengers) {
        if (totalGatesLabel != null) totalGatesLabel.setText(String.valueOf(total));
        if (occupiedGatesLabel != null) occupiedGatesLabel.setText(String.valueOf(occupied));
        if (passengerFlowLabel != null) passengerFlowLabel.setText(passengers + " fő");
    }

    // --- SETUP RÉSZEK ---

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
            loadTerminalDataForAirport(newVal);
        });
    }

    private void loadAirportsList() {
        try {
            airportSelector.setItems(FXCollections.observableArrayList(airportService.getAllAirports()));
        } catch (Exception e) {
            statusLabel.setText("Hiba a repterek betöltésekor!");
        }
    }

    @FXML
    private void onRefresh() {
        loadTerminalDataForAirport(airportSelector.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void onLogout() {
        SceneManager.switchTo("fxml/MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }

    private void setupMenuNavigation() {
        if (menuComboBox == null) return;
        menuComboBox.setValue("Kapuk(Ez inkább a repterekhez menne)");
        menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            switch (newVal) {
                case "Főoldal" -> SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", WIDTH, HEIGHT);
                case "Repülők" -> SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", WIDTH, HEIGHT);
                case "Repterek" -> SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", WIDTH, HEIGHT);
                case "Repülőutak" -> SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Útvonalak", WIDTH, HEIGHT);
                case "Terminál(Ez is inkább reptér)" -> SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", WIDTH, HEIGHT);
            }
        });
    }
}