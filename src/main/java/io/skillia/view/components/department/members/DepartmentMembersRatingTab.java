package io.skillia.view.components.department.members;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.skillia.dto.main.DepartmentDto;
import io.skillia.dto.main.PersonDto;
import io.skillia.dto.main.PersonWithSkillsDto;
import io.skillia.service.DepartmentService;
import io.skillia.service.PersonService;
import io.skillia.service.PersonSkillService;
import io.skillia.view.utils.ViewUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DepartmentMembersRatingTab extends VerticalLayout {
    public static final String PARAM_DEPARTMENT_NAME = "departmentName";

    private final transient DepartmentService departmentService;
    private final transient PersonService personService;
    private final transient PersonSkillService personSkillService;

    // Content
    private final ComboBox<DepartmentDto> departmentSelector;
    private DepartmentSkillsGrid membersGrid;
    private HorizontalLayout expandCollapseButtons;
    private TextField nameTextFieldFilter;
    private DepartmentDto currentDepartment;

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

    private TextField createGridFilter() {
        TextField textField = ViewUtils.createFilterTextField("Search", str ->
            // Trigger data update & filter application (refreshing to ensure the data is not outdated)
            updateSkillsGrid(currentDepartment)
        );
        textField.setValueChangeTimeout(500);
        textField.setTooltipText("Filter by person name, skill group name or skill name");
        return textField;
    }

    private ComboBox<DepartmentDto> createDepartmentSelector() {
        ComboBox<DepartmentDto> selector = new ComboBox<>("Department");
        selector.setPlaceholder("Select a department");
        selector.setClearButtonVisible(true);
        selector.setWidthFull();
        selector.setMaxWidth("100%");
        selector.setItemLabelGenerator(DepartmentDto::getName);
        selector.setItems(departmentService.findAllDepartment());
        selector.addValueChangeListener(event -> {
            DepartmentDto department = event.getValue();
            if (department != null) {
                updateSkillsGrid(department);
                ViewUtils.updateQueryParameter(PARAM_DEPARTMENT_NAME, department.getName());
            } else {
                removeGridElements();
                ViewUtils.removeQueryParameter(PARAM_DEPARTMENT_NAME);
            }
        });
        return selector;
    }

    public void selectDepartmentByName(String departmentName) {
        departmentService.findDepartmentByName(departmentName).ifPresent(departmentSelector::setValue);
    }

    // Initialize or update grid stuff
    private void updateSkillsGrid(DepartmentDto department) {
        this.currentDepartment = department;
        List<PersonWithSkillsDto> personWithSkillsDtos = getAllDepartmentPeopleAndSkills(department);
        if (membersGrid == null) {  // Initialize
            nameTextFieldFilter = createGridFilter();
            membersGrid = new DepartmentSkillsGrid(department, personSkillService, personWithSkillsDtos);
            expandCollapseButtons = membersGrid.createExpandCollapseButtons();
            add(nameTextFieldFilter, expandCollapseButtons, membersGrid);
        } else {
            membersGrid.setDepartment(department, personWithSkillsDtos);
            if (nameTextFieldFilter.getValue() != null) {
                membersGrid.setFilterText(nameTextFieldFilter.getValue());
            }
        }
    }

    private void removeGridElements() {
        if (nameTextFieldFilter != null) {
            remove(nameTextFieldFilter);
            nameTextFieldFilter = null;
        }
        if (expandCollapseButtons != null) {
            remove(expandCollapseButtons);
            expandCollapseButtons = null;
        }
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
