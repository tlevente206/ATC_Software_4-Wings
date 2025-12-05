package com.FourWings.atcSystem.model.gate;

import com.FourWings.atcSystem.model.airport.Airports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("SELECT g FROM Gate g " +
            "JOIN g.terminal t " +
            "WHERE t.airport = :airport")
    List<Gate> findByAirport(@Param("airport") Airports airport);
}

