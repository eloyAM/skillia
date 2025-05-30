package com.example.application.utils;

import com.example.application.dto.PersonDto;
import com.example.application.dto.SkillDto;
import com.example.application.dto.SkillGroupDto;
import com.example.application.dto.SkillTagDto;
import com.example.application.entity.PersonSkill;
import com.example.application.repo.PersonSkillRepo;
import com.example.application.service.PersonService;
import com.example.application.service.SkillGroupService;
import com.example.application.service.SkillService;
import com.example.application.service.SkillTagService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO add SkillGroup and Department data
public class DbInit {

    private final PersonService personService;
    private final SkillService skillService;
    private final PersonSkillRepo personSkillRepo;
    private final SkillTagService skillTagService;
    private final SkillGroupService skillGroupService;

    public DbInit(
        PersonService personService,
        SkillService skillService,
        PersonSkillRepo personSkillRepo,
        SkillTagService skillTagService,
        SkillGroupService skillGroupService
    ) {
        this.personService = personService;
        this.skillService = skillService;
        this.personSkillRepo = personSkillRepo;
        this.skillTagService = skillTagService;
        this.skillGroupService = skillGroupService;
    }

    public void run() {
        List<PersonDto> persons = createPersons();
        Map<String, PersonDto> personsByUsername = persons.stream()
            .collect(Collectors.toMap(PersonDto::getUsername, Function.identity()));
        PersonDto firstPerson = persons.get(0);
        PersonDto secondPerson = persons.get(1);
        PersonDto thirdPerson = persons.get(2);

        Set<SkillTagDto> skillTags = createSkillTags();
        createSkillTagsRandom();
        createSkillsRandom();

        List<SkillDto> skills = createSkills(skillTags);
        Map<String, SkillDto> skillsByName = skills.stream()
            .collect(Collectors.toMap(SkillDto::getName, Function.identity()));
        SkillDto firstSkill = skills.get(0);
        SkillDto secondSkill = skills.get(1);

        Function<String, Long> skillIdByName = (name) -> skillsByName.get(name).getId();

        Collection<PersonSkill> personSkills = List.of(
            new PersonSkill(firstPerson.getUsername(), skillIdByName.apply("C++"), 3),
            new PersonSkill(firstPerson.getUsername(), skillIdByName.apply("Java"), 2),
            new PersonSkill(firstPerson.getUsername(), skillIdByName.apply("Korean"), randomLvl()),
            new PersonSkill(firstPerson.getUsername(), skillIdByName.apply("Open source"), randomLvl()),
            new PersonSkill(firstPerson.getUsername(), skillIdByName.apply("Mockito"), randomLvl()),
            new PersonSkill(firstPerson.getUsername(), skillIdByName.apply("MS Project"), randomLvl()),
            new PersonSkill(secondPerson.getUsername(), firstSkill.getId(), 4),
            new PersonSkill(thirdPerson.getUsername(), secondSkill.getId(), 5),
            new PersonSkill(thirdPerson.getUsername(), firstSkill.getId(), 1)
        );
        personSkillRepo.saveAll(personSkills);

        createSkillGroups(skills);
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
            SkillDto.builder().name("SQL - Structured Query Language")
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
            SkillDto.builder().name("Mockito")
                .tags(Set.of(tagsByName.get("Unit Testing"), tagsByName.get("Java"), tagsByName.get("Mocking libraries")))
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
            new PersonDto("gilberto.jimenezm")
                .setEmail("gilberto.jimenezm@example.com")
                .setFullName("Gilberto Jiménez Montés")
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
                SkillTagDto.builder().name("Java").build(),
                SkillTagDto.builder().name("Mocking libraries").build(),
                SkillTagDto.builder().name("Performance testing tools").build()
            )
            .stream()
            .map(item -> skillTagService
                .saveSkillTag(item)
                .orElseThrow()
            )
            .collect(Collectors.toSet());
    }

    private List<SkillGroupDto> createSkillGroups(List<SkillDto> skills) {
        return Stream.of(
                SkillGroupDto.builder().name("Some empty group").build(),
                SkillGroupDto.builder().name("Empty group with description")
                    .description("This is some useful description which will help you know what is this for")
                    .build(),
                SkillGroupDto.builder().name("Not empty group with desc")
                    .description("Lorem ipsum dolor sit amet ")
                    .skills(getShuffleCopy(skills).stream().limit(5).toList())
                    .build(),
                SkillGroupDto.builder().name("Not empty group without desc")
                    .description("Lorem ipsum dolor sit amet ")
                    .skills(getShuffleCopy(skills).stream().limit(10).toList())
                    .build()
            )
            .map(skillGroupService::saveGroup)
            .map(Optional::orElseThrow)
            .toList();
    }

    private static <R> Collection<R> getShuffleCopy(Collection<R> source) {
        var copy = new ArrayList<>(source);
        Collections.shuffle(copy);
        return copy;
    }


    private void createSkillTagsRandom() {
        int nElements = 51;
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
        int nElements = 51;
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

    private static int randomLvl() {
        return new Random().nextInt(5) + 1;
    }

}