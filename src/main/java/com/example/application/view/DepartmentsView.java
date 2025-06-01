package com.example.application.view;

import com.example.application.dto.DepartmentDto;
import com.example.application.dto.SkillGroupDto;
import com.example.application.service.DepartmentService;
import com.example.application.service.SkillGroupService;
import com.example.application.utils.Validators;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RolesAllowed("HR")
@Route(layout = MainLayout.class, value = "departments")
@PageTitle("Departments")
public class DepartmentsView extends VerticalLayout {

    private final DepartmentService departmentService;
    private final SkillGroupService skillGroupService;

    public DepartmentsView(
        DepartmentService departmentService,
        SkillGroupService skillGroupService) {
        this.departmentService = departmentService;
        createUi();
        this.skillGroupService = skillGroupService;
    }

    private void createUi() {
        setSizeFull();

        List<DepartmentDto> departments = departmentService.findAllDepartment();
        Grid<DepartmentDto> grid = new Grid<>(DepartmentDto.class, false);
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        GridListDataView<DepartmentDto> gridListDataView = grid.setItems(departments);

        Grid.Column<DepartmentDto> departmentNameColumn = grid.addColumn(DepartmentDto::getName)
            .setHeader("Name")
            .setKey("name")
            .setSortable(true);

        Grid.Column<DepartmentDto> skillGroupsColumn = grid.addComponentColumn(department ->
                department.getSkillGroups().stream()
                    .map(SkillGroupDto::getName)
                    .map(Div::new)
                    .collect(VerticalLayout::new, HasComponents::add, HasComponents::add)
            )
            .setHeader("Skill groups")
            .setKey("skill-groups");

        grid.addComponentColumn(department -> {
                Button editButton = new Button(
                    VaadinIcon.EDIT.create(), e -> openSkillGroupDialog(department, grid));
                editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                return editButton;
            })
            .setHeader("Actions")
            .setKey("actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        HeaderRow headerRow = grid.appendHeaderRow();
        var gridFilter = new DepartmentGridFilter(gridListDataView);

        headerRow.getCell(departmentNameColumn).setComponent(
            ViewUtils.createFilterTextField("Search", gridFilter::setDepartmentName)
        );

        headerRow.getCell(skillGroupsColumn).setComponent(
            ViewUtils.createFilterTextField("Search", gridFilter::setSkillGroupName)
        );

        add(grid);
    }


    private void openSkillGroupDialog(DepartmentDto selectedItem, Grid<?> grid) {
        Dialog dialog = new Dialog();

        MultiSelectListBox<SkillGroupDto> skillGroupListBox = new MultiSelectListBox<>();
        skillGroupListBox.setItemLabelGenerator(SkillGroupDto::getName);
        skillGroupListBox.setItems(skillGroupService.getAllGroups());
        skillGroupListBox.setValue(
            Optional.ofNullable(selectedItem.getSkillGroups()).map(Set::copyOf).orElse(Set.of())
        );

        Button saveButton = new Button("Save", e -> {
            selectedItem.setSkillGroups(List.copyOf(skillGroupListBox.getSelectedItems()));
            Optional<DepartmentDto> savedItem = departmentService.saveDepartment(selectedItem);
            if (savedItem.isPresent()) {
                ViewUtils.notificationTopCenter("Updated successfully", true).open();
                grid.getListDataView().refreshAll();
                dialog.close();
            } else {
                ViewUtils.notificationTopCenter("Some error occurred during the operation", false);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.setHeaderTitle("Select Skill Groups for \"" + selectedItem.getName() + "\"");
        dialog.add(skillGroupListBox);
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private static class DepartmentGridFilter {
        private final GridListDataView<DepartmentDto> gridListDataView;
        private String departmentName;
        private String skillGroupName;

        public DepartmentGridFilter(GridListDataView<DepartmentDto> gridListDataView) {
            this.gridListDataView = gridListDataView;
            this.gridListDataView.addFilter(this::test);
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
            gridListDataView.refreshAll();
        }

        public void setSkillGroupName(String skillGroupName) {
            this.skillGroupName = skillGroupName;
            gridListDataView.refreshAll();
        }

        private boolean test(DepartmentDto department) {
            return matches(department.getName(), this.departmentName)
                && matchesSkillGroupName(department);
        }

        private boolean matchesSkillGroupName(DepartmentDto department) {
            return Validators.isNullOrEmpty(this.skillGroupName)
                || department.getSkillGroups().stream()
                .anyMatch(e -> matches(e.getName(), this.skillGroupName));
        }

        private static boolean matches(String value, String searchTerm) {
            return Validators.isNullOrEmpty(searchTerm)
                || (value != null && value.toLowerCase().contains(searchTerm.toLowerCase()));
        }
    }
}