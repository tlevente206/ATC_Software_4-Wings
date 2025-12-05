package com.FourWings.atcSystem.model.flight;

import com.FourWings.atcSystem.model.airport.Airports;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository repo;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    @Transactional(readOnly = true)
    public Flight getLastAdded() {
        Flight f = repo.findTopByOrderByIdDesc();
        if (f != null) {
            prepareFlightForView(f);
        }
        return f;
    }

    // ---------------------------------------------
    // CONTROLLER HOME PAGE FUNKCIÓK
    // ---------------------------------------------

    /**
     * Induló járatok lekérése egy adott repülőtérről.
     */
    @Transactional(readOnly = true)
    public List<Flight> getDeparturesForAirport(Airports airport) {
        if (airport == null) return List.of();
        List<Flight> flights = repo.findByDepartureAirport(airport);
        flights.forEach(this::prepareFlightForView);
        return flights;
    }

    /**
     * Érkező járatok lekérése egy adott repülőtérre.
     */
    @Transactional(readOnly = true)
    public List<Flight> getArrivalsForAirport(Airports airport) {
        if (airport == null) return List.of();
        List<Flight> flights = repo.findByArrivalAirport(airport);
        flights.forEach(this::prepareFlightForView);
        return flights;
    }

    // ---------------------------------------------
    // ADMIN / USER FUNKCIÓK (opcionális)
    // ---------------------------------------------

    @Transactional(readOnly = true)
    public List<Flight> getAll() {
        List<Flight> flights = repo.findAll();
        flights.forEach(this::prepareFlightForView);
        return flights;
    }

    @Transactional
    public Flight save(Flight flight) {
        Flight saved = repo.save(flight);
        prepareFlightForView(saved);
        return saved;
    }

    @Transactional
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    // ---------------------------------------------
    // Segédfüggvény: lazy kapcsolatok inicializálása
    // és UI-hoz szükséges mezők beállítása
    // ---------------------------------------------

    private void prepareFlightForView(Flight f) {
        // --- Airline neve ---
        if (f.getAirline() != null) {
            f.setAirlineName(
                    f.getAirline().getName() != null ? f.getAirline().getName() : ""
            );
        } else {
            f.setAirlineName("");
        }

        // --- Induló / érkező reptér nevek ---
        if (f.getDepartureAirport() != null) {
            f.setOriginName(
                    f.getDepartureAirport().getName() != null ? f.getDepartureAirport().getName() : ""
            );
        } else {
            f.setOriginName("");
        }

        if (f.getArrivalAirport() != null) {
            f.setDestinationName(
                    f.getArrivalAirport().getName() != null ? f.getArrivalAirport().getName() : ""
            );
        } else {
            f.setDestinationName("");
        }

        // --- Időpontok formázása ---
        f.setScheduledDepartureText(
                f.getScheduledDeparture() != null ? f.getScheduledDeparture().format(FORMATTER) : ""
        );
        f.setEstimatedDepartureText(
                f.getEstimatedDeparture() != null ? f.getEstimatedDeparture().format(FORMATTER) : ""
        );
        f.setScheduledArrivalText(
                f.getScheduledArrival() != null ? f.getScheduledArrival().format(FORMATTER) : ""
        );
        f.setEstimatedArrivalText(
                f.getEstimatedArrival() != null ? f.getEstimatedArrival().format(FORMATTER) : ""
        );

        // --- Kapukód ---
        if (f.getGate() != null && f.getGate().getCode() != null) {
            f.setGateCode(f.getGate().getCode());
        } else {
            f.setGateCode("");
        }

        // --- Státusz szöveg ---
        if (f.getStatus() != null) {
            // egyszerűen az enum neve – ha akarsz, írhatsz egy szebb map-et
            f.setStatusText(f.getStatus().name());
        } else {
            f.setStatusText("");
        }

        // --- Repülőgép adatok ---
        if (f.getAircraft() != null) {
            f.setAircraftRegistration(
                    f.getAircraft().getRegistration() != null ? f.getAircraft().getRegistration() : ""
            );
            f.setAircraftTypeIcao(
                    f.getAircraft().getTypeIcao() != null ? f.getAircraft().getTypeIcao() : ""
            );
            f.setAircraftMaxSeatCapacity(f.getAircraft().getMaxSeatCapacity());
            f.setAircraftManufactureYear(f.getAircraft().getManufactureYear());

            if (f.getAircraft().getStatus() != null) {
                f.setAircraftStatusText(f.getAircraft().getStatus().name());
            } else {
                f.setAircraftStatusText("");
            }
        } else {
            f.setAircraftRegistration("");
            f.setAircraftTypeIcao("");
            f.setAircraftMaxSeatCapacity(null);
            f.setAircraftManufactureYear(null);
            f.setAircraftStatusText("");
        }
    }
}