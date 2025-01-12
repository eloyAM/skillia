package com.example.application.view;

import com.example.application.dto.SkillDto;
import com.example.application.dto.SkillTagDto;
import com.example.application.security.SecConstants;
import com.example.application.service.SkillService;
import com.example.application.service.SkillTagService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_WARNING;

@RolesAllowed(SecConstants.HR)
@Route(layout = MainLayout.class, value = "skillsmanagement")
public class SkillsManagementView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(SkillsManagementView.class);
    private final SkillService skillService;
    private final SkillTagService skillTagService;

    public SkillsManagementView(
        SkillService skillService,
        SkillTagService skillTagService
    ) {
        this.skillService = skillService;
        this.skillTagService = skillTagService;
        createUi();
    }

    private void createUi() {
        setSizeFull();
        Grid<SkillDto> skillGrid = new Grid<>(SkillDto.class, false);
        skillGrid.addClassName("skills-grid");

        skillGrid.addColumn(SkillDto::getName)
            .setHeader("Name")
            .setKey("name")
            .setFrozen(true);
        skillGrid.addComponentColumn(skillDto -> {
            FlexLayout tagsContainer = skillDto.getTags().stream()
                .map(SkillTagDto::getName)
                .map(name -> {
                    Span span = new Span(name);
                    span.getElement().getThemeList().add("badge contrast pill");
                    return span;
                })
                .collect(FlexLayout::new, HasComponents::add, HasComponents::add);
            tagsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
            tagsContainer.getStyle()
                .set("gap", "var(--lumo-space-s)")
                .set("padding-top", "var(--lumo-space-s)")
                .set("padding-bottom", "var(--lumo-space-s)");
            return tagsContainer;
        })
            .setHeader("Tags")
            .setKey("tags");
        // Actions column
        skillGrid.addComponentColumn(selectedSkill -> {
            // Edit
            Dialog editSkillDialog = createEditSkillDialog(selectedSkill, skillGrid);
            Button editButton = new Button(VaadinIcon.EDIT.create(), e -> editSkillDialog.open());
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editButton.addClassName("edit-skill-button");
            // Delete
            ConfirmDialog deleteSkillDialog = createDeleteSkillDialog(selectedSkill, skillGrid);
            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteSkillDialog.open());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClassName("delete-skill-button");
            // Result
            HorizontalLayout result = new HorizontalLayout(editButton, deleteButton);
            result.setSpacing(false);
            return result;
        })
            .setHeader("Actions")
            .setKey("actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        CallbackDataProvider.FetchCallback<SkillDto, Void> fetchCallback = query -> {
            log.debug("Fetching skills with limit: {} and page {}", query.getLimit(), query.getPage()); // TODO we are forced to use the limit and page from query, otherwise and error is thrown
            return skillService.getAllSkill().stream();
        };
        skillGrid.setItems(fetchCallback);

        add(createSkillAdderWithDialog(skillGrid));
        add(skillGrid);
    }

    private Component createSkillAdderWithDialog(Grid<SkillDto> skillGrid) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create skill");

        Binder<SkillDto> skillBinder = new Binder<>(SkillDto.class);
        FormLayout formLayout = createSkillFormWithBinder(skillBinder);
        formLayout.addClassName("create-skill-form");
        dialog.add(formLayout);

        Button createButton = new Button("Create", e -> {
            SkillDto formDto = new SkillDto();
            final String skillName;
            try {
                skillBinder.writeBean(formDto);
                skillName = formDto.getName();
            } catch (ValidationException ex) {
                ViewUtils.notificationTopCenter("Please fill in the required fields correctly", false).open();
                return;
            }
            Optional<SkillDto> newSkill = skillService.saveSkill(formDto);
            if (newSkill.isPresent()) {
                skillGrid.getDataProvider().refreshAll();
                ViewUtils.notificationTopCenter("Skill \"" + skillName + "\" created", true).open();
            } else {
                Notification notification = ViewUtils.notificationTopCenter("", LUMO_WARNING);
                notification.removeAll();
                notification.add(new Div(
                    new Div("Unable to create the skill"),
                    new Div(" \"" + skillName + "\" "),
                    new Div("It may already exist")
                ));
                notification.open();
            }
            skillBinder.getFields().forEach(HasValue::clear);
            dialog.close();
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClassName("create-skill-submit-button");
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addClassName("create-skill-cancel-button");
        dialog.getFooter().add(cancelButton, createButton);

        Button addSkillButton =
            new Button("Add Skill", VaadinIcon.PLUS.create(), e -> dialog.open());
        addSkillButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addSkillButton.addClassName("add-skill-button");
        return addSkillButton;
    }

    private Dialog createEditSkillDialog(SkillDto currentSkill, Grid<SkillDto> grid) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit skill \"" + currentSkill.getName() + "\"");

        Binder<SkillDto> skillBinder = new Binder<>(SkillDto.class);
        FormLayout formLayout = createSkillFormWithBinder(skillBinder);
        formLayout.addClassName("edit-skill-form");
        skillBinder.readBean(currentSkill);
        dialog.add(formLayout);

        Button saveButton = new Button("Save", e -> {
            SkillDto formDto = new SkillDto();
            try {
                skillBinder.writeBean(formDto);
                formDto.setId(currentSkill.getId());
            } catch (ValidationException ex) {
                ViewUtils.notificationTopCenter("Please fill in the required fields correctly", false).open();
                return;
            }
            String newName = formDto.getName();
            Optional<SkillDto> updatedSkillOpt =
                skillService.saveSkill(formDto);
            if (updatedSkillOpt.isPresent()) {
                ViewUtils.notificationTopCenter("Skill \"" + newName + "\" updated", true).open();
                skillBinder.getFields().forEach(HasValue::clear);
                grid.getDataProvider().refreshAll();    // TODO IMPROVEMENT how to get that single entry updated with `refreshItem` instead of `refreshAll`
                dialog.close();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClassName("edit-skill-save-button");
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addClassName("edit-skill-cancel-button");
        dialog.getFooter().add(cancelButton, saveButton);

        return dialog;
    }

    private FormLayout createSkillFormWithBinder(Binder<SkillDto> skillBinder) {
        TextField skillNameTextField = new TextField("Skill name");
        skillNameTextField.setRequired(true);
        MultiSelectComboBox<SkillTagDto> tagMultiSelectComboBox = createTagMultiSelectComboBox();
        tagMultiSelectComboBox.setRequired(false);

        skillBinder.forField(skillNameTextField)
            .asRequired()
            .bind(SkillDto::getName, SkillDto::setName);
        skillBinder.forField(tagMultiSelectComboBox)
            .bind(SkillDto::getTags, SkillDto::setTags);

        return new FormLayout(
            skillNameTextField,
            tagMultiSelectComboBox
        );
    }

    private ConfirmDialog createDeleteSkillDialog(
            SkillDto selectedSkill,
            Grid<SkillDto> skillGrid
    ) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete skill \"" + selectedSkill.getName() + "\"");
        confirmDialog.setText("Are you sure you want to permanently delete this item?\r\n"
            + "Any associations with this skill will be removed as well.");
        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(e -> {
            skillService.deleteSkillById(selectedSkill.getId());
            skillGrid.getDataProvider().refreshAll();
        });
        confirmDialog.setCancelable(true);
        return confirmDialog;
    }

    private MultiSelectComboBox<SkillTagDto> createTagMultiSelectComboBox() {
        MultiSelectComboBox<SkillTagDto> tagSelector = new MultiSelectComboBox<>();
        tagSelector.setLabel("Tags");
        tagSelector.setItemLabelGenerator(SkillTagDto::getName);
        tagSelector.setItems(skillTagService.getAllSkillTag());
        return tagSelector;
    }

}
