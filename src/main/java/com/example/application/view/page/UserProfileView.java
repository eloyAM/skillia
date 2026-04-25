package com.example.application.view.page;

import com.example.application.dto.*;
import com.example.application.security.SecConstants;
import com.example.application.security.SecurityService;
import com.example.application.service.DepartmentService;
import com.example.application.service.PersonService;
import com.example.application.service.PersonSkillService;
import com.example.application.view.utils.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.*;
import elemental.json.JsonArray;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.application.view.utils.JsonUtils.toJsonArray;
import static com.example.application.view.utils.ViewUtils.createAndInitialize;
import static com.example.application.view.utils.ViewUtils.notificationTopCenter;

@PermitAll
@Route(layout = MainLayout.class, value = "/profile/:username")
@PageTitle("Profile")
public class UserProfileView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    public static final String USERNAME_PATH_PARAMETER = "username";

    private static final String CURRENT_LEVEL_CSSCLASS = "current-level";
    private static final String LUMO_MENU_BAR_PRIMARY_THEME_VARIANT_NAME = MenuBarVariant.LUMO_PRIMARY.getVariantName();
    public static final String LUMO_MENU_BAR_ICON_THEME_VARIANT = MenuBarVariant.LUMO_ICON.getVariantName();

    private final Authentication authentication;
    private final PersonService personService;
    private final PersonSkillService personSkillService;
    private final DepartmentService departmentService;

    private String routeUsername;
    private boolean useChartJs;
    private boolean useEcharts;

    public UserProfileView(
        SecurityService securityService,
        PersonService personService,
        PersonSkillService personSkillService,
        DepartmentService departmentService
    ) {
        // We can't create the UI here as we have a dependency on the route parameters,
        // who are read later, not here
        this.authentication = securityService.getAuthentication();
        this.personService = personService;
        this.personSkillService = personSkillService;
        this.departmentService = departmentService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.routeUsername = event.getRouteParameters().get(USERNAME_PATH_PARAMETER).orElseThrow();
        var params = event.getLocation().getQueryParameters().getParameters();
        this.useChartJs = params.containsKey("useChartJs");
        this.useEcharts = params.containsKey("useEcharts");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // If we don't remove the existing elements,
        // they would stay there if we re-navigate to the same route
        removeAll();
        createUi();
    }

    private void createUi() {
        PersonDto person = personService.findPersonByUsername(routeUsername);
        if (person == null) {
            add(new H1("User not found"));
            return;
        }

        setSizeFull();

        add(new H4("Profile information"));
        add(createUserDetailsSection(person));

        List<AcquiredSkillDto> acquiredSkills = personSkillService.findAllAcquiredSkillByPersonId(person.getUsername());

        add(new H4("Skills"));
        add(createSkillsTreegrid(person, acquiredSkills));

        add(new H4("Stats"));
        add(createChartSection(acquiredSkills));
    }

    private static Component createUserDetailsSection(PersonDto personDto) {
        H2 fullName = new H2(Objects.requireNonNullElse(personDto.getFullName(), "Unknown User"));
        fullName.getStyle().set("margin", "0").set("color", "var(--lumo-primary-text-color)");

        Span usernameSpan = new Span("@" + personDto.getUsername());
        usernameSpan.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");

        var emailJobTitleAndDepartment = new Div(
            new Span("📧 " + Objects.requireNonNullElse(personDto.getEmail(), "No email")),
            new Div(
                new Span("💼 " + Objects.requireNonNullElse(personDto.getTitle(), "No title")),
                // Spacer
                createAndInitialize(new Span(), span -> span.getStyle().setPaddingRight("var(--lumo-space-m)")),
                new Span("🏢 " + Objects.requireNonNullElse(personDto.getDepartment(), "No department"))
            )
        );
        emailJobTitleAndDepartment.getStyle()
            .set("font-size", "var(--lumo-font-size-s)")
            .set("display", "flex")
            .set("flex-wrap", "wrap")
            .set("flex-direction", "row")
            .set("column-gap", "var(--lumo-space-l")
            .set("row-gap", "var(--lumo-space-xs)");

        // User details
        VerticalLayout detailsLayout = new VerticalLayout(
            fullName, usernameSpan, emailJobTitleAndDepartment
        );
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);

        // Create avatar and user info layout
        HorizontalLayout userInfoLayout = new HorizontalLayout(detailsLayout);
        userInfoLayout.setAlignItems(Alignment.CENTER);
        userInfoLayout.setSpacing(true);

        // Create a compact user details card
        Div userCard = new Div(userInfoLayout);
        userCard.addClassName("user-details-card");
        userCard.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-m)")
            .set("margin-bottom", "var(--lumo-space-l)");

        return userCard;
    }

// TODO group name should be different to the skill
    private TreeGrid<?> createSkillsTreegrid(PersonDto person, List<AcquiredSkillDto> acquiredSkills) {
        var tree = new TreeGrid<AcquiredSkillDto>();

        tree.setWidthFull();
        tree.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        // Horizontal scroll if the screen is not big enough, we don't want to cut the level selector
        tree.setMinWidth("400px");
        // Min height to avoid shrinking due to another section
        tree.setMinHeight("300px");

        tree.addHierarchyColumn(sk -> sk.getSkill().getName())
            .setHeader("Skill")
            .setFlexGrow(1);

        tree.addComponentColumn(v -> {
                if (v.getLevel() == -1) {
                    return new Span();   // Special case for the grouping element
                }
                return createSkillLevelSelector(v.getLevel(), v.getSkill().getId());
            })
            .setHeader("Level")
            .setSortable(true)
            .setComparator(AcquiredSkillDto::getLevel)
            .setAutoWidth(true)
            .setFlexGrow(2);

        DepartmentDto departmentObject = departmentService.findDepartmentByName(person.getDepartment())
            .orElseThrow(() -> new IllegalStateException("Department " + person.getDepartment()
                + " not found for the person " + person.getUsername()));

        // As we show each skill only for the first group where we find it, sorting by ascending number
        // of skills avoids a greater amount of empty groups
        var groupToSkillsMap = departmentObject.getSkillGroups().stream()
            .sorted(Comparator.comparingInt((SkillGroupDto group) -> {
                Set<SkillDto> skills = group.getSkills();
                return (skills == null || skills.isEmpty()) ? Integer.MAX_VALUE : skills.size();
            }))
            .collect(Collectors.toMap(
                skillGroup -> new AcquiredSkillDto(-1L, skillGroup.getName(), -1),
                SkillGroupDto::getSkills,
                (a, b) -> a,
                LinkedHashMap::new
            ));
        List<AcquiredSkillDto> rootItems = groupToSkillsMap.keySet().stream().toList();

        var addedSkillsIds = new HashSet<Long>();
        tree.setItems(rootItems, item -> {
            Set<SkillDto> skillsForThisGroup = groupToSkillsMap.get(item);
            if (skillsForThisGroup != null) {
                return skillsForThisGroup.stream()
                    // Avoid adding the same skill even if related to multiple groups
                    // Select the current level for acquired skills or 0 for non-acquired skills
                    .filter(skill -> addedSkillsIds.add(skill.getId()))
                    .map(skill -> {
                        Optional<AcquiredSkillDto> acquiredSkill = acquiredSkills.stream()
                            .filter(as -> as.getSkill().getId().equals(skill.getId()))
                            .findFirst();
                        int level = acquiredSkill.map(AcquiredSkillDto::getLevel).orElse(0);
                        return new AcquiredSkillDto(skill.getId(), skill.getName(), level);
                    })
                    .toList();
            }
            return List.of();
        });
        tree.expand(rootItems);
        return tree;
    }

    private Component createSkillLevelSelector(Integer currentLevel, Long skillIid) {
        MenuBar menuBar = new MenuBar();
        menuBar.addClassName("skill-level-selector");
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

        // Create menu items for levels 1-5
        List<Integer> levels = PersonSkillService.getLevels();
        int firstLevel = levels.get(0);
        int higherLevel = levels.get(levels.size() - 1);
        List<MenuItem> menuItems = new ArrayList<>(levels.size());
        for (Integer level : levels) {
            String tooltipLevelWithLabel = "%s - %s".formatted(level, PersonSkillService.getLevelName(level));
            String text = String.valueOf(level);    // Compact format - show only the number, not the level label
            MenuItem menuItem = menuBar.addItem(text, tooltipLevelWithLabel);
            menuItems.add(menuItem);

            addSkillSelectorClickListenerIfPermitted(skillIid, menuItem, menuItems);

            // Add CSS classes for styling
            menuItem.addClassNames("level-item", "level-" + level);
            if (level == firstLevel) {
                menuItem.addClassName("first-level");
            } else if (level == higherLevel) {
                menuItem.addClassName("last-level");
            }

            if (level.equals(currentLevel)) {
                menuItem.addThemeNames(
                    LUMO_MENU_BAR_ICON_THEME_VARIANT, LUMO_MENU_BAR_PRIMARY_THEME_VARIANT_NAME
                );
                menuItem.addClassName(CURRENT_LEVEL_CSSCLASS);
            }
        }

        return menuBar;
    }

    // Add click listener to handle selection only if my own profile or role allowed
    private void addSkillSelectorClickListenerIfPermitted(Long skillIid, MenuItem menuItem, List<MenuItem> menuItems) {
        if (isMyProfileOrPermittedRole(routeUsername)) {
            menuItem.addClickListener(e -> {
                MenuItem selectedLevelItem = e.getSource();
                String selectedLevel = selectedLevelItem.getText();

                // If there was no previous skill rating, there's no old item
                Optional<MenuItem> oldLevelItem = menuItems.stream()
                    .filter(item -> item.hasClassName(CURRENT_LEVEL_CSSCLASS)).findFirst();

                // No modification if the target is the same level
                if (oldLevelItem.map(selectedLevelItem::equals).orElse(false))
                    return;

                // Update the skill level
                final PersonSkillBasicDto data = new PersonSkillBasicDto(routeUsername, skillIid, Integer.valueOf(selectedLevel));
                PersonSkillBasicDto savedPersonSkill = personSkillService.savePersonSkill(data);
                if (savedPersonSkill == null) {
                    notificationTopCenter("Some error occurred while setting the skill level", false).open();
                    return;
                }

                // Update the view
                selectedLevelItem.addThemeNames(
                    LUMO_MENU_BAR_ICON_THEME_VARIANT, LUMO_MENU_BAR_PRIMARY_THEME_VARIANT_NAME
                );
                selectedLevelItem.addClassName(CURRENT_LEVEL_CSSCLASS);
                oldLevelItem.ifPresent(old -> {
                    old.removeClassName(CURRENT_LEVEL_CSSCLASS);
                    old.removeThemeNames(LUMO_MENU_BAR_PRIMARY_THEME_VARIANT_NAME);
                });
            });
        } else {
            menuItem.addClassName("readonly-level-item");
        }
    }

    private boolean isMyProfileOrPermittedRole(String requestedUsername) {
        return isMyProfile(requestedUsername) || isPermittedRole();
    }

    private boolean isMyProfile(String requestedUsername) {
        String username = authentication.getName();
        return username.equals(requestedUsername);
    }

    private boolean isPermittedRole() {
        Collection<? extends GrantedAuthority> userAuthorities = authentication.getAuthorities();
        SimpleGrantedAuthority rhAuthority = new SimpleGrantedAuthority(SecConstants.ROLE_HR);
        return userAuthorities.contains(rhAuthority);
    }

    private Component createChartSection(List<AcquiredSkillDto> acquiredSkills) {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.addClassName("user-profile-charts-layout");
        wrapper.setPadding(false);
        wrapper.setSpacing(true);
        wrapper.setWidthFull();

        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        for (var as : acquiredSkills) {
            labels.add(as.getSkill().getName());
            values.add(as.getLevel());
        }
        JsonArray labelsJsonArray = toJsonArray(labels);
        JsonArray valuesJsonArray = toJsonArray(values);

        if (this.useChartJs) {
            Div chartJsContainer = new Div();
            chartJsContainer.setId("profile-chartjs");
            chartJsContainer.setWidthFull();
            chartJsContainer.setHeight("320px");
            chartJsContainer.getElement().executeJs(
                "globalThis.skillia.renderSkillsBarChartChartJs($0, $1, $2);",
                "profile-chartjs", labelsJsonArray, valuesJsonArray
            );
            wrapper.add(new H5("Chart.js"), chartJsContainer);
        }

        if (this.useEcharts) {
            Div echartsContainer = new Div();
            echartsContainer.setId("profile-echarts");
            echartsContainer.setWidthFull();
            echartsContainer.setHeight("320px");
            echartsContainer.getElement().executeJs(
                "globalThis.skillia.renderSkillsBarChartEcharts($0, $1, $2);",
                "profile-echarts", labelsJsonArray, valuesJsonArray
            );
            wrapper.add(new H5("Apache ECharts"), echartsContainer);
        }

        return wrapper;
    }
}
