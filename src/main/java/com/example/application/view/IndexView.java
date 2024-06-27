package com.example.application.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(layout = MainLayout.class, value = "")
@PageTitle("Skillia")
public class IndexView extends VerticalLayout {
    public IndexView() {
        add(new H1("Welcome to Skillia"));
        add(new H3("In this application you will be able to track the skills " +
                "of each employee, with a level of proficiency"));
    }
}
