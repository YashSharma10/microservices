package com.ncu.carbon.apigateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableWebFluxSecurity
public class GatewayConfig {

    GatewayConfig() {}

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        try {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange
                    .pathMatchers("/auth/**").permitAll()
                    .pathMatchers("/actuator/**").permitAll()
                    .anyExchange().permitAll()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable);
            return http.build();
        } catch (Exception e) {
            throw new RuntimeException("Error configuring gateway security: " + e.getMessage(), e);
        }
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder)
    {
        try {
            return builder.routes()
                .route("carbonservice", r -> r.path("/carbon/**")
                    .uri("lb://carbonservice"))

                .route("tradeservice", r -> r.path("/trades/**")
                    .uri("lb://tradeservice"))

                .route("userservice", r -> r.path("/users/**")
                    .uri("lb://userservice"))

                .route("authservice", r -> r.path("/auth/**")
                    .uri("lb://authservice"))
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Error configuring routes: " + e.getMessage(), e);
        }
    }
        
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
