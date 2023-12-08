package com.example.application.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;

    public AuthController(HttpSecurity httpSecurity) {
        AuthenticationManagerBuilder authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManager = authenticationManagerBuilder.getOrBuild();  // Don't try something like httpSecurity.getSharedObject(AuthenticationManager.class) as that doesn't work
    }

    @PostMapping("/checkCredentials")
    public boolean checkCredentials(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        try {
            // If no exception is thrown, the credentials are valid
            Authentication authentication = authenticationManager.authenticate(token);
            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }


}
