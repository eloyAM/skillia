package com.example.application.service;

import com.example.application.dto.PersonDto;
import com.example.application.ldap.LdapProperties;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;

@Service
public class LdapService {
    private final LdapTemplate ldapTemplate;
    private final LdapProperties ldapProperties;

    public LdapService(LdapTemplate ldapTemplate, LdapProperties ldapProperties) {
        this.ldapTemplate = ldapTemplate;
        this.ldapProperties = ldapProperties;
    }

    public List<PersonDto> findAllUsers() throws InvalidNameException {
        LdapQuery query = LdapQueryBuilder.query()
                .attributes("mail", "title", "cn", ldapProperties.getUsernameAttribute(), ldapProperties.getDepartmentAttribute())
                .where("objectClass").is(ldapProperties.getUserObjectClass());
        AttributesMapper<PersonDto> attributesMapper = (attrs) -> PersonDto.builder()
                .username(getAttrAsStr(attrs, ldapProperties.getUsernameAttribute()))
                .displayName(getAttrAsStr(attrs, ldapProperties.getUsernameAttribute()))
                .email(getAttrAsStr(attrs, "mail"))
                .title(getAttrAsStr(attrs, "title"))
                .department(getAttrAsStr(attrs, ldapProperties.getDepartmentAttribute()))
                .build();
        var searchResult = ldapTemplate
                .search(query, attributesMapper);

        return searchResult;
    }

    private static String getAttrAsStr(Attributes attrs, String attrName) throws NamingException {
        var attr = attrs.get(attrName);
        if (attr == null) return null;
        Object attrValue = attr.get();    // Can throw NamingException
        return attrValue.toString();
    }
}
