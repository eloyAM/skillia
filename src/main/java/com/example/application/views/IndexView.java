package com.example.application.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Vaadin")
@PermitAll
@Route(value = "", layout = MainLayout.class)
public class IndexView extends VerticalLayout {
    public IndexView() {
        add(new H1("Welcome to Vaadin"));
    }
}
