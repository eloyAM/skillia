package com.example.application.view;

import com.example.application.dto.PersonWithSkillsDto;
import com.example.application.service.PersonSkillService;
import com.example.application.utils.Comparators;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

@PermitAll
@Route(layout = MainLayout.class, value = "personwithskills")
public class PersonWithSkillsView extends VerticalLayout {

    private final PersonSkillService personSkillService;

    public PersonWithSkillsView(PersonSkillService personSkillService) {
        this.personSkillService = personSkillService;
        createUi();
    }

    private void createUi() {
        // TODO check column widths
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
        personSearchTextField.setTooltipText("Find persons by name, title or department");
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
        Grid.Column<PersonWithSkillsDto> skillsColumn = personSkillGrid.getColumnByKey("skills");
        headerRow.getCell(skillsColumn).setComponent(skillSearchTextField);

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

            var displayNameDiv = new Div();
            displayNameDiv.setText(person.getDisplayName());
            displayNameDiv.getStyle().set("font-weight", "bold");

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
            return new Div(displayNameDiv, personTitleDiv, deparmentDiv);
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
            filterMap.put(SkillsPredicate::testStatic, Optional.empty());
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
            filterMap.put(SkillsPredicate::testStatic, Optional.ofNullable(filterValue));
        }

        public void unsetSkillsFilter() {
            filterMap.put(SkillsPredicate::testStatic, Optional.empty());
        }

        public void setPersonFilter(String filterValue) {
            filterMap.put(PersonPredicate.personPredicate, Optional.ofNullable(filterValue));
        }

        public void unsetPersonFilter() {
            filterMap.put(PersonPredicate.personPredicate, Optional.empty());
        }

        private static final class SkillsPredicate
            implements BiPredicate<PersonWithSkillsDto, String> {
            @Override
            public boolean test(PersonWithSkillsDto person, String filterValue) {
                return testStatic(person, filterValue);
            }

            public static boolean testStatic(PersonWithSkillsDto person, String filterValue) {
                return person.getSkills().stream()
                    .map(acquiredSkillDto -> acquiredSkillDto.getSkill().getName()
                        + acquiredSkillDto.getLevel())
                    .anyMatch(skillStr -> StringUtils.containsIgnoreCase(skillStr, filterValue));
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
