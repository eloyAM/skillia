package com.example.application.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

@CssImport("./styles/shared-styles.css")
@JsModule("./js/light-dark-theme-chooser.js")
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Skillia");
        logo.addClassName("app-logo");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo);
        header.addClassName("app-header");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        Button themeSwitcher = new Button(VaadinIcon.ADJUST.create());
        themeSwitcher.setTooltipText("Switch theme");
        themeSwitcher.addThemeVariants(ButtonVariant.LUMO_ICON);
        themeSwitcher.setId("app-theme-switcher");
        themeSwitcher.addClickListener(e -> e.getSource().getElement().executeJs(
            "const theme = document.documentElement.getAttribute('theme');"
                + "const newTheme = theme === 'dark' ? 'light' : 'dark';"
                + "document.documentElement.setAttribute('theme', newTheme);"
                + "localStorage.setItem('app-theme', newTheme);"
        ));

        header.add(themeSwitcher);

        addToNavbar(header);
    }

    private void createDrawer() {
        // Actual links
        RouterLink personWithSkillsLink =
            new RouterLink("Person With Skills", PersonWithSkillsView.class);
        addToDrawer(new VerticalLayout(personWithSkillsLink));
        RouterLink skillsManagementLink =
            new RouterLink("Skills Management", SkillsManagementView.class);
        addToDrawer(new VerticalLayout(skillsManagementLink));
        RouterLink skillsAssignmentLink =
            new RouterLink("Skills Assignment", SkillsAssignmentView.class);
        addToDrawer(new VerticalLayout(skillsAssignmentLink));

        // TODO remove this views (might be useful while developing)
        RouterLink personGridLink = new RouterLink("Person Grid", PersonGridView.class);
        addToDrawer(new VerticalLayout(personGridLink));
        RouterLink personSkillGridLink =
            new RouterLink("Person Skill Grid", PersonSkillGridView.class);
        addToDrawer(new VerticalLayout(personSkillGridLink));
        RouterLink skillGridLink = new RouterLink("Skill Grid", SkillGridView.class);
        addToDrawer(new VerticalLayout(skillGridLink));
    }

}
