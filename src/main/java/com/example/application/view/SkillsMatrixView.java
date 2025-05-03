package com.example.application.view;

import com.example.application.dto.PersonWithSkillsDto;
import com.example.application.dto.SkillTagDto;
import com.example.application.service.PersonSkillService;
import com.example.application.service.SkillTagService;
import com.example.application.utils.Comparators;
import com.vaadin.flow.component.ItemLabelGenerator;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@PermitAll
@Route(layout = MainLayout.class, value = "skillsmatrix")
public class SkillsMatrixView extends VerticalLayout {

    private final PersonSkillService personSkillService;
    private final SkillTagService skillTagService;

    public SkillsMatrixView(PersonSkillService personSkillService, SkillTagService skillTagService) {
        this.personSkillService = personSkillService;
        this.skillTagService = skillTagService;
        createUi();
    }

    private void createUi() {
        setSizeFull();
        var personSkillGrid = new Grid<>(PersonWithSkillsDto.class, false);
        Grid.Column<PersonWithSkillsDto> personColumn = personSkillGrid.addColumn(createPersonRenderer())
            .setHeader("Person")
            .setKey("person");
        Grid.Column<PersonWithSkillsDto> skillsColumn = personSkillGrid.addColumn(createSkillsRenderer())
            .setHeader("Skills")
            .setKey("skills");
        HeaderRow headerRow = personSkillGrid.appendHeaderRow();
        personSkillGrid.getHeaderRows().clear();

        List<PersonWithSkillsDto> personSkillAll = personSkillService.getAllPersonSkill();
        personSkillGrid.setItems(personSkillAll);

        GridListDataView<PersonWithSkillsDto> listDataView = personSkillGrid.getListDataView();

        // Filter system for the whole grid
        FilterManager filterManager = new FilterManager(listDataView);

        // Create a filter for the person column
        TextField personSearchTextField = createPersonSearchTextField(filterManager);
        MultiSelectComboBox<String> departmentSelectBoxFilter = createDepartmentSelectBoxFilter(
            listDataView.getItems(), filterManager);
        MultiSelectComboBox<String> jobTitleSelectBoxFilter = createJobTitleSelectBoxFilter(
            listDataView.getItems(), filterManager);
        headerRow.getCell(personColumn).setComponent(new VerticalLayout(
            personSearchTextField, jobTitleSelectBoxFilter, departmentSelectBoxFilter));

        // Create a filter for the skill column
        TextField skillSearchTextField = createSkillSearcTextField(filterManager);
        MultiSelectComboBox<SkillTagDto> tagSelectorFilter = ViewUtils.createMultiSelectComboBoxFilter(
            skillTagService::getAllSkillTagInUse, SkillTagDto::getName, "Filter by tags");
        tagSelectorFilter.addValueChangeListener(e -> {
            var selectedTags = e.getValue();
            List<String> valuesList = selectedTags.stream()
                .map(SkillTagDto::getName)
                .collect(Collectors.toList());
            filterManager.setTagFilter(valuesList);
            filterManager.applyFilters();
        });
        headerRow.getCell(skillsColumn).setComponent(new VerticalLayout(skillSearchTextField, tagSelectorFilter));

        add(personSkillGrid);
    }

    private static MultiSelectComboBox<String> createDepartmentSelectBoxFilter(
        Stream<PersonWithSkillsDto> items, FilterManager filterManager
    ) {
        Supplier<List<String>> valueProvider = () -> items
            .map(personWithSkillsDto -> personWithSkillsDto.getPerson().getDepartment())
            .distinct()
            .toList();
        ItemLabelGenerator<String> itemLabelGenerator = item -> item;
        var selector = ViewUtils.createMultiSelectComboBoxFilter(
            valueProvider,
            itemLabelGenerator,
            "Filter by department"
        );
        selector.addValueChangeListener(e -> {
            var selectedValues = e.getValue();
            filterManager.setDepartmentListFilter(selectedValues);
            filterManager.applyFilters();
        });
        return selector;
    }

    private static MultiSelectComboBox<String> createJobTitleSelectBoxFilter(
        Stream<PersonWithSkillsDto> items, FilterManager filterManager
    ) {
        Supplier<List<String>> valueProvider = () -> items
            .map(personWithSkillsDto -> personWithSkillsDto.getPerson().getTitle())
            .distinct()
            .toList();
        ItemLabelGenerator<String> itemLabelGenerator = item -> item;
        var selector = ViewUtils.createMultiSelectComboBoxFilter(
            valueProvider,
            itemLabelGenerator,
            "Filter by job title"
        );
        selector.addValueChangeListener(e -> {
            var selectedValues = e.getValue();
            filterManager.setJobTitleListFilter(selectedValues);
            filterManager.applyFilters();
        });
        return selector;
    }

    private static TextField createSkillSearcTextField(FilterManager filterManager) {
        TextField skillSearchTextField = new TextField();
        skillSearchTextField.setPrefixComponent(VaadinIcon.SEARCH.create());
        skillSearchTextField.setPlaceholder("Search");
        skillSearchTextField.setTooltipText("Find persons by skill name or level");
        skillSearchTextField.setClearButtonVisible(true);
        skillSearchTextField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        skillSearchTextField.setWidthFull();
        skillSearchTextField.setMaxWidth("100%");
        skillSearchTextField.addValueChangeListener(event -> {
            String filterValue = event.getValue();
            filterManager.setSkillsFilter(filterValue);
            filterManager.applyFilters();
        });
        return skillSearchTextField;
    }

    private static TextField createPersonSearchTextField(FilterManager filterManager) {
        TextField personSearchTextField = new TextField();
        personSearchTextField.setPrefixComponent(VaadinIcon.SEARCH.create());
        personSearchTextField.setPlaceholder("Search");
        personSearchTextField.setTooltipText("Find persons by name, job title or department");
        personSearchTextField.setClearButtonVisible(true);
        personSearchTextField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        personSearchTextField.setWidthFull();
        personSearchTextField.setMaxWidth("100%");
        personSearchTextField.addValueChangeListener(event -> {
            String filterValue = event.getValue();
            filterManager.setPersonFilter(filterValue);
            filterManager.applyFilters();
        });
        return personSearchTextField;
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
            filterMap = new HashMap<>(5);
            unsetFilter(PersonPredicate.personPredicate);
            unsetFilter(PersonPredicate.departmentListPredicate);
            unsetFilter(PersonPredicate.jobTitleListPredicate);
            unsetFilter(SkillsPredicate::testSkillNameOrLevel);
            unsetFilter(SkillsPredicate::testSkillTagNames);
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

        public void unsetFilter(BiPredicate<PersonWithSkillsDto, String> predicateFilter) {
            filterMap.put(predicateFilter, Optional.empty());
        }

        public void setSkillsFilter(String filterValue) {
            filterMap.put(SkillsPredicate::testSkillNameOrLevel, Optional.ofNullable(filterValue));
        }

        public void setPersonFilter(String filterValue) {
            filterMap.put(PersonPredicate.personPredicate, Optional.ofNullable(filterValue));
        }

        public void setTagFilter(List<String> filterValues) {
            filterMap.put(SkillsPredicate::testSkillTagNames, Optional.of(String.join(";", filterValues)));
        }

        public void setDepartmentListFilter(Collection<String> filterValues) {
            String joinedValues = String.join(";", filterValues);
            filterMap.put(PersonPredicate.departmentListPredicate, Optional.of(joinedValues));
        }

        public void setJobTitleListFilter(Collection<String> filterValues) {
            String joinedValues = String.join(";", filterValues);
            filterMap.put(PersonPredicate.jobTitleListPredicate, Optional.of(joinedValues));
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
            public static BiPredicate<PersonWithSkillsDto, String> departmentListPredicate =
                (personWithSkillsDto, filterValue) -> {
                    if (filterValue == null || filterValue.isEmpty()) {
                        return true;
                    }
                    // Allow any department from the filter "list"
                    return StringUtils.containsIgnoreCase(filterValue,
                        personWithSkillsDto.getPerson().getDepartment());
                };
            public static BiPredicate<PersonWithSkillsDto, String> jobTitleListPredicate =
                (personWithSkillsDto, filterValue) -> {
                    if (filterValue == null || filterValue.isEmpty()) {
                        return true;
                    }
                    // Allow any job title from the filter "list"
                    return StringUtils.containsIgnoreCase(filterValue,
                        personWithSkillsDto.getPerson().getTitle());
                };
        }
    }
}
