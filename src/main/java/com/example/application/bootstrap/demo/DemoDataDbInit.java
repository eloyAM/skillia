package com.example.application.bootstrap.demo;

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
 * Note:
 * This creates some demo data in addition to the initialization already done by the {@link com.example.application.bootstrap.ImportLdapUsersToDbAppRunner}
 */
public class DemoDataDbInit {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PersonService personService;
    private final SkillService skillService;
    private final PersonSkillRepo personSkillRepo;
    private final DepartmentService departmentService;

    public DemoDataDbInit(
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
                .description("General-purpose programming language created as an extension of the C programming language, or \"C with Classes\". It has imperative, object-oriented and generic programming features, while also providing facilities for low-level memory manipulation.")
                .tags(Set.of(tagsByName.get(TagsNames.PROGRAMMING_LANGUAGES)))
                .build(),
            SkillDto.builder().name("Java")
                .description("Knowledge of the language and its ecosystem, such as libraries, frameworks, and tools commonly used. E.g. Maven, JUnit, Mockito, JPA, Spring, Tomcat, Swing, remote debugging, etc.")
                .tags(Set.of(tagsByName.get(TagsNames.PROGRAMMING_LANGUAGES)))
                .build(),
            SkillDto.builder().name("SQL - Structured Query Language")
                .description("General notion of SQL: queries, functions, views, database design (constraints, indexes), etc. May involve some experience with specific RDBMS such as MySQL, PostgreSQL, SQL Server or Oracle.")
                .tags(Set.of(tagsByName.get(TagsNames.PROGRAMMING_LANGUAGES)))
                .build(),
            SkillDto.builder().name("English")
                .tags(Set.of(tagsByName.get(TagsNames.LANGUAGES)))
                .build(),
            new SkillDto(null, "Korean", tagsByName.get(TagsNames.PROGRAMMING_LANGUAGES)),
            SkillDto.builder().name("Communication")
                .description("Effective communication skills, including verbal and written communication, active listening, and the ability to convey complex ideas clearly and concisely.")
                .tags(Set.of(tagsByName.get("Soft skills")))
                .build(),
            SkillDto.builder().name("Testing")
                .description("General testing knowledge, such as different types of testing (unit, integration, end-to-end, performance, etc.), testing strategies, test automation, common tools such as Selenium, Cucumber, JUnit, Postman, JMeter, etc.")
                .build(),
            SkillDto.builder().name("Open source")
                .description("Contributing to open source projects, such as submitting pull requests, reporting issues, or participating in discussions in open source communities.")
                .build(),
            SkillDto.builder().name("JUnit")
                .description("Writing and running unit tests using the JUnit framework, including test annotations, assertions, test suites, and integration with build tools like Maven or Gradle.")
                .tags(Set.of(tagsByName.get(TagsNames.UNIT_TESTING), tagsByName.get("Java")))
                .build(),
            SkillDto.builder().name("Mockito")
                .description("Stubbing and verification of mocks during unit tests.")
                .tags(Set.of(tagsByName.get(TagsNames.UNIT_TESTING), tagsByName.get("Java"), tagsByName.get("Mocking libraries")))
                .build(),
            SkillDto.builder().name("MS Project")
                .description("Usage of Microsoft Project for project management, such as creating and managing project plans, timelines, resources, and tasks.")
                .tags(Set.of(tagsByName.get("Project Management"), tagsByName.get("Tools")))
                .build()
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
                SkillTagDto.builder().name("Performance testing tools").build(),
                SkillTagDto.builder().name("Soft skills").build()
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
        tags.forEach(tag -> skillService.saveSkillTag(tag).orElseThrow());
    }

    private void createSkillsRandom() {
        int nElements = 11;
        Set<SkillDto> skills = new LinkedHashSet<>(nElements);
        for (int i = 0; i < nElements; i++) {
            String name = String.format("Skill %03d", i);
            String desc = String.format("Description for skill %03d", i);
            skills.add(
                SkillDto.builder().name(name).description(desc).build()
            );
        }
        skills.forEach(s -> skillService.saveSkill(s).orElseThrow());
    }

    private static int randomUpTo(int max) {
        return RANDOM.nextInt(max) + 1;
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