package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.config.SceneManager;
import com.FourWings.atcSystem.config.ThemeManager;
import com.FourWings.atcSystem.model.aircraft.Aircraft; // Aircraft import
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
import java.util.stream.Collectors;

@Component
public class TerminalPageController {

    @FXML
    private void onToggleTheme() {
        ThemeManager.toggleTheme();
        SceneManager.reloadCurrentScene();
    }


    public static final int WIDTH = 1200;
    public static final int HEIGHT = 600;

    private final AirportsService airportService;
    private final GateService gateService;
    private final FlightService flightService;

    @FXML private ComboBox<String> menuComboBox;
    @FXML private ComboBox<Airports> airportSelector;
    @FXML private FlowPane gatesContainer;
    @FXML private ScrollPane scrollPane;

    @FXML private Label totalGatesLabel;
    @FXML private Label occupiedGatesLabel;
    @FXML private Label passengerFlowLabel; // Ezen most már a Cargo-t is mutatjuk
    @FXML private Label statusLabel;

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

    private void loadTerminalDataForAirport(Airports airport) {
        gatesContainer.getChildren().clear();
        updateStats(0, 0, 0, 0); // Reset

        if (airport == null) {
            statusLabel.setText("Válassz repteret!");
            return;
        }

        statusLabel.setText("Terminál adatok betöltése...");

        // 1. Kapuk és Járatok lekérése
        List<Gate> gates = gateService.getGatesForAirport(airport);
        List<Flight> departures = flightService.getDeparturesForAirport(airport);

        // 2. Map készítése
        Map<Long, Flight> gateFlightMap = departures.stream()
                .filter(f -> f.getGate() != null)
                .collect(Collectors.toMap(
                        f -> f.getGate().getId(),
                        f -> f,
                        (existing, replacement) -> existing
                ));

        // 3. Kártyák generálása
        for (Gate gate : gates) {
            Flight flightAtGate = gateFlightMap.get(gate.getId());
            VBox gateCard = createGateCard(gate, flightAtGate);
            gatesContainer.getChildren().add(gateCard);
        }

        // 4. STATISZTIKÁK SZÁMÍTÁSA (UTAS + CARGO)
        int totalGates = gates.size();
        int occupiedGates = gateFlightMap.size();

        int totalPassengers = 0;
        int totalCargo = 0;

        // Végigmegyünk a terminálon lévő járatokon
        for (Flight flight : gateFlightMap.values()) {
            Aircraft aircraft = flight.getAircraft();

            if (aircraft != null) {
                // Utasok (Max Seat Capacity)
                if (aircraft.getMaxSeatCapacity() != null) {
                    totalPassengers += aircraft.getMaxSeatCapacity();
                }

                // Cargo (Cargo Capacity Base)
                if (aircraft.getCargoCapacityBase() != null) {
                    totalCargo += aircraft.getCargoCapacityBase();
                }
            }
        }

        updateStats(totalGates, occupiedGates, totalPassengers, totalCargo);
        statusLabel.setText(airport.getName() + " terminál áttekintés betöltve.");
    }

    private void updateStats(int total, int occupied, int passengers, int cargo) {
        if (totalGatesLabel != null) totalGatesLabel.setText(String.valueOf(total));
        if (occupiedGatesLabel != null) occupiedGatesLabel.setText(String.valueOf(occupied));

        // Itt jelenítjük meg mindkét értéket
        if (passengerFlowLabel != null) {
            passengerFlowLabel.setText(String.format("%d fő / %d kg", passengers, cargo));
            // Ha túl hosszú lenne a szöveg, csökkenthetjük a betűméretet CSS-el,
            // vagy az FXML-ben a Label szélességét növelhetjük.
        }
    }

    private VBox createGateCard(Gate gate, Flight flight) {
        VBox box = new VBox();
        box.setPrefSize(180, 130);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.TOP_CENTER);
        box.setSpacing(5);

        // Alap stílus
        String baseStyle = "-fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);";

        Label codeLabel = new Label(gate.getCode());
        codeLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        Label infoLabel = new Label();
        Label detailLabel = new Label();
        detailLabel.setStyle("-fx-text-alignment: center; -fx-font-size: 11px;");

        // 1. ESET: A kapu le van zárva (A Gate tábla status mezője alapján)
        // Csak azt nézzük, hogy a kapu működőképes-e
        if (gate.getStatus() != null && !gate.getStatus().name().equals("OPEN") && !gate.getStatus().name().equals("ACTIVE")) {
            box.setStyle(baseStyle + "-fx-background-color: #cfd8dc;"); // Szürke
            infoLabel.setText(gate.getStatus().name());
            infoLabel.setStyle("-fx-text-fill: #546e7a; -fx-font-weight: bold;");
        }
        // 2. ESET: Van járat a kapunál -> Megnézzük a GÉP típusát
        else if (flight != null) {

            boolean isCargoPlane = false;
            String aircraftType = "?";

            // --- ITT KÉRDEZZÜK LE A GÉP ADATAIT ---
            if (flight.getAircraft() != null) {
                Aircraft ac = flight.getAircraft();
                aircraftType = ac.getTypeIcao(); // Pl. B747

                // Logika: Ha nincs ülőhely (0 vagy null), de van cargo kapacitás -> CARGO GÉP
                int seats = (ac.getMaxSeatCapacity() != null) ? ac.getMaxSeatCapacity() : 0;
                int cargo = (ac.getCargoCapacityBase() != null) ? ac.getCargoCapacityBase() : 0;

                if (seats == 0 && cargo > 0) {
                    isCargoPlane = true;
                }
            }

            if (isCargoPlane) {
                // --- CARGO MEGJELENÍTÉS (Lila) ---
                box.setStyle(baseStyle + "-fx-background-color: #d1c4e9;");
                infoLabel.setText("CARGO");
                infoLabel.setStyle("-fx-text-fill: #512da8; -fx-font-weight: bold;");
            } else {
                // --- UTASSZÁLLÍTÓ MEGJELENÍTÉS (Pirosas) ---
                box.setStyle(baseStyle + "-fx-background-color: #ffcccc;");
                infoLabel.setText("FOGLALT");
                infoLabel.setStyle("-fx-text-fill: #d9534f; -fx-font-weight: bold;");
            }

            String dest = (flight.getDestinationName() != null) ? flight.getDestinationName() : "N/A";

            detailLabel.setText(
                    flight.getFlightNumber() + " (" + aircraftType + ")\n" +
                            "Cél: " + dest
            );
        }
        // 3. ESET: Üres a kapu
        else {
            box.setStyle(baseStyle + "-fx-background-color: #dff0d8;"); // Zöld
            infoLabel.setText("SZABAD");
            infoLabel.setStyle("-fx-text-fill: #3c763d; -fx-font-weight: bold;");
            detailLabel.setText("Várakozás...");
        }

        box.getChildren().addAll(codeLabel, infoLabel, detailLabel);
        return box;
    }

    // --- SETUP (Változatlan) ---

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

    @FXML private void onRefresh() {
        loadTerminalDataForAirport(airportSelector.getSelectionModel().getSelectedItem());
    }

    @FXML private void onLogout() {
        SceneManager.switchTo("MainPage.fxml", "ATC – Bejelentkezés", 800, 400);
    }

    private void setupMenuNavigation() {
        if (menuComboBox == null) return;
        menuComboBox.setValue("Terminál(Ez is inkább reptér)");
        menuComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            switch (newVal) {
                case "Főoldal" -> SceneManager.switchTo("HomePage.fxml", "ATC – Főoldal", WIDTH, HEIGHT);
                case "Repülők" -> SceneManager.switchTo("HomePage/PlanesPage.fxml", "ATC – Repülők", WIDTH, HEIGHT);
                case "Repterek" -> SceneManager.switchTo("HomePage/AirportsPage.fxml", "ATC – Repterek", WIDTH, HEIGHT);
                case "Repülőutak" -> SceneManager.switchTo("HomePage/RoutesPage.fxml", "ATC – Útvonalak", WIDTH, HEIGHT);
                case "Kapuk(Ez inkább a repterekhez menne)" -> SceneManager.switchTo("HomePage/GatesPage.fxml", "ATC – Kapuk", WIDTH, HEIGHT);
                case "Terminál(Ez is inkább reptér)" -> SceneManager.switchTo("HomePage/TerminalPage.fxml", "ATC – Terminál", WIDTH, HEIGHT);
            }
        });
    }
}