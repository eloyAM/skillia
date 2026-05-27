package com.example.application.view.components;

import com.example.application.dto.SkillDto;
import com.example.application.dto.SkillGroupDto;
import com.example.application.service.SkillService;
import com.example.application.utils.ValidationConstraints;
import com.example.application.utils.Validators;
import com.example.application.view.utils.ViewUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNullElseGet;

@CssImport("./styles/responsive-action-buttons.css")
public class SkillGroupsTab extends VerticalLayout {

    private final SkillService skillService;

    public SkillGroupsTab(
        SkillService skillService
    ) {
        this.skillService = skillService;
        //
        createUi();
    }

    private void createUi() {
        setSizeFull();
        Grid<SkillGroupDto> grid = createGrid();
        add(createAddGroupButton(grid), grid);
    }

    private Grid<SkillGroupDto> createGrid() {
        Grid<SkillGroupDto> grid = new Grid<>(SkillGroupDto.class, false);
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        Grid.Column<SkillGroupDto> nameColumn = grid.addComponentColumn(group -> {
                var nameDiv = new Div(group.getName());
                nameDiv.setTitle(nameDiv.getText());
                nameDiv.getStyle()
                    .set("font-weight", "600");
                var descriptionDiv = new Div(group.getDescription());
                descriptionDiv.getStyle()
                    .set("font-size", "var(--lumo-font-size-s)")
                    .setColor("var(--lumo-secondary-text-color)");
                var result = new VerticalLayout(nameDiv, descriptionDiv);
                result.setSpacing(false);
                result.getThemeList().add("spacing-xs");
                return result;
            })
            .setHeader("Name")
            .setKey("name")
            .setComparator(SkillGroupDto::getName);

        List<SkillGroupDto> items = skillService.getAllGroups();
        // If the item collection is not mutable, we'll have troubles adding data dynamically
        GridListDataView<SkillGroupDto> dataView = grid.setItems(new ArrayList<>(items));

        HeaderRow headerRow = grid.appendHeaderRow();
        SkillGroupFilter skillGroupFilter = new SkillGroupFilter(dataView);
        headerRow.getCell(nameColumn).setComponent(
            ViewUtils.createFilterTextField("Search", skillGroupFilter::setGroupNameOrDescription)
        );

        Grid.Column<SkillGroupDto> skillsColumn = grid.addComponentColumn(ViewUtils::skillsAsBadges)
            .setHeader("Skills")
            .setKey("skills");
        headerRow.getCell(skillsColumn).setComponent(
            ViewUtils.createFilterTextField("Search", skillGroupFilter::setSkillName)
        );


        createActionsColumn(grid);

        // Resize listener to adapt column widths
        getElement().executeJs(
            "globalThis.addEventListener('resize', () => { $0.recalculateColumnWidths(); });",
            grid.getElement()
        );

        return grid;
    }

    private void createActionsColumn(Grid<SkillGroupDto> grid) {
        grid.addComponentColumn(group -> {
                // Mobile view: ContextMenu
                Button contextMenuButton = new Button(VaadinIcon.ELLIPSIS_DOTS_V.create());
                contextMenuButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                contextMenuButton.addClassName("mobile-actions");

                ContextMenu contextMenu = new ContextMenu(contextMenuButton);
                contextMenu.setOpenOnClick(true);
                MenuItem editItem = ViewUtils.createIconItem(contextMenu, VaadinIcon.EDIT, "Edit",
                    e -> openAddOrEditGroupDialog(group, grid));
                editItem.getElement().getStyle().set("color", "var(--lumo-primary-text-color)");
                MenuItem deleteItem = ViewUtils.createIconItem(contextMenu, VaadinIcon.TRASH, "Delete",
                    e -> openDeleteGroupDialog(group, grid));
                deleteItem.getElement().getStyle().set("color", "var(--lumo-error-text-color)");

                // Desktop view: Buttons
                Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openAddOrEditGroupDialog(group, grid));
                editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> openDeleteGroupDialog(group, grid));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
                HorizontalLayout desktopActions = new HorizontalLayout(editButton, deleteButton);
                desktopActions.setSpacing(false);
                desktopActions.addClassName("desktop-actions");

                Div container = new Div(contextMenuButton, desktopActions);
                container.addClassName("actions-container");
                return container;
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
        TextField nameField = new TextField("Name");
        nameField.setRequired(true);
        nameField.setMaxLength(ValidationConstraints.SkillGroup.NAME_MAX_LENGTH);

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setMaxLength(ValidationConstraints.SkillGroup.DESCRIPTION_MAX_LENGTH);

        MultiSelectComboBox<SkillDto> skillSelector = ViewUtils
            .createMultiSelectComboBox(skillService::getAllSkill, SkillDto::getName, null);
        skillSelector.setLabel("Skills");

        binder.forField(nameField).asRequired("Name is required").bind(SkillGroupDto::getName, SkillGroupDto::setName);
        binder.forField(descriptionField).bind(SkillGroupDto::getDescription, SkillGroupDto::setDescription);
        binder.forField(skillSelector).bind(
            group -> requireNonNullElseGet(group.getSkills(), HashSet::new),
            SkillGroupDto::setSkills
        );

        formLayout.add(nameField, descriptionField, skillSelector);

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
            Optional<SkillGroupDto> savedGroup = skillService.saveGroup(inputItem);
            if (savedGroup.isPresent()) {
                // Refresh the grid after adding/modifying a record
                if (isCreationMode) {
                    grid.getListDataView().addItem(savedGroup.get());
                }
                final String successMessage = "Skill group \"" + savedGroup.get().getName()
                    + (isCreationMode ? "\" created" : "\" updated");
                ViewUtils.notificationTopCenter(successMessage, true).open();
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
        cancelButton.addClassNames("cancel-button");
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
            try {
                skillService.deleteGroupById(selectedItem.getId());
            } catch (Exception ex) {
                ViewUtils.notificationTopCenter("Unexpected error.", NotificationVariant.LUMO_ERROR).open();
                throw new RuntimeException(ex);
            }
            ViewUtils.notificationTopCenter("Skill group \"" + selectedItem.getName() + "\" deleted",
                NotificationVariant.LUMO_SUCCESS).open();
            grid.getListDataView().removeItem(selectedItem);
        });
        confirmDialog.setCancelable(true);
        confirmDialog.open();
    }

    // Grid filter

    private static class SkillGroupFilter {
        private final GridListDataView<SkillGroupDto> dataView;
        private String skillName;
        private String groupNameOrDescription;

        public SkillGroupFilter(GridListDataView<SkillGroupDto> dataView) {
            this.dataView = dataView;
            this.dataView.addFilter(this::test);
        }

        public void setSkillName(String skillName) {
            this.skillName = skillName;
            dataView.refreshAll();
        }

        public void setGroupNameOrDescription(String searchTerm) {
            this.groupNameOrDescription = searchTerm;
            dataView.refreshAll();
        }

        private boolean test(SkillGroupDto group) {
            return (matches(group.getName(), groupNameOrDescription)
                || matches(group.getDescription(), groupNameOrDescription)
            ) && matchesGroupSkills(group);
        }

        private boolean matchesGroupSkills(SkillGroupDto group) {
            return Validators.isNullOrEmpty(skillName)
                || group.getSkills().stream().anyMatch(skill -> matches(skill.getName(), skillName));
        }

        private static boolean matches(String value, String searchTerm) {
            return Validators.isNullOrEmpty(searchTerm)
                || (value != null && value.toLowerCase().contains(searchTerm.toLowerCase()));
        }

    }
}
