package com.example.application.ut.mapping;

import com.example.application.dto.AcquiredSkillDto;
import com.example.application.dto.PersonWithSkillsDto;
import com.example.application.dto.SkillTagDto;
import com.example.application.entity.Person;
import com.example.application.entity.PersonSkill;
import com.example.application.entity.Skill;
import com.example.application.entity.SkillTag;
import com.example.application.mapper.IDtoEntityMapper;
import com.example.application.ut.mapping.testutils.MappingAdapter;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class IDtoEntityMapperTest {

    static Stream<IDtoEntityMapper> mappers() {
        return Stream.of(
            Mappers.getMapper(IDtoEntityMapper.class),
            MappingAdapter.getInstance()
        );
    }

    //
    // Tests
    //

    // Test SkillTag to SkillTagDto mapping
    @Test
    void testSkillTag_NoFields_Builder() {
        // Given
        SkillTag.SkillTagBuilder skillTag = SkillTag.builder();
        // When
        ThrowableAssert.ThrowingCallable action = skillTag::build;
        // Then
        assertThatThrownBy(action)
            .isInstanceOf(NullPointerException.class)
            .hasMessage(nullFieldMessage("name"));
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSkillTagToSkillTagDto_NoFields_Constructor(IDtoEntityMapper mapper) {
        // Given
        SkillTag skillTag = new SkillTag();
        // When
        SkillTagDto skillTagDto = mapper.toSkillTagDto(skillTag);
        // Then
        assertNotNull(skillTagDto);
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSkillTagToSkillTagDto_OnlyRequiredFields(IDtoEntityMapper mapper) {
        // Given
        SkillTag skillTag = new SkillTag();
        skillTag.setName("Java");
        // When
        SkillTagDto skillTagDto = mapper.toSkillTagDto(skillTag);
        // Then
        assertNotNull(skillTagDto);
        assertEquals("Java", skillTagDto.getName());
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSkillTagToSkillTagDto_AllFields(IDtoEntityMapper mapper) {
        // Given
        SkillTag skillTag = new SkillTag();
        skillTag.setName("Java");
        skillTag.setId(1L);
        // When
        SkillTagDto skillTagDto = mapper.toSkillTagDto(skillTag);
        // Then
        assertNotNull(skillTagDto);
        assertEquals("Java", skillTagDto.getName());
        assertEquals(1L, skillTagDto.getId());
    }

    // Test SkillTagDto to SkillTag mapping
    @ParameterizedTest
    @MethodSource("mappers")
    void testSkillTagDtoToSkillTag_OnlyRequiredFields(IDtoEntityMapper mapper) {
        // Given
        SkillTagDto skillTagDto = new SkillTagDto();
        skillTagDto.setName("Java");
        // When
        SkillTag skillTag = mapper.toSkillTag(skillTagDto);
        // Then
        assertNotNull(skillTag);
        assertEquals(skillTagDto.getName(), skillTag.getName());
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSkillTagDtoToSkillTag_AllFields(IDtoEntityMapper mapper) {
        // Given
        SkillTagDto skillTagDto = new SkillTagDto();
        skillTagDto.setName("Java");
        skillTagDto.setId(1L);
        // When
        SkillTag skillTag = mapper.toSkillTag(skillTagDto);
        // Then
        assertNotNull(skillTag);
        assertEquals("Java", skillTag.getName());
        assertEquals(1L, skillTag.getId());
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void toListPersonWithSkillsDto_shouldMapListOfPersonSkillsToListOfPersonWithSkillsDto(IDtoEntityMapper mapper) {
        // Given
        List<PersonSkill> personSkills = new ArrayList<>(2);
        {
            Person person1 = new Person();
            person1.setUsername("person1");
            Skill skill1 = new Skill();
            skill1.setName("skill1");
            PersonSkill person1Skill1 = new PersonSkill();
            person1Skill1.setPerson(person1);
            person1Skill1.setSkill(skill1);
            person1Skill1.setLevel(1);
            personSkills.add(person1Skill1);
        }
        {
            Person person2 = new Person();
            person2.setUsername("person2");
            Skill skill2 = new Skill();
            skill2.setName("skill2");
            PersonSkill person2Skill2 = new PersonSkill();
            person2Skill2.setPerson(person2);
            person2Skill2.setSkill(skill2);
            person2Skill2.setLevel(2);
            personSkills.add(person2Skill2);
        }
        {
            Person person3 = new Person();
            person3.setUsername("person3");
            Skill skill3 = new Skill();
            skill3.setName("skill3");
            PersonSkill person3Skill3 = new PersonSkill();
            person3Skill3.setPerson(person3);
            person3Skill3.setSkill(skill3);
            person3Skill3.setLevel(3);
            personSkills.add(person3Skill3);

            Skill skill4 = new Skill();
            skill4.setName("skill4");
            PersonSkill person3Skill4 = new PersonSkill();
            person3Skill4.setPerson(person3);
            person3Skill4.setSkill(skill4);
            person3Skill4.setLevel(4);
            personSkills.add(person3Skill4);
        }

        // When
        List<PersonWithSkillsDto> result = mapper.toListPersonWithSkillsDto(personSkills);

        // Then
        assertNotNull(result);
        assertThat(result).hasSize(3);  // The result should be grouped by person

        PersonWithSkillsDto resultFirst = result.get(0);
        assertEquals("person1", resultFirst.getPerson().getUsername());
        assertEquals(1, resultFirst.getSkills().size());
        AcquiredSkillDto resultSkillFirst = resultFirst.getSkills().get(0);
        assertEquals("skill1", resultSkillFirst.getSkill().getName());
        assertEquals(1, resultSkillFirst.getLevel());

        PersonWithSkillsDto resultSecond = result.get(1);
        assertEquals("person2", resultSecond.getPerson().getUsername());
        assertEquals(1, resultSecond.getSkills().size());
        AcquiredSkillDto resultSkillSecond = resultSecond.getSkills().get(0);
        assertEquals("skill2", resultSkillSecond.getSkill().getName());
        assertEquals(2, resultSkillSecond.getLevel());

        PersonWithSkillsDto resultThird = result.get(2);
        assertEquals("person3", resultThird.getPerson().getUsername());
        assertEquals(2, resultThird.getSkills().size());
        assertEquals("skill3", resultThird.getSkills().get(0).getSkill().getName());
        assertEquals(3, resultThird.getSkills().get(0).getLevel());
        assertEquals("skill4", resultThird.getSkills().get(1).getSkill().getName());
        assertEquals(4, resultThird.getSkills().get(1).getLevel());
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void toListPersonWithSkillsDto_shouldReturnEmptyListWhenInputIsEmpty(IDtoEntityMapper mapper) {
        // Given
        List<PersonSkill> input = Collections.emptyList();
        // When
        List<PersonWithSkillsDto> result = mapper.toListPersonWithSkillsDto(input);
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void toListPersonWithSkillsDto_shouldReturnNullWhenInputIsNull(IDtoEntityMapper mapper) {
        // Given
        List<PersonSkill> input = null;
        // When
        List<PersonWithSkillsDto> result = mapper.toListPersonWithSkillsDto(input);
        // Then
        assertNull(result);
    }

    //
    // Helpers
    //

    private static String nullFieldMessage(String field) {
        return field + " is marked non-null but is null";
    }
}