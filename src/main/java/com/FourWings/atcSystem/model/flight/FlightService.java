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

    @Transactional(readOnly = true)
    public Flight getLastAdded() {
        return repo.findTopByOrderByIdDesc();
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
        return repo.findByDepartureAirport(airport);
    }

    /**
     * Érkező járatok lekérése egy adott repülőtérre.
     */
    @Transactional(readOnly = true)
    public List<Flight> getArrivalsForAirport(Airports airport) {
        if (airport == null) return List.of();
        return repo.findByArrivalAirport(airport);
    }

    // ---------------------------------------------
    // ADMIN / USER FUNKCIÓK (opcionális)
    // ---------------------------------------------

    @Transactional(readOnly = true)
    public List<Flight> getAll() {
        return repo.findAll();
    }

    @Transactional
    public Flight save(Flight flight) {
        return repo.save(flight);
    }

    @Transactional
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}