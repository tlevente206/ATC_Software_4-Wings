package com.FourWings.atcSystem.model.flight;

import com.FourWings.atcSystem.model.airport.Airports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // ÚJ IMPORT
import org.springframework.data.repository.query.Param; // ÚJ IMPORT
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    // --- EREDETI METÓDUSAID (Változatlanul) ---
    Flight findTopByOrderByIdDesc();

    // Induló járatok (ahol a repülőtér az indulási repteret jelenti)
    List<Flight> findByDepartureAirport(Airports airport);

    // Érkező járatok (ahol a repülőtér az érkezési repteret jelenti)
    List<Flight> findByArrivalAirport(Airports airport);

    @Query("SELECT f FROM Flight f " +
            "LEFT JOIN FETCH f.airline " +
            "LEFT JOIN FETCH f.arrivalAirport " +
            "LEFT JOIN FETCH f.gate " +
            "LEFT JOIN FETCH f.aircraft " +
            "WHERE f.departureAirport = :airport")
    List<Flight> findDeparturesByAirportWithDetails(@Param("airport") Airports airport);

    @Query("SELECT f FROM Flight f " +
            "LEFT JOIN FETCH f.airline " +
            "LEFT JOIN FETCH f.departureAirport " +
            "LEFT JOIN FETCH f.gate " +
            "LEFT JOIN FETCH f.aircraft " +
            "WHERE f.arrivalAirport = :airport")
    List<Flight> findArrivalsByAirportWithDetails(@Param("airport") Airports airport);
}