package com.FourWings.atcSystem.model.airline;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
    Optional<Airline> findByIcaoCode(String icaoCode);
    Optional<Airline> findByIataCode(String iataCode);
    @EntityGraph(attributePaths = "baseAirport")
    Airline findTopByOrderByIdDesc();
}
