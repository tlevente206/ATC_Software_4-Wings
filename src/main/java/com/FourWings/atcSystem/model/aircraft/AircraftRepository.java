package com.FourWings.atcSystem.model.aircraft;

import com.FourWings.atcSystem.model.airport.Airports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    Aircraft findTopByOrderByIdDesc();

    List<Aircraft> findByBaseAirportId(Long baseAirportId);   // ✅ EZ KELL
}
