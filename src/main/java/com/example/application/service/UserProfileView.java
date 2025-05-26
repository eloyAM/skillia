package com.example.application.service;

import com.example.application.security.SecurityService;
import com.example.application.view.MainLayout;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;

@PermitAll
@Route(layout = MainLayout.class, value = "profile/:username")
@PageTitle("Profile")
public class UserProfileView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    public static final String USERNAME_PATH_PARAMETER = "username";
    private final SecurityService securityService;
    private String username;

    public UserProfileView(SecurityService securityService) {
        this.securityService = securityService;
        // We can't create the UI here as we have a dependency on the route parameters,
        // who are read later, not here
    }

    private void createUi() {
        add(new H2(new Text("Given username -> "), new com.vaadin.flow.component.html.Pre(username)));
        add(new H2("Is my own profile? -> " + isMyProfile(username)));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.username = event.getRouteParameters().get(USERNAME_PATH_PARAMETER).orElseThrow();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // If we don't remove the existing elements,
        // they would stay there if we re-navigate to the same route
        removeAll();
        createUi();
    }

    private boolean isMyProfile(String requestedUsername) {
        Authentication authentication = securityService.getAuthentication();
        String username = authentication.getName();
        return username.equals(requestedUsername);
    }
}
