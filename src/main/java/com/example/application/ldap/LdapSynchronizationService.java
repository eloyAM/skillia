package com.example.application.ldap;

import com.example.application.dto.PersonDto;
import com.example.application.entity.Department;
import com.example.application.repo.DepartmentRepository;
import com.example.application.repo.PersonRepo;
import com.example.application.service.PersonService;
import com.example.application.utils.Validators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Service
public class LdapSynchronizationService {

    private final LdapClient ldapClient;
    private final PersonService personService;
    private final PersonRepo personRepo;
    private final DepartmentRepository departmentRepository;

    public LdapSynchronizationService(
            LdapClient ldapClient,
            PersonService personService,
            PersonRepo personRepo,
            DepartmentRepository departmentRepository
    ) {
        this.ldapClient = ldapClient;
        this.personService = personService;
        this.personRepo = personRepo;
        this.departmentRepository = departmentRepository;
    }

    public void loadUsersAndDepartmentsWithLdap() {
        log.info("Loading users from LDAP");
        List<PersonDto> ldapUsers = ldapClient.findAllUsers();
        log.info("Found {} users from LDAP, proceeding to save them on the users database", ldapUsers.size());
        personService.savePerson(ldapUsers);
        log.info("The users database has been saved successfully initialized");

        log.info("Loading departments from the users database");
        List<String> departmentNames = personRepo.findDistinctDepartments();
        log.info("Number of departments found : {}", departmentNames.size());
        List<Department> savedDepartments = departmentRepository.saveAll(
                departmentNames.stream()
                        .filter(Predicate.not(Validators::isNullOrEmpty))
                        .map(name -> {
                            Department department = new Department();
                            department.setName(name);
                            return department;
                        }).toList()
        );
        log.info("Created a total of {} departments", savedDepartments.size());
    }
}

