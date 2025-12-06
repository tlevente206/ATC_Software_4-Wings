package com.FourWings.atcSystem.frontend.controller;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airport.AirportsService;
import com.FourWings.atcSystem.model.flight.Flight;
import com.FourWings.atcSystem.model.flight.FlightRepository;
import com.FourWings.atcSystem.model.flight.FlightStatus;
import jakarta.transaction.Transactional;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Component
public class CreateFlightDialogController {

    private final FlightRepository flightRepository;
    private final AirportsService airportsService;

    private Airports homeAirport;
    private Flight editingFlight; // null = új járat, nem null = szerkesztés

    @FXML private Label dialogTitleLabel;
    @FXML private Label airportInfoLabel;

    @FXML private RadioButton departureRadio;
    @FXML private RadioButton arrivalRadio;

    @FXML private TextField flightNumberField;
    @FXML private ComboBox<Airports> otherAirportCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private ComboBox<FlightStatus> statusCombo;
    @FXML private TextArea noteArea;

    @FXML private Label errorLabel;

    public CreateFlightDialogController(FlightRepository flightRepository,
                                        AirportsService airportsService) {
        this.flightRepository = flightRepository;
        this.airportsService = airportsService;
    }

    @FXML
    private void initialize() {
        if (statusCombo != null) {
            statusCombo.setItems(FXCollections.observableArrayList(FlightStatus.values()));
            statusCombo.getSelectionModel().select(FlightStatus.SCHEDULED);
        }

        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }

        if (timeField != null) {
            timeField.setText("12:00");
        }

        setError("");
    }

    // ---- INIT: csak otthoni reptér (új járat) ----
    public void init(Airports homeAirport) {
        init(homeAirport, null);
    }

    // ---- INIT: otthoni reptér + opcionálisan szerkesztendő Flight ----
    public void init(Airports homeAirport, Flight existingFlight) {
        this.homeAirport = homeAirport;
        this.editingFlight = existingFlight;

        if (homeAirport != null && airportInfoLabel != null) {
            String city = homeAirport.getCity() != null ? homeAirport.getCity() : "";
            airportInfoLabel.setText(
                    safe(homeAirport.getIcaoCode()) + " – " +
                            safe(homeAirport.getName()) +
                            (city.isBlank() ? "" : " (" + city + ")")
            );
        }

        setupOtherAirportCombo();

        if (dialogTitleLabel != null) {
            if (editingFlight == null) {
                dialogTitleLabel.setText("Új járat létrehozása");
            } else {
                dialogTitleLabel.setText("Járat szerkesztése: " + safe(editingFlight.getFlightNumber()));
            }
        }

        if (departureRadio != null && arrivalRadio != null) {
            if (editingFlight == null) {
                departureRadio.setSelected(true);
                arrivalRadio.setSelected(false);
            } else {
                fillFormFromFlight(editingFlight);
            }
        } else if (editingFlight != null) {
            fillFormFromFlight(editingFlight);
        }
    }

    private void setError(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg != null ? msg : "");
        }
        System.err.println("CreateFlightDialog: " + msg);
    }

    private void setupOtherAirportCombo() {
        try {
            List<Airports> airports = airportsService.getAllAirports();
            System.out.println("CreateFlightDialog: összes reptér: " +
                    (airports != null ? airports.size() : 0));

            if (airports == null || airports.isEmpty()) {
                setError("Nincs egyetlen repülőtér sem az adatbázisban.");
                if (otherAirportCombo != null) {
                    otherAirportCombo.setItems(FXCollections.observableArrayList());
                }
                return;
            }

            List<Airports> filtered = airports;

            if (homeAirport != null && homeAirport.getId() != null) {
                filtered = airports.stream()
                        .filter(a -> a.getId() != null && !a.getId().equals(homeAirport.getId()))
                        .sorted(Comparator.comparing(a -> safe(a.getIcaoCode())))
                        .toList();

                System.out.println("CreateFlightDialog: otthonit kivéve marad: " + filtered.size());

                if (filtered.isEmpty()) {
                    setError("Nincs az otthoni repülőtértől különböző repülőtér az adatbázisban.");
                }
            }

            if (otherAirportCombo != null) {
                otherAirportCombo.setItems(FXCollections.observableArrayList(filtered));
                otherAirportCombo.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(Airports airport) {
                        if (airport == null) return "";
                        String city = airport.getCity() != null ? airport.getCity() : "";
                        return safe(airport.getIcaoCode()) + " – " +
                                safe(airport.getName()) +
                                (city.isBlank() ? "" : " (" + city + ")");
                    }

                    @Override
                    public Airports fromString(String string) {
                        return null;
                    }
                });
            }

        } catch (Exception ex) {
            setError("Nem sikerült betölteni a reptereket: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void fillFormFromFlight(Flight f) {
        if (f == null) return;

        if (flightNumberField != null && f.getFlightNumber() != null) {
            flightNumberField.setText(f.getFlightNumber());
        }

        boolean isDepartureFromHome = f.getDepartureAirport() != null &&
                homeAirport != null &&
                f.getDepartureAirport().getId() != null &&
                f.getDepartureAirport().getId().equals(homeAirport.getId());

        boolean isArrivalToHome = f.getArrivalAirport() != null &&
                homeAirport != null &&
                f.getArrivalAirport().getId() != null &&
                f.getArrivalAirport().getId().equals(homeAirport.getId());

        Airports other = null;
        LocalDateTime dt = null;

        if (isDepartureFromHome) {
            if (departureRadio != null) departureRadio.setSelected(true);
            if (arrivalRadio != null) arrivalRadio.setSelected(false);
            other = f.getArrivalAirport();
            dt = f.getScheduledDeparture();
        } else if (isArrivalToHome) {
            if (arrivalRadio != null) arrivalRadio.setSelected(true);
            if (departureRadio != null) departureRadio.setSelected(false);
            other = f.getDepartureAirport();
            dt = f.getScheduledArrival();
        } else {
            if (departureRadio != null) departureRadio.setSelected(true);
            if (arrivalRadio != null) arrivalRadio.setSelected(false);
            other = f.getArrivalAirport() != null ? f.getArrivalAirport() : f.getDepartureAirport();
            dt = f.getScheduledDeparture() != null ? f.getScheduledDeparture() : f.getScheduledArrival();
        }

        Airports selectedOther = other;

        if (selectedOther != null &&
                selectedOther.getId() != null &&
                otherAirportCombo != null &&
                otherAirportCombo.getItems() != null) {

            otherAirportCombo.getItems().stream()
                    .filter(a -> selectedOther.getId().equals(a.getId()))
                    .findFirst()
                    .ifPresent(a -> otherAirportCombo.getSelectionModel().select(a));
        }

        if (dt != null && datePicker != null && timeField != null) {
            datePicker.setValue(dt.toLocalDate());
            timeField.setText(String.format("%02d:%02d", dt.getHour(), dt.getMinute()));
        }

        if (statusCombo != null && f.getStatus() != null) {
            statusCombo.getSelectionModel().select(f.getStatus());
        }

        if (noteArea != null && f.getNote() != null) {
            noteArea.setText(f.getNote());
        }
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    // ---- MENTÉS ----
    @FXML
    @Transactional
    private void onSave() {
        setError("");

        if (homeAirport == null) {
            setError("Nincs beállított otthoni repülőtér.");
            return;
        }

        String flightNumber = flightNumberField != null && flightNumberField.getText() != null
                ? flightNumberField.getText().trim()
                : "";
        if (flightNumber.isBlank()) {
            setError("Add meg a járatszámot.");
            return;
        }

        Airports otherAirport = otherAirportCombo != null ? otherAirportCombo.getValue() : null;
        if (otherAirport == null) {
            setError("Válassz másik repteret (kiinduló/cél reptér).");
            return;
        }

        LocalDate date = datePicker != null ? datePicker.getValue() : null;
        if (date == null) {
            setError("Válassz dátumot.");
            return;
        }

        LocalTime time;
        try {
            String timeText = timeField != null ? timeField.getText() : null;
            time = parseTime(timeText);
        } catch (IllegalArgumentException ex) {
            setError(ex.getMessage());
            return;
        }

        FlightStatus status = statusCombo != null ? statusCombo.getValue() : null;
        if (status == null) {
            status = FlightStatus.SCHEDULED;
        }

        boolean isDeparture = departureRadio != null && departureRadio.isSelected();
        boolean isArrival = arrivalRadio != null && arrivalRadio.isSelected();

        if (!isDeparture && !isArrival) {
            setError("Válaszd ki, hogy induló vagy érkező járat.");
            return;
        }

        LocalDateTime dt = LocalDateTime.of(date, time);

        Flight f = (editingFlight == null) ? new Flight() : editingFlight;

        f.setFlightNumber(flightNumber);
        f.setStatus(status);
        if (noteArea != null) {
            f.setNote(noteArea.getText());
        }

        if (isDeparture) {
            f.setDepartureAirport(homeAirport);
            f.setArrivalAirport(otherAirport);
            f.setScheduledDeparture(dt);
        } else {
            f.setDepartureAirport(otherAirport);
            f.setArrivalAirport(homeAirport);
            f.setScheduledArrival(dt);
        }

        flightRepository.save(f);
        closeWindow();
    }

    private LocalTime parseTime(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Add meg az időpontot HH:mm formátumban.");
        }
        String trimmed = text.trim();
        String[] parts = trimmed.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Idő formátum: HH:mm (pl. 14:30).");
        }
        try {
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            if (h < 0 || h > 23 || m < 0 || m > 59) {
                throw new IllegalArgumentException("Érvénytelen idő: óra 0–23, perc 0–59.");
            }
            return LocalTime.of(h, m);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Idő formátum: HH:mm (pl. 08:45).");
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        if (dialogTitleLabel == null || dialogTitleLabel.getScene() == null) {
            return;
        }
        Stage stage = (Stage) dialogTitleLabel.getScene().getWindow();
        stage.close();
    }
}