package com.example.application.controller;

import com.example.application.dto.UsrPwdDto;
import com.example.application.security.CustomJwtEncoder;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Auth", description = "API authentication")
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

    @Operation(description = "Get a bearer token for the given username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login. A bearer token is returned", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Could not authenticate. Check the credentials", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Could not create the JWT token", content = @Content(mediaType = "text/plain"))
    })
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
            return ResponseEntity.internalServerError().body("Could not create the JWT token");
        }
        return ResponseEntity.ok(jwtToken);
    }

}
