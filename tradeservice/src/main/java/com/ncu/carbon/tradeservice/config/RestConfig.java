package com.ncu.carbon.tradeservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class RestConfig {

    @Value("${service.auth.username:trade_service_user}")
    private String serviceUsername;

    @Value("${service.auth.password:trade_service_pass}")
    private String servicePassword;

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .interceptors(authForwardingInterceptor())
            .build();
    }

    private ClientHttpRequestInterceptor authForwardingInterceptor() {
        return (request, body, execution) -> {
            // Try to forward the Authorization header from the incoming request
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest httpRequest = attributes.getRequest();
                String authHeader = httpRequest.getHeader("Authorization");
                if (authHeader != null && !authHeader.isEmpty()) {
                    request.getHeaders().add("Authorization", authHeader);
                    // Mark this as a service-to-service request
                    request.getHeaders().add("X-Service-Request", "true");
                    return execution.execute(request, body);
                }
            }
            
            // Fallback to service credentials if no user auth is present
            String auth = serviceUsername + ":" + servicePassword;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            request.getHeaders().add("Authorization", "Basic " + encodedAuth);
            request.getHeaders().add("X-Service-Request", "true");
            return execution.execute(request, body);
        };
    }
}
