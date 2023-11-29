package com.example.application.security;

import com.example.application.ldap.LdapProperties;
import com.example.application.view.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;

@EnableWebSecurity // <1>
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig extends VaadinWebSecurity { // <2>

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(
//                    PathRequest.toH2Console(),
                    PathRequest.toStaticResources().atCommonLocations()
            ).permitAll();
        });  // <3>
        http.csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console())); // This allows the h2 console access (connect / test connection, etc)
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));    // This allows the different frames of the h2 console to be rendered
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
        // TODO the other LDAP parameters should be configurable as well
        //  (userDnPatterns, userSearchBase, userSearchFilter, groupSearchBase, passwordAttribute)
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
        //.passwordEncoder(NoOpPasswordEncoder.getInstance())    // Plain text password encoder (default one, same as not providing it). Deprecation warning is actually a security matter, no plan from Spring to remove it
        ;
        //@formatter:on
    }
}