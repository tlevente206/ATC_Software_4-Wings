package com.FourWings.atcSystem.model.gate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GateRepository extends JpaRepository<Gate,Long> {
    Optional<Gate> findByCode(String code);
    Gate findTopByOrderByIdDesc();
    @Query("""
      select g from Gate g
      join fetch g.terminal t
      where g.id = :id
    """)
    Gate findByIdFetchTerminal(@Param("id") Long id);
}
