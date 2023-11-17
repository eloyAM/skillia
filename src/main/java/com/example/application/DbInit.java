package com.example.application;

import com.example.application.dto.PersonDto;
import com.example.application.dto.SkillDto;
import com.example.application.entity.PersonSkill;
import com.example.application.repo.PersonSkillRepo;
import com.example.application.service.PersonService;
import com.example.application.service.SkillService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class DbInit implements ApplicationRunner {

    private final PersonService personService;
    private final SkillService skillService;
    private final PersonSkillRepo personSkillRepo;

    public DbInit(
        PersonService personService,
        SkillService skillService,
        PersonSkillRepo personSkillRepo
    ) {
        this.personService = personService;
        this.skillService = skillService;
        this.personSkillRepo = personSkillRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<PersonDto> persons = createPersons();
        PersonDto firstPerson = persons.get(0);
        PersonDto secondPerson = persons.get(1);
        PersonDto thirdPerson = persons.get(2);

        List<SkillDto> skills = createSkills();
        SkillDto firstSkill = skills.get(0);
        SkillDto secondSkill = skills.get(1);

        Collection<PersonSkill> personSkills = List.of(
            new PersonSkill(firstPerson.getUsername(), firstSkill.getId(), 3),
            new PersonSkill(firstPerson.getUsername(), secondSkill.getId(), 2),
            new PersonSkill(secondPerson.getUsername(), firstSkill.getId(), 4),
            new PersonSkill(thirdPerson.getUsername(), secondSkill.getId(), 5),
            new PersonSkill(thirdPerson.getUsername(), firstSkill.getId(), 1)
        );
        personSkillRepo.saveAll(personSkills);
    }

    private List<SkillDto> createSkills() {
        Iterable<SkillDto> skills = List.of(
            SkillDto.builder().name("C++").build(),
            SkillDto.builder().name("Java").build(),
            SkillDto.builder().name("English").build(),
            SkillDto.builder().name("Communication").build(),
            SkillDto.builder().name("Testing").build(),
            SkillDto.builder().name("Open source").build()
        );
        return skillService.saveSkill(skills);
    }

    private List<PersonDto> createPersons() {
        List<PersonDto> persons = List.of(
            new PersonDto("eloy.abellan")
                .setEmail("eloy.abellan@example.com")
                .setDisplayName("Eloy Abellán Mayor")
                .setTitle("Junior Engineer")
                .setDepartment("Innovation"),
            new PersonDto("juan.canovas")
                .setEmail("juan.canovas@example.com")
                .setDisplayName("Juan Cánovas Hernández")
                .setTitle("Senior Engineer")
                .setDepartment("Development"),
            new PersonDto("jacob.smith")
                .setEmail("jacob.smith@example.com")
                .setDisplayName("Jacob Smith")
                .setTitle("Head Of Accounting")
                .setDepartment("Accounting"),
            new PersonDto("hernan.cortes")
        );
        return personService.savePerson(persons);
    }
}