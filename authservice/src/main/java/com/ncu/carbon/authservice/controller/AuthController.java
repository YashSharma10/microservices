package com.ncu.carbon.authservice.controller;

import com.ncu.carbon.authservice.service.UserService;
import com.ncu.carbon.authservice.dto.UserDto;
import com.ncu.carbon.authservice.dto.AuthResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDto body) {
        try {
            if (body == null)
                return ResponseEntity.badRequest().body("missing");
            String username = body.getUsername();
            String password = body.getPassword();
            if (username == null || password == null)
                return ResponseEntity.badRequest().body("missing");
            boolean ok = userService.createUser(username, password);
            if (!ok)
                return ResponseEntity.status(409).body("exists");
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody UserDto body) {
        try {
            if (body == null)
                return ResponseEntity.badRequest().build();
            String username = body.getUsername();
            String password = body.getPassword();
            if (username == null || password == null)
                return ResponseEntity.badRequest().build();
            String token = userService.loginAndCreateToken(username, password);
            if (token == null)
                return ResponseEntity.status(401).build();
            return ResponseEntity.ok(new AuthResponseDto(token));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                               @RequestBody(required = false) java.util.Map<String, String> body) {
        try {
            String username = null;
            String password = null;

            if (authHeader != null && authHeader.toLowerCase().startsWith("basic ")) {
                try {
                    String base64 = authHeader.substring(6).trim();
                    byte[] decoded = java.util.Base64.getDecoder().decode(base64);
                    String cred = new String(decoded);
                    int idx = cred.indexOf(':');
                    if (idx > 0) {
                        username = cred.substring(0, idx);
                        password = cred.substring(idx + 1);
                    }
                } catch (Exception ignored) {}
            }

            if ((username == null || password == null) && body != null) {
                // Support both "email" and "username" fields for API gateway compatibility
                username = body.get("email");
                if (username == null) {
                    username = body.get("username");
                }
                password = body.get("password");
            }

            if (username == null || password == null) return ResponseEntity.badRequest().body("missing");

            boolean ok = userService.validateCredentials(username, password);
            if (ok) return ResponseEntity.ok("ok");
            return ResponseEntity.status(401).body("unauthorized");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}
