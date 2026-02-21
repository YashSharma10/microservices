package com.ncu.carbon.tradeservice.service;

import com.ncu.carbon.tradeservice.dto.TradeDto;
import com.ncu.carbon.tradeservice.dto.CarbonTradeDto;
import com.ncu.carbon.tradeservice.model.Trade;
import com.ncu.carbon.tradeservice.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    private final TradeRepository repo;
    private final RestTemplate rest;
    private final String userServiceBase;
    private final String carbonServiceBase;
    
    public TradeService(TradeRepository repo, RestTemplate rest,
                        @Value("${users.service.base}") String userServiceBase,
                        @Value("${carbon.service.base}") String carbonServiceBase) {
        this.repo = repo;
        this.rest = rest == null ? new RestTemplate() : rest;
        this.userServiceBase = userServiceBase;
        this.carbonServiceBase = carbonServiceBase;
        log.info("TradeService initialized - UserService: {}, CarbonService: {}", userServiceBase, carbonServiceBase);
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

        log.info("Processing trade: from={}, to={}, amount={}", from, to, amount);

        // Call user service via API Gateway to remove credits
        String removeUrl = userServiceBase + "/" + from + "/removeCredits?amount=" + amount;
        log.debug("Calling: {}", removeUrl);
        ResponseEntity<?> removeResp = rest.postForEntity(removeUrl, null, Object.class);
        if (!removeResp.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to remove credits from user {}", from);
            return null;
        }

        // Add credits to receiver via API Gateway
        String addUrl = userServiceBase + "/" + to + "/addCredits?amount=" + amount;
        log.debug("Calling: {}", addUrl);
        ResponseEntity<?> addResp = rest.postForEntity(addUrl, null, Object.class);
        if (!addResp.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to add credits to user {}, rolling back", to);
            // Rollback: add credits back to sender
            rest.postForEntity(userServiceBase + "/" + from + "/addCredits?amount=" + amount, null, Object.class);
            return null;
        }

        Trade t = new Trade();
        t.setFromUserId(from);
        t.setToUserId(to);
        t.setAmount(amount);
        Trade saved = repo.save(t);
        log.info("Trade completed successfully: {}", saved.getId());
        return saved;
    }

    public Trade tradeCarbonCredit(CarbonTradeDto dto) {
        Long carbonId = dto.getCarbonId();
        Long buyerId = dto.getBuyerId();
        double quantity = dto.getQuantity();

        log.info("Processing carbon credit trade: carbonId={}, buyerId={}, quantity={}", carbonId, buyerId, quantity);

        // Get carbon credit details via API Gateway
        String getCarbonUrl = carbonServiceBase + "/" + carbonId;
        log.debug("Calling: {}", getCarbonUrl);
        ResponseEntity<Map> carbonResp = rest.getForEntity(getCarbonUrl, Map.class);
        if (!carbonResp.getStatusCode().is2xxSuccessful() || carbonResp.getBody() == null) {
            log.error("Failed to fetch carbon credit details for id={}", carbonId);
            return null;
        }

        Map<String, Object> carbonData = carbonResp.getBody();
        Long ownerId = ((Number) carbonData.get("ownerId")).longValue();
        Double price = ((Number) carbonData.get("price")).doubleValue();
        Double supply = ((Number) carbonData.get("supply")).doubleValue();

        log.debug("Carbon details - ownerId={}, price={}, supply={}", ownerId, price, supply);

        // Check if supply is sufficient
        if (supply < quantity) {
            log.error("Insufficient supply: available={}, requested={}", supply, quantity);
            return null;
        }

        double totalCost = price * quantity;
        log.debug("Total cost calculated: {}", totalCost);

        // Remove balance from buyer via API Gateway
        String removeBuyerBalanceUrl = userServiceBase + "/" + buyerId + "/removeBalance?amount=" + totalCost;
        log.debug("Calling: {}", removeBuyerBalanceUrl);
        ResponseEntity<?> removeBalanceResp = rest.postForEntity(removeBuyerBalanceUrl, null, Object.class);
        if (!removeBalanceResp.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to remove balance from buyer {}", buyerId);
            return null;
        }

        // Add balance to seller (owner) via API Gateway
        String addSellerBalanceUrl = userServiceBase + "/" + ownerId + "/addBalance?amount=" + totalCost;
        log.debug("Calling: {}", addSellerBalanceUrl);
        ResponseEntity<?> addBalanceResp = rest.postForEntity(addSellerBalanceUrl, null, Object.class);
        if (!addBalanceResp.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to add balance to seller {}, rolling back", ownerId);
            // Rollback - add balance back to buyer
            rest.postForEntity(userServiceBase + "/" + buyerId + "/addBalance?amount=" + totalCost, null, Object.class);
            return null;
        }

        // Remove credits from seller via API Gateway
        String removeSellerCreditsUrl = userServiceBase + "/" + ownerId + "/removeCredits?amount=" + quantity;
        log.debug("Calling: {}", removeSellerCreditsUrl);
        ResponseEntity<?> removeCreditsResp = rest.postForEntity(removeSellerCreditsUrl, null, Object.class);
        if (!removeCreditsResp.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to remove credits from seller {}, rolling back", ownerId);
            // Rollback both balance operations
            rest.postForEntity(userServiceBase + "/" + buyerId + "/addBalance?amount=" + totalCost, null, Object.class);
            rest.postForEntity(userServiceBase + "/" + ownerId + "/removeBalance?amount=" + totalCost, null, Object.class);
            return null;
        }

        // Add credits to buyer via API Gateway
        String addBuyerCreditsUrl = userServiceBase + "/" + buyerId + "/addCredits?amount=" + quantity;
        log.debug("Calling: {}", addBuyerCreditsUrl);
        ResponseEntity<?> addCreditsResp = rest.postForEntity(addBuyerCreditsUrl, null, Object.class);
        if (!addCreditsResp.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to add credits to buyer {}, rolling back all operations", buyerId);
            // Rollback all operations
            rest.postForEntity(userServiceBase + "/" + buyerId + "/addBalance?amount=" + totalCost, null, Object.class);
            rest.postForEntity(userServiceBase + "/" + ownerId + "/removeBalance?amount=" + totalCost, null, Object.class);
            rest.postForEntity(userServiceBase + "/" + ownerId + "/addCredits?amount=" + quantity, null, Object.class);
            return null;
        }

        // Update carbon supply (reduce by quantity) via API Gateway
        Map<String, Object> updateData = Map.of(
            "id", carbonId,
            "name", carbonData.get("name"),
            "supply", supply - quantity,
            "ownerId", ownerId,
            "price", price
        );
        String updateCarbonUrl = carbonServiceBase + "/" + carbonId;
        log.debug("Calling: {} with updated supply={}", updateCarbonUrl, supply - quantity);
        rest.put(updateCarbonUrl, updateData);

        // Create trade record
        Trade t = new Trade();
        t.setFromUserId(ownerId);
        t.setToUserId(buyerId);
        t.setAmount(quantity);
        Trade saved = repo.save(t);
        log.info("Carbon credit trade completed successfully: {}", saved.getId());
        return saved;
    }
}
