package com.FourWings.atcSystem.model.gate;

import com.FourWings.atcSystem.model.airport.Airports;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GateService {
    private final GateRepository repo;

    @Transactional(readOnly = true)
    public Gate getGateWithTerminal(Long id) {
        return repo.findByIdFetchTerminal(id);
    }

    @Transactional(readOnly = true)
    public Gate getLastAdded() {
        Gate last = repo.findTopByOrderByIdDesc();
        if (last != null) {
            return repo.findByIdFetchTerminal(last.getId());
        }
        return null;
    }
    @Transactional(readOnly = true)
    public List<Gate> getGatesForAirport(Airports airport) {
        return repo.findByAirport(airport);
    }
}