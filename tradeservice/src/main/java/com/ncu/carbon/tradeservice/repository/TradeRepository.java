package com.ncu.carbon.tradeservice.repository;

import com.ncu.carbon.tradeservice.model.Trade;

import java.util.List;

public interface TradeRepository {
    Trade save(Trade t);
    List<Trade> findAll();
    Trade findById(Long id);
}
