package com.example.application.view.components.department.skillgroups;

import com.example.application.dto.main.DepartmentDto;
import com.example.application.dto.main.SkillGroupDto;
import com.example.application.service.DepartmentService;
import com.example.application.service.SkillService;
import com.example.application.view.utils.ViewUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@CssImport("./styles/vaadin-selector-elements.css")
public class DepartmentsSkillGroupsViewTab extends Grid<DepartmentDto> {

    private final DepartmentService departmentService;
    private final SkillService skillService;

    public DepartmentsSkillGroupsViewTab(DepartmentService departmentService, SkillService skillService) {
        this.departmentService = departmentService;
        this.skillService = skillService;
        createGrid();
    }

    private void createGrid() {
        Grid<DepartmentDto> grid = this;
        setSizeFull();
        List<DepartmentDto> departments = departmentService.findAllDepartment();
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setMinWidth(300, Unit.PIXELS);
        GridListDataView<DepartmentDto> gridListDataView = grid.setItems(departments);

        Column<DepartmentDto> departmentNameColumn = grid.addComponentColumn(department -> {
                Div nameDiv = new Div(department.getName());
                nameDiv.addClassNames(LumoUtility.FontWeight.SEMIBOLD);
                return nameDiv;
            })
            .setHeader("Name")
            .setKey("name")
            .setComparator(DepartmentDto::getName)
            .setFlexGrow(1);

        Column<DepartmentDto> skillGroupsColumn = grid.addComponentColumn(department ->
                department.getSkillGroups().stream()
                    .map(group -> skillGroupDetails(group, true))
                    .collect(VerticalLayout::new, HasComponents::add, HasComponents::add)
            )
            .setHeader("Skill groups")
            .setKey("skill-groups")
            .setFlexGrow(2);

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
    }

    private void openSkillGroupDialog(DepartmentDto selectedItem, Grid<?> grid) {
        Dialog dialog = new Dialog();

        MultiSelectListBox<SkillGroupDto> skillGroupListBox = new MultiSelectListBox<>();
        skillGroupListBox.setItemLabelGenerator(SkillGroupDto::getName);
        skillGroupListBox.setRenderer(new ComponentRenderer<>(group -> skillGroupDetails(group, false)));
        skillGroupListBox.setItems(skillService.getAllGroups());
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
                ViewUtils.notificationTopCenter("Some error occurred during the operation", false).open();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.setHeaderTitle("Select Skill Groups for \"" + selectedItem.getName() + "\"");
        dialog.add(skillGroupListBox);
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private static Component skillGroupDetails(SkillGroupDto group, boolean useDetailsComponent) {
        // Name text
        var nameComponent = new Div(group.getName());
        nameComponent.addClassNames(LumoUtility.TextColor.BODY);
        // Applied to the rest of the details component
        nameComponent.getStyle().set("white-space", "normal");
        nameComponent.getStyle().set("overflow-wrap", "break-word");

        // Description text
        String skillDescriptionStr = group.getDescription();
        String descText = Optional.ofNullable(skillDescriptionStr)
            .filter(s -> !s.isBlank()).orElse("No description available");
        var descriptionComponent = new Div(descText);
        descriptionComponent.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        // Details content -> description + skills as badges if present
        VerticalLayout detailsContent = new VerticalLayout(descriptionComponent);
        if (!group.getSkills().isEmpty()) {
            detailsContent.add(ViewUtils.skillsAsBadges(group));
        }
        detailsContent.setSpacing(false);
        detailsContent.getThemeList().add("spacing-xs");
        detailsContent.setPadding(false);

        if (useDetailsComponent) {
            return new Details(nameComponent, detailsContent);
        } else {
            // Style added though the CSS file
            VerticalLayout verticalLayout = new VerticalLayout(nameComponent, detailsContent);
            verticalLayout.setSpacing(false);
            verticalLayout.getThemeList().add("spacing-xs");
            verticalLayout.setPadding(false);
            return verticalLayout;
        }
    }

    // Filtering features
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
            return StringUtils.isBlank(this.skillGroupName)
                || department.getSkillGroups().stream()
                .anyMatch(e -> matches(e.getName(), this.skillGroupName));
        }

        private static boolean matches(String value, String searchTerm) {
            return StringUtils.isBlank(searchTerm)
                || (value != null && value.toLowerCase().contains(searchTerm.toLowerCase()));
        }
    }
}
