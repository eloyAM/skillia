package com.example.application.mapper;

import com.example.application.dto.PersonSkillBasicDto;
import com.example.application.dto.PersonSkillIdDto;
import com.example.application.dto.main.*;
import com.example.application.persistence.entity.*;
import org.mapstruct.*;

import java.util.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IDtoEntityMapper {
    // Person

    @Mapping(target = "personSkills", ignore = true)
    Person toPerson(PersonDto personDto);

    PersonDto toPersonDto(Person person);

    // Skill

    SkillDto toSkillDto(Skill skill);

    Skill toSkill(SkillDto skillDto);

    @AfterMapping
    default void toSkillAfterMapping(SkillDto skillDto, @MappingTarget Skill skill) {
        Objects.requireNonNull(skill.getName());
    }

    // PersonSkill

    @Mapping(target = "personSkillId.personId", source = "personId")
    @Mapping(target = "personSkillId.skillId", source = "skillId")
    @Mapping(target = "person.username", source = "personId")
    @Mapping(target = "skill.id", source = "skillId")
    @Mapping(target = "lastModifiedDate", ignore = true)
    PersonSkill toPersonSkill(PersonSkillBasicDto personDto);

    @InheritInverseConfiguration
    PersonSkillBasicDto toPersonSkillBasicDto(PersonSkill personSkill);

    AcquiredSkillDto toAcquiredSkillDto(PersonSkill personSkill);

    List<AcquiredSkillDto> toListAcquiredSkillDto(List<PersonSkill> personSkills);

    @Mapping(target = "skills", source = "personSkillList")
    PersonWithSkillsDto toPersonWithSkillsDto(Person person, List<PersonSkill> personSkillList);

    default List<PersonWithSkillsDto> toListPersonWithSkillsDto(List<PersonSkill> personSkillList) {
        if (personSkillList == null) {
            return null;
        }

        Map<Person, List<PersonSkill>> skillsByPerson = new LinkedHashMap<>();
        for (PersonSkill personSkillItem : personSkillList) {
            Person person = personSkillItem.getPerson();
            skillsByPerson.computeIfAbsent(person, k -> new ArrayList<>());
            skillsByPerson.get(person).add(personSkillItem);
        }

        List<PersonWithSkillsDto> list = new ArrayList<>(skillsByPerson.size());
        for (Map.Entry<Person, List<PersonSkill>> entry : skillsByPerson.entrySet()) {
            PersonWithSkillsDto personWithSkillsDto = toPersonWithSkillsDto(entry.getKey(), entry.getValue());
            list.add(personWithSkillsDto);
        }

        return list;
    }

    PersonSkillId toPersonSkillId(PersonSkillIdDto personSkillIdDto);

    // SkillTag

    SkillTagDto toSkillTagDto(SkillTag skillTag);

    SkillTag toSkillTag(SkillTagDto skillTagDto);

    // Department

    Department toEntity(DepartmentDto departmentDto);

    DepartmentDto toDto(Department department);

    // SkillGroup

    SkillGroup toEntity(SkillGroupDto skillGroupDto);

    SkillGroupDto toDto(SkillGroup skillGroup);
}