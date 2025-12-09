package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.aircraft.Aircraft;
import com.FourWings.atcSystem.model.aircraft.AircraftService;
import com.FourWings.atcSystem.model.airline.Airline;
import com.FourWings.atcSystem.model.airline.AirlineService;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.gate.Gate;
import com.FourWings.atcSystem.model.terminal.Terminal;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

@Component
public class NewFlightDialogController {

    // --- Állapot ---
    private Airports homeAirport;
    private final AirlineService airlineService;
    private final AirportsService airportsService;
    private final AircraftService aircraftService;   // 🔹 ÚJ

    public NewFlightDialogController(AirlineService airlineService,
                                     AirportsService airportsService,
                                     AircraftService aircraftService) {   // 🔹 módosított
        this.airlineService = airlineService;
        this.airportsService = airportsService;
        this.aircraftService = aircraftService;      // 🔹 ÚJ
    }

    // --- FXML mezők ---
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label statusLabel;
    @FXML private Label errorLabel;

    @FXML private ComboBox<Airline>   airlineCombo;
    @FXML private ComboBox<Airports>  destinationAirportCombo;
    @FXML private ComboBox<Aircraft>  aircraftCombo;

    @FXML private ComboBox<Terminal>  departureTerminalCombo;
    @FXML private ComboBox<Terminal>  arrivalTerminalCombo;

    @FXML private ComboBox<Gate>      departureGateCombo;
    @FXML private ComboBox<Gate>      arrivalGateCombo;

    @FXML private DatePicker          departureDatePicker;
    @FXML private TextField           departureTimeField;

    @FXML private DatePicker          arrivalDatePicker;
    @FXML private TextField           arrivalTimeField;

    @FXML private TextField           flightNumberField;

    @FXML
    public void initialize() {
        setupDestinationAirportCombo();
        // Itt majd:
        // - cellFactory / StringConverter beállítások
        // - listener-ek (airline → airport szűrés, stb.)
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        if (statusLabel != null) {
            statusLabel.setText("Add meg az új járat adatait.");
        }

        airlineCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Airline airline) {
                if (airline == null) return "";
                return airline.getName() + " (" + airline.getIcaoCode() + ")";
            }

            @Override
            public Airline fromString(String string) {
                return null; // nem kell visszaalakítani
            }
        });

        if (aircraftCombo != null) {
            aircraftCombo.setConverter(new StringConverter<Aircraft>() {
                @Override
                public String toString(Aircraft a) {
                    if (a == null) return "";
                    // pl.: HA-LYX (A320) vagy amit akarsz
                    String reg = a.getRegistration() != null ? a.getRegistration() : "";
                    String type = a.getTypeIcao() != null ? a.getTypeIcao() : "";
                    return reg + (type.isBlank() ? "" : " (" + type + ")");
                }

                @Override
                public Aircraft fromString(String s) {
                    return aircraftCombo.getItems().stream()
                            .filter(a -> toString(a).equals(s))
                            .findFirst()
                            .orElse(null);
                }
            });
        }

    }

    /**
     * Ezt hívja majd a ControllerHomePage a dialog megnyitásakor.
     */
    public void init(Airports homeAirport) {
        this.homeAirport = homeAirport;

        if (airlineCombo == null) {
            System.out.println("NewFlightDialog: airlineCombo nincs injektálva!");
            return;
        }

        if (homeAirport != null && titleLabel != null) {
            String city = homeAirport.getCity() != null ? homeAirport.getCity() : "";
            titleLabel.setText(
                    "Otthoni repülőtér: " +
                            safe(homeAirport.getIcaoCode()) + " – " +
                            safe(homeAirport.getName()) +
                            (city.isBlank() ? "" : " (" + city + ")")
            );
        }

        if (subtitleLabel != null) {
            subtitleLabel.setText("Először válassz légitársaságot, majd célrepteret és erőforrásokat.");
        }


        loadAirlinesForHomeAirport();
        loadDestinationAirports();
        loadAvailableAircraftForHomeAirport();

        // Később:
        // - airlineCombo feltöltése az otthoni reptérről elérhető légitársaságokkal
        // - destinationAirportCombo feltöltése a hálózatban elérhető repterekkel
        // stb.
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    @FXML
    private void onCancel() {
        close();
    }

    @FXML
    private void onSave() {
        // Itt később:
        // - validáció
        // - LocalDate + time string → LocalDateTime
        // - mentés service-en keresztül
        if (errorLabel != null) {
            errorLabel.setText("Mentés logika még nincs bekötve (skeleton).");
        }
    }

    private void close() {
        if (titleLabel != null && titleLabel.getScene() != null) {
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.close();
        }
    }
    private void loadDestinationAirports() {
        if (destinationAirportCombo == null || homeAirport == null) {
            return;
        }

        var allAirports = airportsService.getAllAirports();

        var destinations = allAirports.stream()
                .filter(a -> a.getId() != null && !a.getId().equals(homeAirport.getId()))
                .toList();

        destinationAirportCombo.setItems(FXCollections.observableArrayList(destinations));

        // opcionálisan választhatsz valami defaultot
        if (!destinations.isEmpty()) {
            destinationAirportCombo.getSelectionModel().selectFirst();
        }
    }

    private void setupDestinationAirportCombo() {
        if (destinationAirportCombo == null) return;

        destinationAirportCombo.setConverter(new StringConverter<Airports>() {
            @Override
            public String toString(Airports airport) {
                if (airport == null) return "";
                String code = airport.getCode();  // a @Transient getCode() amit írtál
                String name = airport.getName() != null ? airport.getName() : "";
                String city = airport.getCity() != null ? airport.getCity() : "";
                if (!city.isBlank()) {
                    return code + " – " + name + " (" + city + ")";
                }
                return code + " – " + name;
            }

            @Override
            public Airports fromString(String s) {
                return destinationAirportCombo.getItems().stream()
                        .filter(a -> toString(a).equals(s))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void loadAirlinesForHomeAirport() {
        if (airlineCombo == null || homeAirport == null) {
            return;
        }

        var airlines = airlineService.findByBaseAirport(homeAirport);

        airlineCombo.setItems(FXCollections.observableArrayList(airlines));

        if (!airlines.isEmpty()) {
            airlineCombo.getSelectionModel().selectFirst();
        }
    }

    private void loadAvailableAircraftForHomeAirport() {
        if (aircraftCombo == null || homeAirport == null) {
            return;
        }

        var aircraftList = aircraftService.findAvailableForAirport(homeAirport);

        aircraftCombo.setItems(FXCollections.observableArrayList(aircraftList));

        if (!aircraftList.isEmpty()) {
            aircraftCombo.getSelectionModel().selectFirst();
        }
    }
}