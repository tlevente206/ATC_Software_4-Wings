package com.FourWings.atcSystem.model.flight;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository repo;

    @Transactional(readOnly = true)
    public Flight getLastAdded() {
        return repo.findTopByOrderByIdDesc();
    }
}
