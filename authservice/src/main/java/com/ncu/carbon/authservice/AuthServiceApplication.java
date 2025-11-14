package com.ncu.carbon.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(AuthServiceApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
