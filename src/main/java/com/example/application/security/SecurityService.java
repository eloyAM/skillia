package com.example.application.security;

import com.example.application.security.jwt.CustomJwtEncoder;
import com.nimbusds.jose.JOSEException;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public static boolean hasHrlRole(Authentication authentication) {
        var userAuthorities = authentication.getAuthorities();
        SimpleGrantedAuthority rhAuthority = new SimpleGrantedAuthority(SecConstants.ROLE_HR);
        return userAuthorities.contains(rhAuthority);
    }

    public static class ProfilePermissionsHelper {
        private final boolean isTheSameUserOrHasHrRole;

        public ProfilePermissionsHelper(Authentication authentication, String requestedUsername) {
            this.isTheSameUserOrHasHrRole = authentication.getName().equals(requestedUsername)
                || SecurityService.hasHrlRole(authentication);
        }


        public boolean isTheSameUserOrHasHrRole() {
            return isTheSameUserOrHasHrRole;
        }
    }
}