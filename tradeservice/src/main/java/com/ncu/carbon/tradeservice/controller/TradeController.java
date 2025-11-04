package com.ncu.carbon.tradeservice.controller;

import com.ncu.carbon.tradeservice.dto.TradeDto;
import com.ncu.carbon.tradeservice.model.Trade;
import com.ncu.carbon.tradeservice.service.TradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trade")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<?> trade(@RequestBody TradeDto dto) {
        Trade created = tradeService.trade(dto);
        if (created == null) return ResponseEntity.badRequest().body("Trade failed");
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public List<Trade> list() {
        return tradeService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trade> get(@PathVariable Long id) {
        Trade t = tradeService.get(id);
        if (t == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(t);
    }
}
