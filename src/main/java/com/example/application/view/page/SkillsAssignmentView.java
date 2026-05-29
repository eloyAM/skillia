package com.example.application.view.page;

import com.example.application.dto.PersonDto;
import com.example.application.dto.PersonSkillBasicDto;
import com.example.application.dto.SkillDto;
import com.example.application.security.SecConstants;
import com.example.application.service.PersonService;
import com.example.application.service.PersonSkillService;
import com.example.application.service.SkillService;
import com.example.application.view.components.PersonAndSkillsGrid;
import com.example.application.view.components.SkillAndPeopleWithLevelGrid;
import com.example.application.view.utils.Comparators;
import com.example.application.view.utils.MainLayout;
import com.example.application.view.utils.ViewUtils;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxBase;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.application.view.utils.ViewUtils.createTab;
import static com.example.application.view.utils.ViewUtils.notificationTopCenter;

@CssImport("./styles/vaadin-selector-elements.css")
@RolesAllowed(SecConstants.HR)
@Route(layout = MainLayout.class, value = "skillsassignment")
@PageTitle("Assign")
public class SkillsAssignmentView extends TabSheet implements BeforeEnterObserver {
    private final Map<String, Tab> tabNameToTab = new HashMap<>();
    private boolean isInitialized = false;

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
        setSizeFull();
        add(createTab("By skill", tabNameToTab), assignBySkillTab());
        add(createTab("By person", tabNameToTab), assignByPersonTab());
        addSelectedChangeListener(e -> {
            Tab selectedTab = e.getSelectedTab();
            if (selectedTab != null) {
                ViewUtils.updateUrlWithTab(selectedTab.getLabel(), isInitialized);
            }
        });
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

        var layout = new VerticalLayout(skillsComboBox, personMultiSelectComboBox, skillLevelSelector,
            saveButton, skillAndPeopleWithLevelGrid);
        layout.setSizeFull();
        return layout;
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

        var layout = new VerticalLayout(personComboBox, skillsComboBox, skillLevelSelector, saveButton,
            personAndSkillsGrid);
        layout.setSizeFull();
        return layout;
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
                notificationTopCenter("Please fill all required fields", false).open();
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
            boolean savedPersonSkillsIsEmpty = savedPersonSkills.isEmpty();
            if (savedPersonSkillsIsEmpty) {
                notificationTopCenter("Some error occurred while saving", false).open();
            } else {
                notificationTopCenter("Saved successfully", true).open();
            }
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
                notificationTopCenter("Please fill all required fields", false).open();
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
            // Error case: skill/person deleted during the process
            boolean savedPersonSkillIsNull = savedPersonSkill == null;
            if (savedPersonSkillIsNull) {
                notificationTopCenter("Some error occurred while saving", false).open();
            } else {
                notificationTopCenter("Saved successfully", true).open();
            }
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
        skillComboBox.setPlaceholder("Select a skill");
        skillComboBox.setRenderer(new ComponentRenderer<>(skill -> {
            Span name = new Span(skill.getName());

            VerticalLayout result = new VerticalLayout(name);
            result.setSpacing(false);
            result.getThemeList().add("spacing-s");
            result.setPadding(false);
            result.getStyle().set("padding-block", "var(--lumo-space-s)");

            if (skill.getTags() != null && !skill.getTags().isEmpty()) {
                Div tagsContainer = new Div();
                tagsContainer.getStyle()
                    .set("display", "flex")
                    .set("flex-wrap", "wrap")
                    .set("gap", "var(--lumo-space-xs)");
                skill.getTags().forEach(tag -> {
                    Span tagBadge = new Span(tag.getName());
                    tagBadge.getElement().getThemeList().add("badge contrast pill small");
                    tagsContainer.add(tagBadge);
                });
                result.add(tagsContainer);
            }

            if (skill.getDescription() != null && !skill.getDescription().isEmpty()) {
                Div description = new Div();
                description.setText(skill.getDescription());
                description.getStyle()
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)");
                result.add(description);
            }

            return result;
        }));
        List<SkillDto> allSkill = skillService.getAllSkill();
        skillComboBox.setItems(Comparators::skillDtoAttributesContains, allSkill);
        return skillComboBox;
    }

    private MultiSelectComboBox<PersonDto> createPersonMultiSelectComboBox() {
        MultiSelectComboBox<PersonDto> selector = new MultiSelectComboBox<>("People");
        configurePersonComboBox(selector);
        selector.setPlaceholder("Select people");
        selector.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
        selector.setSelectedItemsOnTop(true);
        return selector;
    }

    private ComboBox<PersonDto> createPersonComboBox() {
        ComboBox<PersonDto> selector = new ComboBox<>("Person");
        configurePersonComboBox(selector);
        selector.setPlaceholder("Select a person");
        return selector;
    }

    private void configurePersonComboBox(ComboBoxBase<?, PersonDto, ?> selector) {
        selector.setRequired(true);
        selector.setWidthFull();
        selector.setItemLabelGenerator(personDtoItemLabelGenerator);
        selector.setRenderer(personComboBoxRenderer);

        List<PersonDto> allPerson = personService.findAllPerson();
        selector.setItems(Comparators::personDtoAttributesContains, allPerson);
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
            Div details = new Div(MessageFormat.format("({0} - {1} - {2})",
                person.getUsername(), person.getTitle(), person.getDepartment()
            ));
            details.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");
            return new Div(fullName, details);
        });

    // Select the tab based on the URL as we load the page -> access directly to the tab / remember the tab on refresh
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getLocation().getQueryParameters()
            .getSingleParameter(ViewUtils.SELECTED_VIEW_PARAM).ifPresent(this::selectTabByName);
        isInitialized = true;
    }

    private void selectTabByName(String s) {
        Tab tab = tabNameToTab.get(s);
        if (tab != null) {
            setSelectedTab(tab);
        }
    }

}
