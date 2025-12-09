package com.FourWings.atcSystem.frontend.HomePage;

import com.FourWings.atcSystem.model.aircraft.Aircraft;
import com.FourWings.atcSystem.model.airline.Airline;
import com.FourWings.atcSystem.model.flight.Flight;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;

public class FlightDetailsDialogController {

    // --- Járat fejléc ---
    @FXML private Label flightNumberLabel;
    @FXML private Label routeLabel;
    @FXML private Label timesLabel;
    @FXML private Label statusLabel;

    // --- Repülőgép tab ---
    @FXML private Label acIdLabel;
    @FXML private Label acRegistrationLabel;
    @FXML private Label acTypeIcaoLabel;
    @FXML private Label acAirlineIdLabel;
    @FXML private Label acStatusLabel;
    @FXML private Label acMsnLabel;
    @FXML private Label acMaxSeatLabel;
    @FXML private Label acCargoBaseLabel;
    @FXML private Label acBaseAirportIdLabel;
    @FXML private Label acManufactureYearLabel;
    @FXML private Label acNoteLabel;
    @FXML private Label acCreatedAtLabel;
    @FXML private Label acUpdatedAtLabel;

    // --- Légitársaság tab ---
    @FXML private Label alIdLabel;
    @FXML private Label alNameLabel;
    @FXML private Label alIcaoLabel;
    @FXML private Label alIataLabel;
    @FXML private Label alCountryLabel;
    @FXML private Label alFoundedYearLabel;
    @FXML private Label alActiveLabel;
    @FXML private Label alBusinessModeLabel;
    @FXML private Label alBaseAirportLabel;
    @FXML private Label alWebsiteLabel;
    @FXML private Label alPhoneLabel;
    @FXML private Label alEmailLabel;
    @FXML private Label alHqAddressLabel;
    @FXML private Label alNoteLabel;
    @FXML private Label alCreatedAtLabel;
    @FXML private Label alUpdatedAtLabel;

    private final DateTimeFormatter dtf =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public void setData(Flight flight, Aircraft aircraft, Airline airline) {
        setFlightHeader(flight);
        setAircraftData(aircraft);
        setAirlineData(airline);
    }

    private void setFlightHeader(Flight flight) {
        if (flight == null) return;

        flightNumberLabel.setText(safe(flight.getFlightNumber()));

        String origin = safe(flight.getOriginName());
        String dest   = safe(flight.getDestinationName());
        routeLabel.setText(origin + " → " + dest);

        String dep = safeDT(flight.getScheduledDepartureText());
        String arr = safeDT(flight.getScheduledArrivalText());
        // itt már formázott Stringeket tárolsz, úgyhogy csak összefűzzük
        timesLabel.setText(dep + "   →   " + arr);

        statusLabel.setText(safe(flight.getStatusText()));
    }

    private void setAircraftData(Aircraft ac) {
        if (ac == null) {
            acIdLabel.setText("-");
            acRegistrationLabel.setText("Nincs adat");
            acTypeIcaoLabel.setText("-");
            acAirlineIdLabel.setText("-");
            acStatusLabel.setText("-");
            acMsnLabel.setText("-");
            acMaxSeatLabel.setText("-");
            acCargoBaseLabel.setText("-");
            acBaseAirportIdLabel.setText("-");
            acManufactureYearLabel.setText("-");
            acNoteLabel.setText("-");
            acCreatedAtLabel.setText("-");
            acUpdatedAtLabel.setText("-");
            return;
        }

        acIdLabel.setText(safe(ac.getId()));
        acRegistrationLabel.setText(safe(ac.getRegistration()));
        acTypeIcaoLabel.setText(safe(ac.getTypeIcao()));
        acAirlineIdLabel.setText(safe(ac.getAirlineId()));
        acStatusLabel.setText(safe(ac.getStatus()));
        acMsnLabel.setText(safe(ac.getMsn()));
        acMaxSeatLabel.setText(safe(ac.getMaxSeatCapacity()));
        acCargoBaseLabel.setText(safe(ac.getCargoCapacityBase()));
        acBaseAirportIdLabel.setText(safe(ac.getBaseAirportId()));
        acManufactureYearLabel.setText(safe(ac.getManufactureYear()));
        acNoteLabel.setText(safe(ac.getNote()));
        acCreatedAtLabel.setText(safeDT(ac.getCreatedAt()));
        acUpdatedAtLabel.setText(safeDT(ac.getUpdatedAt()));
    }

    private void setAirlineData(Airline al) {
        if (al == null) {
            alIdLabel.setText("-");
            alNameLabel.setText("Nincs adat");
            alIcaoLabel.setText("-");
            alIataLabel.setText("-");
            alCountryLabel.setText("-");
            alFoundedYearLabel.setText("-");
            alActiveLabel.setText("-");
            alBusinessModeLabel.setText("-");
            alBaseAirportLabel.setText("-");
            alWebsiteLabel.setText("-");
            alPhoneLabel.setText("-");
            alEmailLabel.setText("-");
            alHqAddressLabel.setText("-");
            alNoteLabel.setText("-");
            alCreatedAtLabel.setText("-");
            alUpdatedAtLabel.setText("-");
            alBaseAirportLabel.setText("-");
            return;
        }

        alIdLabel.setText(safe(al.getId()));
        alNameLabel.setText(safe(al.getName()));
        alIcaoLabel.setText(safe(al.getIcaoCode()));
        alIataLabel.setText(safe(al.getIataCode()));
        alCountryLabel.setText(safe(al.getCountry()));
        alFoundedYearLabel.setText(safe(al.getFoundedYear()));
        alActiveLabel.setText(al.getActive() != null && al.getActive() ? "Igen" : "Nem");
        alBusinessModeLabel.setText(safe(al.getBusinessMode()));
        String baseAirportName = "-";
        try {
            if (al.getBaseAirport() != null) {
                baseAirportName = safe(al.getBaseAirport().getName());
            }
        } catch (Exception ex) {
            // ha valamiért mégis proxy/no-session, ne dobjuk el az egész dialógust
            baseAirportName = "-";
        }
        alBaseAirportLabel.setText(baseAirportName);
        alWebsiteLabel.setText(safe(al.getWebsiteUrl()));
        alPhoneLabel.setText(safe(al.getPhoneMain()));
        alEmailLabel.setText(safe(al.getEmailMain()));
        alHqAddressLabel.setText(safe(al.getHeadquartersAddress()));
        alNoteLabel.setText(safe(al.getNote()));
        alCreatedAtLabel.setText(safeDT(al.getCreatedAt()));
        alUpdatedAtLabel.setText(safeDT(al.getUpdatedAt()));
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }

    private String safeDT(Object o) {
        if (o == null) return "";
        // ha már String (pl. Flight scheduledDepartureText), csak visszaadjuk
        if (o instanceof String s) return s;
        if (o instanceof java.time.LocalDateTime dt) return dt.format(dtf);
        return o.toString();
    }
}