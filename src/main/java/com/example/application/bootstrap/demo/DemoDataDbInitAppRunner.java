package com.example.application.bootstrap.demo;

import com.example.application.repo.PersonSkillRepo;
import com.example.application.service.DepartmentService;
import com.example.application.service.PersonService;
import com.example.application.service.SkillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Meant to be executed after the {@link com.example.application.bootstrap.ImportLdapUsersToDbAppRunner}
 */
@Order(2)
@Slf4j
@Component
public class DemoDataDbInitAppRunner implements ApplicationRunner {

    private final DemoDataDbInit demoDataDbInitializer;
    @Value("${initdbfromjava}")
    private boolean initdbfromjava;

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
        if (initdbfromjava) {
            log.info("Running database initialization from Java");
            demoDataDbInitializer.run();
        }
    }
}
