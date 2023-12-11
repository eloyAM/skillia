package com.example.application.security;

import com.example.application.ldap.LdapProperties;
import com.example.application.view.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@EnableWebSecurity // <1>
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig extends VaadinWebSecurity { // <2>
    private final AuthenticationProvider jwtAuthenticationProvider;
    private final SecretKey secretKey;

    public SecurityConfig(SecretKey secretKey, JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.secretKey = secretKey;
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(
//                    PathRequest.toH2Console(),
                    PathRequest.toStaticResources().atCommonLocations()
            ).permitAll()   // permitAll() allows both anonymous and authenticated access
            .requestMatchers(
                    antMatcher(HttpMethod.GET, "/api/auth/**"),
                    antMatcher(HttpMethod.POST, "/api/auth/**"),
                    antMatcher(HttpMethod.OPTIONS, "/api/auth/**")
            ).anonymous()   // anonymous() allows anonymous, but not authenticated access
            ;
        });  // <3>

        http.csrf(csrf -> csrf
                .ignoringRequestMatchers(
                        PathRequest.toH2Console(),  // This allows the h2 console access (connect / test connection, etc)
                        antMatcher(HttpMethod.GET, "/api/auth/**"),
                        antMatcher(HttpMethod.POST, "/api/auth/**"),
                        antMatcher(HttpMethod.OPTIONS, "/api/auth/**")
                )
        );
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));    // This allows the different frames of the h2 console to be rendered

        http.authenticationProvider(jwtAuthenticationProvider);
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver = request -> authenticationManagerBuilder.getOrBuild();
        BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter = new BearerTokenAuthenticationFilter(authenticationManagerResolver);
        http.addFilterBefore(bearerTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        super.setStatelessAuthentication(http, secretKey, SecretKeyConfig.JWT_ISSUER);

        super.configure(http);
        setLoginView(http, LoginView.class); // <4>
    }


    @Autowired
    public void configure(
            AuthenticationManagerBuilder auth, LdapContextSource contextSource, LdapProperties ldapProperties
    ) throws Exception {
        openLdapServerConfig(auth, contextSource, ldapProperties);
    }

    private static void openLdapServerConfig(
            AuthenticationManagerBuilder auth, LdapContextSource contextSource, LdapProperties ldapProperties
    ) throws Exception {
        //@formatter:off
        auth
                .ldapAuthentication()
                .userDnPatterns(ldapProperties.getUserDnPatterns())
                .userSearchBase(ldapProperties.getUserSearchBase())
                .userSearchFilter(ldapProperties.getUserLoginFilter())
                .groupSearchBase(ldapProperties.getGroupSearchBase())
//                .groupSearchFilter("")  // TODO Could be interesting to allow this
//                .groupRoleAttribute("cn")   // Default is "cn", currently seems ok
                .contextSource(contextSource)
                .passwordCompare()
                .passwordAttribute(ldapProperties.getPasswordAttribute())
//                .passwordEncoder()    // TODO currently relying on plain text passwords. Also check encoder used with OpenLDAP, AD, etc
        //.passwordEncoder(NoOpPasswordEncoder.getInstance())    // Plain text password encoder (default one, same as not providing it). Deprecation warning is actually a security matter, no plan from Spring to remove it
        ;
        //@formatter:on
    }
}