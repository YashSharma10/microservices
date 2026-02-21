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
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/eureka/**").authenticated()
                .anyRequest().permitAll()
            .and()
            .httpBasic()
            .and()
            .cors();

        return http.build();
    }

}
