package com.FourWings.atcSystem.model.gate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GateRepository extends JpaRepository<Gate,Long> {
    Optional<Gate> findByCode(String code);
    Gate findTopByOrderByIdDesc();
}
