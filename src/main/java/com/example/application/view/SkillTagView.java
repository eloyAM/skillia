package com.example.application.view;

import com.example.application.dto.SkillTagDto;
import com.example.application.security.SecConstants;
import com.example.application.service.SkillTagService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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
            createFilterTextField("Search by name", skillTagFilter::setName)
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
        Binder<SkillTagDto> nameBinder = new Binder<>(SkillTagDto.class);
        nameBinder.forField(nameField)
                .asRequired()
                .bind(SkillTagDto::getName, SkillTagDto::setName);
        FormLayout formLayout = new FormLayout(nameField);
        dialog.add(formLayout);

        Button createButton = new Button("Create", e -> {
            String text = nameField.getValue();
            if (StringUtils.isBlank(text) || StringUtils.length(text) > 50) {
                return;
            }
            Optional<SkillTagDto> newItem = skillTagService.saveSkillTag(
                    new SkillTagDto().setName(text)
            );
            if (newItem.isPresent()) {
                listDataView.addItem(newItem.get());
            } else {
                ViewUtils.notificationTopCenter("The tag already exists", true).open();
            }
            nameField.clear();
            dialog.close();
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
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
        nameTextField.setValue(selectedItem.getName());
        Binder<SkillTagDto> sillBinder = new Binder<>(SkillTagDto.class);
        sillBinder.forField(nameTextField)
                .asRequired()
                .bind(SkillTagDto::getName, SkillTagDto::setName);
        FormLayout formLayout = new FormLayout(nameTextField);
        dialog.add(formLayout);

        Button saveButton = new Button("Save", e -> {
            String newName = nameTextField.getValue();
            if (StringUtils.isBlank(newName) || StringUtils.length(newName) > 50) {
                return;
            }
            Optional<SkillTagDto> updatedSkill = skillTagService.updateSkillTag(
                    newName, selectedItem.getId()
            );
            if (updatedSkill.isPresent()) {
                selectedItem.setName(newName);
                // TODO IMPROVEMENT (FIX) not updating the name in the grid with `refreshItem` which would be better than `refreshAll`
                grid.getDataProvider().refreshAll();
                nameTextField.clear();
                dialog.close();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        return dialog;
    }

    private static Component createFilterTextField(
        String placeHolderText,
        Consumer<String> filterChangeConsumer
    ) {
        TextField textField = new TextField();
        textField.setPrefixComponent(VaadinIcon.SEARCH.create());
        textField.setPlaceholder(placeHolderText);
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.setMaxWidth("100%");
        textField.addValueChangeListener(
            e -> filterChangeConsumer.accept(e.getValue())
        );

        return textField;
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
            boolean matchesName = matches(skillTagDto.getName(), name);
            // boolean matchesX = ...
            return matchesName; // && matchesX;
        }

        private static boolean matches(String value, String searchTerm) {
            return searchTerm == null
                || searchTerm.isEmpty()
                || value.toLowerCase().contains(searchTerm.toLowerCase());
        }
    }
}
