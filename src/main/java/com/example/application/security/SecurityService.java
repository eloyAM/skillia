package com.example.application.security;

import com.nimbusds.jose.JOSEException;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {

    private final AuthenticationContext authenticationContext;
    private final AuthenticationManager authenticationManager;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final CustomJwtEncoder customJwtEncoder;

    public SecurityService(
        AuthenticationContext authenticationContext,
        AuthenticationManagerBuilder authenticationManagerBuilder,
        JwtAuthenticationProvider jwtAuthenticationProvider,
        CustomJwtEncoder customJwtEncoder
    ) {
        this.authenticationContext = authenticationContext;
        this.authenticationManager = authenticationManagerBuilder.getOrBuild();
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
        this.customJwtEncoder = customJwtEncoder;
    }

    public UserDetails getUserDetails() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class).get();
    }

    public Jwt getJwt() {
        return authenticationContext.getAuthenticatedUser(Jwt.class).get();
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder
            .getContext()
            .getAuthentication();
    }

    public void logout() {
        authenticationContext.logout();
    }

    //

    public UsernamePasswordAuthenticationToken authenticate(UsernamePasswordAuthenticationToken usrPwdtoken) {
        // If no exception is thrown, the credentials are valid
        return (UsernamePasswordAuthenticationToken) authenticationManager.authenticate(usrPwdtoken);
    }

    public UsernamePasswordAuthenticationToken authenticate(String username, String password) {
        UsernamePasswordAuthenticationToken usrPwdtoken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticate(usrPwdtoken);
    }

    //

    public JwtAuthenticationToken authenticate(BearerTokenAuthenticationToken token) {
        // If no exception is thrown, the credentials are valid
        return (JwtAuthenticationToken) jwtAuthenticationProvider.authenticate(token);
    }

    public JwtAuthenticationToken authenticate(String token) {
        BearerTokenAuthenticationToken bearerTokenObj = new BearerTokenAuthenticationToken(token);
        return authenticate(bearerTokenObj);
    }

    //

    public String getBearerToken(String username, String password) throws JOSEException {
        var authentication = authenticate(username, password);
        return customJwtEncoder.encodeJwt(authentication);
    }
}