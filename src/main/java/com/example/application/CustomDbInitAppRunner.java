package com.example.application;

import com.example.application.repo.PersonSkillRepo;
import com.example.application.service.*;
import com.example.application.utils.DbInit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Slf4j
@Component
public class CustomDbInitAppRunner implements ApplicationRunner {

    private final DbInit dbInitializer;
    @Value("${initdbfromjava}")
    private boolean initdbfromjava;

    public CustomDbInitAppRunner(
        PersonService personService,
        SkillService skillService,
        PersonSkillRepo personSkillRepo,
        SkillTagService skillTagService,
        SkillGroupService skillGroupService,
        DepartmentService departmentService
    ) {
        this.dbInitializer = new DbInit(
            personService, skillService, personSkillRepo, skillTagService, skillGroupService, departmentService
        );
    }

    @Override
    public void run(ApplicationArguments args) {
        if (initdbfromjava) {
            log.info("Running database initialization from Java");
            dbInitializer.run();
        }
    }
}
