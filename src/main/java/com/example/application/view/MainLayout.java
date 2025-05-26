package com.example.application.view;

import com.example.application.security.SecConstants;
import com.example.application.security.SecurityService;
import com.example.application.view.internal.PersonGridView;
import com.example.application.view.internal.PersonSkillGridView;
import com.example.application.view.internal.SkillGridView;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouterLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@CssImport("./styles/shared-styles.css")
@JsModule("./js/light-dark-theme-chooser.js")
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final SecurityService securityService;
    private final HorizontalLayout header;
    private boolean isDebugMode = false;
    private final Authentication authentication;

    public MainLayout(@Autowired SecurityService securityService) {
        this.securityService = securityService;
        authentication = securityService.getAuthentication();
        // Create UI
        header = createHeader();
        addToNavbar(header);
        createDrawer();
    }

    private HorizontalLayout createHeader() {
        H1 logo = new H1("Skillia");
        logo.addClassName("app-logo");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo);
        header.addClassName("app-header");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Div spacer = new Div();
        spacer.getStyle().setFlexGrow("1");
        header.add(spacer);

        header.add(createThemeSwitcher());

        String username = authentication.getName();
        header.add(createProfileButton(username));

        return header;
    }

    // only for dev purposes
    private static void _addUserInfo(HorizontalLayout header, SecurityService securityService1) {
        //        UserDetails user = securityService.getAuthenticatedUser();
        Authentication authentication = securityService1.getAuthentication();
        header.add(new Div(new Text("user: "
//                + user.getUsername()
                + authentication.getName()
        )));
        header.add(new Div(new Text("roles: "
//                + user.getAuthorities().toString()
                + authentication.getAuthorities().toString()
        )));
    }

    private static Button createThemeSwitcher() {
        Button themeSwitcher = new Button(VaadinIcon.ADJUST.create());
        themeSwitcher.setTooltipText("Switch theme");
        themeSwitcher.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        themeSwitcher.setId("app-theme-switcher");
        themeSwitcher.addClickListener(e -> e.getSource().getElement().executeJs(
            "const theme = document.documentElement.getAttribute('theme');"
                + "const newTheme = theme === 'dark' ? 'light' : 'dark';"
                + "document.documentElement.setAttribute('theme', newTheme);"
                + "localStorage.setItem('app-theme', newTheme);"
        ));
        return themeSwitcher;
    }

    private void createDrawer() {
        addToDrawer(new VerticalLayout(createMenuLink(SkillsMatrixView.class, "Skills Matrix", VaadinIcon.TABLE.create())));

        var userAuthorities = authentication.getAuthorities();
        SimpleGrantedAuthority rhAuthority = new SimpleGrantedAuthority(SecConstants.ROLE_HR);
        if (userAuthorities.contains(rhAuthority)) {
            addToDrawer(new VerticalLayout(createMenuLink(SkillsAssignmentView.class, "Skills Assignment", VaadinIcon.STAR_HALF_LEFT_O.create())));
            addToDrawer(new VerticalLayout(createMenuLink(SkillsManagementView.class, "Skills Management", VaadinIcon.RECORDS.create())));
            addToDrawer(new VerticalLayout(createMenuLink(SkillTagView.class, "Skill tags Management", VaadinIcon.TAG.create())));
            addToDrawer(new VerticalLayout(createMenuLink(SkillGroupsView.class, "Skill groups Management", VaadinIcon.FOLDER_OPEN.create())));
            addToDrawer(new VerticalLayout(createMenuLink(DepartmentsView.class, "Departments", VaadinIcon.WORKPLACE.create())));
        }
    }

    private static RouterLink createMenuLink(Class<? extends Component> viewClass, String caption, Icon icon) {
        final RouterLink routerLink = new RouterLink(viewClass);
        routerLink.setClassName("menu-link");
        routerLink.add(icon, new Span(caption));
        return routerLink;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final String debugMode = event.getLocation().getQueryParameters().getSingleParameter("debugMode").orElse("false");
        if ("true".equals(debugMode)) {
            isDebugMode = true;
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (isDebugMode) {
            addToDrawer(new VerticalLayout(new RouterLink("[DEBUG] Users list", PersonGridView.class)));
            addToDrawer(new VerticalLayout(new RouterLink("[DEBUG] Person Skill Grid", PersonSkillGridView.class)));
            addToDrawer(new VerticalLayout(new RouterLink("[DEBUG] Skill Grid", SkillGridView.class)));
            _addUserInfo(header, securityService);
        }
    }

    private Component createProfileButton(String username) {
        Avatar avatar = new Avatar();

        MenuBar menuBar = new MenuBar();
        menuBar.setId("app-profile-element");
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

        MenuItem menuItem = menuBar.addItem(avatar);
        menuBar.setTooltipText(menuItem, username);
        SubMenu subMenu = menuItem.getSubMenu();

        MenuItem myProfileItem = createIconItem(subMenu, VaadinIcon.USER, "My profile",
            e -> navigateToProfile(username));
        myProfileItem.setId("app-my-profile-button");
        MenuItem logOutItem = createIconItem(subMenu, VaadinIcon.SIGN_OUT, "Log out",
            e -> securityService.logout());
        logOutItem.setId("app-logout-button");

        return menuBar;
    }

    private void navigateToProfile(String username) {
        getUI().ifPresent(ui -> ui.navigate(
            UserProfileView.class,
            new RouteParam(UserProfileView.USERNAME_PATH_PARAMETER, username)));
    }

    private static MenuItem createIconItem(
        HasMenuItems menu, VaadinIcon vaadinIcon, String label,
        ComponentEventListener<ClickEvent<MenuItem>> onClick
    ) {
        Icon icon = new Icon(vaadinIcon);
        icon.getStyle().setMarginRight("var(--lumo-space-m");
        MenuItem item = menu.addItem(icon, onClick);
        item.add(new Text(label));
        item.setAriaLabel(label);
        return item;
    }
}
