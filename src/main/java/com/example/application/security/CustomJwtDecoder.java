package com.example.application.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;

@Configuration
class CustomJwtDecoder implements JwtDecoder {

    private final NimbusJwtDecoder jwtDecoder;

    public CustomJwtDecoder(SecretKey secretKey) {
        this.jwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(SecretKeyConfig.JWT_MAC_ALGORITHM)
                .build();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        return jwtDecoder.decode(token);
    }
}
