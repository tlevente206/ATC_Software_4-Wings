package com.FourWings.atcSystem.model.airport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AirportsService {
    private final AirportsRepository repo;

    public Airports getLastAdded() {
        return repo.findTopByOrderByAirportIdDesc();
    }
}
