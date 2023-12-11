package com.example.application.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class SecretKeyConfig {

    private final String jwtSecret;
    public static final String JWT_ISSUER = "com.example.application";
    public static final String JWT_MAC_ALGORITHM_NAME = MacAlgorithm.HS256.getName();
    public static final MacAlgorithm JWT_MAC_ALGORITHM = MacAlgorithm.HS256;

    public SecretKeyConfig(@Value("${jwt.auth.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Bean
    public SecretKey customSecretKey() {
        byte[] key = Base64.getDecoder().decode(jwtSecret);
        return new SecretKeySpec(key, JWT_MAC_ALGORITHM_NAME);
    }

}
