package com.example.application.view;

import com.example.application.dto.SkillDto;
import com.example.application.security.SecConstants;
import com.example.application.service.SkillService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

@RolesAllowed(SecConstants.HR)
@Route(layout = MainLayout.class, value = "skillsmanagement")
public class SkillsManagementView extends VerticalLayout {

    private final SkillService skillService;

    public SkillsManagementView(SkillService skillService) {
        this.skillService = skillService;
        createUi();
    }

    private void createUi() {
        Grid<SkillDto> skillGrid = new Grid<>(SkillDto.class, false);

        skillGrid.addColumn(SkillDto::getName)
            .setHeader("Name")
            .setKey("name");
        GridListDataView<SkillDto> listDataView = skillGrid.getListDataView();
        // Actions column
        skillGrid.addComponentColumn(selectedSkill -> {
            Dialog editSkillDialog = createEditSkillDialog(selectedSkill, skillGrid);
            Button editButton =
                new Button("Edit", VaadinIcon.EDIT.create(), e -> editSkillDialog.open());
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            ConfirmDialog deleteSkillDialog = createDeleteSkillDialog(selectedSkill, skillGrid);
            Button deleteButton = new Button("Delete", e -> deleteSkillDialog.open());
            deleteButton.setPrefixComponent(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions");

        List<SkillDto> skillAll = skillService.getAllSkill();
        skillGrid.setItems(skillAll);

        add(createSkillAdderWithDialog(listDataView));
        add(skillGrid);
    }

    private Component createSkillAdderWithDialog(GridListDataView<SkillDto> listDataView) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create skill");
        TextField skillNameTextField = new TextField("Skill name");
        skillNameTextField.setRequired(true);
        Binder<SkillDto> sillBinder = new Binder<>(SkillDto.class);
        sillBinder.forField(skillNameTextField)
            .asRequired()
            .bind(SkillDto::getName, SkillDto::setName);
        FormLayout formLayout = new FormLayout(skillNameTextField);
        dialog.add(formLayout);

        Button createButton = new Button("Create", e -> {
            String text = skillNameTextField.getValue();
            if (StringUtils.isBlank(text)) {
                return;
            }
            Optional<SkillDto> newSkill = skillService.saveSkill(new SkillDto(text));
            if (newSkill.isPresent()) {
                listDataView.addItem(newSkill.get());
            } else {
                // TODO notification if skill already exists
                System.out.println("Couldn't create skill '" + text + "'");
            }
            skillNameTextField.clear();
            dialog.close();
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, createButton);

        Button addSkillButton =
            new Button("Add Skill", VaadinIcon.PLUS.create(), e -> dialog.open());
        addSkillButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return addSkillButton;
    }

    private Dialog createEditSkillDialog(SkillDto currentSkill, Grid<SkillDto> grid) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit skill \"" + currentSkill.getName() + "\"");
        TextField skillNameTextField = new TextField("Skill name");
        skillNameTextField.setValue(currentSkill.getName());
        Binder<SkillDto> sillBinder = new Binder<>(SkillDto.class);
        sillBinder.forField(skillNameTextField)
            .asRequired()
            .bind(SkillDto::getName, SkillDto::setName);
        FormLayout formLayout = new FormLayout(skillNameTextField);
        dialog.add(formLayout);

        Button saveButton = new Button("Save", e -> {
            String newName = skillNameTextField.getValue();
            if (StringUtils.isBlank(newName)) {
                return;
            }
            Optional<SkillDto> updatedSkill =
                skillService.updateSkill(currentSkill.getId(), newName);
            if (updatedSkill.isPresent()) {
                currentSkill.setName(newName);
                // TODO IMPROVEMENT (FIX) not updating the skill name in the grid with `refreshItem`
                // which would be better than `refreshAll`
                grid.getDataProvider().refreshAll();
                skillNameTextField.clear();
                dialog.close();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);

        return dialog;
    }

    private ConfirmDialog createDeleteSkillDialog(SkillDto selectedSkill,
                                                  Grid<SkillDto> skillGrid) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete skill \"" + selectedSkill.getName() + "\"?");
        confirmDialog.setText("Are you sure you want to permanently delete this item?\r\n"
            + "Any associations with this skill will be removed as well.");
        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(e -> {
            skillService.deleteSkillById(selectedSkill.getId());
            skillGrid.getListDataView().removeItem(selectedSkill);
        });
        confirmDialog.setCancelable(true);
        return confirmDialog;
    }
}
