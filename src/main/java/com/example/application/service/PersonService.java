package com.example.application.service;

import com.example.application.dto.PersonDto;
import com.example.application.entity.Person;
import com.example.application.mapper.DtoEntityMapping;
import com.example.application.repo.PersonRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class PersonService {
    private final PersonRepo personRepo;

    public PersonService(PersonRepo personRepo) {
        this.personRepo = personRepo;
    }

    public PersonDto savePerson(PersonDto person) {
        Person personEntity = DtoEntityMapping.mapPersonDtoToPersonEntity(person);
        personEntity = personRepo.save(personEntity);
        return DtoEntityMapping.mapPersonEntityToPersonDto(personEntity);
    }

    public List<PersonDto> savePerson(Iterable<PersonDto> persons) {
        List<Person> personEntities = StreamSupport.stream(persons.spliterator(), false)
            .map(DtoEntityMapping::mapPersonDtoToPersonEntity)
            .toList();
        personEntities = personRepo.saveAll(personEntities);
        return personEntities.stream()
            .map(DtoEntityMapping::mapPersonEntityToPersonDto).toList();
    }

    @Secured("ROLE_DEVELOPERS")
    public List<PersonDto> findAllPerson() {
        return personRepo.findBy();
    }

    public List<PersonDto> findAllPerson(int pageNumber, int pageSize) {
        return personRepo.findBy(PageRequest.of(pageNumber, pageSize));
    }
}
