package com.example.application.controller;

import com.example.application.security.SecurityService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Auth", description = "API authentication")
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    private final SecurityService securityService;

    public AuthController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Operation(description = "Get a bearer token for the given username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful login. A bearer token is returned"),
        @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(
            schema = @Schema(implementation = ProblemDetail.class),
            mediaType = "application/problem+json"
        )),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(
            schema = @Schema(implementation = ProblemDetail.class),
            mediaType = "application/problem+json"
        )),
        @ApiResponse(responseCode = "500", description = "Could not create the JWT token", content = @Content(
            schema = @Schema(implementation = ProblemDetail.class),
            mediaType = "application/problem+json"
        )),
    })
    @PostMapping("/login")
    public JwtResponseDto login(@Valid @RequestBody UsrPwdDto body) {
        final String jwtToken;
        try {
            jwtToken = securityService.getBearerToken(body.username(), body.password());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials", e);
        } catch (JOSEException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not create the JWT token", e);
        }
        return new JwtResponseDto(jwtToken);
    }

    public record UsrPwdDto(
        @NotNull
        String username,
        @NotNull
        String password
    ) {
    }

    public record JwtResponseDto(String token) {
    }
}
