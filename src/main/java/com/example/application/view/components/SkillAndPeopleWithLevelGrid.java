package com.example.application.view.components;

import com.example.application.dto.PersonDto;
import com.example.application.dto.PersonWithLevelDto;
import com.example.application.dto.SkillAndPeopleWithLevel;
import com.example.application.dto.SkillDto;
import com.example.application.service.PersonSkillService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

import static com.example.application.view.ViewUtils.getLevelIndicatorSvgPath;

public class SkillAndPeopleWithLevelGrid extends Grid<SkillAndPeopleWithLevel> {
    @NonNull
    private final PersonSkillService personSkillService;

    public SkillAndPeopleWithLevelGrid(@NonNull PersonSkillService personSkillService) {
        this.personSkillService = personSkillService;
        addColumn(
            skillAndPeopleWithLevel -> skillAndPeopleWithLevel.getSkill().getName()
        ).setHeader("Skill");
        addColumn(peopleRenderer).setHeader("People");
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

    private static final ComponentRenderer<Div, SkillAndPeopleWithLevel> peopleRenderer =
        new ComponentRenderer<>(skillAndPeopleWithLevel -> {
            var componentDiv = new Div();
            List<PersonWithLevelDto> peopleWithLevel = skillAndPeopleWithLevel.getPeopleWithLevel();
            for (var personAndLevel : peopleWithLevel) {
                Integer skillLevel = personAndLevel.getLevel();
                Div levelIndicatorDiv = new Div(
                    new Image(getLevelIndicatorSvgPath(skillLevel), "level " + skillLevel)
                );
                levelIndicatorDiv.getStyle().set("padding-bottom", "var(--lumo-space-s");
                PersonDto person = personAndLevel.getPerson();
                componentDiv.add(
                    new Text(person.getDisplayName()),
                    levelIndicatorDiv
                );
            }
            return componentDiv;
        });

}
