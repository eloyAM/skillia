package com.example.application.security;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
class CustomJwtDecoder implements JwtDecoder {

    private final JwtDecoder jwtDecoder;
    public static final JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse(SecretKeyConfig.JWT_MAC_ALGORITHM_NAME);

    public CustomJwtDecoder(SecretKey secretKey) {
        this.jwtDecoder = getJwtDecoder(SecretKeyConfig.JWT_ISSUER, getJWKSource(secretKey, jwsAlgorithm));
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        return jwtDecoder.decode(token);
    }

    private static JwtDecoder getJwtDecoder(String issuer, JWKSource<SecurityContext> jwkSource) {
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWTClaimsSetVerifier((claimsSet, context) -> {
            // No-op, Spring Security’s NimbusJwtDecoder uses its own validator
        });

        JWSKeySelector<SecurityContext> jwsKeySelector = new JWSVerificationKeySelector<>(jwsAlgorithm, jwkSource);
        jwtProcessor.setJWSKeySelector(jwsKeySelector);
        NimbusJwtDecoder nimbusJwtDecoder = new NimbusJwtDecoder(jwtProcessor);
        nimbusJwtDecoder.setJwtValidator(
                issuer != null ? JwtValidators.createDefaultWithIssuer(issuer)
                        : JwtValidators.createDefault());
        return nimbusJwtDecoder;
    }

    public static JWKSource<SecurityContext> getJWKSource(SecretKey secretKey, Algorithm alg) {
        OctetSequenceKey key = new OctetSequenceKey.Builder(secretKey).algorithm(alg).build();
        JWKSet jwkSet = new JWKSet(key);
        return (jwkSelector, context) -> jwkSelector.select(jwkSet);
    }
}
