package com.example.application.controller;

import com.example.application.ldap.LdapUserInfo;
import com.example.application.ldap.LdapUserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ldap")
public class LdapUserInfoController {
    private final LdapUserRepository ldapUserRepository;

    public LdapUserInfoController(LdapUserRepository ldapUserRepository) {
        this.ldapUserRepository = ldapUserRepository;
    }

    @GetMapping("/users")
    public List<LdapUserInfo> get() {
        List<LdapUserInfo> users = ldapUserRepository.findAll();
        return users;
        /*
        return users.stream()
                .map(LdapUserInfo::toString)
                .collect(Collectors.joining("\n\n"));*/
    }
}