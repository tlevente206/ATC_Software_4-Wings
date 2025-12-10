package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.aircraft.Aircraft;
import com.FourWings.atcSystem.model.aircraft.AircraftService;
import com.FourWings.atcSystem.model.airline.Airline;
import com.FourWings.atcSystem.model.airline.AirlineService;
import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightService;
import com.FourWings.atcSystem.model.flight.FlightStatus;
import com.FourWings.atcSystem.model.gate.Gate;
import com.FourWings.atcSystem.model.gate.GateService;
import com.FourWings.atcSystem.model.terminal.Terminal;
import com.FourWings.atcSystem.model.terminal.TerminalService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class NewFlightDialogController {

    // --- Állapot ---
    private Airports homeAirport;
    private final AirlineService airlineService;
    private final AirportsService airportsService;
    private final AircraftService aircraftService;
    private final TerminalService terminalService;
    private final GateService gateService;
    private final FlightService flightService;

    // 🔹 kiválasztott dátumok eltárolva
    private LocalDate selectedDepartureDate;
    private LocalDate selectedArrivalDate;
    // 🔹 idő- és járatszám mezők eltárolva
    private String selectedDepartureTime;
    private String selectedArrivalTime;
    private String selectedFlightNumber;
    private boolean savedSuccessfully = false;

    public NewFlightDialogController(AirlineService airlineService,
                                     AirportsService airportsService,
                                     AircraftService aircraftService,
                                     TerminalService terminalService,
                                     GateService gateService, FlightService flightService) {
        this.airlineService = airlineService;
        this.airportsService = airportsService;
        this.aircraftService = aircraftService;
        this.terminalService = terminalService;
        this.gateService = gateService;
        this.flightService = flightService;
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
        setupTerminalCombos();
        setupGateCombos();
        setupDatePickers();   // 🔹 új: dátum figyelők
        setupTextFields();

        if (errorLabel != null) {
            errorLabel.setText("");
        }
        if (statusLabel != null) {
            statusLabel.setText("Add meg az új járat adatait.");
        }

        // --- Légitársaság Combo ---
        if (airlineCombo != null) {
            airlineCombo.setConverter(new StringConverter<>() {
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
        }

        // --- Gép Combo ---
        if (aircraftCombo != null) {
            aircraftCombo.setConverter(new StringConverter<Aircraft>() {
                @Override
                public String toString(Aircraft a) {
                    if (a == null) return "";
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

        // --- Célreptér változás → érkezési terminálok ---
        if (destinationAirportCombo != null) {
            destinationAirportCombo.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> loadArrivalTerminalsForDestinationAirport(newVal));
        }

        // --- indulási terminál változás → indulási kapuk frissítése ---
        if (departureTerminalCombo != null) {
            departureTerminalCombo.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> loadDepartureGatesForTerminal(newVal));
        }

        // --- érkezési terminál változás → érkezési kapuk frissítése ---
        if (arrivalTerminalCombo != null) {
            arrivalTerminalCombo.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> loadArrivalGatesForTerminal(newVal));
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
        loadDepartureTerminalsForHomeAirport();

        var selectedDestination = destinationAirportCombo != null
                ? destinationAirportCombo.getSelectionModel().getSelectedItem()
                : null;
        loadArrivalTerminalsForDestinationAirport(selectedDestination);

        var selectedDepartureTerminal = departureTerminalCombo != null
                ? departureTerminalCombo.getSelectionModel().getSelectedItem()
                : null;
        loadDepartureGatesForTerminal(selectedDepartureTerminal);

        var selectedArrivalTerminal = arrivalTerminalCombo != null
                ? arrivalTerminalCombo.getSelectionModel().getSelectedItem()
                : null;
        loadArrivalGatesForTerminal(selectedArrivalTerminal);

        // 🔹 opcionálisan: alapértelmezett dátumok maira
        if (departureDatePicker != null && departureDatePicker.getValue() == null) {
            LocalDate today = LocalDate.now();
            departureDatePicker.setValue(today);
            selectedDepartureDate = today;
        }
        if (arrivalDatePicker != null && arrivalDatePicker.getValue() == null) {
            LocalDate today = (selectedDepartureDate != null) ? selectedDepartureDate : LocalDate.now();
            arrivalDatePicker.setValue(today);
            selectedArrivalDate = today;
        }
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    @FXML
    private void onCancel() {
        close();
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
                String code = airport.getCode();
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

    private void loadDepartureTerminalsForHomeAirport() {
        if (departureTerminalCombo == null || homeAirport == null) {
            return;
        }

        var terminals = terminalService.findForAirport(homeAirport);
        departureTerminalCombo.setItems(FXCollections.observableArrayList(terminals));

        if (!terminals.isEmpty()) {
            departureTerminalCombo.getSelectionModel().selectFirst();
        }
    }

    private void loadArrivalTerminalsForDestinationAirport(Airports destination) {
        if (arrivalTerminalCombo == null || destination == null) {
            return;
        }

        var terminals = terminalService.findForAirport(destination);
        arrivalTerminalCombo.setItems(FXCollections.observableArrayList(terminals));

        if (!terminals.isEmpty()) {
            arrivalTerminalCombo.getSelectionModel().selectFirst();
        }
    }

    private void setupTerminalCombos() {
        StringConverter<Terminal> terminalConverter = new StringConverter<>() {
            @Override
            public String toString(Terminal t) {
                if (t == null) return "";
                return t.getCode() != null ? t.getCode() : "Terminal " + t.getId();
            }

            @Override
            public Terminal fromString(String s) {
                if (departureTerminalCombo != null && departureTerminalCombo.getItems() != null) {
                    return departureTerminalCombo.getItems().stream()
                            .filter(t -> toString(t).equals(s))
                            .findFirst().orElse(null);
                }
                return null;
            }
        };

        if (departureTerminalCombo != null) {
            departureTerminalCombo.setConverter(terminalConverter);
        }
        if (arrivalTerminalCombo != null) {
            arrivalTerminalCombo.setConverter(terminalConverter);
        }
    }

    private void setupGateCombos() {
        StringConverter<Gate> gateConverter = new StringConverter<>() {
            @Override
            public String toString(Gate g) {
                if (g == null) return "";
                String code = g.getCode() != null ? g.getCode() : "";
                return code.isBlank() ? ("Gate " + g.getId()) : code;
            }

            @Override
            public Gate fromString(String s) {
                if (departureGateCombo != null && departureGateCombo.getItems() != null) {
                    return departureGateCombo.getItems().stream()
                            .filter(g -> toString(g).equals(s))
                            .findFirst()
                            .orElse(null);
                }
                return null;
            }
        };

        if (departureGateCombo != null) {
            departureGateCombo.setConverter(gateConverter);
        }
        if (arrivalGateCombo != null) {
            arrivalGateCombo.setConverter(gateConverter);
        }
    }

    private void loadDepartureGatesForTerminal(Terminal terminal) {
        if (departureGateCombo == null) {
            return;
        }

        var gates = gateService.getGatesForTerminal(terminal);
        departureGateCombo.setItems(FXCollections.observableArrayList(gates));

        if (!gates.isEmpty()) {
            departureGateCombo.getSelectionModel().selectFirst();
        } else {
            departureGateCombo.getSelectionModel().clearSelection();
        }
    }

    private void loadArrivalGatesForTerminal(Terminal terminal) {
        if (arrivalGateCombo == null) {
            return;
        }

        var gates = gateService.getGatesForTerminal(terminal);
        arrivalGateCombo.setItems(FXCollections.observableArrayList(gates));

        if (!gates.isEmpty()) {
            arrivalGateCombo.getSelectionModel().selectFirst();
        } else {
            arrivalGateCombo.getSelectionModel().clearSelection();
        }
    }

    // 🔹 Dátumkezelés: departure → arrival automatikus másolás + állapot mentés
    private void setupDatePickers() {
        if (departureDatePicker != null) {
            departureDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                selectedDepartureDate = newVal;

                if (newVal != null && arrivalDatePicker != null) {
                    // automatikusan állítsuk be az érkezési dátumot is ugyanarra
                    arrivalDatePicker.setValue(newVal);
                    selectedArrivalDate = newVal;
                }
            });
        }

        if (arrivalDatePicker != null) {
            arrivalDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                selectedArrivalDate = newVal;
            });
        }
    }

    private void setupTextFields() {
        if (departureTimeField != null) {
            departureTimeField.textProperty().addListener((obs, oldVal, newVal) -> {
                selectedDepartureTime = newVal;
            });
        }

        if (arrivalTimeField != null) {
            arrivalTimeField.textProperty().addListener((obs, oldVal, newVal) -> {
                selectedArrivalTime = newVal;
            });
        }

        if (flightNumberField != null) {
            flightNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
                selectedFlightNumber = newVal;
            });
        }
    }

    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
// vagy "HH:mm" ha mindig 2 számjegyet szeretnél
// pl. 07:05, 16:30 stb.

    private LocalTime parseTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(text.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime combineDateAndTime(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return null;
        }
        return LocalDateTime.of(date, time);
    }

    @FXML
    private void onSave() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }

        // 🔹 1) ComboBox értékek begyűjtése
        Airline airline = airlineCombo != null ? airlineCombo.getValue() : null;
        Airports destination = destinationAirportCombo != null ? destinationAirportCombo.getValue() : null;
        Aircraft aircraft = aircraftCombo != null ? aircraftCombo.getValue() : null;

        Terminal depTerminal = departureTerminalCombo != null ? departureTerminalCombo.getValue() : null;
        Terminal arrTerminal = arrivalTerminalCombo != null ? arrivalTerminalCombo.getValue() : null;

        Gate depGate = departureGateCombo != null ? departureGateCombo.getValue() : null;
        Gate arrGate = arrivalGateCombo != null ? arrivalGateCombo.getValue() : null;

        // 🔹 2) Dátum + idő feldolgozása
        LocalDate depDate = departureDatePicker != null ? departureDatePicker.getValue() : null;
        LocalDate arrDate = arrivalDatePicker != null ? arrivalDatePicker.getValue() : null;

        String depTimeText = departureTimeField != null ? departureTimeField.getText() : null;
        String arrTimeText = arrivalTimeField != null ? arrivalTimeField.getText() : null;
        String flightNumber = flightNumberField != null ? flightNumberField.getText() : null;

        LocalTime depTime = parseTime(depTimeText);
        LocalTime arrTime = parseTime(arrTimeText);

        LocalDateTime scheduledDeparture = combineDateAndTime(depDate, depTime);
        LocalDateTime scheduledArrival   = combineDateAndTime(arrDate, arrTime);

        // 🔹 3) Alap validációk
        StringBuilder sb = new StringBuilder();

        if (homeAirport == null) {
            sb.append("Hiányzik az induló repülőtér (homeAirport).\n");
        }
        if (destination == null) {
            sb.append("Válaszd ki a célrepülőteret.\n");
        }
        if (airline == null) {
            sb.append("Válaszd ki a légitársaságot.\n");
        }
        if (aircraft == null) {
            sb.append("Válassz repülőgépet.\n");
        }
        if (depGate == null) {
            sb.append("Válassz indulási kaput.\n");
        }
        // arrGate opcionális lehet – ha kötelező, ezt is ellenőrizheted
        if (flightNumber == null || flightNumber.isBlank()) {
            sb.append("Add meg a járatszámot.\n");
        }
        if (scheduledDeparture == null) {
            sb.append("Adj meg érvényes indulási dátumot és időt (pl. 14:35).\n");
        }
        if (scheduledArrival == null) {
            sb.append("Adj meg érvényes érkezési dátumot és időt (pl. 16:10).\n");
        }

        if (sb.length() > 0) {
            if (errorLabel != null) {
                errorLabel.setText(sb.toString());
            } else {
                System.err.println("Hiba az új járat mentésekor:\n" + sb);
            }
            return;
        }

        try {
            // 🔹 4) Flight objektum felépítése
            Flight flight = Flight.builder()
                    .departureAirport(homeAirport)
                    .arrivalAirport(destination)
                    .airline(airline)
                    .aircraft(aircraft)

                    // ⚠️ Flight-ban jelenleg csak EGY gate van -> legyen az indulási kapu
                    .gate(depGate)

                    .flightNumber(flightNumber.trim())

                    // 🔹 státusz – állítsd arra, ami az enumodban van (pl. SCHEDULED / PLANNED)
                    .status(FlightStatus.SCHEDULED)  // ha másképp hívják az enumodban, írd át

                    .scheduledDeparture(scheduledDeparture)
                    .scheduledArrival(scheduledArrival)

                    // induláskor tehetjük null-ra vagy azonosra a scheduled-del
                    .estimatedDeparture(scheduledDeparture)
                    .estimatedArrival(scheduledArrival)

                    .actualDeparture(null)
                    .actualArrival(null)

                    .note(null)
                    .build();

            // 🔹 5) Mentés service-en keresztül
            Flight saved = flightService.createFlight(flight);

            savedSuccessfully = true;

            if (statusLabel != null) {
                statusLabel.setText("Járat mentve. ID: " + saved.getId());
            }

            // 🔹 6) Dialógus bezárása
            close();

        } catch (Exception e) {
            e.printStackTrace();
            if (errorLabel != null) {
                errorLabel.setText("Hiba történt a járat mentésekor: " + e.getMessage());
            }
        }
    }

    public boolean isSavedSuccessfully() {
        return savedSuccessfully;
    }
}