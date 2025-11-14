package com.ncu.carbon.carbonservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CarbonServiceApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(CarbonServiceApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
