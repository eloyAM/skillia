package com.example.application.view;

import com.example.application.dto.PersonWithSkillsDto;
import com.example.application.dto.SkillTagDto;
import com.example.application.service.PersonSkillService;
import com.example.application.service.SkillTagService;
import com.example.application.utils.Comparators;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static com.example.application.view.SkillsManagementView.createTagMultiSelectComboBoxFilter;

@PermitAll
@Route(layout = MainLayout.class, value = "skillsmatrix")
public class PersonWithSkillsView extends VerticalLayout {

    private final PersonSkillService personSkillService;
    private final SkillTagService skillTagService;

    public PersonWithSkillsView(PersonSkillService personSkillService, SkillTagService skillTagService) {
        this.personSkillService = personSkillService;
        this.skillTagService = skillTagService;
        createUi();
    }

    private void createUi() {
        setSizeFull();
        var personSkillGrid = new Grid<>(PersonWithSkillsDto.class, false);
        personSkillGrid.addColumn(createPersonRenderer()).setHeader("Person")
            .setKey("person");
        personSkillGrid.addColumn(createSkillsRenderer()).setHeader("Skills")
            .setKey("skills");

        List<PersonWithSkillsDto> personSkillAll = personSkillService.getAllPersonSkill();
        personSkillGrid.setItems(personSkillAll);

        personSkillGrid.getHeaderRows().clear();

        TextField personSearchTextField = new TextField();
        personSearchTextField.setPrefixComponent(VaadinIcon.SEARCH.create());
        personSearchTextField.setPlaceholder("Search");
        personSearchTextField.setTooltipText("Find persons by name, job title or department");
        personSearchTextField.setClearButtonVisible(true);
        personSearchTextField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        personSearchTextField.setWidthFull();
        personSearchTextField.setMaxWidth("100%");
        Grid.Column<PersonWithSkillsDto> personColumn = personSkillGrid.getColumnByKey("person");
        HeaderRow headerRow = personSkillGrid.appendHeaderRow();
        headerRow.getCell(personColumn).setComponent(personSearchTextField);

        GridListDataView<PersonWithSkillsDto> listDataView = personSkillGrid.getListDataView();
        FilterManager filterManager = new FilterManager(listDataView);

        personSearchTextField.addValueChangeListener(event -> {
            String filterValue = event.getValue();
            filterManager.setPersonFilter(filterValue);
            filterManager.applyFilters();
        });

        TextField skillSearchTextField = new TextField();
        skillSearchTextField.setPrefixComponent(VaadinIcon.SEARCH.create());
        skillSearchTextField.setPlaceholder("Search");
        skillSearchTextField.setTooltipText("Find persons by skill name or level");
        skillSearchTextField.setClearButtonVisible(true);
        skillSearchTextField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        skillSearchTextField.setWidthFull();
        skillSearchTextField.setMaxWidth("100%");
        MultiSelectComboBox<SkillTagDto> tagSelectorFilter = createTagMultiSelectComboBoxFilter(skillTagService);
        tagSelectorFilter.addValueChangeListener(e -> {
            var selectedTags = e.getValue();
            List<String> valuesList = selectedTags.stream()
                .map(SkillTagDto::getName)
                .collect(Collectors.toList());
            filterManager.setTagFilter(valuesList);
            filterManager.applyFilters();
        });
        Grid.Column<PersonWithSkillsDto> skillsColumn = personSkillGrid.getColumnByKey("skills");
        headerRow.getCell(skillsColumn).setComponent(new VerticalLayout(skillSearchTextField, tagSelectorFilter));

        skillSearchTextField.addValueChangeListener(event -> {
            String filterValue = event.getValue();
            filterManager.setSkillsFilter(filterValue);
            filterManager.applyFilters();
        });

        add(personSkillGrid);
    }

    private static ComponentRenderer<Div, PersonWithSkillsDto> createSkillsRenderer() {
        return new ComponentRenderer<>(personWithSkillsDto -> {
            var mainDiv = new Div();
            for (var skill : personWithSkillsDto.getSkills()) {
                var skillDiv = new Div();
                skillDiv.setText(skill.getSkill().getName());
                String levelIndicatorSvgPath = ViewUtils.getLevelIndicatorSvgPath(skill.getLevel());
                Div levelIndicatorDiv = new Div();
                Image levelIndicatorSvg =
                    new Image(levelIndicatorSvgPath, "level " + skill.getLevel());
                levelIndicatorDiv.add(levelIndicatorSvg);
                levelIndicatorDiv.getStyle().set("padding-bottom", "var(--lumo-space-s");
                skillDiv.add(levelIndicatorDiv);
                mainDiv.add(skillDiv);
            }
            return mainDiv;
        });
    }

    private static ComponentRenderer<Div, PersonWithSkillsDto> createPersonRenderer() {
        return new ComponentRenderer<>(personWithSkillsDto -> {
            var person = personWithSkillsDto.getPerson();

            var fullNameDiv = new Div();
            fullNameDiv.setText(person.getFullName());
            fullNameDiv.getStyle().set("font-weight", "bold");

            var personTitleDiv = new Div();
            personTitleDiv.setText(person.getTitle());
            personTitleDiv.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-style", "italic");

            var deparmentDiv = new Div();
            deparmentDiv.setText(person.getDepartment());
            deparmentDiv.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-style", "italic");
            return new Div(fullNameDiv, personTitleDiv, deparmentDiv);
        });
    }

    private static final class FilterManager {
        private final Map<
            BiPredicate<PersonWithSkillsDto, String>,
            Optional<String>> filterMap;
        private final GridListDataView<PersonWithSkillsDto> listDataView;

        private FilterManager(GridListDataView<PersonWithSkillsDto> listDataView) {
            this.listDataView = listDataView;
            // Can't be an immutable map as we use the 'put' method
            filterMap = new HashMap<>(2);
            filterMap.put(PersonPredicate.personPredicate, Optional.empty());
            filterMap.put(SkillsPredicate::testSkillNameOrLevel, Optional.empty());
        }

        public void applyFilters() {
            listDataView.setFilter(personWithSkillsDto ->
                filterMap.entrySet().stream()
                    .filter(entry -> entry.getValue().isPresent())
                    .allMatch(entry -> {
                        var predicate = entry.getKey();
                        String filterValue = entry.getValue().get();
                        return predicate.test(personWithSkillsDto, filterValue);
                    }));
        }

        public void setSkillsFilter(String filterValue) {
            filterMap.put(SkillsPredicate::testSkillNameOrLevel, Optional.ofNullable(filterValue));
        }

        public void unsetSkillsFilter() {
            filterMap.put(SkillsPredicate::testSkillNameOrLevel, Optional.empty());
        }

        public void setPersonFilter(String filterValue) {
            filterMap.put(PersonPredicate.personPredicate, Optional.ofNullable(filterValue));
        }

        public void unsetPersonFilter() {
            filterMap.put(PersonPredicate.personPredicate, Optional.empty());
        }

        public void setTagFilter(List<String> filterValues) {
            filterMap.put(SkillsPredicate::testSkillTagNames, Optional.of(String.join(";", filterValues)));
        }

        private static final class SkillsPredicate
            implements BiPredicate<PersonWithSkillsDto, String> {
            @Override
            public boolean test(PersonWithSkillsDto person, String filterValue) {
                return testSkillNameOrLevel(person, filterValue);
            }

            public static boolean testSkillNameOrLevel(PersonWithSkillsDto person, String filterValue) {
                return person.getSkills().stream()
                    .map(acquiredSkillDto -> acquiredSkillDto.getSkill().getName()
                        + acquiredSkillDto.getLevel())
                    .anyMatch(skillStr -> StringUtils.containsIgnoreCase(skillStr, filterValue));
            }

            public static boolean testSkillTagNames(PersonWithSkillsDto person, String joinedValues) {
                if (joinedValues == null || joinedValues.isBlank()) {
                    return true;
                }
                var filterValues = Arrays.stream(joinedValues.split(";")).collect(Collectors.toSet());
                var flattenTagNames = person.getSkills().stream()
                    .flatMap(acquiredSkillDto -> acquiredSkillDto.getSkill().getTags().stream())
                    .map(SkillTagDto::getName)
                    .collect(Collectors.toSet());
                return flattenTagNames.containsAll(filterValues);
            }
        }

        private static final class PersonPredicate {
            public static final BiPredicate<PersonWithSkillsDto, String> personPredicate =
                (personWithSkillsDto, filterValue) ->
                    Comparators.personDtoAttributesContains(personWithSkillsDto.getPerson(),
                        filterValue);
        }
    }
}
