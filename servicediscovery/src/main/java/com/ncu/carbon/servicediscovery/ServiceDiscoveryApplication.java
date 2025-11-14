package com.ncu.carbon.servicediscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceDiscoveryApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(ServiceDiscoveryApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
