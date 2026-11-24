package io.skillia.mapper;

import io.skillia.dto.PersonSkillBasicDto;
import io.skillia.dto.PersonSkillIdDto;
import io.skillia.dto.main.*;
import io.skillia.persistence.entity.*;
import org.mapstruct.*;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IDtoEntityMapper {
    // Person <-> PersonDto

    @Mapping(target = "personSkills", ignore = true)
    Person toPerson(PersonDto personDto);

    PersonDto toPersonDto(Person person);

    // Skill <-> SkillDto

    SkillDto toSkillDto(Skill skill);

    Skill toSkill(SkillDto skillDto);

    @AfterMapping
    default void toSkillAfterMapping(SkillDto skillDto, @MappingTarget Skill skill) {
        Objects.requireNonNull(skill.getName());
    }

    // PersonSkill <-> PersonSkillBasicDto
    // AcquiredSkillDto <- PersonSkill
    // PersonWithSkillsDto <- Person + List<PersonSkill> // List<PersonWithSkillsDto> <- List<PersonSkill>
    // PersonSkill.Id <- PersonSkillIdDto

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
        if (personSkillList == null)
            return null;
        // Group by Person
        Map<Person, List<PersonSkill>> skillsByPerson = personSkillList.stream()
            .collect(Collectors.groupingBy(
                PersonSkill::getPerson, LinkedHashMap::new, Collectors.toList()
            ));
        // Map to List<PersonWithSkillsDto>
        return skillsByPerson.entrySet().stream()
            .map(entry -> toPersonWithSkillsDto(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    PersonSkill.Id toPersonSkillId(PersonSkillIdDto personSkillIdDto);

    // SkillTag <-> SkillTagDto

    SkillTagDto toSkillTagDto(SkillTag skillTag);

    SkillTag toSkillTag(SkillTagDto skillTagDto);

    // Department <-> DepartmentDto

    Department toEntity(DepartmentDto departmentDto);

    DepartmentDto toDto(Department department);

    // SkillGroup <-> SkillGroupDto

    SkillGroup toEntity(SkillGroupDto skillGroupDto);

    SkillGroupDto toDto(SkillGroup skillGroup);
}