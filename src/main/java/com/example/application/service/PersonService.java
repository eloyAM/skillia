package com.example.application.service;

import com.example.application.dto.main.PersonDto;
import com.example.application.entity.Person;
import com.example.application.mapper.IDtoEntityMapper;
import com.example.application.repo.PersonRepo;
import com.example.application.service.utils.FunctionalUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonService {
    private final PersonRepo personRepo;
    private final IDtoEntityMapper dtoEntityMapper;

    public PersonService(
        PersonRepo personRepo,
        IDtoEntityMapper dtoEntityMapper
    ) {
        this.personRepo = personRepo;
        this.dtoEntityMapper = dtoEntityMapper;
    }

    public List<PersonDto> savePerson(Iterable<PersonDto> persons) {
        List<Person> personEntities = FunctionalUtils.iterableToStream(persons)
            .map(dtoEntityMapper::toPerson)
            .toList();
        personEntities = personRepo.saveAll(personEntities);
        return personEntities.stream()
            .map(dtoEntityMapper::toPersonDto).toList();
    }

    public List<PersonDto> findAllPerson() {
        return personRepo.findBy();
    }

    public PersonDto findPersonByUsername(String username) {
        return personRepo.findByUsername(username).map(dtoEntityMapper::toPersonDto).orElse(null);
    }

    public List<PersonDto> findPeopleByDepartment(String departmentName) {
        return personRepo.findAllByDepartment(departmentName);
    }

    public List<String> findDistinctDepartments() {
        return personRepo.findDistinctDepartments();
    }
}
