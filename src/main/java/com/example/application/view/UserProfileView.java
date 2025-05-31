package com.example.application.view;

import com.example.application.dto.AcquiredSkillDto;
import com.example.application.dto.PersonDto;
import com.example.application.dto.PersonSkillBasicDto;
import com.example.application.security.SecConstants;
import com.example.application.security.SecurityService;
import com.example.application.service.PersonService;
import com.example.application.service.PersonSkillService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.example.application.view.ViewUtils.createAndInitialize;
import static com.example.application.view.ViewUtils.notificationTopCenter;

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
    private String routeUsername;

    public UserProfileView(
        SecurityService securityService,
        PersonService personService,
        PersonSkillService personSkillService
    ) {
        // We can't create the UI here as we have a dependency on the route parameters,
        // who are read later, not here
        this.authentication = securityService.getAuthentication();
        this.personService = personService;
        this.personSkillService = personSkillService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.routeUsername = event.getRouteParameters().get(USERNAME_PATH_PARAMETER).orElseThrow();
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


        add(new H4("Skills"));
        List<AcquiredSkillDto> acquiredSkills = personSkillService.findAllAcquiredSkillByPersonId(person.getUsername());
        add(createSkillsGrid(acquiredSkills));
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

    private Grid<AcquiredSkillDto> createSkillsGrid(List<AcquiredSkillDto> acquiredSkills) {
        // Create skills grid
        var grid = new Grid<>(AcquiredSkillDto.class, false);
        grid.setWidthFull();
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setItems(acquiredSkills);

        // Configure grid columns
        grid.addColumn(v -> v.getSkill().getName())
            .setHeader("Skill")
            .setFlexGrow(1)
            .setSortable(true);

        grid.addComponentColumn(v -> {
                Div container = new Div(createSkillLevelSelector(v.getLevel(), v.getSkill().getId()));
                container.getStyle().setPaddingTop("var(--lumo-space-s)");
                container.getStyle().setPaddingBottom("var(--lumo-space-s)");
                return container;
            })
            .setHeader("Level")
            .setAutoWidth(true)
            .setFlexGrow(2)
            .setSortable(true)
            .setComparator(AcquiredSkillDto::getLevel);

        return grid;
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
            String levelWithLabel = "%s - %s".formatted(level, PersonSkillService.getLevelName(level));
            String text = String.valueOf(level);    // Compact format - show only the number, not the level label
            MenuItem menuItem = menuBar.addItem(text, levelWithLabel);
            menuItems.add(menuItem);

            // Add click listener to handle selection only if my own profile or role allowed
            if (isMyProfileOrPermittedRole(routeUsername)) {
                menuItem.addClickListener(e -> {
                    MenuItem selectedLevelItem = e.getSource();
                    String selectedLevel = selectedLevelItem.getText();

                    MenuItem oldLevelItem = menuItems.stream()
                        .filter(item -> item.hasClassName(CURRENT_LEVEL_CSSCLASS)).findFirst().orElseThrow();

                    // No modification if the target is the same level
                    if (selectedLevelItem.equals(oldLevelItem))
                        return;

                    // Update the skill level
                    final PersonSkillBasicDto data = new PersonSkillBasicDto(routeUsername, skillIid, Integer.valueOf(selectedLevel));
                    PersonSkillBasicDto savedPersonSkill = personSkillService.savePersonSkill(data);
                    if (savedPersonSkill == null) {
                        notificationTopCenter("Some error occurred while saving", false).open();
                        return;
                    }

                    // Update the view
                    selectedLevelItem.addThemeNames(
                        LUMO_MENU_BAR_ICON_THEME_VARIANT, LUMO_MENU_BAR_PRIMARY_THEME_VARIANT_NAME
                    );
                    selectedLevelItem.addClassName(CURRENT_LEVEL_CSSCLASS);
                    oldLevelItem.removeClassName(CURRENT_LEVEL_CSSCLASS);
                    oldLevelItem.removeThemeNames(LUMO_MENU_BAR_PRIMARY_THEME_VARIANT_NAME);
                });
            }

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
}
