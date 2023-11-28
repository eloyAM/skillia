package com.example.application.ldap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ldap")
@lombok.Data
@lombok.NoArgsConstructor
public class LdapProperties {
    private String url;
    private String base;
    private String bindUser;
    private String bindPassword;
    private String passwordAttribute;
    private String usernameAttribute;
    private String departmentAttribute;
    private String userObjectClass;
    private String userDnPatterns;
    private String userSearchBase;
    private String userSearchFilter;
    private String groupSearchBase;
}
