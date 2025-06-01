package com.example.application;

import com.example.application.service.LdapDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(1)
@Component
public class ImportLdapUsersToDbAppRunner implements ApplicationRunner {

    private final LdapDbService ldapDbService;

    public ImportLdapUsersToDbAppRunner(LdapDbService ldapDbService) {
        this.ldapDbService = ldapDbService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Running database initialization from LDAP");
        ldapDbService.loadUsersAndDepartmentsWithLdap();
    }
}
