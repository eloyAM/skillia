package com.example.application;

import com.example.application.service.LdapDbService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"!embedded-ldap"})
public class ImportLdapUsersToDbAppRunner implements ApplicationRunner {

    private final LdapDbService ldapDbService;

    public ImportLdapUsersToDbAppRunner(LdapDbService ldapDbService) {
        this.ldapDbService = ldapDbService;
    }

    @Override
    public void run(ApplicationArguments args) {
        ldapDbService.loadDbUsersWithLdap();
    }
}
