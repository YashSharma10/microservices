package com.ncu.carbon.servicediscovery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ApplicationConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception 
    {
        try {
            http
                .csrf().disable()
                .authorizeRequests()
                    .anyRequest().permitAll();

            return http.build();
        } catch (Exception e) {
            throw new RuntimeException("Error configuring security: " + e.getMessage(), e);
        }
    }

}
