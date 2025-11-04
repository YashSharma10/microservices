package com.ncu.carbon.authservice.controller;

import com.ncu.carbon.authservice.service.UserService;
import com.ncu.carbon.authservice.dto.UserDto;
import com.ncu.carbon.authservice.dto.AuthResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody UserDto body) {
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
    }
}
