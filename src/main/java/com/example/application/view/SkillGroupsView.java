package com.example.application.view;

import com.example.application.dto.SkillDto;
import com.example.application.dto.SkillGroupDto;
import com.example.application.service.SkillGroupService;
import com.example.application.service.SkillService;
import com.example.application.utils.ValidationConstraints;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RolesAllowed("HR")
@Route(layout = MainLayout.class, value = "skillgroups")
public class SkillGroupsView extends VerticalLayout {

    private final SkillGroupService skillGroupService;
    private final SkillService skillService;

    public SkillGroupsView(
        SkillGroupService skillGroupService,
        SkillService skillService
    ) {
        this.skillGroupService = skillGroupService;
        this.skillService = skillService;
        createUi();
    }

    private void createUi() {
        setSizeFull();
        Grid<SkillGroupDto> grid = createGrid();
        add(createAddGroupButton(grid), grid);
    }

    private Grid<SkillGroupDto> createGrid() {
        Grid<SkillGroupDto> grid = new Grid<>(SkillGroupDto.class, false);

        Grid.Column<SkillGroupDto> nameColumn = grid.addColumn(SkillGroupDto::getName)
            .setHeader("Name")
            .setKey("name")
            .setSortable(true);

        List<SkillGroupDto> items = skillGroupService.getAllGroups();
        // If the item collection is not mutable, we'll have troubles adding data dynamically
        ArrayList<SkillGroupDto> fixedItems = new ArrayList<>(items);
        GridListDataView<SkillGroupDto> dataView = grid.setItems(fixedItems);

        HeaderRow headerRow = grid.appendHeaderRow();
        SkillGroupFilter skillGroupFilter = new SkillGroupFilter(dataView);
        headerRow.getCell(nameColumn).setComponent(
            ViewUtils.createFilterTextField("Search", skillGroupFilter::setName)
        );

        grid.addColumn(SkillGroupDto::getDescription)
            .setHeader("Description")
            .setKey("description");

        ValueProvider<SkillGroupDto, Object> skillsColumnValueProvider = group -> group.getSkills().stream()
            .map(SkillDto::getName)
            .collect(Collectors.joining(", "));
        grid.addColumn(skillsColumnValueProvider)
            .setHeader("Skills")
            .setKey("skills");

        createActionsColumn(grid);
        return grid;
    }

    private void createActionsColumn(Grid<SkillGroupDto> grid) {
        grid.addComponentColumn(group -> {
                // Edit
                Button editButton = new Button(VaadinIcon.EDIT.create(),
                    e -> openAddOrEditGroupDialog(group, grid)
                );
                editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                // Delete
                Button deleteButton = new Button(VaadinIcon.TRASH.create(),
                    e -> openDeleteGroupDialog(group, grid)
                );
                deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
                // Result component
                return new Div(editButton, deleteButton);
            })
            .setHeader("Actions")
            .setKey("actions")
            .setAutoWidth(true)
            .setFlexGrow(0);
    }

    private Button createAddGroupButton(Grid<SkillGroupDto> grid) {
        Button addGroupButton = new Button("Add Group", VaadinIcon.PLUS.create(), e -> openAddOrEditGroupDialog(null, grid));
        addGroupButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return addGroupButton;
    }

    private void openAddOrEditGroupDialog(@Nullable SkillGroupDto selectedItem, Grid<SkillGroupDto> grid) {
        boolean isCreationMode = selectedItem == null;

        FormLayout formLayout = new FormLayout();
        Binder<SkillGroupDto> binder = new Binder<>(SkillGroupDto.class);
        // Form fields
        {
            TextField nameField = new TextField("Name");
            nameField.setRequired(true);
            nameField.setMaxLength(ValidationConstraints.SkillGroup.NAME_MAX_LENGTH);

            TextArea descriptionField = new TextArea("Description");
            descriptionField.setMaxLength(ValidationConstraints.SkillGroup.DESCRIPTION_MAX_LENGTH);

            MultiSelectComboBox<SkillDto> skillSelector = new MultiSelectComboBox<>("Skills");
            skillSelector.setItems(skillService.getAllSkill());
            skillSelector.setItemLabelGenerator(SkillDto::getName);

            binder.forField(nameField).asRequired("Name is required").bind(SkillGroupDto::getName, SkillGroupDto::setName);
            binder.forField(descriptionField).bind(SkillGroupDto::getDescription, SkillGroupDto::setDescription);
            binder.forField(skillSelector).bind(
                group -> group.getSkills() == null ? new HashSet<>() : new HashSet<>(group.getSkills()),
                (group, skills) -> group.setSkills(List.copyOf(skills))
            );

            formLayout.add(nameField, descriptionField, skillSelector);
        }

        // Set the fields with the selected item
        if (selectedItem != null) {
            binder.readBean(selectedItem);
        }

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Save", e -> {
            // Merge the selected item with the new values from the form
            // The selected item, if present, will get updated directly
            // But we still need to refresh the grid manually
            var inputItem = Optional.ofNullable(selectedItem).orElse(new SkillGroupDto());
            try {
                binder.writeBean(inputItem);
            } catch (ValidationException ex) {
                ViewUtils.notificationTopCenter("Please fill in the required fields correctly", false).open();
                return;
            }
            Optional<SkillGroupDto> savedGroup = skillGroupService.saveGroup(inputItem);
            if (savedGroup.isPresent()) {
                ViewUtils.notificationTopCenter("Group saved successfully", true).open();
                // Refresh the grid after adding/modifying a record
                if (isCreationMode) {
                    grid.getListDataView().addItem(savedGroup.get());
                }
                grid.getListDataView().refreshAll();
                // Close the dialog
                dialog.close();
            } else {
                ViewUtils.notificationTopCenter("Failed to save the group", false).open();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(formLayout);
        dialog.setHeaderTitle((selectedItem == null || selectedItem.getId() == null) ? "Create Group" : "Edit Group");
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteGroupDialog(
        SkillGroupDto selectedItem,
        Grid<SkillGroupDto> grid
    ) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete group \"" + selectedItem.getName() + "\"");
        confirmDialog.setText(
            "Are you sure you want to permanently delete this item?\r\n"
                + "It will no affect to any skill."
        );
        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(e -> {
            skillGroupService.deleteGroupById(selectedItem.getId());
            grid.getListDataView().removeItem(selectedItem);
        });
        confirmDialog.setCancelable(true);
        confirmDialog.open();
    }

    // Grid filter

    private static class SkillGroupFilter {
        private final GridListDataView<SkillGroupDto> dataView;
        private String name;

        public SkillGroupFilter(GridListDataView<SkillGroupDto> dataView) {
            this.dataView = dataView;
            this.dataView.addFilter(this::test);
        }

        public void setName(String name) {
            this.name = name;
            dataView.refreshAll();
        }

        private boolean test(SkillGroupDto skillTagDto) {
            return matches(skillTagDto.getName(), name);
        }

        private static boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                || (value != null && value.toLowerCase().contains(searchTerm.toLowerCase()));
        }
    }
}
