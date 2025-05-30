package com.example.application.view.components;

import com.example.application.dto.AcquiredSkillDto;
import com.example.application.dto.PersonDto;
import com.example.application.dto.PersonWithSkillsDto;
import com.example.application.service.PersonSkillService;
import com.example.application.view.ViewUtils;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class PersonAndSkillsGrid extends Grid<PersonWithSkillsDto> {
    @NonNull
    private final PersonSkillService personSkillService;

    public PersonAndSkillsGrid(@NonNull PersonSkillService personSkillService) {
        this.personSkillService = personSkillService;

        addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        addColumn(personWithSkills -> personWithSkills.getPerson().getFullName())
            .setHeader("Person")
            .setFlexGrow(1);
        addColumn(ViewUtils.skillLevelIndicatorRendererForPersonWithSkills())
            .setHeader("Skills")
            .setFlexGrow(2);
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

}
