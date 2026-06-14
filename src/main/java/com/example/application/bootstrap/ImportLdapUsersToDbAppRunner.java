package com.example.application.bootstrap;

import com.example.application.service.ldap.LdapSynchronizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Runs on application startup to import users and departments from LDAP
 */
@Slf4j
@Order(1)
@Component
public class ImportLdapUsersToDbAppRunner implements ApplicationRunner {

    private final LdapSynchronizationService ldapService;

    public ImportLdapUsersToDbAppRunner(LdapSynchronizationService ldapService) {
        this.ldapService = ldapService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Running database initialization from LDAP");
        ldapService.loadUsersAndDepartmentsWithLdap();
    }
}
