package com.FourWings.atcSystem.model.aircraft;

import com.FourWings.atcSystem.model.airport.Airports;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AircraftService {
    private final AircraftRepository repo;

    public Aircraft getLastAdded() {
        return repo.findTopByOrderByIdDesc();
    }

    public List<Aircraft> getAllAircraft() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Aircraft> findAvailableForAirport(Airports airport) {
        if (airport == null || airport.getId() == null) {
            return List.of();
        }

        // egyszerű verzió: minden gép, ami ezen a reptéren "base"
        return repo.findByBaseAirportId(airport.getId());   // ✅ ID alapján kérdezünk
    }
}