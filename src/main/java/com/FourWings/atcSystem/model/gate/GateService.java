package com.FourWings.atcSystem.model.gate;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.terminal.Terminal;
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

    @Transactional(readOnly = true)
    public List<Gate> getGatesForTerminal(Terminal terminal) {
        if (terminal == null) {
            return List.of();
        }
        return repo.findByTerminal(terminal);
    }

    @Transactional(readOnly = true)
    public long countFreeGatesForAirport(Airports airport) {
        if (airport == null) {
            return 0L;
        }
        // Itt azt az enum értéket használd, ami NÁLAD a "szabad" vagy "elérhető" kaput jelenti:
        return repo.countByAirportAndStatus(airport, GateStatus.ACTIVE);
        // Ha nálad pl. FREE, OPEN stb. a neve, akkor azt írd:
        // return repo.countByAirportAndStatus(airport, GateStatus.FREE);
    }
}