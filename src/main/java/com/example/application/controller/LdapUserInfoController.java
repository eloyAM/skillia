package com.example.application.controller;

import com.example.application.dto.PersonDto;
import com.example.application.service.LdapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ldap")
public class LdapUserInfoController {
    private final LdapService ldapService;

    public LdapUserInfoController(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @GetMapping("/users")
    public List<PersonDto> get() {
        var users = ldapService.findAllUsers();
        return users;
    }
}