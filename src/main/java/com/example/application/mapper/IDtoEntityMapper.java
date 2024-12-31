package com.example.application.mapper;

import com.example.application.dto.*;
import com.example.application.entity.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

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

    // Person entity from username
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "username", source = "personId")
    Person toPerson(String personId);

    // Skill

    SkillDto toSkillDto(Skill skill);

    @Mapping(target = "personSkills", ignore = true)
    Skill toSkill(SkillDto skillDto);

    // Skill entity from id
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "id", source = "skillId")
    Skill toSkill(Long skillId);

    // PersonSkill

    @Mapping(target = "personSkillId.personId", source = "personId")
    @Mapping(target = "personSkillId.skillId", source = "skillId")
    @Mapping(target = "person", source = "personId")
    @Mapping(target = "skill", source = "skillId")
    PersonSkill toPersonSkill(PersonSkillBasicDto personDto);

    @Mapping(target = "personId", source = "personSkillId.personId")
    @Mapping(target = "skillId", source = "personSkillId.skillId")
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
            if (!skillsByPerson.containsKey(person)) {
                skillsByPerson.put(person, new ArrayList<>());
            }
            skillsByPerson.get(person).add(personSkillItem);
        }

        List<PersonWithSkillsDto> list = new ArrayList<>(skillsByPerson.size());
        for (Map.Entry<Person, List<PersonSkill>> entry : skillsByPerson.entrySet()) {
            PersonWithSkillsDto personWithSkillsDto = toPersonWithSkillsDto(entry.getKey(), entry.getValue());
            list.add(personWithSkillsDto);
        }

        return list;
    }

    // Not implemented on the manual mapping
    // PersonSkillIdDto toPersonSkillIdDto(PersonSkillId personSkillId);

    PersonSkillId toPersonSkillId(PersonSkillIdDto personSkillIdDto);

    // SkillTag

    SkillTagDto toSkillTagDto(SkillTag skillTag);

    SkillTag toSkillTag(SkillTagDto skillTagDto);
}