package com.example.application.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Second view")
@PermitAll
@Route(value = "second-view", layout = MainLayout.class)
public class SecondView extends VerticalLayout {
    public SecondView() {
        add(new H1("Second view content"));
    }
}
