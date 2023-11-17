package com.example.application.view;

import com.example.application.dto.PersonDto;
import com.example.application.service.PersonService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@PermitAll
@Route(layout = MainLayout.class, value = "persongrid")
@PageTitle("Person Grid | Vaadin Demo")
public class PersonGridView extends VerticalLayout {

    private final PersonService personService;

    public PersonGridView(PersonService personService) {
        this.personService = personService;
        createUi();
    }

    private void createUi() {
        Grid<PersonDto> personGrid = new Grid<>(PersonDto.class, false);
        personGrid.addColumn(PersonDto::getUsername).setHeader("Username");
        personGrid.addColumn(PersonDto::getDisplayName).setHeader("Name");
        personGrid.addColumn(PersonDto::getEmail).setHeader("Email");
        personGrid.addColumn(PersonDto::getTitle).setHeader("Title");
        personGrid.addColumn(PersonDto::getDepartment).setHeader("Department");

        List<PersonDto> personAll = personService.findAllPerson();
        personGrid.setItems(personAll);

        add(personGrid);
    }

}
