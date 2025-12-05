package com.FourWings.atcSystem.model.flight;

import com.FourWings.atcSystem.model.airport.Airports;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository repo;

    /**
     * Minden olyan lazy kapcsolatot "megbökünk", amire a UI-ban szükség lesz,
     * még a tranzakciós kontextuson BELÜL.
     */
    private void initializeForUi(Flight f) {
        if (f == null) return;

        // Induló reptér
        try {
            if (f.getDepartureAirport() != null) {
                f.getDepartureAirport().getName();
                f.getDepartureAirport().getIcaoCode();
            }
        } catch (Exception ignored) {}

        // Érkező reptér
        try {
            if (f.getArrivalAirport() != null) {
                f.getArrivalAirport().getName();
                f.getArrivalAirport().getIcaoCode();
            }
        } catch (Exception ignored) {}

        // Légitársaság
        try {
            if (f.getAirline() != null) {
                f.getAirline().getName();
            }
        } catch (Exception ignored) {}

        // Kapu
        try {
            if (f.getGate() != null) {
                f.getGate().getCode();
            }
        } catch (Exception ignored) {}

        // Ha valaha kell majd aircraft is a UI-ban,
        // ide tudod betenni, pl.:
        // try {
        //     if (f.getAircraft() != null) {
        //         f.getAircraft().getRegistration();
        //     }
        // } catch (Exception ignored) {}
    }

    @Transactional(readOnly = true)
    public Flight getLastAdded() {
        Flight f = repo.findTopByOrderByIdDesc();
        initializeForUi(f);
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
        flights.forEach(this::initializeForUi);
        return flights;
    }

    /**
     * Érkező járatok lekérése egy adott repülőtérre.
     */
    @Transactional(readOnly = true)
    public List<Flight> getArrivalsForAirport(Airports airport) {
        if (airport == null) return List.of();
        List<Flight> flights = repo.findByArrivalAirport(airport);
        flights.forEach(this::initializeForUi);
        return flights;
    }

    // ---------------------------------------------
    // ADMIN / USER FUNKCIÓK
    // ---------------------------------------------

    @Transactional(readOnly = true)
    public List<Flight> getAll() {
        List<Flight> flights = repo.findAll();
        flights.forEach(this::initializeForUi);
        return flights;
    }

    @Transactional
    public Flight save(Flight flight) {
        Flight saved = repo.save(flight);
        initializeForUi(saved);
        return saved;
    }

    @Transactional
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}