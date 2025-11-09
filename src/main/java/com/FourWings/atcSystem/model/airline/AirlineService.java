package com.FourWings.atcSystem.model.airline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AirlineService {
    private final AirlineRepository repo;

    @Transactional(readOnly = true)
    public Airline getLastAddedWithAirport() {
        return repo.findTopByOrderByIdDesc();
    }

    public Optional<Airline> findByIcao(String icao) { return repo.findByIcaoCode(icao); }

    public Optional<Airline> findByIata(String iata) { return repo.findByIataCode(iata); }
}
