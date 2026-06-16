package io.skillia.ldap.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "ldap")
@Validated
@lombok.Data
@lombok.NoArgsConstructor
public class LdapProperties {
    // Main attributes
    @NotBlank
    private String passwordAttribute;
    @NotBlank
    private String usernameAttribute;
    @NotBlank
    private String departmentAttribute;
    @NotBlank
    private String fullNameAttribute;
    // User & group/roles search settings
    @NotBlank
    private String userObjectClass;
    @NotBlank
    private String userDnPatterns;
    @NotBlank
    private String userSearchBase;
    @NotBlank
    private String userLoginFilter;
    @NotBlank
    private String groupSearchBase;
    @NotBlank
    private String groupSearchFilter;
    @NotBlank
    private String groupRoleAttribute;
}
