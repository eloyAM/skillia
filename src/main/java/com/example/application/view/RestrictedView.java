package com.example.application.view;

import com.example.application.security.SecConstants;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(SecConstants.HR)
@Route(layout = MainLayout.class, value = "restricted")
public class RestrictedView extends VerticalLayout {
    public RestrictedView() {
        add(new H1("This view is restricted to the following role: " + SecConstants.HR));
    }
}
