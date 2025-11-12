package com.FourWings.atcSystem.model.gate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GateService {
    private final GateRepository repo;

    @Transactional(readOnly = true)
    public Gate getLastAdded() { return repo.findTopByOrderByIdDesc(); }
}
