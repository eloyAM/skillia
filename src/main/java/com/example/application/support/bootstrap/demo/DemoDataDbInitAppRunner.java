package com.example.application.support.bootstrap.demo;

import com.example.application.persistence.repo.PersonSkillRepo;
import com.example.application.service.DepartmentService;
import com.example.application.service.PersonService;
import com.example.application.service.SkillService;
import com.example.application.support.bootstrap.ImportLdapUsersToDbAppRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Meant to be executed after the {@link ImportLdapUsersToDbAppRunner}
 */
@Order(2)
@Slf4j
@Component
@ConditionalOnProperty(name = "initialization.create-sample-data", havingValue = "true")
public class DemoDataDbInitAppRunner implements ApplicationRunner {

    private final DemoDataDbInit demoDataDbInitializer;

    public DemoDataDbInitAppRunner(
        PersonService personService,
        SkillService skillService,
        PersonSkillRepo personSkillRepo,
        DepartmentService departmentService
    ) {
        this.demoDataDbInitializer = new DemoDataDbInit(
            personService, skillService, personSkillRepo, departmentService
        );
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting demo data initialization");
        demoDataDbInitializer.run();
    }
}
