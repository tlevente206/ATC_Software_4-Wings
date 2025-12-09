package com.FourWings.atcSystem.model.airline;

import com.FourWings.atcSystem.model.airport.Airports;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AirlineService {
    private final AirlineRepository repo;

    @Transactional(readOnly = true)
    public Airline getLastAddedWithAirport() {
        return repo.findTopByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Airline> findByIcao(String icao) {
        return repo.findByIcaoCode(icao);
    }

    @Transactional(readOnly = true)
    public Optional<Airline> findByIata(String iata) {
        return repo.findByIataCode(iata);
    }

    @Transactional(readOnly = true)
    public Optional<Airline> findByIdWithAirport(Long id) {
        return repo.findById(id);   // itt már EntityGraph tölti a baseAirport-ot is
    }

    @Transactional(readOnly = true)
    public List<Airline> findByBaseAirport(Airports airport) {
        if (airport == null || airport.getId() == null) {
            return List.of();
        }
        return repo.findAllByBaseAirport_Id(airport.getId());
    }
}
