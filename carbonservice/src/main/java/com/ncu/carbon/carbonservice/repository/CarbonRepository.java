package com.ncu.carbon.carbonservice.repository;

import com.ncu.carbon.carbonservice.model.Carbon;

import java.util.List;

public interface CarbonRepository {
    List<Carbon> findAll();
    Carbon findById(Long id);
    Carbon save(Carbon carbon);
    boolean update(Carbon carbon);
    boolean deleteById(Long id);
}
