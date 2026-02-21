package com.ncu.carbon.carbonservice.service;

import com.ncu.carbon.carbonservice.model.Carbon;
import com.ncu.carbon.carbonservice.repository.CarbonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarbonService {

    private final CarbonRepository repository;

    public CarbonService(CarbonRepository repository) {
        this.repository = repository;
    }

    public List<Carbon> listAll() {
        return repository.findAll();
    }

    public Carbon get(Long id) {
        return repository.findById(id);
    }

    public Carbon create(Carbon c) {
        return repository.save(c);
    }

    public boolean update(Carbon c) {
        return repository.update(c);
    }

    public boolean delete(Long id) {
        return repository.deleteById(id);
    }
}
