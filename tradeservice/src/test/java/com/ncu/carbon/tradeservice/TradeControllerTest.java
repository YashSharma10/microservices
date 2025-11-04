package com.ncu.carbon.tradeservice;

import com.ncu.carbon.tradeservice.controller.TradeController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

class TradeControllerTest {

    @Test
    void noopTradeControllerInstantiation() {
        TradeController ctrl = new TradeController();
        Assertions.assertNotNull(ctrl);
    }

    @Test
    void tradeBadInputHandled() {
        TradeController ctrl = new TradeController();
        Map<String, Object> req = new HashMap<>();
        req.put("from", 1);
        req.put("to", 2);
        req.put("amount", 5);

        // This will attempt to call local userservice endpoints; ensure method runs and returns a response entity
        ResponseEntity<String> resp = ctrl.trade(req);
        Assertions.assertNotNull(resp);
    }
}
