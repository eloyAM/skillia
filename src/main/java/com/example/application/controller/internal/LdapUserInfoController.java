package com.example.application.controller.internal;

import com.example.application.dto.PersonDto;
import com.example.application.ldap.LdapClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@io.swagger.v3.oas.annotations.Hidden   // Hide this controller from Swagger, internal purposes only
@RestController
@RequestMapping("/api/ldap")
public class LdapUserInfoController {
    private final LdapClient ldapClient;

    public LdapUserInfoController(LdapClient ldapClient) {
        this.ldapClient = ldapClient;
    }

    @GetMapping("/users")
    public List<PersonDto> get() {
        return ldapClient.findAllUsers();
    }
}