package com.example.application.view.components;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AccessDenied extends VerticalLayout {
    public AccessDenied() {
        setAlignItems(Alignment.CENTER);
        Icon warningIcon = VaadinIcon.LOCK.create();
        warningIcon.setSize("100px");
        add(
                warningIcon,
                new H1("Access denied")
        );
    }
}
