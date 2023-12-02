package com.example.application.view;

import com.example.application.dto.PersonDto;
import com.example.application.dto.PersonSkillBasicDto;
import com.example.application.dto.SkillDto;
import com.example.application.security.SecConstants;
import com.example.application.service.PersonService;
import com.example.application.service.PersonSkillService;
import com.example.application.service.SkillService;
import com.example.application.utils.Comparators;
import com.example.application.view.components.PersonAndSkillsGrid;
import com.example.application.view.components.SkillAndPeopleWithLevelGrid;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

@RolesAllowed(SecConstants.HR)
@Route(layout = MainLayout.class, value = "skillsassignment")
public class SkillsAssignmentView extends TabSheet {

    private final SkillService skillService;
    private final PersonService personService;
    private final PersonSkillService personSkillService;

    public SkillsAssignmentView(
        SkillService skillService,
        PersonService personService,
        PersonSkillService personSkillService
    ) {
        this.skillService = skillService;
        this.personService = personService;
        this.personSkillService = personSkillService;
        createUi();
    }

    private void createUi() {
        add(new Tab("By skill"), assignBySkillTab());
        add(new Tab("By person"), assignByPersonTab());
    }

    private VerticalLayout assignBySkillTab() {
        ComboBox<SkillDto> skillsComboBox = createSkillsComboBox();
        MultiSelectComboBox<PersonDto> personMultiSelectComboBox =
            createPersonMultiSelectComboBox();
        RadioButtonGroup<Integer> skillLevelSelector = skillSelectorRadioButtonGroup();

        SkillAndPeopleWithLevelGrid skillAndPeopleWithLevelGrid =
            new SkillAndPeopleWithLevelGrid(personSkillService);
        Button saveButton =
            createAssignBySkillTabSaveButton(skillsComboBox, personMultiSelectComboBox,
                skillLevelSelector, skillAndPeopleWithLevelGrid);

        skillsComboBox.addValueChangeListener(
            event -> skillAndPeopleWithLevelGrid.updateItemsFromDb(event.getValue()));

        return new VerticalLayout(skillsComboBox, personMultiSelectComboBox, skillLevelSelector,
            saveButton, skillAndPeopleWithLevelGrid);
    }

    private VerticalLayout assignByPersonTab() {
        ComboBox<PersonDto> personComboBox = createPersonComboBox();
        ComboBox<SkillDto> skillsComboBox = createSkillsComboBox();
        RadioButtonGroup<Integer> skillLevelSelector = skillSelectorRadioButtonGroup();

        PersonAndSkillsGrid personAndSkillsGrid = new PersonAndSkillsGrid(personSkillService);

        Button saveButton =
            createAssignByPersonTabSaveButton(personComboBox, skillsComboBox, skillLevelSelector,
                personAndSkillsGrid);

        personComboBox.addValueChangeListener(
            event -> personAndSkillsGrid.updateItemsFromDb(event.getValue()));

        return new VerticalLayout(personComboBox, skillsComboBox, skillLevelSelector, saveButton,
            personAndSkillsGrid);
    }

    private Button createAssignBySkillTabSaveButton(
        ComboBox<SkillDto> skillsComboBox,
        MultiSelectComboBox<PersonDto> personMultiSelectComboBox,
        RadioButtonGroup<Integer> skillLevelSelector,
        SkillAndPeopleWithLevelGrid skillAndPeopleWithLevelGrid
    ) {
        return new Button("Save", event -> {
            if (personMultiSelectComboBox.isEmpty() || skillsComboBox.isEmpty()
                || skillLevelSelector.isEmpty()) {
                // TODO put a message in the form instead of a notification
                Notification notification =
                    new Notification("Please fill all required fields", 5000,
                        Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
                return;
            }
            // Input data
            Collection<PersonDto> selectedPersons = personMultiSelectComboBox.getValue();
            SkillDto skill = skillsComboBox.getValue();
            int skillLevel = skillLevelSelector.getValue();

            List<PersonSkillBasicDto> personSkills = selectedPersons.stream()
                .map(person -> PersonSkillBasicDto.builder()
                    .personId(person.getUsername())
                    .skillId(skill.getId())
                    .level(skillLevel).build())
                .toList();
            List<PersonSkillBasicDto> savedPersonSkills =
                personSkillService.savePersonSkill(personSkills);
            // TODO try some error cases. What to do? Exception / empty result.
            // NOTE: seems to be ok, specifying a new level for an existing relation, updates it
            // Error case: skill/person deleted during the process
            boolean savedPersonSkillsIsEmpty = savedPersonSkills.isEmpty();
            Notification notification;
            if (savedPersonSkillsIsEmpty) {
                notification = notificationTopCenter("No person skills saved", false);
            } else {
                // TODO probably, only show OK notification, not the saved items
                notification =
                    notificationTopCenter("Saved person skills: {0}" + savedPersonSkills, true);

            }
            notification.open();
            if (!savedPersonSkillsIsEmpty) {
                // Clean the data (not the skill, as you might want to add the same skill
                // to other people with different level)
                personMultiSelectComboBox.clear();
                skillLevelSelector.clear();
                skillAndPeopleWithLevelGrid.updateItemsFromDb(skill);
            }
        });
    }

    private Button createAssignByPersonTabSaveButton(
        ComboBox<PersonDto> personComboBox,
        ComboBox<SkillDto> skillsComboBox,
        RadioButtonGroup<Integer> skillLevelSelector,
        PersonAndSkillsGrid personAndSkillsGrid
    ) {
        return new Button("Save", e -> {
            if (personComboBox.isEmpty() || skillsComboBox.isEmpty()
                || skillLevelSelector.isEmpty()) {
                // TODO put a message in the form instead of a notification
                Notification notification =
                    new Notification("Please fill all required fields", 5000,
                        Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
                return;
            }
            // Input data
            PersonDto person = personComboBox.getValue();
            SkillDto skill = skillsComboBox.getValue();
            int skillLevel = skillLevelSelector.getValue();

            PersonSkillBasicDto personSkill = PersonSkillBasicDto.builder()
                .personId(person.getUsername())
                .skillId(skill.getId())
                .level(skillLevel).build();
            PersonSkillBasicDto savedPersonSkill = personSkillService.savePersonSkill(personSkill);
            // TODO try some error cases. What to do? Exception / empty result.
            // NOTE: seems to be ok, specifying a new level for an existing relation, updates it
            // Error case: skill/person deleted during the process
            boolean savedPersonSkillIsNull = savedPersonSkill == null;
            Notification notification;
            if (savedPersonSkillIsNull) {
                notification = notificationTopCenter("No person skill saved", false);
            } else {
                // TODO probably, only show OK notification, not the saved item
                notification =
                    notificationTopCenter("Saved person skill: {0}" + savedPersonSkill, true);
            }
            notification.open();
            if (!savedPersonSkillIsNull) {
                // Clean the data (not the person, as you might want to add more skills
                // to the same person)
                skillsComboBox.clear();
                skillLevelSelector.clear();
                personAndSkillsGrid.updateItemsFromDb(person);
            }
        });
    }

    private ComboBox<SkillDto> createSkillsComboBox() {
        ComboBox<SkillDto> skillComboBox = new ComboBox<>("Skill");
        skillComboBox.setRequired(true);
        skillComboBox.setWidthFull();
        skillComboBox.setItemLabelGenerator(SkillDto::getName);
        List<SkillDto> allSkill = skillService.getAllSkill();
        skillComboBox.setItems(allSkill);
        return skillComboBox;
    }

    private MultiSelectComboBox<PersonDto> createPersonMultiSelectComboBox() {
        MultiSelectComboBox<PersonDto> personMultiSelectComboBox =
            new MultiSelectComboBox<>("People");
        personMultiSelectComboBox.setRequired(true);
        personMultiSelectComboBox.setWidthFull();
        // The item label generator decides how the selected items are displayed in the input field
        // If no renderer is set, the item label generator is also used to display dropdown elements
        personMultiSelectComboBox.setItemLabelGenerator(personDtoItemLabelGenerator);
        personMultiSelectComboBox.setRenderer(personComboBoxRenderer);

        List<PersonDto> allPerson = personService.findAllPerson();
        personMultiSelectComboBox.setItems(Comparators::personDtoAttributesContains, allPerson);
        return personMultiSelectComboBox;
    }

    private ComboBox<PersonDto> createPersonComboBox() {
        ComboBox<PersonDto> personComboBox = new ComboBox<>("Person");
        personComboBox.setRequired(true);
        personComboBox.setWidthFull();
        personComboBox.setItemLabelGenerator(personDtoItemLabelGenerator);
        personComboBox.setRenderer(personComboBoxRenderer);

        List<PersonDto> allPerson = personService.findAllPerson();
        personComboBox.setItems(Comparators::personDtoAttributesContains, allPerson);
        return personComboBox;
    }

    private static final ItemLabelGenerator<PersonDto> personDtoItemLabelGenerator = person ->
        MessageFormat.format("{0} ({1})",
            person.getFullName(), person.getUsername()
        );

    private static RadioButtonGroup<Integer> skillSelectorRadioButtonGroup() {
        RadioButtonGroup<Integer> group = new RadioButtonGroup<>(
            "Skill level",
            PersonSkillService.getLevels()
        );
        group.setRequired(true);
        group.setItemLabelGenerator(level ->
            MessageFormat.format("{0} - {1}", level, PersonSkillService.getLevelName(level))
        );
        return group;
    }

    //

    private static final Renderer<PersonDto> personComboBoxRenderer = new ComponentRenderer<>(
        person -> {
            Span fullName = new Span(person.getFullName());
            Div details = new Div();
            details.setText(MessageFormat.format("({0} - {1} - {2})",
                person.getUsername(), person.getTitle(), person.getDepartment()
            ));
            details.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");
            return new Div(fullName, details);
        });

    @NonNull
    private static Notification notificationTopCenter(String message, boolean success) {
        Notification notification = new Notification(message, 5000,
            Notification.Position.TOP_CENTER);
        notification.addThemeVariants(success
            ? NotificationVariant.LUMO_SUCCESS
            : NotificationVariant.LUMO_ERROR
        );
        return notification;
    }
}
