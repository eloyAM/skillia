package com.example.application.utils;

import com.example.application.dto.PersonDto;
import com.example.application.dto.SkillDto;
import com.example.application.dto.SkillTagDto;
import com.example.application.entity.PersonSkill;
import com.example.application.repo.PersonSkillRepo;
import com.example.application.service.PersonService;
import com.example.application.service.SkillService;
import com.example.application.service.SkillTagService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DbInit {

    private final PersonService personService;
    private final SkillService skillService;
    private final PersonSkillRepo personSkillRepo;
    private final SkillTagService skillTagService;

    public DbInit(
        PersonService personService,
        SkillService skillService,
        PersonSkillRepo personSkillRepo,
        SkillTagService skillTagService
    ) {
        this.personService = personService;
        this.skillService = skillService;
        this.personSkillRepo = personSkillRepo;
        this.skillTagService = skillTagService;
    }

    public void run() {
        List<PersonDto> persons = createPersons();
        PersonDto firstPerson = persons.get(0);
        PersonDto secondPerson = persons.get(1);
        PersonDto thirdPerson = persons.get(2);

        Set<SkillTagDto> skillTags = createSkillTags();
        createSkillTagsRandom();
        createSkillsRandom();

        List<SkillDto> skills = createSkills(skillTags);
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

    private List<SkillDto> createSkills(Set<SkillTagDto> skillTags) {
        Map<String, SkillTagDto> tagsByName = skillTags.stream()
            .collect(Collectors.toMap(SkillTagDto::getName, Function.identity()));
        Iterable<SkillDto> skills = List.of(
            SkillDto.builder().name("C++")
                .tags(Set.of(tagsByName.get("Programming Languages")))
                .build(),
            SkillDto.builder().name("Java")
                .tags(Set.of(tagsByName.get("Programming Languages")))
                .build(),
            SkillDto.builder().name("English")
                .tags(Set.of(tagsByName.get("Languages")))
                .build(),
            new SkillDto(null, "Korean", tagsByName.get("Languages")),
            SkillDto.builder().name("Communication").build(),
            SkillDto.builder().name("Testing").build(),
            SkillDto.builder().name("Open source").build(),
            SkillDto.builder().name("JUnit")
                .tags(Set.of(tagsByName.get("Unit Testing"), tagsByName.get("Java")))
                .build(),
            new SkillDto(null,
                "MS Project",
                tagsByName.get("Project Management"), tagsByName.get("Tools")
            )
        );
        return skillService.saveSkill(skills);
    }

    private List<PersonDto> createPersons() {
        List<PersonDto> persons = List.of(
            new PersonDto("eloy.abellan")
                .setEmail("eloy.abellan@example.com")
                .setFullName("Eloy Abellán Mayor")
                .setTitle("Junior Engineer")
                .setDepartment("Innovation"),
            new PersonDto("juan.canovas")
                .setEmail("juan.canovas@example.com")
                .setFullName("Juan Cánovas Hernández")
                .setTitle("Senior Engineer")
                .setDepartment("Development"),
            new PersonDto("jacob.smith")
                .setEmail("jacob.smith@example.com")
                .setFullName("Jacob Smith")
                .setTitle("Head Of Accounting")
                .setDepartment("Accounting"),
            new PersonDto("hernan.cortes")
        );
        return personService.savePerson(persons);
    }

    private Set<SkillTagDto> createSkillTags() {
        return Set.of(
                SkillTagDto.builder().name("Programming Languages").build(),
                SkillTagDto.builder().name("Languages").build(),
                SkillTagDto.builder().name("Project Management").build(),
                SkillTagDto.builder().name("Tools").build(),
                SkillTagDto.builder().name("Unit Testing").build(),
                SkillTagDto.builder().name("Java").build()
            )
            .stream()
            .map(item -> skillTagService
                .saveSkillTag(item)
                .orElseThrow()
            )
            .collect(Collectors.toSet());
    }


    private void createSkillTagsRandom() {
        int nElements = 150;
        Set<SkillTagDto> tags = new LinkedHashSet<>(nElements);
        for (int i = 0; i < nElements; i++) {
            String name = String.format("Tag %03d", i);
            tags.add(
                SkillTagDto.builder().name(name).build()
            );
        }
        for (SkillTagDto tag : tags) {
            skillTagService.saveSkillTag(tag)
                .orElseThrow();
        }
    }

    private void createSkillsRandom() {
        int nElements = 101;
        Set<SkillDto> tags = new LinkedHashSet<>(nElements);
        for (int i = 0; i < nElements; i++) {
            String name = String.format("Skill %03d", i);
            tags.add(
                SkillDto.builder().name(name).build()
            );
        }
        for (SkillDto tag : tags) {
            skillService.saveSkill(tag)
                .orElseThrow();
        }
    }

}