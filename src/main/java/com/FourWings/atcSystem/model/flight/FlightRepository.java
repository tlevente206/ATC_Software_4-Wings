package com.FourWings.atcSystem.model.flight;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight,Long> {
    @EntityGraph(attributePaths = {"departureAirport", "arrivalAirport", "airline", "aircraft", "gate"})
    Flight findTopByOrderByIdDesc();
}
