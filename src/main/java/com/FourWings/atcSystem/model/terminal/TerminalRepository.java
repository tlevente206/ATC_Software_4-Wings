package com.FourWings.atcSystem.model.terminal;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TerminalRepository extends JpaRepository<Terminal, Long> {

    @EntityGraph(attributePaths = "airport")
    Terminal findTopByOrderByIdDesc();

    Optional<Terminal> findByAirport_IdAndCode(Long airportId, String code);
}