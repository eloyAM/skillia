package com.example.application.view.components;

import com.example.application.dto.AcquiredSkillDto;
import com.example.application.dto.PersonDto;
import com.example.application.dto.PersonWithSkillsDto;
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

public class PersonAndSkillsGrid extends Grid<PersonWithSkillsDto> {
    @NonNull
    private final PersonSkillService personSkillService;

    public PersonAndSkillsGrid(@NonNull PersonSkillService personSkillService) {
        this.personSkillService = personSkillService;
        addColumn(
            personWithSkills -> personWithSkills.getPerson().getDisplayName()
        ).setHeader("Person");
        addColumn(skillsRenderer).setHeader("Skills");
    }

    /**
     * Update the grid with the given person if not null, otherwise clear the grid.
     */
    public void updateItemsFromDb(@Nullable PersonDto person) {
        if (person != null) {
            List<AcquiredSkillDto> personAndSkills =
                personSkillService.findAllAcquiredSkillByPersonId(person.getUsername());
            setItems(new PersonWithSkillsDto(person, personAndSkills));
        } else {
            setItems();
        }
    }

    private static final ComponentRenderer<Div, PersonWithSkillsDto> skillsRenderer =
        new ComponentRenderer<>(personWithSkills -> {
            var componentDiv = new Div();
            for (var personSkill : personWithSkills.getSkills()) {
                Integer skillLevel = personSkill.getLevel();
                Div levelIndicatorDiv = new Div(
                    new Image(getLevelIndicatorSvgPath(skillLevel), "level " + skillLevel)
                );
                levelIndicatorDiv.getStyle().set("padding-bottom", "var(--lumo-space-s");
                componentDiv.add(
                    new Text(personSkill.getSkill().getName()),
                    levelIndicatorDiv
                );
            }
            return componentDiv;
        });

}
