package com.example.application.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(layout = MainLayout.class, value = "")
@PageTitle("Skillia")
public class IndexView extends VerticalLayout {
}
