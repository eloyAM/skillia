package com.example.application.view;

import com.example.application.dto.AcquiredSkillDto;
import com.example.application.dto.PersonDto;
import com.example.application.security.SecConstants;
import com.example.application.security.SecurityService;
import com.example.application.service.PersonService;
import com.example.application.service.PersonSkillService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
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

import static com.example.application.view.ViewUtils.getLevelIndicatorSvgPath;

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

        add(new H3("Profile information"));
        add(createUserDetailsSection(person));


        add(new H3("Skills"));
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

    private static Component createSkillsGrid(List<AcquiredSkillDto> acquiredSkills) {
        // Create skills grid
        var skillsGrid = new Grid<>(AcquiredSkillDto.class, false);
        skillsGrid.setWidthFull();

        // Configure grid columns
        skillsGrid.addColumn(v -> v.getSkill().getName())
            .setHeader("Skill")
            .setFlexGrow(2)
            .setSortable(true);

        skillsGrid.addComponentColumn(v ->
                new Image(getLevelIndicatorSvgPath(v.getLevel()), "level " + v.getLevel())
            )
            .setHeader("Level")
            .setFlexGrow(1)
            .setSortable(true)
            .setComparator(AcquiredSkillDto::getLevel);

        // Add sample data to the grid
        skillsGrid.setItems(acquiredSkills);

        return skillsGrid;
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
