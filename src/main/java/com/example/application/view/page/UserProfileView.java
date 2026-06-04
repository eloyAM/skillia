package com.example.application.view.page;

import com.example.application.dto.AcquiredSkillDto;
import com.example.application.dto.main.PersonDto;
import com.example.application.security.SecurityService;
import com.example.application.service.DepartmentService;
import com.example.application.service.PersonService;
import com.example.application.service.PersonSkillService;
import com.example.application.view.components.profile.AcquiredSkillsChart;
import com.example.application.view.components.profile.SkillsTreeGrid;
import com.example.application.view.components.profile.UserDetailsCard;
import com.example.application.view.utils.LumoVars;
import com.example.application.view.utils.MainLayout;
import com.example.application.view.utils.ViewUtils;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;

import java.util.List;


@PermitAll
@Route(layout = MainLayout.class, value = "/profile/:username")
@PageTitle("Profile")
public class UserProfileView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    public static final String USERNAME_PATH_PARAMETER = "username";

    private final Authentication authentication;
    private final PersonService personService;
    private final PersonSkillService personSkillService;
    private final DepartmentService departmentService;

    private String routeUsername;

    public UserProfileView(
        SecurityService securityService,
        PersonService personService,
        PersonSkillService personSkillService,
        DepartmentService departmentService) {
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

        add(new UserDetailsCard(person));

        List<AcquiredSkillDto> acquiredSkills = personSkillService.findAllAcquiredSkillByPersonId(person.getUsername());

        add(new H4("Assigned skills"));
        add(new SkillsTreeGrid(person, acquiredSkills,
            this.authentication, this.routeUsername, this.personSkillService, this.departmentService));

        add(
            ViewUtils.createAndInitialize(new Div(), d -> d.getStyle().setPaddingTop(LumoVars.LUMO_SPACE_L)),
            new H4("Stats"));
        add(new AcquiredSkillsChart(
            acquiredSkills, personSkillService.getAllStats()));
    }

}