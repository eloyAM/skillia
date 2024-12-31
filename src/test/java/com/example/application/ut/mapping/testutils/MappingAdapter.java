package com.example.application.ut.mapping.testutils;

import com.example.application.dto.*;
import com.example.application.entity.*;
import com.example.application.mapper.DtoEntityMapping;
import com.example.application.mapper.IDtoEntityMapper;

import java.util.List;

public final class MappingAdapter implements IDtoEntityMapper {

    private static IDtoEntityMapper INSTANCE = null;

    private MappingAdapter() {
    }

    public static synchronized IDtoEntityMapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MappingAdapter();
        }
        return INSTANCE;
    }

    @Override
    public Person toPerson(PersonDto personDto) {
        return DtoEntityMapping.mapPersonDtoToPersonEntity(personDto);
    }

    @Override
    public PersonDto toPersonDto(Person person) {
        return DtoEntityMapping.mapPersonEntityToPersonDto(person);
    }

    @Override
    public Person toPerson(String personId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public SkillDto toSkillDto(Skill skill) {
        return DtoEntityMapping.mapSkillEntityToSkillDto(skill);
    }

    @Override
    public Skill toSkill(SkillDto skillDto) {
        return DtoEntityMapping.mapSkillDtoToSkillEntity(skillDto);
    }

    @Override
    public Skill toSkill(Long skillId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PersonSkill toPersonSkill(PersonSkillBasicDto personDto) {
        return DtoEntityMapping.mapPersonSkillDtoToPersonSkillEntity(personDto);
    }

    @Override
    public PersonSkillBasicDto toPersonSkillBasicDto(PersonSkill personSkill) {
        return DtoEntityMapping.mapPersonSkillEntityToPersonSkillDto(personSkill);
    }

    @Override
    public AcquiredSkillDto toAcquiredSkillDto(PersonSkill personSkill) {
        return DtoEntityMapping.mapPersonSkillEntityToAcquiredSkillDto(personSkill);
    }

    @Override
    public List<AcquiredSkillDto> toListAcquiredSkillDto(PersonSkill personSkill) {
        return IDtoEntityMapper.super.toListAcquiredSkillDto(personSkill);
    }

    @Override
    public PersonWithSkillsDto toPersonWithSkillsDto(PersonSkill personSkill) {
        // return DtoEntityMapping.mapPersonSkillEntityToPersonWithSkillsDto(personSkill);  // Not used elsewhere
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PersonWithSkillsDto toPersonWithSkillsDto(Person person, List<PersonSkill> personSkillList) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<PersonWithSkillsDto> toListPersonWithSkillsDto(List<PersonSkill> personSkillList) {
        return DtoEntityMapping.mapPersonSkillEntityToPersonWithSkillsDto(personSkillList);
    }

    @Override
    public List<AcquiredSkillDto> toListAcquiredSkillDto(List<PersonSkill> personSkills) {
        return personSkills.stream()
            .map(DtoEntityMapping::mapPersonSkillEntityToAcquiredSkillDto)
            .toList();
    }

    @Override
    public PersonSkillId toPersonSkillId(PersonSkillIdDto personSkillIdDto) {
        return DtoEntityMapping.mapPersonSkillIdDtoToPersonSkillIdEntity(personSkillIdDto);
    }

    @Override
    public SkillTagDto toSkillTagDto(SkillTag skillTag) {
        return DtoEntityMapping.mapSkillTagEntityToSkillTagDto(skillTag);
    }

    @Override
    public SkillTag toSkillTag(SkillTagDto skillTagDto) {
        return DtoEntityMapping.mapSkillTagDtoToSkillTagEntity(skillTagDto);
    }
}