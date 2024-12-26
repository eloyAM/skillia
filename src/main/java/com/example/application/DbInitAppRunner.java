package com.example.application;

import com.example.application.repo.PersonSkillRepo;
import com.example.application.service.PersonService;
import com.example.application.service.SkillService;
import com.example.application.service.SkillTagService;
import com.example.application.utils.DbInit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DbInitAppRunner implements ApplicationRunner {

    private final DbInit dbInitializer;
    @Value("${initdbfromjava}")
    private boolean initdbfromjava;

    public DbInitAppRunner(
            PersonService personService,
            SkillService skillService,
            PersonSkillRepo personSkillRepo,
            SkillTagService skillTagService
    ) {
        this.dbInitializer = new DbInit(personService, skillService, personSkillRepo, skillTagService);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (initdbfromjava) {
            log.info("Initializing database from Java");
            dbInitializer.run();
        }
    }
}
