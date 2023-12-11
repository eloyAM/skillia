package com.example.application.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class SecretKeyBean {

    private final String jwtSecret;
    public static final String JWT_ISSUER = "com.example.application";
    public static final String JWT_ALGORITHM = JwsAlgorithms.HS256;

    public SecretKeyBean(@Value("${jwt.auth.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Bean
    public SecretKey customSecretKey() {
        byte[] key = Base64.getDecoder().decode(jwtSecret);
        return new SecretKeySpec(key, JWT_ALGORITHM);
    }

}
