package com.example.application.view;

import com.example.application.dto.SkillTagDto;
import com.example.application.security.SecConstants;
import com.example.application.service.SkillTagService;
import com.example.application.utils.ValidationConstraints;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_WARNING;

@RolesAllowed(SecConstants.HR)
@Route(layout = MainLayout.class, value = "skilltags")
@PageTitle("Tags")
public class SkillTagView extends VerticalLayout {

    private final SkillTagService skillTagService;

    public SkillTagView(SkillTagService skillTagService) {
        this.skillTagService = skillTagService;
        createUi();
    }

    private void createUi() {
        setSizeFull();
        Grid<SkillTagDto> grid = new Grid<>(SkillTagDto.class, false);

        Grid.Column<SkillTagDto> nameColumn = grid.addColumn(SkillTagDto::getName)
            .setHeader("Name")
            .setKey("name")
            .setSortable(true);

        List<SkillTagDto> items = skillTagService.getAllSkillTag();
        // If the item collection is not mutable, we'll have troubles adding data dynamically
        List<SkillTagDto> fixedItems = new ArrayList<>(items);
        GridListDataView<SkillTagDto> dataView = grid.setItems(fixedItems);

        HeaderRow headerRow = grid.appendHeaderRow();
        SkillTagFilter skillTagFilter = new SkillTagFilter(dataView);
        headerRow.getCell(nameColumn).setComponent(
            ViewUtils.createFilterTextField("Search", skillTagFilter::setName)
        );
        createActionsColumn(grid);

        add(createAddTagButton(grid.getListDataView()));

        add(grid);
    }


    private Component createAddTagButton(GridListDataView<SkillTagDto> listDataView) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create tag");
        TextField nameField = new TextField("Tag name");
        nameField.setRequired(true);
        nameField.setMaxLength(ValidationConstraints.SkillTag.NAME_MAX_LENGTH);
        Binder<SkillTagDto> binder = new Binder<>(SkillTagDto.class);
        binder.forField(nameField)
                .asRequired("Name is required")
                .bind(SkillTagDto::getName, SkillTagDto::setName);
        dialog.add(new FormLayout(nameField));

        Button createButton = new Button("Create", e -> {
            SkillTagDto inputSkillTag = new SkillTagDto();
            try {
                binder.writeBean(inputSkillTag);
            } catch (ValidationException ex) {
                ViewUtils.notificationTopCenter("Please fill in the required fields correctly", false).open();
                return;
            }
            Optional<SkillTagDto> newItem = skillTagService.saveSkillTag(inputSkillTag);
            if (newItem.isPresent()) {
                listDataView.addItem(newItem.get());
            } else {
                ViewUtils.notificationTopCenter(new Div(
                    new Div("Unable to create the tag"),
                    new Div(" \"" + inputSkillTag.getName() + "\" "),
                    new Div("It may already exist")
                ), LUMO_WARNING).open();
            }
            binder.getFields().forEach(HasValue::clear);
            dialog.close();
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickShortcut(Key.ENTER);
        Button cancelButton = new Button("Cancel",
                e -> dialog.close()
        );
        dialog.getFooter().add(cancelButton, createButton);

        Button addSkillButton = new Button("Add tag", VaadinIcon.PLUS.create(),
                e -> dialog.open()
        );
        addSkillButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return addSkillButton;
    }

    private void createActionsColumn(Grid<SkillTagDto> grid) {
        grid.addComponentColumn(selectedTag -> {
            // Edit
            Dialog editDialog = createEditDialog(selectedTag, grid);
            Button editButton = new Button(VaadinIcon.EDIT.create(),
                    e -> editDialog.open()
            );
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            // Delete
            ConfirmDialog deleteDialog = createDeleteDialog(selectedTag, grid);
            Button deleteButton = new Button(VaadinIcon.TRASH.create(),
                e -> deleteDialog.open()
            );
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            // Result component
            HorizontalLayout buttonsLayout = new HorizontalLayout(editButton, deleteButton);
            buttonsLayout.setSpacing(false);
            return buttonsLayout;
        })
            .setHeader("Actions")
            .setKey("actions")
            .setAutoWidth(true)
            .setFlexGrow(0);
    }

    private ConfirmDialog createDeleteDialog(
            SkillTagDto selectedItem,
            Grid<SkillTagDto> grid
    ) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete tag \"" + selectedItem.getName() + "\"");
        confirmDialog.setText(
                "Are you sure you want to permanently delete this item?\r\n"
                        + "It will be no longer be linked to any skill."
        );
        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(e -> {
            skillTagService.deleteSkillTagById(selectedItem.getId());
            grid.getListDataView().removeItem(selectedItem);
        });
        confirmDialog.setCancelable(true);
        return confirmDialog;
    }

    private Dialog createEditDialog(
            SkillTagDto selectedItem,
            Grid<SkillTagDto> grid
    ) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit tag \"" + selectedItem.getName() + "\"");
        TextField nameTextField = new TextField("Tag name");
        Binder<SkillTagDto> binder = new Binder<>(SkillTagDto.class);
        binder.forField(nameTextField)
                .asRequired()
                .bind(SkillTagDto::getName, SkillTagDto::setName);
        binder.readBean(selectedItem);
        dialog.add(new FormLayout(nameTextField));

        Button saveButton = new Button("Save", e -> {
            SkillTagDto inputSkillTag = new SkillTagDto();
            try {
                binder.writeBean(inputSkillTag);
            } catch (ValidationException ex) {
                ViewUtils.notificationTopCenter("Please fill in the required fields correctly", false).open();
                return;
            }
            Optional<SkillTagDto> updatedSkill = skillTagService.updateSkillTag(
                inputSkillTag.getName(), selectedItem.getId()
            );
            if (updatedSkill.isPresent()) {
                selectedItem.setName(inputSkillTag.getName());
                // TODO IMPROVEMENT (FIX) not updating the name in the grid with `refreshItem` which would be better than `refreshAll`
                grid.getDataProvider().refreshAll();
                binder.getFields().forEach(HasValue::clear);
                dialog.close();
            } else {
                ViewUtils.notificationTopCenter(new Div(
                    new Div("Unable to update the tag"),
                    new Div(" \"" + inputSkillTag.getName() + "\" "),
                    new Div("It may already exist")
                ), LUMO_WARNING).open();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickShortcut(Key.ENTER);
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        return dialog;
    }


    private static class SkillTagFilter {
        private final GridListDataView<SkillTagDto> dataView;
        private String name;

        public SkillTagFilter(GridListDataView<SkillTagDto> dataView) {
            this.dataView = dataView;
            this.dataView.addFilter(this::test);
        }

        public void setName(String name) {
            this.name = name;
            dataView.refreshAll();
        }

        private boolean test(SkillTagDto skillTagDto) {
            return matches(skillTagDto.getName(), name);
        }

        private static boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                || (value != null && value.toLowerCase().contains(searchTerm.toLowerCase()));
        }
    }
}
