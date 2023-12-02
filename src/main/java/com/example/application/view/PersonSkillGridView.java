package com.example.application.view;

import com.example.application.dto.PersonSkillBasicDto;
import com.example.application.service.PersonSkillService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@PermitAll
@Route(layout = MainLayout.class, value = "personskillgrid")
public class PersonSkillGridView extends VerticalLayout {

    private final PersonSkillService personSkillService;

    public PersonSkillGridView(PersonSkillService personSkillService) {
        this.personSkillService = personSkillService;
        createUi();
    }

    private void createUi() {
        setSizeFull();
        Grid<PersonSkillBasicDto> personSkillGrid = new Grid<>(PersonSkillBasicDto.class, false);
        personSkillGrid.addColumn(PersonSkillBasicDto::getPersonId).setHeader("Person id");
        personSkillGrid.addColumn(PersonSkillBasicDto::getSkillId).setHeader("Skill id");
        personSkillGrid.addColumn(PersonSkillBasicDto::getLevel).setHeader("Level");

        List<PersonSkillBasicDto> personSkillAll = personSkillService.getAllPersonSkillBasic();
        personSkillGrid.setItems(personSkillAll);

        add(personSkillGrid);
    }
}
