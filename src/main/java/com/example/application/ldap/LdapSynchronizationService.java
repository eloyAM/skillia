package com.example.application.ldap;

import com.example.application.dto.DepartmentDto;
import com.example.application.dto.PersonDto;
import com.example.application.repo.PersonRepo;
import com.example.application.service.DepartmentService;
import com.example.application.service.PersonService;
import com.example.application.utils.Validators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class LdapSynchronizationService {

    private final LdapClient ldapClient;
    private final PersonService personService;
    private final PersonRepo personRepo;
    private final DepartmentService departmentService;

    public LdapSynchronizationService(
        LdapClient ldapClient,
        PersonService personService,
        PersonRepo personRepo,
        DepartmentService departmentService
    ) {
        this.ldapClient = ldapClient;
        this.personService = personService;
        this.personRepo = personRepo;
        this.departmentService = departmentService;
    }

    public void loadUsersAndDepartmentsWithLdap() {
        log.info("Loading users from LDAP");
        List<PersonDto> ldapUsers = ldapClient.findAllUsers();
        log.info("Found {} users from LDAP, proceeding to save them on the users database", ldapUsers.size());
        // Straightforward users save as we already have the username, which is the PK
        personService.savePerson(ldapUsers);
        log.info("The users database has been saved successfully initialized");

        log.info("Loading departments from the users database");
        List<String> departmentNames = personRepo.findDistinctDepartments();
        log.info("Found a total of {} departments: {}", departmentNames.size(),
            joinStrings(departmentNames.stream()));
        // Save non existing departments
        List<DepartmentDto> savedDepartments = departmentNames.stream()
            .filter(Predicate.not(Validators::isNullOrEmpty))
            .map(departmentService::saveByNameIfDoesntExist)
            .filter(Objects::nonNull)
            .toList();
        log.info("A total of {} previously non-existing departments were added: {}", savedDepartments.size(),
            joinStrings(savedDepartments.stream().map(DepartmentDto::getName)));
    }

    private static String joinStrings(Stream<String> strings) {
        return strings
            .map(s -> "\"" + s + "\"")
            .collect(Collectors.joining(", ", "[", "]"));
    }
}

