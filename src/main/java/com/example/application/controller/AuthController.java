package com.example.application.controller;

import com.example.application.dto.UsrPwdDto;
import com.example.application.security.CustomJwtEncoder;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final CustomJwtEncoder customJwtEncoder;

    public AuthController(HttpSecurity httpSecurity, CustomJwtEncoder customJwtEncoder) {
        AuthenticationManagerBuilder authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManager = authenticationManagerBuilder.getOrBuild();  // Don't try something like httpSecurity.getSharedObject(AuthenticationManager.class) as that doesn't work
        this.customJwtEncoder = customJwtEncoder;
    }

    @PostMapping("/checkCredentials")
    public boolean checkCredentials(@RequestBody UsrPwdDto body) {
        UsernamePasswordAuthenticationToken usrPwdtoken = new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword());
        try {
            // If no exception is thrown, the credentials are valid
            Authentication authentication = authenticationManager.authenticate(usrPwdtoken);
            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UsrPwdDto body) {
        UsernamePasswordAuthenticationToken usrPwdtoken = new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword());
        final Authentication authentication;
        try {
            // If no exception is thrown, the credentials are valid
            authentication = authenticationManager.authenticate(usrPwdtoken);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Could not authenticate");
        }

        final String jwtToken;
        try {
            jwtToken = customJwtEncoder.encodeJwt(authentication);
        } catch (JOSEException e) {
            return ResponseEntity.badRequest().body("Could not create the JWT token");
        }
        return ResponseEntity.ok(jwtToken);
    }

}
