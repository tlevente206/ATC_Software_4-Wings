package com.FourWings.atcSystem.model.aircraft;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AircraftService {
    private final AircraftRepository repo;

    public Aircraft getLastAdded() {
        return repo.findTopByOrderByIdDesc();
    }
}