package com.example.application.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class SecretKeyConfig {

    private final JwtProperties jwtProperties;

    public SecretKeyConfig(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Bean
    public SecretKey customSecretKey() {
        byte[] key = Base64.getDecoder().decode(jwtProperties.secret());
        return new SecretKeySpec(key, jwtProperties.algorithm());
    }

}
