package com.ncu.carbon.carbonservice;

import com.ncu.carbon.carbonservice.controller.CarbonController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CarbonControllerTest {

    @Test
    void controllerInstantiation() {
        CarbonController c = new CarbonController();
        Assertions.assertNotNull(c.getSupply());
    }
}
