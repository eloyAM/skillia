package com.example.application.view.components.department.members;

import com.example.application.dto.DepartmentDto;
import com.example.application.dto.PersonDto;
import com.example.application.dto.PersonWithSkillsDto;
import com.example.application.service.DepartmentService;
import com.example.application.service.PersonService;
import com.example.application.service.PersonSkillService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DepartmentMembersRatingTab extends VerticalLayout {

    private final transient DepartmentService departmentService;
    private final transient PersonService personService;
    private final transient PersonSkillService personSkillService;

    // Content
    private final ComboBox<DepartmentDto> departmentSelector;
    private DepartmentSkillsGrid membersGrid;

    public DepartmentMembersRatingTab(DepartmentService departmentService, PersonService personService, PersonSkillService personSkillService) {
        this.departmentService = departmentService;
        this.personService = personService;
        this.personSkillService = personSkillService;

        setSizeFull();

        // The selector will be in charge of the membersGrid's content
        departmentSelector = createDepartmentSelector();
        departmentSelector.getStyle().setPaddingTop("0px");
        add(departmentSelector);
    }

    private ComboBox<DepartmentDto> createDepartmentSelector() {
        ComboBox<DepartmentDto> selector = new ComboBox<>("Department");
        selector.setPlaceholder("Select a department");
        selector.setClearButtonVisible(true);
        selector.setWidthFull();
        selector.setMaxWidth("100%");
        selector.setItemLabelGenerator(DepartmentDto::getName);
        selector.setItems(departmentService.findAllDepartment());
        selector.addValueChangeListener(event -> Optional.ofNullable(event.getValue())
            .ifPresentOrElse(this::updateSkillsGrid, this::removeSkillsGrid));
        return selector;
    }

    private void updateSkillsGrid(DepartmentDto department) {
        List<PersonWithSkillsDto> personWithSkillsDtos = getAllDepartmentPeopleAndSkills(department);
        if (membersGrid == null) {
            membersGrid = new DepartmentSkillsGrid(department, personSkillService, personWithSkillsDtos);
            add(membersGrid.createExpandCollapseButtons());
            add(membersGrid);
        } else {
            membersGrid.setDepartment(department, personWithSkillsDtos);
        }
    }

    private void removeSkillsGrid() {
        if (membersGrid != null) {
            remove(membersGrid);
            membersGrid = null;
        }
    }

    private List<PersonWithSkillsDto> getAllDepartmentPeopleAndSkills(DepartmentDto department) {
        // All the people related to the department
        List<PersonDto> people = personService.findPeopleByDepartment(department.getName());
        // People with ratings
        List<PersonWithSkillsDto> departmentPeopleSkillsOriginal = personSkillService.getAllPersonSkillForDepartment(department.getName());

        // Return both -> all department members (with and without ratings)
        Map<String, PersonWithSkillsDto> skillsByUsername = departmentPeopleSkillsOriginal.stream()
            .collect(Collectors.toMap(pws -> pws.getPerson().getUsername(), Function.identity()));
        return people.stream()
            .map(person -> skillsByUsername.getOrDefault(person.getUsername(),
                new PersonWithSkillsDto(person, List.of())
            ))
            .toList();
    }
}
