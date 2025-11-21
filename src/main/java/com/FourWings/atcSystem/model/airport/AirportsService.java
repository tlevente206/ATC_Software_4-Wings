package com.FourWings.atcSystem.model.airport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AirportsService {

    private final AirportsRepository repo;

    public Airports getLastAdded() {
        return repo.findTopByOrderByIdDesc();
    }

    public List<Airports> getAllAirports() {
        return repo.findAll();
    }
}