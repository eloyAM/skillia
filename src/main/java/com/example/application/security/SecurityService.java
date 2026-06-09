package com.example.application.security;

import com.example.application.security.jwt.CustomJwtEncoder;
import com.nimbusds.jose.JOSEException;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    private final AuthenticationContext vaadinAuthenticationContext;
    private final AuthenticationManager authenticationManager;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final CustomJwtEncoder customJwtEncoder;

    public SecurityService(
        AuthenticationContext vaadinAuthenticationContext,
        AuthenticationManager authenticationManager,
        JwtAuthenticationProvider jwtAuthenticationProvider,
        CustomJwtEncoder customJwtEncoder
    ) {
        this.vaadinAuthenticationContext = vaadinAuthenticationContext;
        this.authenticationManager = authenticationManager;
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
        this.customJwtEncoder = customJwtEncoder;
    }

    public UserDetails getUserDetails() {
        return vaadinAuthenticationContext.getAuthenticatedUser(UserDetails.class).orElseThrow();
    }

    public Jwt getJwt() {
        return vaadinAuthenticationContext.getAuthenticatedUser(Jwt.class).orElseThrow();
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder
            .getContext()
            .getAuthentication();
    }

    public void logout() {
        vaadinAuthenticationContext.logout();
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

    public String getBearerToken(String username, String password) throws JOSEException {
        var authentication = authenticate(username, password);
        return customJwtEncoder.encodeJwt(authentication);
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

}