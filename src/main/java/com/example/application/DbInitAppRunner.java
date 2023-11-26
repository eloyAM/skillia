package com.example.application;

import com.example.application.repo.PersonSkillRepo;
import com.example.application.service.PersonService;
import com.example.application.service.SkillService;
import com.example.application.utils.DbInit;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DbInitAppRunner implements ApplicationRunner {

    private final DbInit dbInitializer;

    public DbInitAppRunner(
            PersonService personService,
            SkillService skillService,
            PersonSkillRepo personSkillRepo
    ) {
        this.dbInitializer = new DbInit(personService, skillService, personSkillRepo);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (args.containsOption("initdbfromjava")) {
            dbInitializer.run();
        }
    }
}
