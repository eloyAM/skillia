package com.example.application.service;

import com.example.application.dto.PersonDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class LdapDbService {

    private final LdapService ldapService;
    private final PersonService personService;

    public LdapDbService(LdapService ldapService, PersonService personService) {
        this.ldapService = ldapService;
        this.personService = personService;
    }

    public void loadDbUsersWithLdap() {
        log.info("Loading users from LDAP");
        List<PersonDto> ldapUsers = ldapService.findAllUsers();
        log.info("Found {} users from LDAP, proceeding to save them on the users database", ldapUsers.size());
        personService.savePerson(ldapUsers);
        log.info("The users database have been saved successfully initialized");
    }
}
