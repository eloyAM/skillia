package com.example.application.view.components;

import com.example.application.dto.PersonWithLevelDto;
import com.example.application.dto.SkillAndPeopleWithLevel;
import com.example.application.dto.SkillDto;
import com.example.application.service.PersonSkillService;
import com.example.application.view.ViewUtils;
import com.vaadin.flow.component.grid.Grid;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class SkillAndPeopleWithLevelGrid extends Grid<SkillAndPeopleWithLevel> {
    @NonNull
    private final PersonSkillService personSkillService;

    public SkillAndPeopleWithLevelGrid(@NonNull PersonSkillService personSkillService) {
        this.personSkillService = personSkillService;
        addColumn(
            skillAndPeopleWithLevel -> skillAndPeopleWithLevel.getSkill().getName()
        ).setHeader("Skill");
        addColumn(ViewUtils.skillLevelIndicatorRendererForSkillAndPeopleWithLevel())
            .setHeader("People");
    }

    /**
     * Update the grid with the given skill if not null, otherwise clear the grid.
     */
    public void updateItemsFromDb(@Nullable SkillDto skill) {
        if (skill != null) {
            List<PersonWithLevelDto> peopleWithSkillList =
                personSkillService.findAllPersonWithLevelBySkillId(skill.getId());
            setItems(new SkillAndPeopleWithLevel(skill, peopleWithSkillList));
        } else {
            setItems();
        }
    }

}
