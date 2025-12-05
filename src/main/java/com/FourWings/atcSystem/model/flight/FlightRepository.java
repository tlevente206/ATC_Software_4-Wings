package com.FourWings.atcSystem.model.flight;

import com.FourWings.atcSystem.model.airport.Airports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    Flight findTopByOrderByIdDesc();

    // Induló járatok (ahol a repülőtér az indulási repteret jelenti)
    List<Flight> findByDepartureAirport(Airports airport);

    // Érkező járatok (ahol a repülőtér az érkezési repteret jelenti)
    List<Flight> findByArrivalAirport(Airports airport);
}