package com.ncu.carbon.tradeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TradeServiceApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(TradeServiceApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
