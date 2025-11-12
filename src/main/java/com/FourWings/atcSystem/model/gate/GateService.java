package com.FourWings.atcSystem.model.gate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}