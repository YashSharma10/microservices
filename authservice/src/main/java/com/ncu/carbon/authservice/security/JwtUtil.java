package com.ncu.carbon.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        try {
            // In production use a stronger/rotated key source
            key = Keys.hmacShaKeyFor(secret.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Error initializing JWT key: " + e.getMessage(), e);
        }
    }

    public String generateToken(String username) {
        try {
            Date now = new Date();
            Date exp = new Date(now.getTime() + expirationMs);
            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(exp)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT token: " + e.getMessage(), e);
        }
    }

    public String validateAndGetUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Error validating JWT token: " + e.getMessage(), e);
        }
    }
}
