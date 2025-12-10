package com.FourWings.atcSystem.model.terminal;

import com.FourWings.atcSystem.model.airport.Airports;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TerminalService {
    private final TerminalRepository repo;

    @Transactional(readOnly = true)
    public Terminal getLastAdded() {
        return repo.findTopByOrderByIdDesc();
    }


    @Transactional(readOnly = true)
    public List<Terminal> findForAirport(Airports airport) {
        if (airport == null || airport.getId() == null) {
            return List.of();
        }
        return repo.findByAirport_Id(airport.getId());
    }
}