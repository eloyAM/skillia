package com.example.application.view;

import com.example.application.dto.AcquiredSkillDto;
import com.example.application.dto.PersonDto;
import com.example.application.security.SecConstants;
import com.example.application.security.SecurityService;
import com.example.application.service.PersonService;
import com.example.application.service.PersonSkillService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@PermitAll
@Route(layout = MainLayout.class, value = "/profile/:username")
@PageTitle("Profile")
public class UserProfileView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    public static final String USERNAME_PATH_PARAMETER = "username";

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
        setSizeFull();
        PersonDto person = personService.findPersonByUsername(routeUsername);

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

        Span email = new Span("📧 " + (Objects.requireNonNullElse(personDto.getEmail(), "No email")));
        email.getStyle().set("color", "var(--lumo-body-text-color)").set("font-size", "var(--lumo-font-size-s)");

        Span title = new Span("💼 " + (Objects.requireNonNullElse(personDto.getTitle(), "No title")));
        title.getStyle().set("color", "var(--lumo-body-text-color)").set("font-size", "var(--lumo-font-size-s)");

        Span department = new Span("🏢 " + (Objects.requireNonNullElse(personDto.getDepartment(), "No department")));
        department.getStyle().set("color", "var(--lumo-body-text-color)").set("font-size", "var(--lumo-font-size-s)");

        // User details
        VerticalLayout detailsLayout = new VerticalLayout(
            fullName, usernameSpan, email, title, department
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

    private static Grid<AcquiredSkillDto> createSkillsGrid(List<AcquiredSkillDto> acquiredSkills) {
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
                Div container = new Div(createSkillLevelSelector(v.getLevel()));
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

    private static Component createSkillLevelSelector(Integer currentLevel) {
        MenuBar menuBar = new MenuBar();
        menuBar.addClassName("skill-level-selector");
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

        // Create menu items for levels 1-5
        List<Integer> levels = PersonSkillService.getLevels();
        int firstLevel = levels.get(0);
        int higherLevel = levels.get(levels.size() - 1);
        for (Integer level : levels) {
            String levelWithLabel = "%s - %s".formatted(level, PersonSkillService.getLevelName(level));
            String text = String.valueOf(level);    // Compact format - show only the number, not the level label
            MenuItem menuItem = menuBar.addItem(text, levelWithLabel);

            // Add click listener to handle selection
            menuItem.addClickListener(e -> {
                // TODO
                Notification.show("Hi from level " + e.getSource().getText());
            });

            // Add CSS classes for styling
            menuItem.addClassName("level-item");
            if (level == firstLevel) {
                menuItem.addClassName("first-level");
            } else if (level == higherLevel) {
                menuItem.addClassName("last-level");
            }

            if (level.equals(currentLevel)) {
                menuItem.addThemeNames(
                    MenuBarVariant.LUMO_ICON.getVariantName(),
                    MenuBarVariant.LUMO_PRIMARY.getVariantName()
                );
                menuItem.addClassName("current-level");
            }
        }

        // TODO set current level

        // TODO add button to reset level

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
