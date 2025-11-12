package com.FourWings.atcSystem.model.terminal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TerminalService {
    private final TerminalRepository repo;

    @Transactional(readOnly = true)
    public Terminal getLastAdded() {
        return repo.findTopByOrderByIdDesc();
    }
}