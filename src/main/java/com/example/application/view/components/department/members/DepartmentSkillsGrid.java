package com.example.application.view.components.department.members;

import com.example.application.dto.*;
import com.example.application.service.PersonSkillService;
import com.example.application.view.components.profile.SkillLevelSelector;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DepartmentSkillsGrid extends TreeGrid<DepartmentSkillRowData> {

    private final PersonSkillService personSkillService;
    private final NameFilter nameFilter = new NameFilter();

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
        // Build hierarchy: person -> skill group -> skill
        TreeData<DepartmentSkillRowData> data = new TreeData<>();
        for (DepartmentSkillRowData personRow : rootItems) {
            data.addItem(null, personRow);
            for (SkillGroupDto group : department.getSkillGroups()) {
                DepartmentSkillRowData groupRow = new DepartmentSkillRowData(personRow.person(), group);
                data.addItem(personRow, groupRow);
                Set<SkillDto> skills = group.getSkills();
                if (skills != null) {
                    for (SkillDto skill : skills) {
                        data.addItem(groupRow, new DepartmentSkillRowData(personRow.person(), group, skill));
                    }
                }
            }
        }
        TreeDataProvider<DepartmentSkillRowData> provider = new TreeDataProvider<>(data);
        setDataProvider(provider);
        nameFilter.updateDataAndApply(data, provider);
        expandAll();
    }

    public HorizontalLayout createExpandCollapseButtons() {
        Button expandButton = new Button(
            "Expand all", VaadinIcon.ANGLE_DOUBLE_DOWN.create(), e -> expandAll());
        expandButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        Button collapseButton = new Button(
            "Collapse all", VaadinIcon.ANGLE_DOUBLE_UP.create(), e1 -> collapseAll());
        collapseButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        return new HorizontalLayout(expandButton, collapseButton);
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

    public void setFilterText(String text) {
        nameFilter.setFilterText(text);
        nameFilter.apply();
        expandAll();
    }

    private static class NameFilter {
        private TreeData<DepartmentSkillRowData> treeData;
        private TreeDataProvider<DepartmentSkillRowData> treeDataProvider;
        private String filterText = "";

        void updateDataAndApply(TreeData<DepartmentSkillRowData> treeData, TreeDataProvider<DepartmentSkillRowData> treeDataProvider) {
            this.treeData = treeData;
            this.treeDataProvider = treeDataProvider;
            apply();
        }

        void setFilterText(String rawValue) {
            this.filterText = rawValue == null ? "" : rawValue.trim().toLowerCase();
        }

        private boolean hasNoFilter() {
            return filterText == null || filterText.isBlank();
        }

        void apply() {
            if (treeDataProvider == null) {
                return;
            }
            if (hasNoFilter()) {
                treeDataProvider.setFilter(null);
            } else {
                treeDataProvider.setFilter(this::matchesCurrentOrAnyParentOrChild);
            }
        }

        private boolean matchesCurrentOrAnyParentOrChild(DepartmentSkillRowData row) {
            return dataMatches(row)
                || anyParentMatches(row) || anyChildMatches(row);
        }

        private boolean dataMatches(DepartmentSkillRowData row) {
            return stringMatches(row.getDisplayName());
        }

        private boolean stringMatches(String value) {
            return hasNoFilter()
                || (value != null && value.toLowerCase().contains(filterText));
        }

        private boolean anyParentMatches(DepartmentSkillRowData row) {
            if (treeData == null) {
                return false;
            }
            DepartmentSkillRowData parent = treeData.getParent(row);
            while (parent != null) {
                if (dataMatches(parent)) {
                    return true;
                }
                parent = treeData.getParent(parent);
            }
            return false;
        }

        private boolean anyChildMatches(DepartmentSkillRowData row) {
            if (treeData == null) {
                return false;
            }
            return treeData.getChildren(row).stream()
                .anyMatch(child -> dataMatches(child) || anyChildMatches(child));
        }
    }

}
