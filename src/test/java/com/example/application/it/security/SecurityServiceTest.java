package com.example.application.it.security;

import com.example.application.security.SecurityService;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityServiceTest {

    @Autowired
    private SecurityService securityService;

    @Test
    void authenticateWithCredentialsReturnsLdapAuth() {
        // Given
        // a security configuration with LDAP and JWT
        String username = "hugo.reyes", password = "1234";

        // When
        Authentication retrievedAuth = securityService.authenticate(username, password);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(retrievedAuth).isNotNull();
        assertThat(retrievedAuth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(retrievedAuth.getPrincipal()).isInstanceOf(LdapUserDetails.class);
        assertThat(retrievedAuth.getName()).isEqualTo(username);
        assertThat(retrievedAuth.getAuthorities())
            .singleElement()
            .extracting(GrantedAuthority::getAuthority)
            .isEqualTo("ROLE_HR");
    }

    @Test
    void getBearerTokenOk() throws JOSEException {
        // Given
        // a security configuration with LDAP and JWT
        String username = "hugo.reyes", password = "1234";

        // When
        String bearerTokenStr = securityService.getBearerToken(username, password);

        // Then
        assertThat(bearerTokenStr).isNotEmpty();
    }

    @Test
    void authenticateWithBearerTokenReturnsJwtAuth() throws JOSEException {
        // Given
        // a security configuration with LDAP and JWT
        String username = "hugo.reyes", password = "1234";

        String bearerTokenStr = securityService.getBearerToken(username, password);
        assertThat(bearerTokenStr).isNotEmpty();
        JwtAuthenticationToken retrievedAuth = securityService.authenticate(bearerTokenStr);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(retrievedAuth).isNotNull();
        assertThat(retrievedAuth).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(retrievedAuth.getPrincipal()).isInstanceOf(Jwt.class);
        assertThat(retrievedAuth.getName()).isEqualTo(username);
        assertThat(retrievedAuth.getAuthorities())
            .singleElement()
            .extracting(GrantedAuthority::getAuthority)
            .isEqualTo("ROLE_HR");
    }

}