package com.example.application.view.components.department.members;

import com.example.application.dto.*;
import com.example.application.service.PersonSkillService;
import com.example.application.view.components.profile.SkillLevelSelector;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DepartmentSkillsGrid extends TreeGrid<DepartmentSkillRowData> {

    private final PersonSkillService personSkillService;

    private DepartmentDto department;
    private Map<String, List<AcquiredSkillDto>> acquiredSkillsByPerson;
    private List<DepartmentSkillRowData> rootItems;

    public DepartmentSkillsGrid(DepartmentDto department, PersonSkillService personSkillService, List<PersonWithSkillsDto> departmentPeopleSkills) {
        this.personSkillService = personSkillService;
        setDepartment(department, departmentPeopleSkills, false);

        setWidthFull();
        addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        addClassNames("members-tree-grid");
        setMinWidth("500px");
        setMinHeight("400px");

        addComponentHierarchyColumn(row -> {
            Span span = new Span(row.getDisplayName());
            switch (row.getKind()) {
                case PERSON -> span.addClassNames(LumoUtility.FontWeight.SEMIBOLD);
                case SKILL_GROUP -> span.addClassNames(LumoUtility.TextColor.SECONDARY);
                case SKILL -> span.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.FontWeight.SEMIBOLD);
                default -> {    // No action
                }
            }
            return span;
        })
            .setHeader("Name")
            .setFlexGrow(1);

        addComponentColumn(this::renderLevelSelector)
            .setHeader("Level")
            .setAutoWidth(true)
            .setFlexGrow(2);

        setAndExpandItems(departmentPeopleSkills);
    }

    private void setAndExpandItems(List<PersonWithSkillsDto> departmentPeopleSkills) {
        this.rootItems = departmentPeopleSkills.stream()
            .map(PersonWithSkillsDto::getPerson).map(DepartmentSkillRowData::new).toList();
        // People as root items -> for each person -> the department skill groups will be loaded -> and for each group its skills
        setItems(this.rootItems, this::getChildren);
        expandAll();
    }

    public HorizontalLayout createExpandCollapseButtons() {
        return new HorizontalLayout(
            new Button("Expand all", e -> expandAll()),
            new Button("Collapse all", e1 -> collapseAll())
        );
    }

    private Component renderLevelSelector(DepartmentSkillRowData row) {
        if (row.skill() != null) {
            SkillDto skill = row.skill();
            PersonDto person = row.person();
            List<AcquiredSkillDto> acquiredSkills = acquiredSkillsByPerson.get(person.getUsername());
            // Find the current level if a rating exists, 0 otherwise
            int currentLevel = acquiredSkills.stream()
                .filter(as -> as.getSkill().getId().equals(skill.getId()))
                .findFirst()
                .map(AcquiredSkillDto::getLevel)
                .orElse(0);

            boolean allowedToEdit = true;   // Permissions already checked when accessing the page
            return new SkillLevelSelector(currentLevel, skill.getId(),
                person.getUsername(), personSkillService, allowedToEdit);
        }
        return new Span();
    }

    // Hierarchy: Person -> Skill Group -> Skill
    // Method called recursively
    private List<DepartmentSkillRowData> getChildren(DepartmentSkillRowData parent) {
        // Note : parent.person() always present
        if (parent.skillGroup() == null) { // Person level -> returns the department skill groups as the children
            return department.getSkillGroups().stream()
                .map(group -> new DepartmentSkillRowData(parent.person(), group))
                .toList();  // Empty list if no skill groups -> no more recursion
        } else if (parent.skill() == null) { // Skill group level -> returns its skills as the children
            Set<SkillDto> skills = parent.skillGroup().getSkills();
            if (skills != null) {
                return skills.stream()
                    .map(skill -> new DepartmentSkillRowData(parent.person(), parent.skillGroup(), skill))
                    .toList();
            } else {
                return List.of(); // Empty skill group -> no more children
            }
        } else {
            return List.of(); // Skill -> last level, no more children
        }
    }


    public void expandAll() {
        if (rootItems != null && !rootItems.isEmpty()) {
            expandRecursively(rootItems, Integer.MAX_VALUE);
        }
    }

    public void collapseAll() {
        if (rootItems != null && !rootItems.isEmpty()) {
            collapseRecursively(rootItems, Integer.MAX_VALUE);
        }
    }

    public void setDepartment(DepartmentDto department, List<PersonWithSkillsDto> departmentPeopleSkills) {
        setDepartment(department, departmentPeopleSkills, true);
    }

    public void setDepartment(DepartmentDto department, List<PersonWithSkillsDto> departmentPeopleSkills, boolean doExpand) {
        this.department = department;
        this.acquiredSkillsByPerson = departmentPeopleSkills.stream()
            .collect(Collectors.toMap(p -> p.getPerson().getUsername(), PersonWithSkillsDto::getSkills));
        if (doExpand) {
            setAndExpandItems(departmentPeopleSkills);
        }
    }
}
