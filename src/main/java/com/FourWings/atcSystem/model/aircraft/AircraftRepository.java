package com.FourWings.atcSystem.model.aircraft;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    Aircraft findTopByOrderByIdDesc();
}
