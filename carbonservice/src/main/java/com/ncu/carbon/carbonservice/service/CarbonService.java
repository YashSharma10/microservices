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
        try {
            return repository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Error listing carbon credits: " + e.getMessage(), e);
        }
    }

    public Carbon get(Long id) {
        try {
            return repository.findById(id);
        } catch (Exception e) {
            return null;
        }
    }

    public Carbon create(Carbon c) {
        try {
            return repository.save(c);
        } catch (Exception e) {
            throw new RuntimeException("Error creating carbon credit: " + e.getMessage(), e);
        }
    }

    public boolean update(Carbon c) {
        try {
            return repository.update(c);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean delete(Long id) {
        try {
            return repository.deleteById(id);
        } catch (Exception e) {
            return false;
        }
    }
}
