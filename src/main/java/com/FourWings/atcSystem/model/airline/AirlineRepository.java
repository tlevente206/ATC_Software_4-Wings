package com.FourWings.atcSystem.model.airline;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
    @EntityGraph(attributePaths = "baseAirport")
    Optional<Airline> findByIcaoCode(String icaoCode);

    @EntityGraph(attributePaths = "baseAirport")
    Optional<Airline> findByIataCode(String iataCode);

    @EntityGraph(attributePaths = "baseAirport")
    Airline findTopByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = "baseAirport")
    Optional<Airline> findById(Long id);
}
