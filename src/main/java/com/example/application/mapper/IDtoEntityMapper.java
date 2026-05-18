package com.example.application.mapper;

import com.example.application.dto.*;
import com.example.application.entity.*;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface IDtoEntityMapper {
    // Person

    @Mapping(target = "personSkills", ignore = true)
    Person toPerson(PersonDto personDto);

    PersonDto toPersonDto(Person person);

    // Skill

    SkillDto toSkillDto(Skill skill);

    Skill toSkill(SkillDto skillDto);

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

    default List<AcquiredSkillDto> toListAcquiredSkillDto(PersonSkill personSkill) {
        if (personSkill == null) {
            return null;
        }
        List<AcquiredSkillDto> list = new ArrayList<>(1);
        list.add(toAcquiredSkillDto(personSkill));
        return list;
    }

    List<AcquiredSkillDto> toListAcquiredSkillDto(List<PersonSkill> personSkills);

    // Was implemented on the manual mapping, but never used
    // // @Mapping(target = "skills", expression = "java(java.util.Collections.singletonList(toAcquiredSkillDto(personSkill)))")
    @Mapping(target = "skills", source = "personSkill")
    PersonWithSkillsDto toPersonWithSkillsDto(PersonSkill personSkill);

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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Department partialUpdate(DepartmentDto departmentDto, @MappingTarget Department department);

    // SkillGroup

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Skill partialUpdate(SkillDto skillDto, @MappingTarget Skill skill);

    SkillGroup toEntity(SkillGroupDto skillGroupDto);

    SkillGroupDto toDto(SkillGroup skillGroup);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    SkillGroup partialUpdate(SkillGroupDto skillGroupDto, @MappingTarget SkillGroup skillGroup);
}