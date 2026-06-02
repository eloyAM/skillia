package com.example.application.controller;

import com.example.application.security.jwt.CustomJwtEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "API authentication")
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    public static final String MESSAGE_FIELD = "message";
    private final AuthenticationManager authenticationManager;
    private final CustomJwtEncoder customJwtEncoder;
    private final ObjectMapper objectMapper;

    public AuthController(HttpSecurity httpSecurity, CustomJwtEncoder customJwtEncoder, ObjectMapper objectMapper) {
        AuthenticationManagerBuilder authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManager = authenticationManagerBuilder.getOrBuild();  // Don't try something like httpSecurity.getSharedObject(AuthenticationManager.class) as that doesn't work
        this.customJwtEncoder = customJwtEncoder;
        this.objectMapper = objectMapper;
    }

    @Operation(description = "Get a bearer token for the given username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful login. A bearer token is returned", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(mediaType = "application/problem+json")),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Could not create the JWT token", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/login")
    public ResponseEntity<ObjectNode> login(@Valid @RequestBody UsrPwdDto body) {
        UsernamePasswordAuthenticationToken usrPwdtoken = new UsernamePasswordAuthenticationToken(
            body.username(), body.password()
        );
        final Authentication authentication;
        try {
            // If no exception is thrown, the credentials are valid
            authentication = authenticationManager.authenticate(usrPwdtoken);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                objectMapper.createObjectNode().put(MESSAGE_FIELD, "Invalid credentials"));
        }

        final String jwtToken;
        try {
            jwtToken = customJwtEncoder.encodeJwt(authentication);
        } catch (JOSEException e) {
            return ResponseEntity.internalServerError().body(
                objectMapper.createObjectNode().put(MESSAGE_FIELD, "Could not create the JWT token")
            );
        }
        return ResponseEntity.ok(
            objectMapper.createObjectNode().put("token", jwtToken)
        );
    }

    record UsrPwdDto(
        @NotNull
        String username,
        @NotNull
        String password
    ) {
    }
}
