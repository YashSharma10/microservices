package com.ncu.carbon.tradeservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class RestConfig {

    @Value("${service.auth.username:trade_service_user}")
    private String serviceUsername;

    @Value("${service.auth.password:trade_service_pass}")
    private String servicePassword;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .interceptors(basicAuthInterceptor())
            .build();
    }

    private ClientHttpRequestInterceptor basicAuthInterceptor() {
        return (request, body, execution) -> {
            String auth = serviceUsername + ":" + servicePassword;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            request.getHeaders().add("Authorization", "Basic " + encodedAuth);
            // Mark this as a service-to-service request
            request.getHeaders().add("X-Service-Request", "true");
            return execution.execute(request, body);
        };
    }
}
