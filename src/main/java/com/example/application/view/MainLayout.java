package com.example.application.view;

import com.example.application.security.SecConstants;
import com.example.application.security.SecurityService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@CssImport("./styles/shared-styles.css")
@JsModule("./js/light-dark-theme-chooser.js")
public class MainLayout extends AppLayout {

    private final SecurityService securityService;

    public MainLayout(@Autowired SecurityService securityService) {
        this.securityService = securityService;
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

        // TODO remove. this is only for dev purposes
        UserDetails user = securityService.getAuthenticatedUser();
        header.add(new Div(new Text("user: " + user.getUsername())));
        header.add(new Div(new Text("roles: " + user.getAuthorities().toString())));

        Button logout = new Button("Log out", e -> securityService.logout()); // <2>
        header.add(logout);

        addToNavbar(header);
    }

    private void createDrawer() {
        // TODO maybe add icons to the links
        // Actual links
        addToDrawer(new VerticalLayout(new RouterLink("Person With Skills", PersonWithSkillsView.class)));

        UserDetails user = securityService.getAuthenticatedUser();
        var userAuthorities = user.getAuthorities();
        SimpleGrantedAuthority rhAuthority = new SimpleGrantedAuthority(SecConstants.ROLE_HR);
        if (userAuthorities.contains(rhAuthority)) {
            addToDrawer(new VerticalLayout(new RouterLink("Skills Management", SkillsManagementView.class)));
            addToDrawer(new VerticalLayout(new RouterLink("Skills Assignment", SkillsAssignmentView.class)));
        }

        // TODO remove this views (might be useful while developing)
        addToDrawer(new VerticalLayout(new RouterLink("Person Grid", PersonGridView.class)));
        addToDrawer(new VerticalLayout(new RouterLink("Person Skill Grid", PersonSkillGridView.class)));
        addToDrawer(new VerticalLayout(new RouterLink("Skill Grid", SkillGridView.class)));
    }

}
