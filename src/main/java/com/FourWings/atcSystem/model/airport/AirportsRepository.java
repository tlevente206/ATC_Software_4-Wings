package com.FourWings.atcSystem.model.airport;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportsRepository extends JpaRepository<Airports, Long> {
    Airports findTopByOrderByAirportIdDesc();
}
