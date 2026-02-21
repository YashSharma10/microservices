package com.ncu.carbon.apigateway.config;


import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthHeaderFactory {

    @Value("${carbonservice.auth.username}")
    String carbonUsername;
    @Value("${carbonservice.auth.password}")
    String carbonPassword;

    @Value("${tradeservice.auth.username}")
    String tradeUsername;
    @Value("${tradeservice.auth.password}")
    String tradePassword;

    @Value("${userservice.auth.username}")
    String userUsername;
    @Value("${userservice.auth.password}")
    String userPassword;

    @Value("${apigateway.shared.secret}")
    String sharedSecret;
    
    String buildAuthHeader(String serviceName)
    {
        String username = "";
        String password = "";

        if("carbonservice".equals(serviceName))
        {
            username = carbonUsername; 
            password = carbonPassword;
        }
        else if("tradeservice".equals(serviceName))
        {
            username = tradeUsername; 
            password = tradePassword;            
        }
        else if("userservice".equals(serviceName))
        {
            username = userUsername; 
            password = userPassword;
        }

        String auth = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    String getSharedSecret()
    {
        return sharedSecret;
    }
}
