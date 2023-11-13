package com.example.application.security;

import com.example.application.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity // <1>
@Configuration
public class SecurityConfig extends VaadinWebSecurity { // <2>

    private static final String ldapUrl = "ldap://localhost:1389/dc=example,dc=org";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(
                    AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/images/*.png")).permitAll());  // <3>
        super.configure(http);
        setLoginView(http, LoginView.class); // <4>
    }


    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        openLdapServerConfig(auth, ldapUrl);
    }

    private static void openLdapServerConfig(AuthenticationManagerBuilder auth, String ldapUrl) throws Exception {
        //@formatter:off
        //String ldapUrl = "ldap://localhost:1389/dc=example,dc=org";
        auth
                .ldapAuthentication()
                .userDnPatterns("uid={0},ou=users")
                .userSearchBase("ou=users")
                .userSearchFilter("(&(uid={0})(objectClass=person))")
                .groupSearchBase("ou=groups")
                .contextSource()
                .url(ldapUrl) // alternative .url("ldap://localhost").port(1389).root("dc=example,dc=org")
                .managerDn("cn=admin,dc=example,dc=org")
                .managerPassword("adminpassword")
                .and()
                .passwordCompare()
                .passwordAttribute("userPassword")
        //.passwordEncoder(NoOpPasswordEncoder.getInstance())    // Plain text password encoder (default one, same as not providing it). Deprecation warning is actually a security matter, no plan from Spring to remove it
        ;
        //@formatter:on
    }
}