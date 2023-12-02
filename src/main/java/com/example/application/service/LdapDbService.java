package com.example.application.service;

import com.example.application.dto.PersonDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LdapDbService {

    private final LdapService ldapService;
    private final PersonService personService;

    public LdapDbService(LdapService ldapService, PersonService personService) {
        this.ldapService = ldapService;
        this.personService = personService;
    }

    public void loadDbUsersWithLdap() {
        List<PersonDto> ldapUsers = ldapService.findAllUsers();
        personService.savePerson(ldapUsers);
    }
}
