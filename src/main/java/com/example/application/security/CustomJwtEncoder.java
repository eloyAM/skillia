package com.example.application.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.application.security.JwtAuthenticationProviderConfig.ROLES_CLAIM;
import static com.example.application.security.JwtAuthenticationProviderConfig.ROLE_AUTHORITY_PREFIX;

@Component
public class CustomJwtEncoder {
    private final SecretKey secretKey;
    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
    private final JWKSource<SecurityContext> jwkSource;
    public static final JWSAlgorithm jwsAlgorithm = CustomJwtDecoder.jwsAlgorithm;
    public static final String issuer = SecretKeyConfig.JWT_ISSUER;
    private long expiresIn = 1800L;

    public CustomJwtEncoder(SecretKey secretKey) {
        this.secretKey = secretKey;
        this.jwkSource = CustomJwtDecoder.getJWKSource(secretKey, jwsAlgorithm);
    }

    public String encodeJwt(Authentication authentication) throws JOSEException {
        if (authentication == null || trustResolver.isAnonymous(authentication)) {
            return null;
        }

        final Date now = new Date();

        final List<String> roles = authentication.getAuthorities().stream()
                .map(Objects::toString)
                .filter(a -> a.startsWith(ROLE_AUTHORITY_PREFIX))
                .map(a -> a.substring(ROLE_AUTHORITY_PREFIX.length()))
                .collect(Collectors.toList());

        JWSHeader jwsHeader = new JWSHeader(jwsAlgorithm);
        JWKSelector jwkSelector = new JWKSelector(JWKMatcher.forJWSHeader(jwsHeader));

        List<JWK> jwks = jwkSource.get(jwkSelector, null);
        JWK jwk = jwks.get(0);

        JWSSigner signer = new DefaultJWSSignerFactory().createJWSSigner(jwk, jwsAlgorithm);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(authentication.getName()).issuer(issuer).issueTime(now)
                .expirationTime(new Date(now.getTime() + expiresIn * 1000))
                .claim(ROLES_CLAIM, roles).build();
        SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

}
