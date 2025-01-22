package com.example.application.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt.auth")
public record JwtProperties(
    @NotBlank String secret,
    @Min(1) Long expirationSeconds,
    @NotBlank String issuer,
    @NotBlank String algorithm
) {
}
