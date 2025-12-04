package com.FourWings.atcSystem.model.aircraft;

import com.FourWings.atcSystem.model.airport.Airports;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}