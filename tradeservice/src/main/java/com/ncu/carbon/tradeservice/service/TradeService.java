package com.ncu.carbon.tradeservice.service;

import com.ncu.carbon.tradeservice.dto.TradeDto;
import com.ncu.carbon.tradeservice.dto.CarbonTradeDto;
import com.ncu.carbon.tradeservice.model.Trade;
import com.ncu.carbon.tradeservice.repository.TradeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class TradeService {

    private final TradeRepository repo;
    private final RestTemplate rest;
    private final String userServiceBase;
    private final String carbonServiceBase;

    public TradeService(TradeRepository repo, org.springframework.web.client.RestTemplate rest,
                        org.springframework.beans.factory.annotation.Value("${users.service.base:http://localhost:8082/users}") String userServiceBase,
                        org.springframework.beans.factory.annotation.Value("${carbon.service.base:http://localhost:8083/carbon}") String carbonServiceBase) {
        this.repo = repo;
        this.rest = rest == null ? new RestTemplate() : rest;
        this.userServiceBase = userServiceBase;
        this.carbonServiceBase = carbonServiceBase;
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

    public Trade tradeCarbonCredit(CarbonTradeDto dto) {
        Long carbonId = dto.getCarbonId();
        Long buyerId = dto.getBuyerId();
        double quantity = dto.getQuantity();

        // Get carbon credit details
        ResponseEntity<Map> carbonResp = rest.getForEntity(carbonServiceBase + "/" + carbonId, Map.class);
        if (!carbonResp.getStatusCode().is2xxSuccessful() || carbonResp.getBody() == null) {
            return null;
        }

        Map<String, Object> carbonData = carbonResp.getBody();
        Long ownerId = ((Number) carbonData.get("ownerId")).longValue();
        Double price = ((Number) carbonData.get("price")).doubleValue();
        Double supply = ((Number) carbonData.get("supply")).doubleValue();

        // Check if supply is sufficient
        if (supply < quantity) {
            return null;
        }

        double totalCost = price * quantity;

        // Remove balance from buyer
        ResponseEntity<?> removeBalanceResp = rest.postForEntity(
            userServiceBase + "/" + buyerId + "/removeBalance?amount=" + totalCost, null, Object.class);
        if (!removeBalanceResp.getStatusCode().is2xxSuccessful()) {
            return null;
        }

        // Add balance to seller (owner)
        ResponseEntity<?> addBalanceResp = rest.postForEntity(
            userServiceBase + "/" + ownerId + "/addBalance?amount=" + totalCost, null, Object.class);
        if (!addBalanceResp.getStatusCode().is2xxSuccessful()) {
            // rollback - add balance back to buyer
            rest.postForEntity(userServiceBase + "/" + buyerId + "/addBalance?amount=" + totalCost, null, Object.class);
            return null;
        }

        // Remove credits from seller
        ResponseEntity<?> removeCreditsResp = rest.postForEntity(
            userServiceBase + "/" + ownerId + "/removeCredits?amount=" + quantity, null, Object.class);
        if (!removeCreditsResp.getStatusCode().is2xxSuccessful()) {
            // rollback both balance operations
            rest.postForEntity(userServiceBase + "/" + buyerId + "/addBalance?amount=" + totalCost, null, Object.class);
            rest.postForEntity(userServiceBase + "/" + ownerId + "/removeBalance?amount=" + totalCost, null, Object.class);
            return null;
        }

        // Add credits to buyer
        ResponseEntity<?> addCreditsResp = rest.postForEntity(
            userServiceBase + "/" + buyerId + "/addCredits?amount=" + quantity, null, Object.class);
        if (!addCreditsResp.getStatusCode().is2xxSuccessful()) {
            // rollback all operations
            rest.postForEntity(userServiceBase + "/" + buyerId + "/addBalance?amount=" + totalCost, null, Object.class);
            rest.postForEntity(userServiceBase + "/" + ownerId + "/removeBalance?amount=" + totalCost, null, Object.class);
            rest.postForEntity(userServiceBase + "/" + ownerId + "/addCredits?amount=" + quantity, null, Object.class);
            return null;
        }

        // Update carbon supply (reduce by quantity)
        // Note: This is a simplified approach - in production, you might want to track individual listings
        Map<String, Object> updateData = Map.of(
            "id", carbonId,
            "name", carbonData.get("name"),
            "supply", supply - quantity,
            "ownerId", ownerId,
            "price", price
        );
        rest.put(carbonServiceBase + "/" + carbonId, updateData);

        // Create trade record
        Trade t = new Trade();
        t.setFromUserId(ownerId);
        t.setToUserId(buyerId);
        t.setAmount(quantity);
        return repo.save(t);
    }
}
