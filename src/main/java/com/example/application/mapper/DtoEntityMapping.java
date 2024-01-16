package com.example.application.mapper;

import com.example.application.dto.*;
import com.example.application.entity.Person;
import com.example.application.entity.PersonSkill;
import com.example.application.entity.PersonSkillId;
import com.example.application.entity.Skill;
import jakarta.annotation.Nonnull;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public final class DtoEntityMapping {
    public static Person mapPersonDtoToPersonEntity(PersonDto personDto) {
        return new Person(personDto.getUsername())
            .setEmail(personDto.getEmail())
            .setFullName(personDto.getFullName())
            .setDepartment(personDto.getDepartment())
            .setTitle(personDto.getTitle());
    }

    public static PersonDto mapPersonEntityToPersonDto(Person personEntity) {
        return new PersonDto(personEntity.getUsername())
            .setEmail(personEntity.getEmail())
            .setFullName(personEntity.getFullName())
            .setDepartment(personEntity.getDepartment())
            .setTitle(personEntity.getTitle());
    }

    public static SkillDto mapSkillEntityToSkillDto(Skill skillEntity) {
        return SkillDto.builder()
            .id(skillEntity.getId())
            .name(skillEntity.getName()).build();
    }

    public static Skill mapSkillDtoToSkillEntity(@Nonnull SkillDto skillDto) {
        return new Skill(skillDto.getId(), skillDto.getName());
    }

    public static PersonSkill mapPersonSkillDtoToPersonSkillEntity(PersonSkillBasicDto dto) {
        return new PersonSkill(dto.getPersonId(), dto.getSkillId(), dto.getLevel());
    }

    public static PersonSkillBasicDto mapPersonSkillEntityToPersonSkillDto(
        PersonSkill personSkill) {
        PersonSkillId personSkillId = personSkill.getPersonSkillId();
        return PersonSkillBasicDto.builder()
            .personId(personSkillId.getPersonId())
            .skillId(personSkillId.getSkillId())
            .level(personSkill.getLevel())
            .build();
    }

    public static AcquiredSkillDto mapPersonSkillEntityToAcquiredSkillDto(PersonSkill personSkill) {
        return AcquiredSkillDto.builder()
            .skill(mapSkillEntityToSkillDto(personSkill.getSkill()))
            .level(personSkill.getLevel())
            .build();
    }

    public static PersonWithSkillsDto mapPersonSkillEntityToPersonWithSkillsDto(
        PersonSkill personSkill) {
        return PersonWithSkillsDto.builder()
            .person(mapPersonEntityToPersonDto(personSkill.getPerson()))
            .skills(List.of(mapPersonSkillEntityToAcquiredSkillDto(personSkill)))
            .build();
    }

    public static List<PersonWithSkillsDto> mapPersonSkillEntityToPersonWithSkillsDto(
        List<PersonSkill> personSkillList
    ) {
        Map<Person, List<PersonSkill>> skillsByPerson = personSkillList.stream()
            .collect(Collectors.groupingBy(PersonSkill::getPerson));
        return skillsByPerson.entrySet().stream()
            .map(entry -> PersonWithSkillsDto.builder()
                .person(mapPersonEntityToPersonDto(entry.getKey()))
                .skills(entry.getValue().stream()
                    .map(ps -> AcquiredSkillDto.builder()
                        .skill(mapSkillEntityToSkillDto(ps.getSkill()))
                        .level(ps.getLevel())
                        .build())
                    .toList())
                .build()
            )
            .toList();
    }

    public static PersonSkillId mapPersonSkillIdDtoToPersonSkillIdEntity(PersonSkillIdDto personSkillIdDto) {
        return new PersonSkillId(personSkillIdDto.getPersonId(), personSkillIdDto.getSkillId());
    }
}
