package com.ncu.carbon.authservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.testcontainers.junit.jupiter.Testcontainers
class AuthControllerTest {

    @org.testcontainers.junit.jupiter.Container
    static org.testcontainers.containers.MySQLContainer<?> mysql = new org.testcontainers.containers.MySQLContainer<>("mysql:8.0.34")
            .withDatabaseName("testdb").withUsername("test").withPassword("test");

    @org.springframework.beans.factory.annotation.Autowired
    private TestRestTemplate rest;

    @org.junit.jupiter.api.BeforeAll
    static void beforeAll() {
        // dynamic properties set via DynamicPropertySource below
    }

    @org.junit.jupiter.api.Test
    void signupAndLoginFlow() {
        Map<String, String> body = new HashMap<>();
        body.put("username", "testuser");
        body.put("password", "secret");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> signup = rest.postForEntity("/auth/signup", req, String.class);
        Assertions.assertEquals(HttpStatus.OK, signup.getStatusCode());

        ResponseEntity<com.ncu.carbon.authservice.dto.AuthResponseDto> login = rest.postForEntity("/auth/login", req, com.ncu.carbon.authservice.dto.AuthResponseDto.class);
        Assertions.assertEquals(HttpStatus.OK, login.getStatusCode());
        Assertions.assertNotNull(login.getBody());
        Assertions.assertTrue(login.getBody().getToken().length() > 10);
    }

    @org.springframework.test.context.DynamicPropertySource
    static void mysqlProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mysql.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mysql.getUsername());
        registry.add("spring.datasource.password", () -> mysql.getPassword());
    }
}
