package com.example.application.ldap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ldap")
@lombok.Data
@lombok.NoArgsConstructor
public class LdapProperties {
    private String passwordAttribute;
    private String usernameAttribute;
    private String departmentAttribute;
    private String fullNameAttribute;

    private String userObjectClass;
    private String userDnPatterns;
    private String userSearchBase;
    private String userLoginFilter;
    private String groupSearchBase;
}
