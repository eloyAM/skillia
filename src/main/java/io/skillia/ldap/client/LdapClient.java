package io.skillia.ldap.client;

import io.skillia.dto.main.PersonDto;
import io.skillia.ldap.properties.LdapProperties;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LdapClient {
    private final LdapTemplate ldapTemplate;
    private final LdapProperties ldapProperties;

    public LdapClient(LdapTemplate ldapTemplate, LdapProperties ldapProperties) {
        this.ldapTemplate = ldapTemplate;
        this.ldapProperties = ldapProperties;
    }


    public List<PersonDto> findAllUsers() {
        String base = ldapProperties.getUserSearchBase();
        String filter = "(objectClass=" + ldapProperties.getUserObjectClass() + ")";
        return ldapTemplate.search(base, filter, new AbstractContextMapper<>() {
            @Override
            protected PersonDto doMapFromContext(DirContextOperations ctx) {
                return PersonDto.builder()
                    .username(ctx.getStringAttribute(ldapProperties.getUsernameAttribute()))
                    .fullName(ctx.getStringAttribute(ldapProperties.getFullNameAttribute()))
                    .email(ctx.getStringAttribute("mail"))
                    .title(ctx.getStringAttribute("title"))
                    .department(ctx.getStringAttribute(ldapProperties.getDepartmentAttribute()))
                    .build();
            }
        });
    }

}

