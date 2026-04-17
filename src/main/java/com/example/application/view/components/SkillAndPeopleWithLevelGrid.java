package com.example.application.view.components;

import com.example.application.dto.PersonWithLevelDto;
import com.example.application.dto.SkillAndPeopleWithLevel;
import com.example.application.dto.SkillDto;
import com.example.application.service.PersonSkillService;
import com.example.application.view.utils.ViewUtils;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class SkillAndPeopleWithLevelGrid extends Grid<SkillAndPeopleWithLevel> {
    @NonNull
    private final PersonSkillService personSkillService;

    public SkillAndPeopleWithLevelGrid(@NonNull PersonSkillService personSkillService) {
        this.personSkillService = personSkillService;

        addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        addColumn(skillAndPeopleWithLevel -> skillAndPeopleWithLevel.getSkill().getName())
            .setHeader("Skill")
            .setFlexGrow(1);
        addColumn(ViewUtils.skillLevelIndicatorRendererForSkillAndPeopleWithLevel())
            .setHeader("People")
            .setFlexGrow(2);
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
