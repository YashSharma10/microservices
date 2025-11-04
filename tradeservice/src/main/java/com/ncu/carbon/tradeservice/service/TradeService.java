package com.ncu.carbon.tradeservice.service;

import com.ncu.carbon.tradeservice.dto.TradeDto;
import com.ncu.carbon.tradeservice.model.Trade;
import com.ncu.carbon.tradeservice.repository.TradeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class TradeService {

    private final TradeRepository repo;
    private final RestTemplate rest;
    private final String userServiceBase;

    public TradeService(TradeRepository repo) {
        this.repo = repo;
        this.rest = new RestTemplate();
        this.userServiceBase = "http://localhost:8082/users"; // keep same base as controller used previously
    }

    public List<Trade> listAll() {
        return repo.findAll();
    }

    public Trade get(Long id) {
        return repo.findById(id);
    }

    public Trade trade(TradeDto dto) {
        Long from = dto.getFrom();
        Long to = dto.getTo();
        double amount = dto.getAmount();

        // call user service to remove
        ResponseEntity<?> removeResp = rest.postForEntity(userServiceBase + "/" + from + "/removeCredits?amount=" + amount, null, Object.class);
        if (!removeResp.getStatusCode().is2xxSuccessful()) {
            return null;
        }

        // add to receiver
        ResponseEntity<?> addResp = rest.postForEntity(userServiceBase + "/" + to + "/addCredits?amount=" + amount, null, Object.class);
        if (!addResp.getStatusCode().is2xxSuccessful()) {
            // rollback
            rest.postForEntity(userServiceBase + "/" + from + "/addCredits?amount=" + amount, null, Object.class);
            return null;
        }

        Trade t = new Trade();
        t.setFromUserId(from);
        t.setToUserId(to);
        t.setAmount(amount);
        return repo.save(t);
    }
}
