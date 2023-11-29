package com.example.application.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(RestrictedView.ROLE)
@Route(layout = MainLayout.class, value = "restricted")
public class RestrictedView extends VerticalLayout {
    public static final String ROLE = "HR";

    public RestrictedView() {
        add(new H1("This view is restricted to the following role: " + ROLE));
    }
}
