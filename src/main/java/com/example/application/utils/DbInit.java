package com.example.application.utils;

import com.example.application.dto.PersonDto;
import com.example.application.dto.SkillDto;
import com.example.application.dto.SkillGroupDto;
import com.example.application.dto.SkillTagDto;
import com.example.application.entity.PersonSkill;
import com.example.application.repo.PersonSkillRepo;
import com.example.application.service.DepartmentService;
import com.example.application.service.PersonService;
import com.example.application.service.SkillService;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Important:
 * take care with dependencies/overwriting data
 * imported from LDAP through the runner {@link com.example.application.ImportLdapUsersToDbAppRunner}
 */
public class DbInit {

    private final PersonService personService;
    private final SkillService skillService;
    private final PersonSkillRepo personSkillRepo;
    private final DepartmentService departmentService;

    public DbInit(
        PersonService personService,
        SkillService skillService,
        PersonSkillRepo personSkillRepo,
        DepartmentService departmentService
    ) {
        this.personService = personService;
        this.skillService = skillService;
        this.personSkillRepo = personSkillRepo;
        this.departmentService = departmentService;
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
        Map<String, SkillDto> skillsByName = skills.stream()
            .collect(Collectors.toMap(SkillDto::getName, Function.identity()));
        SkillDto firstSkill = skills.get(0);
        SkillDto secondSkill = skills.get(1);

        ToLongFunction<String> skillIdByName = name -> skillsByName.get(name).getId();

        Collection<PersonSkill> personSkills = List.of(
            new PersonSkill(firstPerson.getUsername(), skillIdByName.applyAsLong("C++"), 3),
            new PersonSkill(firstPerson.getUsername(), skillIdByName.applyAsLong("Java"), 2),
            new PersonSkill(firstPerson.getUsername(), skillIdByName.applyAsLong("Korean"), randomLvl()),
            new PersonSkill(firstPerson.getUsername(), skillIdByName.applyAsLong("Open source"), randomLvl()),
            new PersonSkill(firstPerson.getUsername(), skillIdByName.applyAsLong("Mockito"), randomLvl()),
            new PersonSkill(firstPerson.getUsername(), skillIdByName.applyAsLong("MS Project"), randomLvl()),
            new PersonSkill(secondPerson.getUsername(), firstSkill.getId(), 4),
            new PersonSkill(thirdPerson.getUsername(), secondSkill.getId(), 5),
            new PersonSkill(thirdPerson.getUsername(), firstSkill.getId(), 1)
        );
        personSkillRepo.saveAll(personSkills);

        List<SkillGroupDto> skillGroups = createSkillGroups(skills);
        setDeparmentSkillsGroups(skillGroups);
    }

    private List<SkillDto> createSkills(Set<SkillTagDto> skillTags) {
        Map<String, SkillTagDto> tagsByName = skillTags.stream()
            .collect(Collectors.toMap(SkillTagDto::getName, Function.identity()));
        Iterable<SkillDto> skills = List.of(
            SkillDto.builder().name("C++")
                .tags(Set.of(tagsByName.get(TagsNames.PROGRAMMING_LANGUAGES)))
                .build(),
            SkillDto.builder().name("Java")
                .tags(Set.of(tagsByName.get(TagsNames.PROGRAMMING_LANGUAGES)))
                .build(),
            SkillDto.builder().name("SQL - Structured Query Language")
                .tags(Set.of(tagsByName.get(TagsNames.PROGRAMMING_LANGUAGES)))
                .build(),
            SkillDto.builder().name("English")
                .tags(Set.of(tagsByName.get(TagsNames.LANGUAGES)))
                .build(),
            new SkillDto(null, "Korean", tagsByName.get(TagsNames.PROGRAMMING_LANGUAGES)),
            SkillDto.builder().name("Communication").build(),
            SkillDto.builder().name("Testing").build(),
            SkillDto.builder().name("Open source").build(),
            SkillDto.builder().name("JUnit")
                .tags(Set.of(tagsByName.get(TagsNames.UNIT_TESTING), tagsByName.get("Java")))
                .build(),
            SkillDto.builder().name("Mockito")
                .tags(Set.of(tagsByName.get(TagsNames.UNIT_TESTING), tagsByName.get("Java"), tagsByName.get("Mocking libraries")))
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
                SkillTagDto.builder().name(TagsNames.PROGRAMMING_LANGUAGES).build(),
                SkillTagDto.builder().name(TagsNames.LANGUAGES).build(),
                SkillTagDto.builder().name("Project Management").build(),
                SkillTagDto.builder().name("Tools").build(),
                SkillTagDto.builder().name(TagsNames.UNIT_TESTING).build(),
                SkillTagDto.builder().name("Java").build(),
                SkillTagDto.builder().name("Mocking libraries").build(),
                SkillTagDto.builder().name("Performance testing tools").build()
            )
            .stream()
            .map(item -> skillService
                .saveSkillTag(item)
                .orElseThrow()
            )
            .collect(Collectors.toSet());
    }

    private List<SkillGroupDto> createSkillGroups(List<SkillDto> skills) {
        return Stream.of(
                SkillGroupDto.builder().name("Some group empty group without a description").build(),
                SkillGroupDto.builder().name("Another empty group a description indeed")
                    .description("This is some useful description which will help you know what is this for")
                    .build(),
                SkillGroupDto.builder().name("Group number 1 22 333")
                    .description("Lorem ipsum dolor sit amet ")
                    .skills(getShuffleCopy(skills).stream().limit(5).collect(Collectors.toSet()))
                    .build(),
                SkillGroupDto.builder().name("Group eternal duck green")
                    .skills(getShuffleCopy(skills).stream().limit(5).collect(Collectors.toSet()))
                    .build(),
                SkillGroupDto.builder().name("Group abcdefg hijklmn")
                    .skills(getShuffleCopy(skills).stream().limit(10).collect(Collectors.toSet()))
                    .build()
            )
            .map(skillService::saveGroup)
            .map(Optional::orElseThrow)
            .toList();
    }

    private void setDeparmentSkillsGroups(List<SkillGroupDto> skillGroups) {
        departmentService.findAllDepartment().forEach(d -> {
            List<SkillGroupDto> skillGroupsToAdd = getShuffleCopy(skillGroups).stream()
                .limit(randomUpTo(5)).toList();
            d.getSkillGroups().addAll(skillGroupsToAdd);
            departmentService.saveDepartment(d);
        });
    }

    private static <R> Collection<R> getShuffleCopy(Collection<R> source) {
        var copy = new ArrayList<>(source);
        Collections.shuffle(copy);
        return copy;
    }


    private void createSkillTagsRandom() {
        int nElements = 11;
        Set<SkillTagDto> tags = new LinkedHashSet<>(nElements);
        for (int i = 0; i < nElements; i++) {
            String name = String.format("Tag %03d", i);
            tags.add(
                SkillTagDto.builder().name(name).build()
            );
        }
        for (SkillTagDto tag : tags) {
            skillService.saveSkillTag(tag)
                .orElseThrow();
        }
    }

    private void createSkillsRandom() {
        int nElements = 11;
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

    private static int randomUpTo(int max) {
        return new SecureRandom().nextInt(max) + 1;
    }

    private static int randomLvl() {
        return randomUpTo(5);
    }

    // Constants

    private static class TagsNames {
        public static final String PROGRAMMING_LANGUAGES = "Programming Languages";
        public static final String LANGUAGES = "Languages";
        public static final String UNIT_TESTING = "Unit Testing";
    }
}