package io.skillia.view.components.department.members;

import io.skillia.dto.main.PersonDto;
import io.skillia.dto.main.SkillDto;
import io.skillia.dto.main.SkillGroupDto;

// Wrapper for the tree grid data
// Flattened representation of a "Person" being related to multiple "SkillGroup", each one with multiple "Skill"
public record DepartmentSkillRowData(
    // If Person + SkillGroup + Skill presents -> we are referring to a skill from a skill group related to the person
    PersonDto person,
    SkillGroupDto skillGroup,
    SkillDto skill
) {
    // If Person + SkillGroup presents -> we are referring to a skill group related to the person
    public DepartmentSkillRowData(PersonDto person, SkillGroupDto skillGroup) {
        this(person, skillGroup, null);
    }

    // If only Person present -> we are referring to the person
    public DepartmentSkillRowData(PersonDto person) {
        this(person, null, null);
    }

    public String getDisplayName() {
        if (skill != null) {
            return skill.getName();
        }
        if (skillGroup != null) {
            return skillGroup.getName();
        }
        return person.getFullName();
    }

    public Kind getKind() {
        if (skill != null) {
            return Kind.SKILL;
        }
        if (skillGroup != null) {
            return Kind.SKILL_GROUP;
        }
        return Kind.PERSON;
    }

    public enum Kind {
        SKILL,
        SKILL_GROUP,
        PERSON
    }

}